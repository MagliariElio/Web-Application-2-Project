package it.polito.students.crm.services


import it.polito.students.crm.controllers.CrmCustomersController
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Customer
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.InvalidUpdateException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.Factory.Companion.toEntity
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CustomerServiceImpl(
    private val customerRepository: CustomerRepository,
    private val contactService: ContactService,
    private val contactRepository: ContactRepository,
    private val jobOfferRepository: JobOfferRepository,
    private val emailRepository: EmailRepository,
    private val telephoneRepository: TelephoneRepository,
    private val addressRepository: AddressRepository,
    private val factory: Factory,
    private val kafkaProducer: KafkaProducerService
) : CustomerService {
    private val logger = LoggerFactory.getLogger(CrmCustomersController::class.java)
    private val formatter = DateTimeFormatter.ofPattern("MMMMyyyy", Locale.ENGLISH)

    override fun getAllCustomers(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ContactEnumFields, String>
    ): PageImpl<CustomerDTO> {
        val pageable = PageRequest.of(pageNumber, pageSize)

        // Ottieni tutti i customers non cancellati dal database senza paginazione
        val allCustomers: List<Customer> = customerRepository.findAllByDeletedFalse(Pageable.unpaged()).content

        // Applica i filtri in memoria
        var filteredList = allCustomers
            .filter { it.information.category == CategoryOptions.CUSTOMER }
            .map { customer ->
                customer.joboffers = customer.joboffers.filter { !it.deleted }.toMutableSet()
                customer
            }.map { it.toDTO() }

        // Applica ulteriori filtri basati sui campi
        filterMap.entries.forEach { filter ->
            filteredList = when (filter.key) {
                ContactEnumFields.NAME -> filteredList.filter {
                    it.information.contactDTO.name.contains(
                        filter.value,
                        ignoreCase = true
                    )
                }

                ContactEnumFields.SURNAME -> filteredList.filter {
                    it.information.contactDTO.surname.contains(
                        filter.value,
                        ignoreCase = true
                    )
                }

                ContactEnumFields.CATEGORY -> filteredList.filter { it.information.contactDTO.category == CategoryOptions.CUSTOMER }
                ContactEnumFields.SSN_CODE -> filteredList.filter {
                    it.information.contactDTO.ssnCode.contains(
                        filter.value,
                        ignoreCase = true
                    )
                }

                ContactEnumFields.COMMENT -> filteredList.filter {
                    it.information.contactDTO.comment.contains(
                        filter.value,
                        ignoreCase = true
                    )
                }
            }
        }

        // Calcola il numero totale di elementi dopo il filtraggio
        val totalElements = filteredList.size

        // Esegui la paginazione manuale sulla lista filtrata
        val start = (pageNumber * pageSize).coerceAtMost(totalElements)
        val end = (start + pageSize).coerceAtMost(totalElements)
        val paginatedList = if (start <= end) filteredList.subList(start, end) else emptyList()

        // Crea l'oggetto PageImpl usando la lista paginata e il totale degli elementi
        return PageImpl(paginatedList, pageable, totalElements.toLong())
    }


    override fun getCustomer(customerId: Long): CustomerDTO {
        val existedCustomer = customerRepository.findById(customerId)
        if (!existedCustomer.isPresent || existedCustomer.get().deleted) {
            // TODO: scrivere i messaggi di errore nel ErrorPage
            logger.info("CustomerService: The customer with id $customerId was not found on the db")
            throw CustomerNotFoundException("Customer with id = '$customerId' not found!")
        }

        val customer = existedCustomer.get()
        customer.joboffers = customer.joboffers.filter { !it.deleted }.toMutableSet()
        return customer.toDTO()
    }

    @Transactional
    override fun postNewCustomer(contactInfo: CreateContactDTO): CustomerDTO {
        val newContactDTO = contactService.storeContact(contactInfo, CategoryOptions.CUSTOMER)

        val newContact = contactRepository.findById(newContactDTO.contactDTO.id)

        val newCustomer = Customer().apply {
            information = newContact.get()
        }

        val savedCustomer = customerRepository.save(newCustomer)

        return savedCustomer.toDTO()
    }

    override fun updateCustomer(customerID: Long, contactID: Long): CustomerDTO {
        val customer = getCustomer(customerID).toEntity(factory)
        val contact = contactService.getContact(contactID)        // if not found it raises an exception

        if (contact.category == CategoryOptions.PROFESSIONAL) {
            throw InvalidUpdateException(ErrorsPage.INVALID_UPDATE_CUSTOMER)
        }

        customer.information = contact
        return customerRepository.save(customer).toDTO()
    }

    override fun updateCustomerDetails(customerID: Long, newContactDetails: UpdateContactDTO): CustomerDTO {
        val customer = getCustomer(customerID).toEntity(factory)
        val prev_contact =
            contactService.getContact(customer.information.id)        // if not found it raises an exception

        if (prev_contact.category == CategoryOptions.PROFESSIONAL) {
            throw InvalidUpdateException(ErrorsPage.INVALID_UPDATE_CUSTOMER)
        }

        contactService.updateContact(newContactDetails, CategoryOptions.CUSTOMER)

        return customerRepository.findById(customerID).get().toDTO()

    }

    @Transactional
    override fun deleteCustomer(customerID: Long) {
        val customer = getCustomer(customerID).toEntity(factory)
        val jobOfferList = customer.joboffers
        customer.joboffers = mutableSetOf()

        // Delete the Customer
        customer.deleted = true
        customerRepository.save(customer)

        // Delete all Job Offers linked to the customer
        jobOfferList.forEach {
            it.deleted = true
            if (it.professional?.employmentState == EmploymentStateEnum.EMPLOYED) {
                it.professional!!.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
                kafkaProducer.sendProfessional(KafkaTopics.TOPIC_PROFESSIONAL, ProfessionalAnalyticsDTO(EmploymentStateEnum.EMPLOYED, EmploymentStateEnum.AVAILABLE_FOR_WORK))
            }
            jobOfferRepository.save(it)
            kafkaProducer.sendJobOffer(KafkaTopics.TOPIC_JOB_OFFER, JobOfferAnalyticsDTO(it.status, null, LocalDate.now().format(formatter).lowercase(), it.creationTime, it.endTime))
        }
    }

}