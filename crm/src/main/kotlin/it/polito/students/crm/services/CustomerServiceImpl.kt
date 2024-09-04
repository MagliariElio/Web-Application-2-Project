package it.polito.students.crm.services

import it.polito.students.crm.controllers.CrmCustomersController
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.InvalidUpdateException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.Factory.Companion.toEntity
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerServiceImpl(
    private val customerRepository: CustomerRepository,
    private val contactService: ContactService,
    private val contactRepository: ContactRepository,
    private val jobOfferRepository: JobOfferRepository,
    private val emailRepository: EmailRepository,
    private val telephoneRepository: TelephoneRepository,
    private val addressRepository: AddressRepository,
    private val factory: Factory
) : CustomerService {
    private val logger = LoggerFactory.getLogger(CrmCustomersController::class.java)

    override fun getAllCustomers(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ContactEnumFields, String>
    ): PageImpl<CustomerDTO> {
        val pageable = PageRequest.of(pageNumber, pageSize)
        val page: Page<Customer> = customerRepository.findAll(pageable)
        var list = page.content
            .filter { !it.deleted && it.information.category == CategoryOptions.CUSTOMER }
            .map{customer ->
                customer.joboffers = customer.joboffers.filter { !it.deleted }.toMutableSet()
                customer
            }.map { it.toDTO() }

        filterMap.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.information.contactDTO.name.contains(filter.value, ignoreCase = true) }
                ContactEnumFields.SURNAME -> list.filter { it.information.contactDTO.surname.contains(filter.value, ignoreCase = true) }
                ContactEnumFields.CATEGORY -> list.filter { it.information.contactDTO.category == CategoryOptions.CUSTOMER }
                ContactEnumFields.SSN_CODE -> list.filter { it.information.contactDTO.ssnCode.contains(filter.value, ignoreCase = true) }
                ContactEnumFields.COMMENT -> list.filter { it.information.contactDTO.comment.contains(filter.value, ignoreCase = true) }
            }
        }

        val pageImpl = PageImpl(list, pageable, page.totalElements)
        return pageImpl
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

    override fun updateCustomerDetails(customerID: Long, newContactDetails: CreateContactDTO): CustomerDTO {
        val customer = getCustomer(customerID).toEntity(factory)
        val prev_contact = contactService.getContact(customer.information.id)        // if not found it raises an exception

        if (prev_contact.category == CategoryOptions.PROFESSIONAL) {
            throw InvalidUpdateException(ErrorsPage.INVALID_UPDATE_CUSTOMER)
        }

        // Create a new Contact
        val contact = Contact().apply {
            name = newContactDetails.name
            surname = newContactDetails.surname
            ssnCode = newContactDetails.ssnCode ?: ""
            this.category = CategoryOptions.CUSTOMER
            comment = newContactDetails.comment ?: ""
        }

// If inserted add email
        if (newContactDetails.emails != null) {
            // Read from a list of emails and add to contact
            newContactDetails.emails?.forEach { e: CreateEmailDTO ->
                // Check if email is already stored in DB
                val existedEmail = emailRepository.findByEmail(e.email)
                if (existedEmail != null) {
                    contact.addEmail(existedEmail)
                } else {
                    val email = Email().apply {
                        email = e.email
                        comment = e.comment ?: ""
                    }
                    emailRepository.save(email) // Save the email first
                    contact.addEmail(email)
                }
            }
        }

// If inserted add telephone
        if (newContactDetails.telephones != null) {
            // Read from a list of telephones and add to contact
            newContactDetails.telephones?.forEach { t: CreateTelephoneDTO ->
                // Check if telephone is already stored in DB
                val existedTelephone = telephoneRepository.findByTelephone(t.telephone)
                if (existedTelephone != null) {
                    contact.addTelephone(existedTelephone)
                } else {
                    val telephone = Telephone().apply {
                        telephone = t.telephone
                        comment = t.comment ?: ""
                    }
                    telephoneRepository.save(telephone) // Save the telephone first
                    contact.addTelephone(telephone)
                }
            }
        }

// If inserted add address
        if (newContactDetails.addresses != null) {
            // Read from a list of addresses and add to contact
            newContactDetails.addresses?.forEach {
                // Check if an address has been already stored
                val existedAddress: Address? = addressRepository
                    .findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                        it.address!!,
                        it.city!!,
                        it.region!!,
                        it.state!!
                    )
                if (existedAddress != null) {
                    contact.addAddress(existedAddress)
                } else {
                    // Create a new Address
                    val address = Address().apply {
                        state = it.state ?: ""
                        region = it.region ?: ""
                        city = it.city ?: ""
                        address = it.address ?: ""
                        comment = it.comment ?: ""
                    }
                    addressRepository.save(address) // Save the address first
                    contact.addAddress(address)
                }
            }
        }

// Save contact in the database
        val contactSaved = contactRepository.save(contact) // Ensure the contact is saved

        customer.information = contactSaved
        return customerRepository.save(customer).toDTO()

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
            if(it.professional?.employmentState == EmploymentStateEnum.EMPLOYED) {
                it.professional!!.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
            }
            jobOfferRepository.save(it)
        }
    }

}