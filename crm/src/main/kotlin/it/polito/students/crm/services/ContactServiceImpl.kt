package it.polito.students.crm.services

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Address
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Email
import it.polito.students.crm.entities.Telephone
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.repositories.AddressRepository
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.EmailRepository
import it.polito.students.crm.repositories.TelephoneRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContactServiceImpl(
    private val contactRepository: ContactRepository,
    private val emailRepository: EmailRepository,
    private val telephoneRepository: TelephoneRepository,
    private val addressRepository: AddressRepository,
) : ContactService {
    private val logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)

    override fun getAllContacts(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ContactEnumFields, String>
    ): PageImpl<ContactDTO> {
        val pageable = PageRequest.of(pageNumber, pageSize)
        val page: Page<Contact> = contactRepository.findAll(pageable)
        var list = page.content.map { it.toDTO() }

        filterMap.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.name == filter.value }
                ContactEnumFields.SURNAME -> list.filter { it.surname == filter.value }
                ContactEnumFields.CATEGORY -> list.filter { it.category.name == filter.value } // adapted a categoryOptions
                ContactEnumFields.SSN_CODE -> list.filter { it.ssnCode == filter.value }
                ContactEnumFields.COMMENT -> list.filter { it.comment == filter.value }
            }
        }

        val pageImpl = PageImpl(list, pageable, page.totalElements)
        return pageImpl
    }

    @Transactional
    override fun storeContact(contactDto: CreateContactDTO, category: CategoryOptions): ContactWithAssociatedDataDTO {

        //Create a new Contact
        val contact = Contact().apply {
            name = contactDto.name
            surname = contactDto.surname
            ssnCode = contactDto.ssnCode ?: ""
            this.category = category
            comment = contactDto.comment ?: ""
        }

        // If inserted add email
        if (contactDto.emails != null) {

            // Read from a list of emails and add to contact
            contactDto.emails?.forEach { e: CreateEmailDTO ->
                // Check if email is already stored in DB
                val existedEmail = emailRepository.findByEmail(e.email)
                if (existedEmail != null) {
                    contact.addEmail(existedEmail)
                } else {
                    val email = Email().apply {
                        email = e.email
                        comment = e.comment ?: ""
                    }
                    emailRepository.save(email)
                    contact.addEmail(email)
                }
            }
        }
        // If inserted add telephone
        if (contactDto.telephones != null) {
            // Read from a list of telephones and add to contact
            contactDto.telephones?.forEach { t: CreateTelephoneDTO ->
                // Check if telephone is already stored in DB
                val existedTelephone = telephoneRepository.findByTelephone(t.telephone)
                if (existedTelephone != null) {
                    contact.addTelephone(existedTelephone)
                } else {
                    val telephone = Telephone().apply {
                        telephone = t.telephone
                        comment = t.comment ?: ""
                    }
                    telephoneRepository.save(telephone)
                    contact.addTelephone(telephone)
                }
            }
        }
        // If inserted add address
        if (contactDto.addresses != null) {
            // Read from a list of addresses and add to contact
            contactDto.addresses?.forEach {
                //check if an address as been already stored ???
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
                    //Create a new Address
                    val address = Address().apply {
                        state = it.state ?: ""
                        region = it.region ?: ""
                        city = it.city ?: ""
                        address = it.address ?: ""
                        comment = it.comment ?: ""
                    }
                    addressRepository.save(address)
                    contact.addAddress(address)
                }
            }
        }

        //Save contact in db
        val contactSaved = contactRepository.save(contact).toDTOWithAssociatedData()

        return contactSaved
    }

    override fun getContact(id: Long): Contact {
        val optionalContact = contactRepository.findById(id)
        if (optionalContact.isPresent) {
            val contact = optionalContact.get()
            return contact
        } else {
            logger.info("The contact with id $id was not found on the db")
            throw ContactNotFoundException("The contact with id equal to $id was not found!")
        }
    }

    override fun updateContact(contactDto: UpdateContactDTO, categoryParam: CategoryOptions): ContactDTO {
        val contactDb = getContact(contactDto.id)
        //Create a contact Entity from Db Contact
        val contact = Contact().apply {
            id = contactDb.id
            name = contactDb.name
            surname = contactDb.surname
            ssnCode = contactDb.ssnCode
            category = contactDb.category
            comment = contactDb.comment
        }

        if (contactDto.name != null) {

            contact.name = contactDto.name!!
        }

        if (contactDto.surname != null) {
            contact.surname = contactDto.surname!!
        }

        if (contactDto.ssnCode != null) {
            contact.ssnCode = contactDto.ssnCode!!
        }

        if (contactDto.category != null) {
            contact.category = categoryParam
        }

        if (contactDto.comment != null) {
            contact.comment = contactDto.comment!!
        }

        //Update the contact in db
        val contactSaved = contactRepository.save(contact).toDTO()
        return contactSaved
    }

    override fun checkExistingContact(contact: CreateContactDTO): Long? {
        val existingContacts = contactRepository.findAll()
        for (c in existingContacts) {
            if (contact.name == c.name &&
                contact.surname == c.surname &&
                contact.ssnCode == c.ssnCode &&
                contact.category == c.category.name &&
                contact.comment == c.comment &&
                contact.emails!!.size == c.emails.size &&
                contact.telephones!!.size == c.telephones.size &&
                contact.addresses!!.size == c.addresses.size
            ) {
                return c.id
            }
        }
        return null
    }

}