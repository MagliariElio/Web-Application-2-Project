package it.polito.students.crm.integration.service

import it.polito.students.crm.dtos.CreateAddressDTO
import it.polito.students.crm.dtos.CreateContactDTO
import it.polito.students.crm.dtos.CreateEmailDTO
import it.polito.students.crm.dtos.CreateTelephoneDTO
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.InvalidContactDetailsException
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmContactServiceIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var addressRepository: AddressRepository

    @Autowired
    private lateinit var emailRepository: EmailRepository

    @Autowired
    private lateinit var telephoneRepository: TelephoneRepository

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    private val contactService: ContactServiceImpl by lazy {
        ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)
    }

    private val messageService: MessageServiceImpl by lazy {
        MessageServiceImpl(
            messageRepository,
            historyRepository,
            contactRepository,
            emailRepository,
            telephoneRepository
        )
    }

    private val emailService: EmailServiceImpl by lazy {
        EmailServiceImpl(emailRepository, contactRepository, messageService)
    }

    private val telephoneService: TelephoneServiceImpl by lazy {
        TelephoneServiceImpl(telephoneRepository, contactRepository, messageService)
    }

    private val addressService: AddressServiceImpl by lazy {
        AddressServiceImpl(addressRepository, contactRepository, messageService)
    }

    @BeforeEach
    fun setUp() {
        // Clean repositories before each test
        addressRepository.deleteAll()
        emailRepository.deleteAll()
        telephoneRepository.deleteAll()
        contactRepository.deleteAll()
        historyRepository.deleteAll()
        messageRepository.deleteAll()
    }

    //Create new contact
    val createContactDto = CreateContactDTO(
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
    )
    private val createContactDto1 = CreateContactDTO(
        name = "Name2",
        surname = "Surname 2",
        ssnCode = "ssnCode 2",
        category = "CUSTOMER",
        comment = "Comment 2",
        emails = listOf(
            CreateEmailDTO(
                email = "jane.doe@example.com",
                comment = "This is another comment",
            )
        ),
        telephones = listOf(
            CreateTelephoneDTO(
                telephone = "3219877892",
                comment = "This is another comment",
            )
        ),
        addresses = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Lombardy",
                city = "Milan",
                address = "via Verdi",
                comment = "This is another comment",
            )
        )
    )

    private val createContactDto2 = CreateContactDTO(
        name = "Name 3",
        surname = "Surname 3",
        ssnCode = "ssnCode 3",
        category = "CUSTOMER",
        comment = "Comment 3",
        emails = listOf(
            CreateEmailDTO(
                email = "test@example.com",
                comment = "This is a test comment",
            )
        ),
        telephones = listOf(
            CreateTelephoneDTO(
                telephone = "3219877893",
                comment = "This is a test comment",
            )
        ),
        addresses = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Veneto",
                city = "Venice",
                address = "via Roma",
                comment = "This is a test comment",
            )
        )
    )

    /*
    *
    *   GET /API/contacts test cases
    *
     */

    fun createManyContacts() {
        val contactDtos = listOf(createContactDto, createContactDto1, createContactDto2)

        contactDtos.forEach { contactDto ->
            contactService.storeContact(contactDto, CategoryOptions.valueOf(contactDto.category!!))
        }
    }

    @Test
    fun getAllContacts_goodCase() {
        createManyContacts()

        val contacts = listOf(createContactDto, createContactDto1, createContactDto2)

        val result = contactService.getAllContacts(0, 30, HashMap())

        assert(result.content.size == 3)

        result.content.forEachIndexed { index, contactDTO ->
            val expectedContactDTO = contacts[index]

            assert(expectedContactDTO.name == contactDTO.name)
            assert(expectedContactDTO.surname == contactDTO.surname)
            assert(expectedContactDTO.ssnCode == contactDTO.ssnCode)
            assert(CategoryOptions.valueOf(expectedContactDTO.category!!) == contactDTO.category)
            assert(expectedContactDTO.comment == contactDTO.comment)

            /**  TODO           SE VOGLIAMO POI RITORNARE ANCHE LE MAIL, I NUMERI DI TELEFONO E GLI INDIRIZZI
            val expectedAddress = expectedContactDTO.addresses?.get(0)
            assert(expectedAddress.state == contactDTO.addresses[0].state)
            assert(expectedAddress.region == contactDTO.addresses[0].region)
            assert(expectedAddress.city == contactDTO.addresses[0].city)
            assert(expectedAddress.address == contactDTO.addresses[0].address)
            assert(expectedAddress.comment == contactDTO.addresses[0].comment)


            val expectedEmail = expectedContactDTO.emails?.get(0)
            assert(expectedEmail.email == contactDTO.emails[0].email)
            assert(expectedEmail.comment == contactDTO.emails[0].comment)

            val expectedTelephone = expectedContactDTO.telephones?.get(0)
            assert(expectedTelephone.telephone == contactDTO.telephones[0].telephone)
            assert(expectedTelephone.comment == contactDTO.telephones[0].comment)
             */
        }

    }

    @Test
    fun getAllContacts_goodCasePageSize() {
        createManyContacts()

        val contacts = listOf(createContactDto, createContactDto1, createContactDto2)

        val result = contactService.getAllContacts(0, 2, HashMap())

        assert(result.content.size == 2)

        result.content.forEachIndexed { index, contactDTO ->
            val expectedContactDTO = contacts[index]

            assert(expectedContactDTO.name == contactDTO.name)
            assert(expectedContactDTO.surname == contactDTO.surname)
            assert(expectedContactDTO.ssnCode == contactDTO.ssnCode)
            assert(CategoryOptions.valueOf(expectedContactDTO.category!!) == contactDTO.category)
            assert(expectedContactDTO.comment == contactDTO.comment)

            /** TODO            SE VOGLIAMO POI RITORNARE ANCHE LE MAIL, I NUMERI DI TELEFONO E GLI INDIRIZZI
            val expectedAddress = expectedContactDTO.addresses?.get(0)
            assert(expectedAddress.state == contactDTO.addresses[0].state)
            assert(expectedAddress.region == contactDTO.addresses[0].region)
            assert(expectedAddress.city == contactDTO.addresses[0].city)
            assert(expectedAddress.address == contactDTO.addresses[0].address)
            assert(expectedAddress.comment == contactDTO.addresses[0].comment)


            val expectedEmail = expectedContactDTO.emails?.get(0)
            assert(expectedEmail.email == contactDTO.emails[0].email)
            assert(expectedEmail.comment == contactDTO.emails[0].comment)

            val expectedTelephone = expectedContactDTO.telephones?.get(0)
            assert(expectedTelephone.telephone == contactDTO.telephones[0].telephone)
            assert(expectedTelephone.comment == contactDTO.telephones[0].comment)
             */
        }

    }

    @Test
    fun getAllContacts_goodCasePageNumber() {
        createManyContacts()

        val contacts = listOf(createContactDto, createContactDto1, createContactDto2)

        val result = contactService.getAllContacts(1, 2, HashMap())

        assert(result.content.size == 1)

        result.content.forEachIndexed { _, contactDTO ->
            val expectedContactDTO = contacts[2]

            assert(expectedContactDTO.name == contactDTO.name)
            assert(expectedContactDTO.surname == contactDTO.surname)
            assert(expectedContactDTO.ssnCode == contactDTO.ssnCode)
            assert(CategoryOptions.valueOf(expectedContactDTO.category!!) == contactDTO.category)
            assert(expectedContactDTO.comment == contactDTO.comment)

            /**   TODO          SE VOGLIAMO POI RITORNARE ANCHE LE MAIL, I NUMERI DI TELEFONO E GLI INDIRIZZI
            val expectedAddress = expectedContactDTO.addresses?.get(0)
            assert(expectedAddress.state == contactDTO.addresses[0].state)
            assert(expectedAddress.region == contactDTO.addresses[0].region)
            assert(expectedAddress.city == contactDTO.addresses[0].city)
            assert(expectedAddress.address == contactDTO.addresses[0].address)
            assert(expectedAddress.comment == contactDTO.addresses[0].comment)


            val expectedEmail = expectedContactDTO.emails?.get(0)
            assert(expectedEmail.email == contactDTO.emails[0].email)
            assert(expectedEmail.comment == contactDTO.emails[0].comment)

            val expectedTelephone = expectedContactDTO.telephones?.get(0)
            assert(expectedTelephone.telephone == contactDTO.telephones[0].telephone)
            assert(expectedTelephone.comment == contactDTO.telephones[0].comment)
             */
        }

    }

    @Test
    fun getAllContacts_goodCaseFiltering() {
        createManyContacts()

        val contacts = listOf(createContactDto, createContactDto1, createContactDto2)

        val map = HashMap<ContactEnumFields, String>().apply {
            put(ContactEnumFields.NAME, "Name 1")
        }

        val result = contactService.getAllContacts(0, 30, map)

        assert(result.content.size == 1)

        val contactDTO = result.content[0]
        val expectedContactDTO = contacts[0]

        assert(expectedContactDTO.name == contactDTO.name)
        assert(expectedContactDTO.surname == contactDTO.surname)
        assert(expectedContactDTO.ssnCode == contactDTO.ssnCode)
        assert(CategoryOptions.valueOf(expectedContactDTO.category!!) == contactDTO.category)
        assert(expectedContactDTO.comment == contactDTO.comment)

        /**  TODO           SE VOGLIAMO POI RITORNARE ANCHE LE MAIL, I NUMERI DI TELEFONO E GLI INDIRIZZI
        val expectedAddress = expectedContactDTO.addresses?.get(0)
        assert(expectedAddress.state == contactDTO.addresses[0].state)
        assert(expectedAddress.region == contactDTO.addresses[0].region)
        assert(expectedAddress.city == contactDTO.addresses[0].city)
        assert(expectedAddress.address == contactDTO.addresses[0].address)
        assert(expectedAddress.comment == contactDTO.addresses[0].comment)


        val expectedEmail = expectedContactDTO.emails?.get(0)
        assert(expectedEmail.email == contactDTO.emails[0].email)
        assert(expectedEmail.comment == contactDTO.emails[0].comment)

        val expectedTelephone = expectedContactDTO.telephones?.get(0)
        assert(expectedTelephone.telephone == contactDTO.telephones[0].telephone)
        assert(expectedTelephone.comment == contactDTO.telephones[0].comment)
         */

    }

    @Test
    fun getAllContacts_goodCaseEmptyList() {
        val map = HashMap<ContactEnumFields, String>()

        val result = contactService.getAllContacts(0, 30, map)

        assert(result.content.size == 0)
    }

    //storeContact test cases
    /**
     * storeContact and getContact TEST CASES
     */
    @Test
    fun storeContact_checkContact() {
        //Create new contact
        val createContact = createContactDto

        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //get the previously created contact
        val result = contactService.getContact(contact.contactDTO.id)

        assert(result.name == createContact.name)
        assert(result.surname == createContact.surname)
        assert(result.ssnCode == createContact.ssnCode)
        assert(result.category.name == createContact.category)
        assert(result.comment == createContact.comment)
    }

    @Test
    fun storeContact_checkContactNoEmail() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(),
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
        )
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //get the previously created contact
        val result = contactService.getContact(contact.contactDTO.id)

        assert(result.name == createContact.name)
        assert(result.surname == createContact.surname)
        assert(result.ssnCode == createContact.ssnCode)
        assert(result.category.name == createContact.category)
        assert(result.comment == createContact.comment)
    }

    @Test
    fun storeContact_checkContactNoAddress() {
        //Create new contact
        val createContact = CreateContactDTO(
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
            addresses = listOf()
        )
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //get the previously created contact
        val result = contactService.getContact(contact.contactDTO.id)

        assert(result.name == createContact.name)
        assert(result.surname == createContact.surname)
        assert(result.ssnCode == createContact.ssnCode)
        assert(result.category.name == createContact.category)
        assert(result.comment == createContact.comment)
    }

    @Test
    fun storeContact_checkContactNoTelephone() {
        //Create new contact
        val createContact = CreateContactDTO(
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
            telephones = listOf(),
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Lazio",
                    city = "Rome",
                    address = "via Napoli",
                    comment = "This is a comment",
                )
            )
        )
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //get the previously created contact
        val result = contactService.getContact(contact.contactDTO.id)

        assert(result.name == createContact.name)
        assert(result.surname == createContact.surname)
        assert(result.ssnCode == createContact.ssnCode)
        assert(result.category.name == createContact.category)
        assert(result.comment == createContact.comment)
    }

    /**
     *
     *   POST /API/contacts/{contactID}/{whatContact} test cases
     *
     */

    @Test
    fun getContact_notFoundContact() {
        val contactId: Long = 999
        val expectedMessage = "The contact with id equal to $contactId was not found!"

        val result = assertThrows<ContactNotFoundException> {
            contactService.getContact(contactId)
        }

        assert(expectedMessage == result.message)
    }

    @Test
    fun getContact_negativeId() {
        val contactId: Long = -1
        val expectedMessage = "The contact with id equal to $contactId was not found!"

        val result = assertThrows<ContactNotFoundException> {
            contactService.getContact(contactId)
        }

        assert(expectedMessage == result.message)
    }

    /**
     *   POST /API/contacts/{contactID}/{whatContact} test cases
     */
    fun createContactAndReturnItsId(): Long {
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "name@example.com",
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
        )

        val result = contactService.storeContact(createContact, CategoryOptions.CUSTOMER)

        return result.contactDTO.id

    }

    @Test
    fun storeEmailList_goodCase() {
        val contactId = createContactAndReturnItsId()
        val emailList = listOf(
            CreateEmailDTO(email = "email1@example.com", comment = "Comment 1"),
            CreateEmailDTO(email = "email2@example.com", comment = "Comment 2"),
            CreateEmailDTO(email = "email3@example.com", comment = null)
        )

        val result = emailService.storeEmailList(contactId, emailList)

        assert(result.size == 3)

        result.forEachIndexed { index, email ->
            assert(email.id > 0)
            assert(emailList[index].email == email.email)
            if (index == 2) {
                assert(email.comment == "")
            } else {
                assert(emailList[index].comment == email.comment)
            }

        }

    }

    @Test
    fun storeEmailList_InvalidContent() {
        val contactId = createContactAndReturnItsId()
        val emailList = null

        assertThrows<InvalidContactDetailsException> {
            emailService.storeEmailList(contactId, emailList)
        }

    }

    @Test
    fun storeEmailList_contactNotFound() {
        val contactId = 999
        val emailList = listOf(
            CreateEmailDTO(email = "email1@example.com", comment = "Comment 1"),
            CreateEmailDTO(email = "email2@example.com", comment = "Comment 2"),
            CreateEmailDTO(email = "email3@example.com", comment = null)
        )

        assertThrows<ContactNotFoundException> {
            emailService.storeEmailList(contactId.toLong(), emailList)
        }

    }

    @Test
    fun storeAddressList_goodCase() {                            //It returns uppercase addresses.. to be changed
        val contactId = createContactAndReturnItsId()
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

        val savedAddresses = addressService.storeAddressList(contactId, addressList)

        assert(addressList.size == savedAddresses.size)

        for ((index, savedAddress) in savedAddresses.withIndex()) {
            val expectedAddress = addressList[index]
            assert(expectedAddress.state == savedAddress.state)
            assert(expectedAddress.region == savedAddress.region)
            assert(expectedAddress.city == savedAddress.city)
            assert(expectedAddress.address == savedAddress.address)
            assert(expectedAddress.comment == savedAddress.comment)
        }
    }

    @Test
    fun storeAddressList_InvalidContent() {
        val contactId = createContactAndReturnItsId()
        val addressList = null

        assertThrows<InvalidContactDetailsException> {
            addressService.storeAddressList(contactId, addressList)
        }
    }

    @Test
    fun storeAddressList_contactNotFound() {
        val contactId = 999
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

        assertThrows<ContactNotFoundException> {
            addressService.storeAddressList(contactId.toLong(), addressList)
        }

    }

    @Test
    fun storeTelephoneList_goodCase() {
        val contactId = createContactAndReturnItsId()
        val telephoneList = listOf(
            CreateTelephoneDTO(telephone = "1234567890", comment = "Comment 1"),
            CreateTelephoneDTO(telephone = "1234567891", comment = "Comment 2"),
            CreateTelephoneDTO(telephone = "1234567892", comment = null)
        )

        val savedTelephones = telephoneService.storeTelephoneList(contactId, telephoneList)

        assert(telephoneList.size == savedTelephones.size)

        for ((index, savedTelephone) in savedTelephones.withIndex()) {
            val expectedTelephone = telephoneList[index]
            assert(expectedTelephone.telephone == savedTelephone.telephone)
            if (index == 2) {
                assert(savedTelephone.comment == "")
            } else {
                assert(expectedTelephone.comment == savedTelephone.comment)
            }
        }
    }

    @Test
    fun storeTelephoneList_InvalidContent() {
        val contactId = createContactAndReturnItsId()
        val telephoneList = null

        assertThrows<InvalidContactDetailsException> {
            telephoneService.storeTelephoneList(contactId, telephoneList)
        }

    }

    @Test
    fun storeTelephoneList_contactNotFound() {
        val contactId = 1
        val telephoneList = listOf(
            CreateTelephoneDTO(telephone = "1234567890", comment = null),
            CreateTelephoneDTO(telephone = "1234567891", comment = null),
            CreateTelephoneDTO(telephone = "1234567892", comment = null)
        )

        assertThrows<ContactNotFoundException> {
            telephoneService.storeTelephoneList(contactId.toLong(), telephoneList)
        }

    }

    /**
     * PUT API/contacts/{contactID}/{whatContact}/{id} TEST CASES
     */
    @Test
    fun modifyTelephone_createAndEdit() {
        //Create new contact
        val createContact = createContactDto
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //modify the email
        val newTelephone = telephoneService.modifyTelephone(
            contact.contactDTO.id,
            CreateTelephoneDTO("3333333333", "Updated comment"),
            contact.telephoneDTOs[0].id
        )

        assert(newTelephone.telephone == "3333333333")
        assert(newTelephone.comment == "Updated comment")
    }

    @Test
    fun modifyTelephone_contactNotFound() {
        assertThrows<ContactNotFoundException> {
            // Call the function with a non-existing message ID
            telephoneService.modifyTelephone(1000, CreateTelephoneDTO("3333333333", "Updated comment"), 0)
        }
    }

    @Test
    fun modifyTelephone_telephoneNotFound() {
        //Create new contact
        val createContact = CreateContactDTO(
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
        )
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)
        assertThrows<Exception> {
            // Call the function with a non-existing message ID
            telephoneService.modifyTelephone(
                contact.contactDTO.id,
                CreateTelephoneDTO("3333333333", "Updated comment"),
                0
            )
        }
    }

    //email tests
    @Test
    fun modifyEmail_createAndEdit() {
        //Create new contact
        val createContact = createContactDto
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        //modify the email
        val newEmail = emailService.modifyEmail(
            contact.contactDTO.id,
            CreateEmailDTO("newemail@email.com", "Updated comment"),
            contact.emailDTOs[0].id
        )

        assert(newEmail.email == "newemail@email.com")
        assert(newEmail.comment == "Updated comment")
    }

    @Test
    fun modifyEmail_contactNotFound() {
        assertThrows<ContactNotFoundException> {
            // Call the function with a non-existing message ID
            emailService.modifyEmail(1000, CreateEmailDTO("newemail@email.com", "Updated comment"), 0)
        }
    }

    @Test
    fun modifyEmail_emailNotFound() {
        //Create new contact
        val createContact = createContactDto
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)
        assertThrows<Exception> {
            // Call the function with a non-existing message ID
            emailService.modifyEmail(
                contact.contactDTO.id,
                CreateEmailDTO("newemail@email.com", "Updated comment"),
                0
            )
        }
    }

    //address tests
    @Test
    fun modifyAddress_createAndEdit() {
        //Create new contact
        val createContact = createContactDto
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)

        println("CONTACT address: " + contact.addressDTOs[0].id)

        //modify the email
        val newAddress = addressService.modifyAddress(
            contact.contactDTO.id,
            CreateAddressDTO("Italy", "Sicilia", "Palermo", "Via Roma 11", "New comment"),
            contact.addressDTOs[0].id
        )

        assert(newAddress.state == "Italy")
        assert(newAddress.region == "Sicilia")
        assert(newAddress.city == "Palermo")
        assert(newAddress.address == "Via Roma 11")
        assert(newAddress.comment == "New comment")
    }

    @Test
    fun modifyAddres_contactNotFound() {
        assertThrows<ContactNotFoundException> {
            // Call the function with a non-existing message ID
            addressService.modifyAddress(
                1000,
                CreateAddressDTO("Italy", "Sicilia", "Palermo", "Via Roma 11", "New comment"),
                0
            )
        }
    }

    @Test
    fun modifyAddress_addressNotFound() {
        //Create new contact
        val createContact = createContactDto
        val categoryOption = CategoryOptions.valueOf(createContact.category!!)
        val contact = contactService.storeContact(createContact, categoryOption)
        assertThrows<Exception> {
            // Call the function with a non-existing message ID
            addressService.modifyAddress(
                contact.contactDTO.id,
                CreateAddressDTO("Italy", "Sicilia", "Palermo", "Via Roma 11", "New comment"),
                1000
            )
        }
    }
}
