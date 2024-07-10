package it.polito.students.crm.integration.service

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.InvalidUpdateException
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.Factory
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmCustomerServiceIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var addressRepository: AddressRepository

    @Autowired
    private lateinit var emailRepository: EmailRepository

    @Autowired
    private lateinit var telephoneRepository: TelephoneRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var jobOfferRepository: JobOfferRepository

    private val factory: Factory by lazy {
        Factory(jobOfferRepository)
    }

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

    private val customerService: CustomerServiceImpl by lazy {
        CustomerServiceImpl(customerRepository, contactService, contactRepository, jobOfferRepository, factory)
    }

    @BeforeEach
    fun setUp() {
        // Clean repositories before each test
        customerRepository.deleteAll()
        contactRepository.deleteAll()
        addressRepository.deleteAll()
        emailRepository.deleteAll()
        telephoneRepository.deleteAll()
        historyRepository.deleteAll()
        messageRepository.deleteAll()
        jobOfferRepository.deleteAll()
    }

    val customersList = listOf(
        Customer().apply {
            id = 1
            information = Contact().apply {
                id = 11
                name = "Alice"
                surname = "Smith"
                ssnCode = "987-65-4321"
                category = CategoryOptions.CUSTOMER
                comment = "Another comment"
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
            }
        },
        Customer().apply {
            id = 2
            information = Contact().apply {
                id = 22
                name = "Name 2"
                surname = "Surname 2"
                ssnCode = "ssnCode 2"
                category = CategoryOptions.CUSTOMER
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
            }
        },
        Customer().apply {
            id = 3
            information = Contact().apply {
                id = 33
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
            }
        })

    // Convert Customer objects to CustomerDTO objects
    val customerDTOList = customersList.map { it.toDTO() }

    val createCustomerList = listOf(
        CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
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
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
            emails = listOf(
                CreateEmailDTO(
                    email = "jane.smith@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "1234567890",
                    comment = "This is a comment",
                )
            ),
            addresses = listOf(
                CreateAddressDTO(
                    state = "USA",
                    region = "California",
                    city = "Los Angeles",
                    address = "123 Main St",
                    comment = "This is a comment",
                )
            )
        ),
        CreateContactDTO(
            name = customersList[2].information.name,
            surname = customersList[2].information.surname,
            ssnCode = customersList[2].information.ssnCode,
            category = customersList[2].information.category.name,
            comment = customersList[2].information.comment,
            emails = listOf(
                CreateEmailDTO(
                    email = "michael.jackson@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "9876543210",
                    comment = "This is a comment",
                )
            ),
            addresses = listOf(
                CreateAddressDTO(
                    state = "USA",
                    region = "California",
                    city = "Santa Monica",
                    address = "456 Ocean Ave",
                    comment = "This is a comment",
                )
            )
        ),

        )

    /*
    *
    *   GET /API/customers/{customerID} test cases
    *
     */

    @Test
    fun getCustomers_goodCase() {

        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>()

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 3)
        assert(customers.content[0].id == customersList[0].id)
        assert(customers.content[1].id == customersList[1].id)
        assert(customers.content[2].id == customersList[2].id)

    }

    @Test
    fun getCustomers_limit() {

        val pageNumber = 0
        val pageSize = 1
        val filterMap = hashMapOf<ContactEnumFields, String>()

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 1)
        assert(customers.content[0].id == customersList[0].id)

    }

    @Test
    fun getCustomers_page() {

        val pageNumber = 5
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>()

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 0)

    }

    @Test
    fun getCustomers_nameFilter() {

        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>(
            ContactEnumFields.NAME to customersList[0].information.name
        )

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 1)
        assert(customers.content[0].id == customersList[0].id)

    }

    @Test
    fun getCustomers_surnameFilter() {
        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>(
            ContactEnumFields.SURNAME to customersList[0].information.surname
        )

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 1)
        assert(customers.content[0].id == customersList[0].id)
    }

    @Test
    fun getCustomers_ssnCodeFilter() {

        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>(
            ContactEnumFields.SSN_CODE to customersList[0].information.ssnCode
        )

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 1)
        assert(customers.content[0].id == customersList[0].id)

    }

    @Test
    fun getCustomers_commentFilter() {

        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ContactEnumFields, String>(
            ContactEnumFields.COMMENT to customersList[0].information.comment
        )

        createCustomerList.forEach {
            customerService.postNewCustomer(it)
        }

        val customers: PageImpl<CustomerDTO> = customerService.getAllCustomers(pageNumber, pageSize, filterMap)

        assert(customers.content.size == 1)
        assert(customers.content[0].id == customersList[0].id)

    }

    /**
     *   GET /API/customers/{customerID} test cases
     */
    @Test
    fun getCustomerById_goodCase() {
        val createContact = CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
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

        val result = customerService.postNewCustomer(createContact)

        val newCustomer = customerService.getCustomer(result.id)

        assert(newCustomer.id == result.id)
        assert(newCustomer.information.contactDTO == result.information.contactDTO)
        assert(newCustomer.information.emailDTOs == result.information.emailDTOs)
        assert(newCustomer.information.addressDTOs == result.information.addressDTOs)
    }

    @Test
    fun getCustomerById_CustomerNotFound() {
        val result = assertThrows<CustomerNotFoundException> {
            customerService.getCustomer(1000)
        }
        val expectedMessage = "Customer with id = '1000' not found!"
        assert(expectedMessage == result.message)
    }

    /**
     * POST /API/customers/
     */

    private fun customerChecks(createCustomer: CreateContactDTO, result: CustomerDTO) {
        Assert.assertEquals(true, result.jobOffers.isEmpty())
        Assert.assertEquals(createCustomer.name, result.information.contactDTO.name)
        Assert.assertEquals(createCustomer.surname, result.information.contactDTO.surname)
        Assert.assertEquals(createCustomer.category, CategoryOptions.CUSTOMER.name)
        Assert.assertEquals(createCustomer.ssnCode, result.information.contactDTO.ssnCode)
        Assert.assertEquals(createCustomer.comment, result.information.contactDTO.comment)

        /*Assert.assertEquals(createCustomer.emails?.size, result.information.emailDTOs.size)
        Assert.assertEquals(createCustomer.addresses?.size, result.information.addressDTOs.size)
        Assert.assertEquals(createCustomer.telephones?.size, result.information.telephoneDTOs.size)

        // Checks Emails, Addresses, Telephones
        result.information.emailDTOs.forEach { emailPost ->
            val email = createCustomer.emails?.first { it.email == emailPost.email }
            Assert.assertEquals(email?.email, emailPost.email)
            Assert.assertEquals(email?.comment, emailPost.comment)
        }

        result.information.addressDTOs.forEach { addressPost ->
            val address = createCustomer.addresses?.first {
                it.state == addressPost.state &&
                        it.region == addressPost.region &&
                        it.city == addressPost.city &&
                        it.address == addressPost.address
            }
            Assert.assertEquals(address?.state, addressPost.state)
            Assert.assertEquals(address?.region, addressPost.region)
            Assert.assertEquals(address?.city, addressPost.city)
            Assert.assertEquals(address?.address, addressPost.address)
            Assert.assertEquals(address?.comment, addressPost.comment)
        }

        result.information.telephoneDTOs.forEach { telephonePost ->
            val telephone = createCustomer.telephones?.first { it.telephone == telephonePost.telephone }
            Assert.assertEquals(telephone?.telephone, telephonePost.telephone)
            Assert.assertEquals(telephone?.comment, telephonePost.comment)
        }*/
    }

    @Test
    fun postCustomer_goodCase() {
        val createCustomer = createCustomerList.first().copy()

        val resultPost = customerService.postNewCustomer(createCustomer)
        customerChecks(createCustomer, resultPost)

        val resulGet = customerService.getCustomer(resultPost.id)
        customerChecks(createCustomer, resulGet)
    }

    /**
     * PATCH /API/customers/{customerID}
     */

    @Test
    fun patchCustomer_goodCase() {
        val createContact = CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
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

        var customer = customerService.postNewCustomer(createContact)

        val createContact2 = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
            emails = listOf(),
            telephones = listOf(),
            addresses = listOf()
        )

        val contact = contactService.storeContact(createContact2, CategoryOptions.valueOf(createContact2.category!!))

        val result = customerService.updateCustomer(customer.id, contact.contactDTO.id)

        assert(result.information.contactDTO.name == createContact2.name)
        assert(result.information.contactDTO.surname == createContact2.surname)
        assert(result.information.contactDTO.ssnCode == createContact2.ssnCode)
        assert(result.information.contactDTO.category.name == createContact2.category)
        assert(result.information.contactDTO.comment == createContact2.comment)
    }

    @Test
    fun patchCustomer_CustomerNotFound() {

        val createContact2 = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
            emails = listOf(),
            telephones = listOf(),
            addresses = listOf()
        )

        val contact = contactService.storeContact(createContact2, CategoryOptions.valueOf(createContact2.category!!))

        val result = assertThrows<CustomerNotFoundException> {
            customerService.updateCustomer(3, contact.contactDTO.id)
        }
        val expectedMessage = "Customer with id = '3' not found!"
        assert(expectedMessage == result.message)
    }

    @Test
    fun patchCustomer_ContactNotFound() {
        val createContact = CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
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

        var customer = customerService.postNewCustomer(createContact)

        val result = assertThrows<ContactNotFoundException> {
            customerService.updateCustomer(customer.id, 100)
        }
        val expectedMessage = "The contact with id equal to 100 was not found!"
        assert(expectedMessage == result.message)
    }

    @Test
    fun patchCustomer_ContactNotCustomer() {
        val createContact = CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
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

        var customer = customerService.postNewCustomer(createContact)

        val createContact2 = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
            emails = listOf(),
            telephones = listOf(),
            addresses = listOf()
        )

        createContact2.category = "PROFESSIONAL"

        val contact = contactService.storeContact(createContact2, CategoryOptions.valueOf(createContact2.category!!))

        val result = assertThrows<InvalidUpdateException> {
            customerService.updateCustomer(customer.id, contact.contactDTO.id)
        }

        assert(ErrorsPage.INVALID_UPDATE_CUSTOMER == result.message)

    }

    /**
     * DELETE /API/customers/{customerID}
     */

    @Test
    fun deleteCustomers_goodCase() {

        customerService.postNewCustomer(createCustomerList[0])

        customerService.deleteCustomer(customersList[0].id)

        try {
            customerService.getCustomer(customersList[0].id)
        } catch (e: CustomerNotFoundException) {
            return
        }

        assert(false)

    }

    @Test
    fun deleteCustomers_customerNotFound() {

        try {
            customerService.deleteCustomer(customersList[0].id)
        } catch (e: CustomerNotFoundException) {
            return
        }

        assert(false)

    }

    @Test
    fun deleteCustomers_customerAlreadyDeleted() {

        customerService.postNewCustomer(createCustomerList[0])

        customerService.deleteCustomer(customersList[0].id)

        try {
            customerService.getCustomer(customersList[0].id)
        } catch (e: CustomerNotFoundException) {
            try {
                customerService.deleteCustomer(customersList[0].id)
            } catch (e: CustomerNotFoundException) {
                return
            }
        }

        assert(false)

    }
}