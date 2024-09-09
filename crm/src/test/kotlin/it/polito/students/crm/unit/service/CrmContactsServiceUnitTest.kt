package it.polito.students.crm.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.students.crm.CrmApplication
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Address
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Email
import it.polito.students.crm.entities.Telephone
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.DetailContactNotLinkedException
import it.polito.students.crm.exception_handlers.DetailNotFoundException
import it.polito.students.crm.exception_handlers.InvalidContactDetailsException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@SpringBootTest(
    classes = [CrmApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CrmContactsServiceUnitTest {
    private val messageRepository: MessageRepository = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val addressRepository: AddressRepository = mockk()
    private val emailRepository: EmailRepository = mockk()
    private val telephoneRepository: TelephoneRepository = mockk()

    val contactService = ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)
    private val messageService = MessageServiceImpl(
        messageRepository,
        historyRepository,
        contactRepository,
        emailRepository,
        telephoneRepository
    )
    val emailService = EmailServiceImpl(emailRepository, contactRepository, messageService)
    val addressService = AddressServiceImpl(addressRepository, contactRepository, messageService)
    val telephoneService = TelephoneServiceImpl(telephoneRepository, contactRepository, messageService)

    // Test cases
    val contactsDTOList: List<ContactDTO> = listOf(
        ContactDTO(
            id = 1,
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = CategoryOptions.CUSTOMER,
            comment = "Comment 1"
        ),
        ContactDTO(
            id = 2,
            name = "Name 2",
            surname = "Surname 2",
            ssnCode = "ssnCode 2",
            category = CategoryOptions.PROFESSIONAL,
            comment = "Comment 2"
        ),
        ContactDTO(
            id = 3,
            name = "Name 3",
            surname = "Surname 3",
            ssnCode = "ssnCode 3",
            category = CategoryOptions.UNKNOWN,
            comment = "Comment 3"
        )
    )

    // Test cases
    val contactsListToSave: List<Contact> = listOf(
        Contact().apply {
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            emails = mutableSetOf(Email().apply {
                id = 1
                email = "john.doe@example.com"
                comment = "This is a comment"
            })
            addresses = mutableSetOf(Address().apply {
                id = 1
                state = "Italy"
                region = "Lazio"
                city = "Rome"
                address = "via Napoli"
                comment = "This is a comment"
            })
            telephones = mutableSetOf(Telephone().apply {
                id = 1
                telephone = "3219877891"
                comment = "This is a comment"
            })
        },
        Contact().apply {
            name = "Name 2"
            surname = "Surname 2"
            ssnCode = "ssnCode 2"
            category = CategoryOptions.PROFESSIONAL
            comment = "Comment 2"
            emails = mutableSetOf(Email().apply {
                id = 2
                email = "sarah.doe@example.com"
                comment = "This is a comment"
            })
            addresses = mutableSetOf(Address().apply {
                id = 2
                state = "Italy"
                region = "Piemonte"
                city = "Turin"
                address = "via Roma"
                comment = "This is a comment"
            })
        },
        Contact().apply {
            name = "Name 3"
            surname = "Surname 3"
            ssnCode = "ssnCode 3"
            category = CategoryOptions.UNKNOWN
            comment = "Comment 3"
            telephones = mutableSetOf(Telephone().apply {
                id = 2
                telephone = "1237899871"
                comment = "This is a comment"
            })
        },
        Contact().apply {
            name = "Name 4"
            surname = "Surname 4"
            ssnCode = "ssnCode 4"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 4"
        },
        Contact().apply {
            name = "Name 5"
            surname = "Surname 5"
            ssnCode = "ssnCode 5"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 5"
            emails = mutableSetOf(
                Email().apply {
                    email = "chris.doe@example.com"
                    comment = "This is a comment"
                }
            )
        },
        Contact().apply {
            name = "Name 6"
            surname = "Surname 6"
            ssnCode = "ssnCode 6"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 6"
            addresses = mutableSetOf(Address().apply {
                state = "Italy"
                region = "Piemonte"
                city = "Turin"
                address = "via Roma"
                comment = "This is a comment"
            })
        }
    )

    // Test cases
    val createContactsList: List<CreateContactDTO> = listOf(
        CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "3219877891",
                    comment = "This is a comment",
                )
            ),
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Lazio",
                    city = "Rome",
                    address = "via Napoli",
                    comment = "This is a comment",
                )
            )
        ),
        CreateContactDTO(
            name = "Name 2",
            surname = "Surname 2",
            ssnCode = "ssnCode 2",
            category = "PROFESSIONAL",
            comment = "Comment 2",
            emails = listOf(
                CreateEmailDTO(
                    email = "sarah.doe@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = null,
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Piemonte",
                    city = "Turin",
                    address = "via Roma",
                    comment = "This is a comment"
                )
            )
        ),
        CreateContactDTO(
            name = "Name 3",
            surname = "Surname 3",
            ssnCode = "ssnCode 3",
            category = "UNKNOWN",
            comment = "Comment 3",
            emails = null,
            addresses = null,
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "1237899871",
                    comment = "This is a comment"
                )
            )
        ),
        CreateContactDTO(
            name = "Name 4",
            surname = "Surname 4",
            ssnCode = "ssnCode 4",
            category = "CUSTOMER",
            comment = "Comment 4",
            addresses = null,
            telephones = null,
            emails = null
        ),
        CreateContactDTO(
            name = "Name 5",
            surname = "Surname 5",
            ssnCode = "ssnCode 5",
            category = "CUSTOMER",
            comment = "Comment 5",
            emails = listOf(
                CreateEmailDTO(
                    email = "chris.doe@example.com",
                    comment = "This is a comment",
                )
            ),
            addresses = null,
            telephones = null
        ),
        CreateContactDTO(
            name = "Name 6",
            surname = "Surname 6",
            ssnCode = "ssnCode 6",
            category = "CUSTOMER",
            comment = "Comment 6",
            emails = null,
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Piemonte",
                    city = "Turin",
                    address = "via Roma",
                    comment = "This is a comment"
                )
            ),
            telephones = null
        ),
    )

    /**
     * GET ALL CONTACTS TEST CASES
     */

    @Test
    fun getAllContacts_goodCase() {
        val contacts: Page<Contact> = PageImpl(contactsDTOList.map { dto ->
            Contact().apply {
                id = dto.id
                name = dto.name
                surname = dto.surname
                ssnCode = dto.ssnCode
                category = dto.category
                comment = dto.comment
            }
        })
        val list = contacts.content.map { it.toDTO() }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), contacts.totalElements)


        every { contactRepository.findAll(PageRequest.of(0, 30)) } returns contacts

        val result = contactService.getAllContacts(0, 30, HashMap<ContactEnumFields, String>())

        verify(exactly = 1) { contactRepository.findAll(PageRequest.of(0, 30)) }
        assertEquals(expectedPage, result)
    }

    @Test
    fun getAllContacts_goodCaseFiltering() {
        val contacts: Page<Contact> = PageImpl(contactsDTOList.map { dto ->
            Contact().apply {
                id = dto.id
                name = dto.name
                surname = dto.surname
                ssnCode = dto.ssnCode
                category = dto.category
                comment = dto.comment
            }
        })
        val map = HashMap<ContactEnumFields, String>().apply {
            put(ContactEnumFields.NAME, "Name 1")
        }

        var list = contacts.content.map { it.toDTO() }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.name == filter.value }
                ContactEnumFields.SURNAME -> list.filter { it.surname == filter.value }
                ContactEnumFields.CATEGORY -> list.filter { it.category.name == filter.value }
                ContactEnumFields.SSN_CODE -> list.filter { it.ssnCode == filter.value }
                ContactEnumFields.COMMENT -> list.filter { it.comment == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), contacts.totalElements)

        every { contactRepository.findAll(PageRequest.of(0, 30)) } returns contacts

        val result = contactService.getAllContacts(0, 30, map)

        verify(exactly = 1) { contactRepository.findAll(PageRequest.of(0, 30)) }
        assertEquals(expectedPage, result)
    }

    @Test
    fun getAllContacts_goodCaseEmptyList() {
        val contacts: Page<Contact> = PageImpl(emptyList())
        val map = HashMap<ContactEnumFields, String>()

        var list = contacts.content.map { it.toDTO() }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.name == filter.value }
                ContactEnumFields.SURNAME -> list.filter { it.surname == filter.value }
                ContactEnumFields.CATEGORY -> list.filter { it.category.name == filter.value }
                ContactEnumFields.SSN_CODE -> list.filter { it.ssnCode == filter.value }
                ContactEnumFields.COMMENT -> list.filter { it.comment == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), contacts.totalElements)

        every { contactRepository.findAll(PageRequest.of(0, 30)) } returns contacts

        val result = contactService.getAllContacts(0, 30, map)

        verify(exactly = 1) { contactRepository.findAll(PageRequest.of(0, 30)) }
        assertEquals(expectedPage, result)
    }

    /**
     * STORE CONTACT TEST CASES
     */

    @Test
    fun storeContact_success() {
        val createContactDto: CreateContactDTO = createContactsList[1]
        val contact: Contact = contactsListToSave[1]
        val categoryOption = CategoryOptions.valueOf(createContactsList[1].category!!)

        for (emailField in contact.emails) {
            every { emailRepository.findByEmail(emailField.email) } returns emailField
        }

        for (addressField in contact.addresses) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        for (telephoneField in contact.telephones) {
            every { telephoneRepository.findByTelephone(telephoneField.telephone) } returns telephoneField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutEmail() {
        val createContactDto: CreateContactDTO = createContactsList[5]
        val categoryOption = CategoryOptions.valueOf(createContactsList[5].category!!)
        val contact: Contact = contactsListToSave[5]

        for (addressField in contact.addresses) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        for (telephoneField in contact.telephones) {
            every { telephoneRepository.findByTelephone(telephoneField.telephone) } returns telephoneField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutAddress() {
        val createContactDto: CreateContactDTO = createContactsList[4]
        val categoryOption = CategoryOptions.valueOf(createContactsList[4].category!!)
        val contact: Contact = contactsListToSave[4]

        for (emailField in contact.emails) {
            every { emailRepository.findByEmail(emailField.email) } returns emailField
        }

        for (addressField in contact.addresses) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        for (telephoneField in contact.telephones) {
            every { telephoneRepository.findByTelephone(telephoneField.telephone) } returns telephoneField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutTelephone() {
        val createContactDto: CreateContactDTO = createContactsList[1]
        val categoryOption = CategoryOptions.valueOf(createContactsList[1].category!!)
        val contact: Contact = contactsListToSave[1]

        for (emailField in contact.emails) {
            every { emailRepository.findByEmail(emailField.email) } returns emailField
        }

        for (addressField in contact.addresses) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutEmailAndAddress() {
        val createContactDto: CreateContactDTO = createContactsList[3]
        val categoryOption = CategoryOptions.valueOf(createContactsList[3].category!!)
        val contact: Contact = contactsListToSave[3]

        for (telephoneField in contact.telephones) {
            every { telephoneRepository.findByTelephone(telephoneField.telephone) } returns telephoneField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutEmailAndTelephone() {
        val createContactDto: CreateContactDTO = createContactsList[5]
        val categoryOption = CategoryOptions.valueOf(createContactsList[5].category!!)
        val contact: Contact = contactsListToSave[5]

        for (addressField in contact.addresses) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutAddressAndTelephone() {
        val createContactDto: CreateContactDTO = createContactsList[4]
        val categoryOption = CategoryOptions.valueOf(createContactsList[4].category!!)
        val contact: Contact = contactsListToSave[4]

        for (emailField in contact.emails) {
            every { emailRepository.findByEmail(emailField.email) } returns emailField
        }

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    @Test
    fun storeContact_successWithoutEmailAndTelephoneAndAddress() {
        val createContactDto: CreateContactDTO = createContactsList[3]
        val categoryOption = CategoryOptions.valueOf(createContactsList[3].category!!)
        val contact: Contact = contactsListToSave[3]

        every { contactRepository.save(any()) } returns contact

        val result = contactService.storeContact(createContactDto, categoryOption)

        assertEquals(contact.toDTOWithAssociatedData(), result)
    }

    /**
     * GET CONTACT BY ID TEST CASES
     */

    @Test
    fun getContactById_correctParameter() {
        every { contactRepository.findById(1) } returns Optional.of(contactsListToSave[0])

        val result = contactService.getContact(1)

        verify(exactly = 1) { contactRepository.findById(1) }
        assertEquals(contactsListToSave[0], result)
    }

    @Test
    fun getContactById_nonExistingID() {
        every { contactRepository.findById(30) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> { contactService.getContact(30) }
        assertEquals("The contact with id equal to 30 was not found!", exception.message)

        verify(exactly = 1) { contactRepository.findById(30) }
    }

    /**
     * POST /API/contacts/{contactId}/{whatContact} TEST CASES
     */
    @Test
    fun storeEmailList_goodCase() {
        val contactId = 1
        val emailList = listOf(
            CreateEmailDTO(email = "email1@example.com", comment = "Comment 1"),
            CreateEmailDTO(email = "email2@example.com", comment = "Comment 2"),
            CreateEmailDTO(email = "email3@example.com", comment = null)
        )
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId.toLong()) } returns Optional.of(contact)
        every { emailRepository.findByEmail(any()) } returns null

        val email1 = Email().apply {
            email = emailList[0].email
            comment = emailList[0].comment ?: ""
        }
        email1.addContact(contact)

        every { emailRepository.save(any()) } returns email1
        every { contactRepository.save(any()) } returns contact

        emailService.storeEmailList(contactId.toLong(), emailList)

        verify(exactly = 1) { contactRepository.findById(1) }
        verify(exactly = 3) { emailRepository.findByEmail(any()) }

    }

    @Test
    fun storeEmailList_InvalidContent() {
        val contactId = 1
        val emailList = null
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId.toLong()) } returns Optional.of(contact)

        assertThrows<InvalidContactDetailsException> {
            emailService.storeEmailList(contactId.toLong(), emailList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }

    }

    @Test
    fun storeEmailList_contactNotFound() {
        val contactId = 1
        val emailList = listOf(
            CreateEmailDTO(email = "email1@example.com", comment = "Comment 1"),
            CreateEmailDTO(email = "email2@example.com", comment = "Comment 2"),
            CreateEmailDTO(email = "email3@example.com", comment = null)
        )

        every { contactRepository.findById(contactId.toLong()) } returns Optional.empty()

        assertThrows<ContactNotFoundException> {
            emailService.storeEmailList(contactId.toLong(), emailList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }

    }

    @Test
    fun storeAddressList_goodCase() {
        val contactId: Long = 1
        val addressList = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Roma 11",
                comment = "first floor"
            ),
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Milano 15",
                comment = "second floor"
            ),
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Palermo 23",
                comment = "third floor"
            )
        )
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId) } returns Optional.of(contact)
        every {
            addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                any(),
                any(),
                any(),
                any()
            )
        } returns null
        every { addressRepository.save(any()) } returns Address().apply {
            state = addressList[0].state ?: ""
            region = addressList[0].region ?: ""
            city = addressList[0].city ?: ""
            address = addressList[0].address ?: ""
            comment = addressList[0].comment ?: ""
        }
        every { contactRepository.save(any()) } returns contact

        var addressSaved = addressService.storeAddressList(contactId, addressList)

        verify(exactly = 1) { contactRepository.findById(1) }
        verify(exactly = 3) { addressRepository.save(any()) }
        assertEquals(addressSaved.size, 3)
    }

    @Test
    fun storeAddressList_InvalidContent() {
        val contactId = 1
        val addressList = null
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId.toLong()) } returns Optional.of(contact)

        assertThrows<InvalidContactDetailsException> {
            addressService.storeAddressList(contactId.toLong(), addressList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }
    }

    @Test
    fun storeAddressList_contactNotFound() {
        val contactId = 1
        val addressList = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Roma 11",
                comment = "first floor"
            ),
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Milano 15",
                comment = "second floor"
            ),
            CreateAddressDTO(
                state = "Italy",
                region = "Piedmont",
                city = "Turin",
                address = "Via Palermo 23",
                comment = "third floor"
            )
        )

        every { contactRepository.findById(contactId.toLong()) } returns Optional.empty()

        assertThrows<ContactNotFoundException> {
            addressService.storeAddressList(contactId.toLong(), addressList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }

    }

    @Test
    fun storeTelephoneList_goodCase() {
        val contactId = 1
        val telephoneList = listOf(
            CreateTelephoneDTO(telephone = "1234567890", comment = null),
            CreateTelephoneDTO(telephone = "1234567891", comment = null),
            CreateTelephoneDTO(telephone = "1234567892", comment = null)
        )
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId.toLong()) } returns Optional.of(contact)
        every { telephoneRepository.findByTelephone(any()) } returns null
        every { telephoneRepository.save(any()) } returns Telephone().apply {
            telephone = telephoneList[0].telephone
            comment = telephoneList[0].comment ?: ""
        }
        every { contactRepository.save(any()) } returns contact

        telephoneService.storeTelephoneList(contactId.toLong(), telephoneList)

        verify(exactly = 1) { contactRepository.findById(1) }
        verify(exactly = 3) { telephoneRepository.save(any()) }

    }

    @Test
    fun storeTelephoneList_InvalidContent() {
        val contactId = 1
        val telephoneList = null
        val contact = Contact().apply {
            id = 1
            name = "Name"
            surname = "Surname"
            ssnCode = "ssnCode"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
        }

        every { contactRepository.findById(contactId.toLong()) } returns Optional.of(contact)

        assertThrows<InvalidContactDetailsException> {
            telephoneService.storeTelephoneList(contactId.toLong(), telephoneList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }

    }

    @Test
    fun storeTelephoneList_contactNotFound() {
        val contactId = 1
        val telephoneList = listOf(
            CreateTelephoneDTO(telephone = "1234567890", comment = null),
            CreateTelephoneDTO(telephone = "1234567891", comment = null),
            CreateTelephoneDTO(telephone = "1234567892", comment = null)
        )

        every { contactRepository.findById(contactId.toLong()) } returns Optional.empty()

        assertThrows<ContactNotFoundException> {
            telephoneService.storeTelephoneList(contactId.toLong(), telephoneList)
        }

        verify(exactly = 1) { contactRepository.findById(1) }

    }

    /**
     * PUT /API/contacts/{contactId}/{whatContact}/{id} TEST CASES
     */

    @Test
    fun updateEmail_success() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()
        val createEmailDto = CreateEmailDTO(email = email.email, comment = email.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { emailRepository.findById(email.id) } returns Optional.of(email)
        every { emailRepository.save(email) } returns email

        val result = emailService.modifyEmail(contact.id, createEmailDto, email.id)

        assertEquals(email.toDTO(), result)
    }

    @Test
    fun updateEmail_contactNotFound() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()
        val createEmailDto = CreateEmailDTO(email = email.email, comment = email.comment)

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            emailService.modifyEmail(
                contact.id, createEmailDto, email.id
            )
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun updateEmail_emailNotFound() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()
        val createEmailDto = CreateEmailDTO(email = email.email, comment = email.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { emailRepository.findById(email.id) } returns Optional.empty()

        val exception = assertThrows<Exception> {
            emailService.modifyEmail(
                contact.id, createEmailDto, email.id
            )
        }

        val expectedErrorMessage = "Email with id ${email.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun updateEmail_emptyComment() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()
        email.comment = ""
        val createEmailDto = CreateEmailDTO(email = email.email, comment = email.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { emailRepository.findById(email.id) } returns Optional.of(email)
        every { emailRepository.save(email) } returns email

        val result = emailService.modifyEmail(contact.id, createEmailDto, email.id)

        assertEquals(email.toDTO(), result)
    }

    @Test
    fun updateTelephone_success() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()
        val createTelephoneDto = CreateTelephoneDTO(telephone = telephone.telephone, comment = telephone.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { telephoneRepository.findById(telephone.id) } returns Optional.of(telephone)
        every { telephoneRepository.save(telephone) } returns telephone

        val result = telephoneService.modifyTelephone(contact.id, createTelephoneDto, telephone.id)

        assertEquals(telephone.toDTO().id, result.id)
        assertEquals(telephone.toDTO().telephone, result.telephone)
        assertEquals(telephone.toDTO().comment, result.comment)
    }

    @Test
    fun updateTelephone_contactNotFound() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()
        val createTelephoneDto = CreateTelephoneDTO(telephone = telephone.telephone, comment = telephone.comment)

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            telephoneService.modifyTelephone(
                contact.id, createTelephoneDto, telephone.id
            )
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun updateTelephone_emailNotFound() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()
        val createTelephoneDto = CreateTelephoneDTO(telephone = telephone.telephone, comment = telephone.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { telephoneRepository.findById(telephone.id) } returns Optional.empty()

        val exception = assertThrows<Exception> {
            telephoneService.modifyTelephone(
                contact.id, createTelephoneDto, telephone.id
            )
        }

        val expectedErrorMessage = "Telephone with id ${telephone.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    /*@Test
    fun updateTelephone_emptyComment() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()
        telephone.comment = ""
        val createTelephoneDto = CreateTelephoneDTO(telephone = telephone.telephone, comment = telephone.comment)

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { telephoneRepository.findById(telephone.id) } returns Optional.of(telephone)
        every { telephoneRepository.save(telephone) } returns telephone

        val result = telephoneService.modifyTelephone(contact.id, createTelephoneDto, telephone.id)

        assertEquals(telephone.toDTO(), result)
    }*/

    @Test
    fun updateAddress_success() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()
        val createAddressDto = CreateAddressDTO(
            state = address.state,
            region = address.region,
            city = address.city,
            address = address.address,
            comment = address.comment
        )

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { addressRepository.findById(address.id) } returns Optional.of(address)
        every { addressRepository.save(address) } returns address

        val result = addressService.modifyAddress(contact.id, createAddressDto, address.id)

        assertEquals(address.toDTO(), result)
    }

    @Test
    fun updateAddress_contactNotFound() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()
        val createAddressDto = CreateAddressDTO(
            state = address.state,
            region = address.region,
            city = address.city,
            address = address.address,
            comment = address.comment
        )

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            addressService.modifyAddress(
                contact.id, createAddressDto, address.id
            )
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun updateAddress_emailNotFound() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()
        val createAddressDto = CreateAddressDTO(
            state = address.state,
            region = address.region,
            city = address.city,
            address = address.address,
            comment = address.comment
        )

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { addressRepository.findById(address.id) } returns Optional.empty()

        val exception = assertThrows<Exception> {
            addressService.modifyAddress(
                contact.id, createAddressDto, address.id
            )
        }

        val expectedErrorMessage = "Address with id ${address.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun updateAddress_emptyComment() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()
        address.comment = ""
        val createAddressDto = CreateAddressDTO(
            state = address.state,
            region = address.region,
            city = address.city,
            address = address.address,
            comment = address.comment
        )

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { addressRepository.findById(address.id) } returns Optional.of(address)
        every { addressRepository.save(address) } returns address

        val result = addressService.modifyAddress(contact.id, createAddressDto, address.id)

        assertEquals(address.toDTO(), result)
    }

    /**
     *  DELETE /API/contacts/{contactId}/{whatContact}/{id}
     */

    @Test
    fun deleteContact_contactIdEmailId_good() {
        // Simulate to have a contact with an email and the email is linked to this email
        val contactBeforeDelete = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            emails = mutableSetOf(Email().apply {
                id = 1
                email = "john.doe@example.com"
                comment = "This is a comment"
            })
        }
        val emailBeforeDelete = contactBeforeDelete.emails.first()
        emailBeforeDelete.addContact(contactBeforeDelete)

        // This is the result after to delete
        val afterSaveContact = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            emails = mutableSetOf()
        }
        val afterSaveEmail = contactBeforeDelete.emails.first() // the email is not linked to the contact anymore

        // Simulate the calls
        every { contactRepository.findById(contactBeforeDelete.id) } returns Optional.of(contactBeforeDelete)
        every { emailRepository.findById(emailBeforeDelete.id) } returns Optional.of(emailBeforeDelete)
        every { contactRepository.save(any()) } returns afterSaveContact
        every { emailRepository.save(emailBeforeDelete) } returns afterSaveEmail

        // Call the function
        emailService.deleteContactEmail(contactBeforeDelete.id, emailBeforeDelete.id)

        // Check if they are called exactly once
        verify(exactly = 1) { contactRepository.findById(contactBeforeDelete.id) }
        verify(exactly = 1) { emailRepository.findById(emailBeforeDelete.id) }
        verify(exactly = 1) { contactRepository.save(contactBeforeDelete) }
        verify(exactly = 1) { emailRepository.save(emailBeforeDelete) }
    }

    @Test
    fun deleteContact_contactIdTelephoneId_good() {
        val contactBeforeDelete = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            telephones = mutableSetOf(Telephone().apply {
                id = 1
                telephone = "0245678936"
                comment = "comment"
            })
        }
        val phoneToDelete = contactBeforeDelete.telephones.first()
        phoneToDelete.addContact(contactBeforeDelete)

        val contactAfterDelete = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            telephones = mutableSetOf()
        }

        val afterSaveTelephone =
            contactBeforeDelete.telephones.first() // the telephone is not linked to the contact anymore

        every { contactRepository.findById(contactBeforeDelete.id) } returns Optional.of(contactBeforeDelete)
        every { telephoneRepository.findById(phoneToDelete.id) } returns Optional.of(phoneToDelete)
        every { contactRepository.save(any()) } returns contactAfterDelete
        every { telephoneRepository.save(phoneToDelete) } returns afterSaveTelephone

        telephoneService.deleteContactTelephone(contactBeforeDelete.id, phoneToDelete.id)

        verify(exactly = 1) { contactRepository.findById(contactBeforeDelete.id) }
        verify(exactly = 1) { telephoneRepository.findById(phoneToDelete.id) }
        verify(exactly = 1) { contactRepository.save(contactBeforeDelete) }
        verify(exactly = 1) { telephoneRepository.save(phoneToDelete) }
    }

    @Test
    fun deleteContact_contactIdAddressId_good() {
        val contactBeforeDelete = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            addresses = mutableSetOf(Address().apply {
                id = 1
                state = "Italy"
                region = "Piemonte"
                city = "Torino"
                address = "Corso Duca"
                comment = "comment"
            })
        }

        val addressToDelete = contactBeforeDelete.addresses.first()
        addressToDelete.addContact(contactBeforeDelete)

        val contactAfterDelete = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            addresses = mutableSetOf()
        }

        val afterSaveAddress = contactBeforeDelete.addresses.first() // the address is not linked to the contact anymore

        every { contactRepository.findById(contactBeforeDelete.id) } returns Optional.of(contactBeforeDelete)
        every { addressRepository.findById(addressToDelete.id) } returns Optional.of(addressToDelete)
        every { contactRepository.save(any()) } returns contactAfterDelete
        every { addressRepository.save(addressToDelete) } returns afterSaveAddress

        addressService.deleteContactAddress(contactBeforeDelete.id, addressToDelete.id)

        verify(exactly = 1) { contactRepository.findById(contactBeforeDelete.id) }
        verify(exactly = 1) { addressRepository.findById(addressToDelete.id) }
        verify(exactly = 1) { contactRepository.save(contactBeforeDelete) }
        verify(exactly = 1) { addressRepository.save(addressToDelete) }
    }

    @Test
    fun deleteContactEmail_contactNotFound() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            emailService.deleteContactEmail(contact.id, email.id)
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactTelephone_contactNotFound() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            telephoneService.deleteContactTelephone(contact.id, telephone.id)
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactAddress_contactNotFound() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()

        every { contactRepository.findById(contact.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            addressService.deleteContactAddress(contact.id, address.id)
        }

        val expectedErrorMessage = "Contact with id=${contact.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactEmail_EmailNotFound() {
        val contact = contactsListToSave.first()
        val email = contact.emails.first()

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { emailRepository.findById(email.id) } returns Optional.empty()

        val exception = assertThrows<DetailNotFoundException> {
            emailService.deleteContactEmail(contact.id, email.id)
        }

        val expectedErrorMessage = "Email with id = ${email.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactTelephone_TelephoneNotFound() {
        val contact = contactsListToSave.first()
        val telephone = contact.telephones.first()

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { telephoneRepository.findById(telephone.id) } returns Optional.empty()

        val exception = assertThrows<DetailNotFoundException> {
            telephoneService.deleteContactTelephone(contact.id, telephone.id)
        }

        val expectedErrorMessage = "Telephone with id = ${telephone.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactAddress_AddressNotFound() {
        val contact = contactsListToSave.first()
        val address = contact.addresses.first()

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { addressRepository.findById(address.id) } returns Optional.empty()

        val exception = assertThrows<DetailNotFoundException> {
            addressService.deleteContactAddress(contact.id, address.id)
        }

        val expectedErrorMessage = "Address with id = ${address.id} not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactEmail_ContactAndEmailNotFound() {
        val contact = contactsListToSave.first()
        val email = Email().apply {
            id = 145
            email = "ex@example.com"
            comment = "A comment"
        }

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { emailRepository.findById(email.id) } returns Optional.of(email)

        val exception = assertThrows<DetailContactNotLinkedException> {
            emailService.deleteContactEmail(contact.id, email.id)
        }

        val expectedErrorMessage = "Contact with id=${contact.id} doesn't contain the email with id=${email.id}!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactTelephone_ContactAndTelephoneNotFound() {
        val contact = contactsListToSave.first()
        val telephone = Telephone().apply {
            id = 145
            telephone = "0119876543"
            comment = "A comment"
        }

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { telephoneRepository.findById(telephone.id) } returns Optional.of(telephone)

        val exception = assertThrows<DetailContactNotLinkedException> {
            telephoneService.deleteContactTelephone(contact.id, telephone.id)
        }

        val expectedErrorMessage =
            "Contact with id=${contact.id} doesn't contain the telephone number with id=${telephone.id}!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun deleteContactAddress_ContactAndAddressNotFound() {
        val contact = contactsListToSave.first()
        val address = Address().apply {
            id = 145
            state = "Italy"
            region = "Piemonte"
            city = "Torino"
            address = "Corso Duca"
            comment = "comment"
        }

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { addressRepository.findById(address.id) } returns Optional.of(address)

        val exception = assertThrows<DetailContactNotLinkedException> {
            addressService.deleteContactAddress(contact.id, address.id)
        }

        val expectedErrorMessage = "Contact with id=${contact.id} doesn't contain the address with id=${address.id}!"
        assertEquals(expectedErrorMessage, exception.message)
    }

    /**
     * PATCH /API/contacts/{contactID}
     */
    @Test
    fun patchContact_ContactIdGoodCase() {
        val contact = Contact().apply {
            id = 1
            name = "UGO"
            surname = "ROSSI"
            ssnCode = "oldSSNCode"
            category = CategoryOptions.CUSTOMER
            comment = "A comment"
        }

        val categoryParam = CategoryOptions.PROFESSIONAL

        val updateContactDto = UpdateContactDTO(
            id = 1,
            name = "UGO",
            surname = "ROSSI",
            ssnCode = "SSNCode",
            category = "PROFESSIONAL",
            comment = "A comment"
        )

        every { contactRepository.findById(contact.id) } returns Optional.of(contact)
        every { contactRepository.save(any()) } returns Contact().apply {
            id = 1
            name = "UGO"
            surname = "ROSSI"
            ssnCode = "SSNCode"
            category = CategoryOptions.PROFESSIONAL
            comment = "A comment"
        }

        var result = contactService.updateContact(updateContactDto, categoryParam)

        verify(exactly = 1) { contactRepository.findById(contact.id) }
        assertEquals(result.id, updateContactDto.id)
        assertEquals(result.name, updateContactDto.name)
        assertEquals(result.surname, updateContactDto.surname)
        assertEquals(result.ssnCode, updateContactDto.ssnCode)
        assertEquals(result.category, categoryParam)
        assertEquals(result.comment, updateContactDto.comment)
    }

    @Test
    fun patchContact_ContactIdNotFound() {
        val categoryParam = CategoryOptions.PROFESSIONAL

        val updateContactDto = UpdateContactDTO(
            id = 145,
            name = "UGO",
            surname = "ROSSI",
            ssnCode = "SSNCode",
            category = "PROFESSIONAL",
            comment = "A comment"
        )

        every { contactRepository.findById(updateContactDto.id) } returns Optional.empty()

        val exception = assertThrows<ContactNotFoundException> {
            contactService.updateContact(updateContactDto, categoryParam)
        }

        val expectedErrorMessage = "The contact with id equal to ${updateContactDto.id} was not found!"
        assertEquals(expectedErrorMessage, exception.message)
    }

}
