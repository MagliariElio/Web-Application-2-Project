package it.polito.students.crm.unit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.ErrorsPage.Companion.INTERNAL_SERVER_ERROR_MESSAGE
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
class CrmCustomersControllerUnitTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var messageService: MessageService

    @MockkBean
    lateinit var jobOfferService: JobOfferService

    @MockkBean
    lateinit var customerService: CustomerService

    @MockkBean
    lateinit var professionalService: ProfessionalService

    @MockkBean
    lateinit var contactService: ContactServiceImpl

    @MockkBean
    lateinit var emailService: EmailService

    @MockkBean
    lateinit var addressService: AddressService

    @MockkBean
    lateinit var telephoneService: TelephoneService

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
     * GET CUSTOMERS TEST CASES
     */

    @Test
    fun getCustomers_statusOK() {
        val customers: PageImpl<CustomerDTO> = PageImpl(customerDTOList)

        every { customerService.getAllCustomers(0, 10, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=0&pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.id")
                    .value(customerDTOList[2].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.name")
                    .value(customerDTOList[2].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.surname")
                    .value(customerDTOList[2].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.ssnCode")
                    .value(customerDTOList[2].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.category")
                    .value(customerDTOList[2].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.comment")
                    .value(customerDTOList[2].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_pageSizeLow() {
        val customers: PageImpl<CustomerDTO> = PageImpl(listOf(customerDTOList[0], customerDTOList[1]))

        every { customerService.getAllCustomers(0, 2, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=0&pageSize=2"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_invalidPage() {
        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=-1&pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").value("The page number and the page size cannot be negative!")
            )
    }

    @Test
    fun getCustomers_invalidPageSize() {
        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers/?pageNumber=0&pageSize=-1"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").value("The page number and the page size cannot be negative!")
            )
    }

    @Test
    fun getCustomers_invalidPageAndPageSize() {
        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers/?pageNumber=-1&pageSize=-1"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").value("The page number and the page size cannot be negative!")
            )
    }

    @Test
    fun getCustomers_noPage() {
        val customers: PageImpl<CustomerDTO> = PageImpl(customerDTOList)

        every { customerService.getAllCustomers(0, 10, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.id")
                    .value(customerDTOList[2].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.name")
                    .value(customerDTOList[2].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.surname")
                    .value(customerDTOList[2].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.ssnCode")
                    .value(customerDTOList[2].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.category")
                    .value(customerDTOList[2].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.comment")
                    .value(customerDTOList[2].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_noPageSize() {
        val customers: PageImpl<CustomerDTO> = PageImpl(customerDTOList)

        every { customerService.getAllCustomers(0, 10, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=0"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.id")
                    .value(customerDTOList[2].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.name")
                    .value(customerDTOList[2].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.surname")
                    .value(customerDTOList[2].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.ssnCode")
                    .value(customerDTOList[2].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.category")
                    .value(customerDTOList[2].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.comment")
                    .value(customerDTOList[2].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_noParameters() {
        val customers: PageImpl<CustomerDTO> = PageImpl(customerDTOList)

        every { customerService.getAllCustomers(0, 10, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.id")
                    .value(customerDTOList[2].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.name")
                    .value(customerDTOList[2].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.surname")
                    .value(customerDTOList[2].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.ssnCode")
                    .value(customerDTOList[2].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.category")
                    .value(customerDTOList[2].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.comment")
                    .value(customerDTOList[2].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_bigPageSize() {
        val customers: PageImpl<CustomerDTO> = PageImpl(customerDTOList)

        every { customerService.getAllCustomers(0, 1000, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=0&pageSize=1000"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.id")
                    .value(customerDTOList[1].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.name")
                    .value(customerDTOList[1].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.surname")
                    .value(customerDTOList[1].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.ssnCode")
                    .value(customerDTOList[1].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.category")
                    .value(customerDTOList[1].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[1].information.contactDTO.comment")
                    .value(customerDTOList[1].information.contactDTO.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.id")
                    .value(customerDTOList[2].information.contactDTO.id)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.name")
                    .value(customerDTOList[2].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.surname")
                    .value(customerDTOList[2].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.ssnCode")
                    .value(customerDTOList[2].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.category")
                    .value(customerDTOList[2].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[2].information.contactDTO.comment")
                    .value(customerDTOList[2].information.contactDTO.comment)
            )
    }

    @Test
    fun getCustomers_filtered() {
        val customers: PageImpl<CustomerDTO> = PageImpl(listOf(customerDTOList[0]))

        every { customerService.getAllCustomers(0, 10, any()) } returns customers

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers?pageNumber=0&pageSize=10&name=Alice"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )

    }

    /**
     * GET /API/customers/{customerID}
     */
    @Test
    fun getCustomerId_goodCase() {
        val customer = customersList[0]

        every { customerService.getCustomer(customer.id) } returns customer.toDTO()

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers/${customer.id}"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customerDTOList[0].id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.name")
                    .value(customerDTOList[0].information.contactDTO.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname")
                    .value(customerDTOList[0].information.contactDTO.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode")
                    .value(customerDTOList[0].information.contactDTO.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category")
                    .value(customerDTOList[0].information.contactDTO.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment")
                    .value(customerDTOList[0].information.contactDTO.comment)
            )

    }

    @Test
    fun getCustomerId_IdNegative() {
        val customer = customersList[0]

        every { customerService.getCustomer(customer.id) } returns customer.toDTO()

        mockMvc.perform(MockMvcRequestBuilders.get("/API/customers/-1"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

    }

    @Test
    fun getCustomerID_IdNotFound() {
        val customerId: Long = 20

        val customer = customersList[0]

        every { customerService.getCustomer(customerId) } throws CustomerNotFoundException("The customer with id equal to $customerId was not found!")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${customerId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").value("The customer with id equal to $customerId was not found!")
            )
    }

    /**
     * STORE CUSTOMER TEST CASES
     */

    @Test
    fun storeNewCustomer_success() {
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
            id = 1
            information = newContact

        }

        every { customerService.postNewCustomer(contactCreate) } returns newCustomer.toDTO()

        val requestBody = ObjectMapper().writeValueAsString(contactCreate)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(newCustomer.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(newCustomer.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname")
                    .value(newCustomer.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode")
                    .value(newCustomer.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category")
                    .value(newCustomer.information.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment")
                    .value(newCustomer.information.comment)
            )

        verify(exactly = 1) { customerService.postNewCustomer(contactCreate) }
    }

    @Test
    fun storeNewCustomer_invalidEmail() {
        val contactCreate = CreateContactDTO(
            name = customersList[0].information.name,
            surname = customersList[0].information.surname,
            ssnCode = customersList[0].information.ssnCode,
            category = customersList[0].information.category.name,
            comment = customersList[0].information.comment,
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doeexample.com",
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
                email = "john.doeexample.com"
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

        val requestBody = ObjectMapper().writeValueAsString(contactCreate)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.EMAILS_NOT_VALID))


        verify(exactly = 0) { customerService.postNewCustomer(any()) }
    }

    @Test
    fun storeNewCustomer_invalidTelephone() {
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
                    telephone = "77891",
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
                telephone = "77891"
                comment = "This is a comment"
            })
        }

        val newCustomer = Customer().apply {
            id = 1
            information = newContact

        }

        val requestBody = ObjectMapper().writeValueAsString(contactCreate)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.TELEPHONES_NOT_VALID))


        verify(exactly = 0) { customerService.postNewCustomer(any()) }
    }

    @Test
    fun storeNewCustomer_invalidAddress() {
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
                    state = "",
                    region = "",
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
                state = ""
                region = ""
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
            id = 1
            information = newContact

        }

        val requestBody = ObjectMapper().writeValueAsString(contactCreate)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.ADDRESSES_NOT_VALID))


        verify(exactly = 0) { customerService.postNewCustomer(any()) }
    }

    /**
     * PATCH /API/customers/{customerID}
     */
    @Test
    fun patchCustomer_CustomerIdGoodCase() {
        val newContact = Contact().apply {
            id = 2
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
                state = ""
                region = ""
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
            id = 1
            information = newContact

        }

        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        every { customerService.updateCustomer(any(), any()) } returns newCustomer.toDTO()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(newCustomer.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(newCustomer.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname")
                    .value(newCustomer.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode")
                    .value(newCustomer.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category")
                    .value(newCustomer.information.category.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment")
                    .value(newCustomer.information.comment)
            )

        verify(exactly = 1) { customerService.updateCustomer(any(), any()) }
    }

    @Test
    fun patchCustomer_CustomerNotFound() {
        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        every {
            customerService.updateCustomer(
                1,
                2
            )
        } throws CustomerNotFoundException("The customer with id equal to 1 was not found!")

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.error").value("The customer with id equal to 1 was not found!")
            )


        verify(exactly = 1) { customerService.updateCustomer(any(), any()) }
    }

    @Test
    fun patchCustomer_ContactNotFound() {
        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        every {
            customerService.updateCustomer(
                1,
                2
            )
        } throws ContactNotFoundException("The contact with id equal to 2 was not found!")

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("The contact with id equal to 2 was not found!"))


        verify(exactly = 1) { customerService.updateCustomer(any(), any()) }
    }

    @Test
    fun patchCustomer_ContactIdNegative() {
        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to -1L))

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))


        verify(exactly = 0) { customerService.updateCustomer(any(), any()) }
    }

    @Test
    fun patchCustomer_CustomerIdNegative() {
        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))


        verify(exactly = 0) { customerService.updateCustomer(any(), any()) }
    }

    @Test
    fun patchCustomer_CBadBodyReq() {
        //Update the customer to have contact 2
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("id" to 2L))

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CONTACT_ID_NOT_SPECIFIED_ERROR))


        verify(exactly = 0) { customerService.updateCustomer(any(), any()) }
    }

    /**
     * Test Delete Customer
     */

    @Test
    fun deleteCustomer_Ok() {
        val customerID: Long = 1

        every { customerService.deleteCustomer(customerID) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/customers/${customerID}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(ErrorsPage.DELETED_SUCCESSFULLY))

        verify(exactly = 1) { customerService.deleteCustomer(customerID) }
    }

    @Test
    fun deleteCustomer_NotFound() {
        val customerID: Long = 1
        val errorMessage = "Failed to delete the customer. Customer with id = $customerID not found!"

        every { customerService.deleteCustomer(customerID) } throws CustomerNotFoundException(errorMessage)

        mockMvc.perform(
            delete("/API/customers/${customerID}")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value(errorMessage))

        verify(exactly = 1) { customerService.deleteCustomer(customerID) }
    }

    @Test
    fun deleteCustomer_NegativeId() {
        val customerID: Long = -1
        val errorMessage = "The provided value for customerId is not valid. " +
                "Please ensure that you provide a positive integer for the customerId parameter."

        mockMvc.perform(
            delete("/API/customers/${customerID}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(errorMessage))

        verify(exactly = 0) { customerService.deleteCustomer(customerID) }
    }

    @Test
    fun deleteCustomer_InternalServerException() {
        val customerID: Long = 1

        every { customerService.deleteCustomer(customerID) } throws Exception(INTERNAL_SERVER_ERROR_MESSAGE)

        mockMvc.perform(
            delete("/API/customers/${customerID}")
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.error").value(INTERNAL_SERVER_ERROR_MESSAGE))

        verify(exactly = 1) { customerService.deleteCustomer(customerID) }
    }

}