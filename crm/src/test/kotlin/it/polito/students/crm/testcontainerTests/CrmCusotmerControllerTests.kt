package it.polito.students.crm.testcontainerTests

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import it.polito.students.crm.dtos.CreateAddressDTO
import it.polito.students.crm.dtos.CreateContactDTO
import it.polito.students.crm.dtos.CreateEmailDTO
import it.polito.students.crm.dtos.CreateTelephoneDTO
import it.polito.students.crm.entities.*
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.CustomerRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.shaded.org.bouncycastle.math.raw.Nat.equalTo
import org.hamcrest.Matchers.equalTo
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import org.testcontainers.shaded.org.hamcrest.Matchers
import java.util.List


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmCustomerControllerTests {
    @LocalServerPort
    private var port: Int = 0

    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var contactRepository : ContactRepository

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"
        customerRepository.deleteAll()
        contactRepository.deleteAll()
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
                category = CategoryOptions.CUSTOMER
                comment = "Comment 3"
                telephones = mutableSetOf(Telephone().apply {
                    id = 2
                    telephone = "1237899871"
                    comment = "This is a comment"
                })
            }
        })

    /**
     * GET /API/customers/
     */

    @Test
    fun shouldGetAllCustomers() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(customersList.size))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
            .body("content[2].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[2].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[2].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))
    }

    @Test
    fun shouldGetAllCustomersFilteredbyName() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers?name=${customersList[0].information.name}")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
    }

    @Test
    fun shouldGetAllCustomersFilteredBySurname() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers?surname=${customersList[1].information.surname}")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
    }

    @Test
    fun shouldGetAllCustomersFilteredBySsnCode() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers?ssnCode=${customersList[2].information.ssnCode}")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))
    }

    /**
     * GET /API/customers/{customerID}
     */

    @Test
    fun shouldGetTheCustomerByID() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers/1")
            .then()
            .statusCode(200)
            .body("information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))

    }

    @Test
    fun shouldGetTheCustomerAndGetErrorNegativeId() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers/-1")
            .then()
            .statusCode(400)
            .body("error", equalTo(ErrorsPage.CUSTOMER_ID_ERROR))

    }

    @Test
    fun shouldNotGetTheCustomerAndGetErrorNotFound() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers/5")
            .then()
            .statusCode(404)

    }

    /**
     * POST /API/customers/
     */

    @Test
    fun shouldSaveTheCustomer() {
        val contact = CreateContactDTO(
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

        val requestBody = ObjectMapper().writeValueAsString(contact)


        //save the new customer
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/API/customers/")
            .then()
            .statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .get("/API/customers/1")
            .then()
            .statusCode(200)
            .body("information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))

    }

    @Test
    fun shouldNotSaveTheCustomerBadRequest() {
        val contact = CreateContactDTO(
            name = "",
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

        val requestBody = ObjectMapper().writeValueAsString(contact)


        //save the new customer
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/API/customers/")
            .then()
            .statusCode(400)

        //should be not found
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers/1")
            .then()
            .statusCode(404)
    }


    /**
     * PATCH /API/customers/{customerID}
     */
    @Test
    fun shouldUpdateTheCustomer() {
        //save all customers of the list
        customerRepository.save(customersList[0])

        //New contact
        contactRepository.save(customersList[1].information)

        val requestBody = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/1")
            .then()
            .statusCode(200)
            .body("information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
    }

    @Test
    fun shouldNotUpdateTheCustomer_CustomerNotFound() {

        //New contact
        contactRepository.save(customersList[1].information)

        val requestBody = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/1")
            .then()
            .statusCode(404)
            .body("error", equalTo("Customer with id = '1' not found!"))
    }

    @Test
    fun shouldNotUpdateTheCustomer_ContaactNotFound() {
        //save all customers of the list
        customerRepository.save(customersList[0])

        val requestBody = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/1")
            .then()
            .statusCode(404)
            .body("error", equalTo("The contact with id equal to 2 was not found!"))
    }

    @Test
    fun shouldNotUpdateTheCustomer_BadRequest() {
        //save all customers of the list
        customerRepository.save(customersList[0])

        //New contact
        contactRepository.save(customersList[1].information)

        val requestBody = ObjectMapper().writeValueAsString(mapOf("id" to 2L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/1")
            .then()
            .statusCode(400)
            .body("error", equalTo("Unable to update customer. It is necessary to specify a contact id in the body request."))
    }

    @Test
    fun shouldNotUpdateTheCustomer_NegativeCustomerId() {
        //save all customers of the list
        customerRepository.save(customersList[0])

        //New contact
        contactRepository.save(customersList[1].information)

        val requestBody = ObjectMapper().writeValueAsString(mapOf("contactID" to 2L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/-1")
            .then()
            .statusCode(400)
            .body("error", equalTo(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))
    }

    @Test
    fun shouldNotUpdateTheCustomer_NegativeContactId() {
        //save all customers of the list
        customerRepository.save(customersList[0])

        //New contact
        contactRepository.save(customersList[1].information)

        val requestBody = ObjectMapper().writeValueAsString(mapOf("contactID" to -1L))

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/customers/1")
            .then()
            .statusCode(400)
            .body("error", equalTo(ErrorsPage.CUSTOMER_ID_CONTACT_ID_ERROR))
    }

    /**
     * DELETE /API/customers/{customerID}
     */

    @Test
    fun shouldDeleteTheCustomer() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        //Check that all customers are correctly saved
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(customersList.size))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
            .body("content[2].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[2].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[2].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))

        //Delete the customer
        given()
            .contentType(ContentType.JSON)
            .delete("API/customers/3")
            .then()
            .statusCode(200)

        //Check that only 2 customers are remaining (last one deleted)
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))

    }

    @Test
    fun shouldNotDeleteTheCustomer_CustomernotFound() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        //Check that all customers are correctly saved
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(customersList.size))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
            .body("content[2].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[2].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[2].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))

        //Delete the customer (should not succeed)
        given()
            .contentType(ContentType.JSON)
            .delete("API/customers/5")
            .then()
            .statusCode(404)
            .body("error", equalTo("Customer with id = '5' not found!"))

        //Check that only 2 customers are remaining (last one deleted)
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(3))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
            .body("content[2].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[2].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[2].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))

    }

    @Test
    fun shouldNotDeleteTheCustomer_AlreadyDeleted() {
        //save all customers of the list
        customersList.forEach{
            customerRepository.save(it)
        }

        //Check that all customers are correctly saved
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(customersList.size))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))
            .body("content[2].information.contactDTO.name", equalTo(customersList[2].information.name))
            .body("content[2].information.contactDTO.surname", equalTo(customersList[2].information.surname))
            .body("content[2].information.contactDTO.ssnCode", equalTo(customersList[2].information.ssnCode))

        //Delete the customer
        given()
            .contentType(ContentType.JSON)
            .delete("API/customers/3")
            .then()
            .statusCode(200)

        //Delete the customer (already deleted)
        given()
            .contentType(ContentType.JSON)
            .delete("API/customers/3")
            .then()
            .statusCode(404)
            .body("error", equalTo("Customer with id = '3' not found!"))

        //Check that only 2 customers are remaining (last one deleted)
        given()
            .contentType(ContentType.JSON)
            .get("/API/customers")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
            .body("content[0].information.contactDTO.name", equalTo(customersList[0].information.name))
            .body("content[0].information.contactDTO.surname", equalTo(customersList[0].information.surname))
            .body("content[0].information.contactDTO.ssnCode", equalTo(customersList[0].information.ssnCode))
            .body("content[1].information.contactDTO.name", equalTo(customersList[1].information.name))
            .body("content[1].information.contactDTO.surname", equalTo(customersList[1].information.surname))
            .body("content[1].information.contactDTO.ssnCode", equalTo(customersList[1].information.ssnCode))

    }

}