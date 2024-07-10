package it.polito.students.crm.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.CrmApplicationTests
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmCustomerControllerIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

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
                category = CategoryOptions.CUSTOMER
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

    /**
     * GET /API/customers/{customerID}
     */

    @Test
    fun getCustomers_goodCase() {

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(createCustomerList.size))
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
    fun getCustomers_limit() {

        val limit = 1

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?pageSize=$limit")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(limit))
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

    @Test
    fun getCustomers_page() {

        val page = 5

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?pageNumber=$page")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(0))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.currentPage")
                    .value(page)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.elementPerPage")
                    .value(10)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.totalPages")
                    .value(1)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.totalElements")
                    .value(customersList.size)
            )

    }

    @Test
    fun getCustomers_nameFilter() {

        val limit = 1

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?name=${createCustomerList[0].name}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
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

    @Test
    fun getCustomers_surnameFilter() {

        val limit = 1

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?surname=${createCustomerList[0].surname}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
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

    @Test
    fun getCustomers_ssnCodeFilter() {

        val limit = 1

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?ssnCode=${createCustomerList[0].ssnCode}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
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

    @Test
    fun getCustomers_commentFilter() {

        val limit = 1

        createCustomerList.forEach {

            val requestBody = ObjectMapper().writeValueAsString(it)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/customers/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

        }

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/?comment=${createCustomerList[0].comment}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
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

        val requestBody = ObjectMapper().writeValueAsString(contactCreate)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
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
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/-1/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CUSTOMER_ID_ERROR))

    }

    @Test
    fun getCustomerID_IdNotFound() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/1/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("Customer with id = '1' not found!"))
    }

    /**
     * POST /API/customers/
     */

    @Test
    fun postCustomer_goodCase() {
        val createCustomer = createCustomerList.first().copy()

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        val mapper = ObjectMapper()
        var customerJson = mapper.readTree(resultPost.response.contentAsString)

        var emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        var addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])
        var telephones =
            CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }

        // Extracting ID from the response content
        val responseContent = resultPost.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        customerJson = mapper.readTree(resultGet.response.contentAsString)

        emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])
        telephones = CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        // Check Emails, Addresses and Telephones
        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }
    }

    @Test
    fun postCustomer_goodCaseWithoutEmails() {
        val createCustomer = createCustomerList.first().copy()

        createCustomer.emails = null

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        val mapper = ObjectMapper()
        var customerJson = mapper.readTree(resultPost.response.contentAsString)

        var addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])
        var telephones =
            CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        Assertions.assertEquals(true, customerJson["information"]["emailDTOs"].isEmpty)

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }

        // Extracting ID from the response content
        val responseContent = resultPost.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        customerJson = mapper.readTree(resultGet.response.contentAsString)

        addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])
        telephones = CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        // Check Emails, Addresses and Telephones
        Assertions.assertEquals(true, customerJson["information"]["emailDTOs"].isEmpty)

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }
    }

    @Test
    fun postCustomer_goodCaseWithoutAddresses() {
        val createCustomer = createCustomerList.first().copy()

        createCustomer.addresses = null

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        val mapper = ObjectMapper()
        var customerJson = mapper.readTree(resultPost.response.contentAsString)

        var emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        var telephones = CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        Assertions.assertEquals(true, customerJson["information"]["addressDTOs"].isEmpty)

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }

        // Extracting ID from the response content
        val responseContent = resultPost.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        customerJson = mapper.readTree(resultGet.response.contentAsString)

        emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        telephones = CrmApplicationTests.createTelephoneDTOListFromString(customerJson["information"]["telephoneDTOs"])

        // Check Emails, Addresses and Telephones
        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        Assertions.assertEquals(true, customerJson["information"]["addressDTOs"].isEmpty)

        telephones.forEach { telephone ->
            val resultTelephone = createCustomer.telephones?.first { it.telephone == telephone.telephone }
            Assertions.assertEquals(telephone.telephone, resultTelephone?.telephone)
            Assertions.assertEquals(telephone.comment, resultTelephone?.comment)
        }
    }

    @Test
    fun postCustomer_goodCaseWithoutTelephones() {
        val createCustomer = createCustomerList.first().copy()

        createCustomer.telephones = null

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        val mapper = ObjectMapper()
        var customerJson = mapper.readTree(resultPost.response.contentAsString)

        var emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        var addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])

        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

            Assertions.assertEquals(true, customerJson["information"]["telephoneDTOs"].isEmpty)

        // Extracting ID from the response content
        val responseContent = resultPost.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()

        customerJson = mapper.readTree(resultGet.response.contentAsString)

        emails = CrmApplicationTests.createEmailDTOListFromString(customerJson["information"]["emailDTOs"])
        addresses = CrmApplicationTests.createAddressDTOListFromString(customerJson["information"]["addressDTOs"])

        // Check Emails, Addresses and Telephones
        emails.forEach { email ->
            val resultEmail = createCustomer.emails?.first { it.email == email.email }
            Assertions.assertEquals(email.email, resultEmail?.email)
            Assertions.assertEquals(email.comment, resultEmail?.comment)
        }

        addresses.forEach { address ->
            val resultAddress =
                createCustomer.addresses?.first {
                    it.state == address.state &&
                            it.region == address.region &&
                            it.city == address.city &&
                            it.address == address.address
                }
            Assertions.assertEquals(address.state, resultAddress?.state)
            Assertions.assertEquals(address.region, resultAddress?.region)
            Assertions.assertEquals(address.city, resultAddress?.city)
            Assertions.assertEquals(address.address, resultAddress?.address)
            Assertions.assertEquals(address.comment, resultAddress?.comment)
        }

        Assertions.assertEquals(true, customerJson["information"]["telephoneDTOs"].isEmpty)
    }

    @Test
    fun postCustomer_goodCaseWithoutEmailsAndAddressesAndTelephones() {
        val createCustomer = createCustomerList.first().copy()

        createCustomer.emails = null
        createCustomer.addresses = null
        createCustomer.telephones = null

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()


        // Extracting ID from the response content
        val responseContent = resultPost.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/customers/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.name").value(createCustomer.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.surname").value(createCustomer.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.ssnCode").value(createCustomer.ssnCode))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.contactDTO.category").value(CategoryOptions.CUSTOMER.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.information.contactDTO.comment").value(createCustomer.comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobOffers").isEmpty)
            .andReturn()
    }

    @Test
    fun postCustomer_badRequestError() {
        val createCustomer = createCustomerList.first().copy()
        val errorMessage = "Name and Surname are mandatory!"

        createCustomer.name = ""

        val requestBody = ObjectMapper().writeValueAsString(createCustomer)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(errorMessage))
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

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val customer = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

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

        createContact2.category = "CUSTOMER"

        val requestBody2 = ObjectMapper().writeValueAsString(createContact2)
        val contact = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isOk)
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

        val requestBody2 = ObjectMapper().writeValueAsString(createContact2)
        val contact = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Customer with id = '1' not found!"))
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

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val customer = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("The contact with id equal to 2 was not found!"))
    }

    @Test
    fun patchCustomer_BadRequestBody() {
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("id" to 2L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.error")
                    .value("Unable to update customer. It is necessary to specify a contact id in the body request.")
            )
    }

    @Test
    fun patchCustomer_NegativeCustomerId() {
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))
    }

    @Test
    fun patchCustomer_NegativeContactId() {
        val requestBody3 = ObjectMapper().writeValueAsString(mapOf("contactID" to -1L))
        val newCustomer = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))
    }

    /**
     * DELETE /API/customers/{customerID}
     */

    @Test
    fun deleteCustomers_goodCase() {

        val requestBody = ObjectMapper().writeValueAsString(createCustomerList[0])
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()


        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/customers/${customersList[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun deleteCustomers_customerNotFound() {

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/customers/${customersList[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Customer with id = '1' not found!"))
    }

    @Test
    fun deleteCustomers_customerAlreadyDeleted() {

        val requestBody = ObjectMapper().writeValueAsString(createCustomerList[0])
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()


        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/customers/${customersList[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/customers/${customersList[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Customer with id = '1' not found!"))
    }
}