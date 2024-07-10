package it.polito.students.crm.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.students.crm.CrmApplication
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.ContactServiceImpl
import it.polito.students.crm.services.CustomerServiceImpl
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import it.polito.students.crm.utils.Factory
import it.polito.students.crm.utils.Factory.Companion.copy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.internal.stubbing.answers.ThrowsException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@SpringBootTest(
    classes = [CrmApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CrmCustomersServiceUnitTest {
    private final val customerRepository: CustomerRepository = mockk()
    private final val contactRepository: ContactRepository = mockk()
    private final val addressRepository: AddressRepository = mockk()
    private final val emailRepository: EmailRepository = mockk()
    private final val telephoneRepository: TelephoneRepository = mockk()
    private final val jobOfferRepository: JobOfferRepository = mockk()
    private final val factory = Factory(jobOfferRepository)

    val contactService = ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)

    val customerService =
        CustomerServiceImpl(customerRepository, contactService, contactRepository, jobOfferRepository, factory)

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

    /**
     * GET ALL CUSTOMERS TEST CASES
     */
    @Test
    fun getAllCustomers_goodCase() {
        val customers: Page<Customer> = PageImpl(customersList)
        val list = customers.content.map { it.toDTO() }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), customers.totalElements)


        every { customerRepository.findAll(PageRequest.of(0, 30)) } returns customers

        val result = customerService.getAllCustomers(0, 30, HashMap<ContactEnumFields, String>())

        verify(exactly = 1) { customerRepository.findAll(PageRequest.of(0, 30)) }
        Assertions.assertEquals(expectedPage.size, result.size)
    }

    @Test
    fun getAllCustomers_goodCaseFiltering() {
        val customers: Page<Customer> = PageImpl(listOf(customersList[0]))
        val map = HashMap<ContactEnumFields, String>().apply {
            put(ContactEnumFields.NAME, "Alice")
        }

        var list = customers.content.map { it.toDTO() }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.information.contactDTO.name == filter.value }
                ContactEnumFields.SURNAME -> list.filter { it.information.contactDTO.surname == filter.value }
                ContactEnumFields.CATEGORY -> list.filter { it.information.contactDTO.category.name == filter.value }
                ContactEnumFields.SSN_CODE -> list.filter { it.information.contactDTO.ssnCode == filter.value }
                ContactEnumFields.COMMENT -> list.filter { it.information.contactDTO.comment == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), customers.totalElements)

        every { customerRepository.findAll(PageRequest.of(0, 30)) } returns customers

        val result = customerService.getAllCustomers(0, 30, map)

        verify(exactly = 1) { customerRepository.findAll(PageRequest.of(0, 30)) }
        Assertions.assertEquals(expectedPage.size, result.size)
    }

    @Test
    fun getAllCustomers_goodCaseEmptyList() {
        val customers: Page<Customer> = PageImpl(emptyList())
        val map = HashMap<ContactEnumFields, String>()

        var list = customers.content.map { it.toDTO() }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ContactEnumFields.NAME -> list.filter { it.information.contactDTO.name == filter.value }
                ContactEnumFields.SURNAME -> list.filter { it.information.contactDTO.surname == filter.value }
                ContactEnumFields.CATEGORY -> list.filter { it.information.contactDTO.category.name == filter.value }
                ContactEnumFields.SSN_CODE -> list.filter { it.information.contactDTO.ssnCode == filter.value }
                ContactEnumFields.COMMENT -> list.filter { it.information.contactDTO.comment == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(0, 30), customers.totalElements)

        every { customerRepository.findAll(PageRequest.of(0, 30)) } returns customers

        val result = customerService.getAllCustomers(0, 30, map)

        verify(exactly = 1) { customerRepository.findAll(PageRequest.of(0, 30)) }
        Assertions.assertEquals(expectedPage, result)
    }

    /**
     * GET CUSTOMER BY ID TEST CASES
     */

    @Test
    fun getCustomerById_correctParameter() {
        every { customerRepository.findById(1) } returns Optional.of(customersList[0])

        val result = customerService.getCustomer(1)

        verify(exactly = 1) { customerRepository.findById(1) }
        Assertions.assertEquals(customerDTOList[0].id, result.id)
        Assertions.assertEquals(customerDTOList[0].information.contactDTO, result.information.contactDTO)
        Assertions.assertEquals(customerDTOList[0].information.emailDTOs, result.information.emailDTOs)
        Assertions.assertEquals(customerDTOList[0].information.addressDTOs, result.information.addressDTOs)
    }

    @Test
    fun getCustomerById_nonExistingID() {
        every { customerRepository.findById(30) } returns Optional.empty()

        val exception = assertThrows<CustomerNotFoundException> { customerService.getCustomer(30) }
        Assertions.assertEquals("Customer with id = '30' not found!", exception.message)

        verify(exactly = 1) { customerRepository.findById(30) }
    }

    /**
     * STORE CUSTOMER TEST CASES
     */

    @Test
    fun storeCustomer_success() {
        val contactCreate = CreateContactDTO(
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

        val newContact = Contact().apply {
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

        val newCustomer = Customer().apply {
            information = newContact

        }

        //every { contactService.storeContact(contactCreate, CategoryOptions.CUSTOMER) } returns newContact.toDTOWithAssociatedData()
        for (emailField in newContact.emails!!) {
            every { emailRepository.findByEmail(emailField.email) } returns emailField
        }

        for (addressField in newContact.addresses!!) {
            every {
                addressRepository.findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                    addressField.address,
                    addressField.city,
                    addressField.region,
                    addressField.state
                )
            } returns addressField
        }

        for (telephoneField in newContact.telephones!!) {
            every { telephoneRepository.findByTelephone(telephoneField.telephone) } returns telephoneField
        }

        every { contactRepository.save(any()) } returns newContact

        every { contactRepository.findById(newContact.id) } returns Optional.of(newContact)

        every { customerRepository.save(any()) } returns newCustomer

        val result = customerService.postNewCustomer(contactCreate)

        Assertions.assertEquals(newCustomer.toDTO().id, result.id)
        Assertions.assertEquals(newCustomer.toDTO().information.contactDTO, result.information.contactDTO)
        Assertions.assertEquals(newCustomer.toDTO().information.emailDTOs, result.information.emailDTOs)
        Assertions.assertEquals(newCustomer.toDTO().information.addressDTOs, result.information.addressDTOs)
    }

    /**
     * PATCH /API/customers/{customerID}
     */
    @Test
    fun patchCustomer_CustomerIdGoodCase() {
        val customer = customersList[0]

        every { customerRepository.findById(any()) } returns Optional.of(customer)
        every { jobOfferRepository.findAllByCustomer_Id(any()) } returns listOf()
        every { contactRepository.findById(any()) } returns Optional.of(customer.information)
        every { customerRepository.save(any()) } returns customer

        customerService.updateCustomer(customer.id, customer.information.id)

        verify(exactly = 1) { customerRepository.save(any()) }
        verify(exactly = 1) { contactRepository.findById(any()) }
        verify(exactly = 1) { customerRepository.findById(any()) }
    }

    @Test
    fun patchCustomer_CustomerIdNotFound() {
        val customer = customersList[0]

        every { customerRepository.findById(any()) } returns Optional.empty()

        val exception = assertThrows<CustomerNotFoundException> {
            customerService.updateCustomer(customer.id, customer.information.id)
        }

        verify(exactly = 0) { customerRepository.save(any()) }
        verify(exactly = 0) { contactRepository.findById(any()) }
        verify(exactly = 1) { customerRepository.findById(any()) }

        val expectedErrorMessage = "Customer with id = '${customer.id}' not found!"
        Assertions.assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun patchCustomer_ContactIdNotFound() {
        val customer = customersList[0]

        every { customerRepository.findById(any()) } returns Optional.of(customer)
        every { contactRepository.findById(any()) } returns Optional.empty()
        every { jobOfferRepository.findAllByCustomer_Id(any()) } returns listOf()

        val exception = assertThrows<ContactNotFoundException> {
            customerService.updateCustomer(customer.id, 3)
        }

        verify(exactly = 0) { customerRepository.save(any()) }
        verify(exactly = 1) { contactRepository.findById(any()) }
        verify(exactly = 1) { customerRepository.findById(any()) }

        val expectedErrorMessage = "The contact with id equal to 3 was not found!"
        Assertions.assertEquals(expectedErrorMessage, exception.message)
    }

    /**
     * DELETE /API/CUSTOMERS/CUSTOMERID
     */

    @Test
    fun deleteCustomer_goodCase() {
        val customerBeforeStore = Customer().apply {
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
                deleted = false
            }
        }

        val customerNextStore = customersList[0]
        customerNextStore.deleted = true

        every { customerRepository.findById(customerBeforeStore.id) } returns Optional.of(customerBeforeStore)
        every { customerRepository.save(any()) } answers { customerNextStore }
        every { jobOfferRepository.findAllByCustomer_Id(customerBeforeStore.id) } returns listOf()

        customerService.deleteCustomer(customerBeforeStore.id)

        verify(exactly = 1) { customerRepository.findById(customerBeforeStore.id) }
        verify(exactly = 1) { customerRepository.save(any()) }
    }

    @Test
    fun deleteCustomer_NotFoundId() {
        val customerBeforeStore = customersList[0]
        customerBeforeStore.deleted = false

        every { customerRepository.findById(customerBeforeStore.id) } returns Optional.empty()

        val exception = assertThrows<CustomerNotFoundException> {
            customerService.deleteCustomer(customerBeforeStore.id)
        }

        assertEquals(
            "Customer with id = '${customerBeforeStore.id}' not found!",
            exception.message
        )

        verify(exactly = 1) { customerRepository.findById(customerBeforeStore.id) }
    }

    @Test
    fun deleteCustomer_alreadyDeleted() {
        val customerBeforeStore = customersList[0]
        customerBeforeStore.deleted = true

        every { customerRepository.findById(customerBeforeStore.id) } returns Optional.empty()

        val exception = assertThrows<CustomerNotFoundException> {
            customerService.deleteCustomer(customerBeforeStore.id)
        }

        assertEquals(
            "Customer with id = '${customerBeforeStore.id}' not found!",
            exception.message
        )

        verify(exactly = 1) { customerRepository.findById(customerBeforeStore.id) }
    }
}