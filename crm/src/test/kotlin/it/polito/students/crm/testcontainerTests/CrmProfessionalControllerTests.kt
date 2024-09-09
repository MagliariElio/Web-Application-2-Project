package it.polito.students.crm.testcontainerTests

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.JobOffer
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.CustomerRepository
import it.polito.students.crm.repositories.ProfessionalRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.ErrorsPage
import org.hamcrest.Matchers.equalTo
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrmProfessionalControllerTests {

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
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var professionalRepository: ProfessionalRepository


    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"
        customerRepository.deleteAll()
    }

    val professionalsList = listOf(
        Professional().apply {
            id = 1
            information = Contact().apply {
                id = 1
                name = "John"
                surname = "Doe"
                ssnCode = "123-45-6789"
                category = CategoryOptions.PROFESSIONAL
                comment = "Lorem ipsum dolor sit amet"
            }
            skills = listOf("Java", "Kotlin", "Spring Boot")
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
            geographicalLocation = "New York"
            dailyRate = 500.0
            deleted = false
        },
        Professional().apply {
            id = 2
            information = Contact().apply {
                id = 2
                name = "Alice"
                surname = "Smith"
                ssnCode = "987-65-4321"
                category = CategoryOptions.PROFESSIONAL
                comment = "Consectetur adipiscing elit"
            }
            skills = listOf("Python", "Django", "SQL")
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
            geographicalLocation = "San Francisco"
            dailyRate = 600.0
            deleted = false
        },
        Professional().apply {
            id = 3
            information = Contact().apply {
                id = 3
                name = "Bob"
                surname = "Johnson"
                ssnCode = "456-78-9012"
                category = CategoryOptions.PROFESSIONAL
                comment = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
            }
            skills = listOf("JavaScript", "React", "Node.js")
            employmentState = EmploymentStateEnum.UNEMPLOYED
            geographicalLocation = "Los Angeles"
            dailyRate = 450.0
            deleted = false
        }
    )

    private val createProfessionalDtoList = listOf(
        CreateProfessionalDTO(
            information = CreateContactDTO(
                name = "John",
                surname = "Doe",
                ssnCode = "123-45-6789",
                category = CategoryOptions.PROFESSIONAL.name,
                emails = null,
                addresses = null,
                telephones = null,
                comment = "Lorem ipsum dolor sit amet"
            ),
            skills = listOf("Programming", "Java", "Kotlin", "Spring Boot"),
            geographicalLocation = "Rome",
            dailyRate = 500.0
        ),
        CreateProfessionalDTO(
            information = CreateContactDTO(
                name = "John",
                surname = "Doe",
                ssnCode = "123-45-6789",
                category = CategoryOptions.PROFESSIONAL.name,
                emails = listOf(
                    CreateEmailDTO(
                        email = "john.doe@example.com",
                        comment = "This is a comment"
                    )
                ),
                addresses = null,
                telephones = null,
                comment = "Lorem ipsum dolor sit amet"
            ),
            skills = listOf("Java", "Kotlin", "Spring Boot"),
            geographicalLocation = "New York",
            dailyRate = 500.0
        ),
        CreateProfessionalDTO(
            information = CreateContactDTO(
                name = "John",
                surname = "Doe",
                ssnCode = "123-45-6789",
                category = CategoryOptions.PROFESSIONAL.name,
                emails = null,
                addresses = listOf(
                    CreateAddressDTO(
                        state = "Italy",
                        region = "Lazio",
                        city = "Rome",
                        address = "via Napoli",
                        comment = "This is a comment"
                    )
                ),
                telephones = null,
                comment = "Lorem ipsum dolor sit amet"
            ),
            skills = listOf("Java", "Kotlin", "Spring Boot"),
            geographicalLocation = "New York",
            dailyRate = 500.0
        ),
        CreateProfessionalDTO(
            information = CreateContactDTO(
                name = "John",
                surname = "Doe",
                ssnCode = "123-45-6789",
                category = CategoryOptions.PROFESSIONAL.name,
                emails = null,
                addresses = null,
                telephones = listOf(
                    CreateTelephoneDTO(
                        telephone = "3219877891",
                        comment = "This is a comment"
                    )
                ),
                comment = "Lorem ipsum dolor sit amet"
            ),
            skills = listOf("Java", "Kotlin", "Spring Boot"),
            geographicalLocation = "New York",
            dailyRate = 500.0
        )
    )

    private val updateProfessionalDTO = UpdateProfessionalDTO(
        id = 1,
        information = UpdateContactDTO(
            id = 1,
            name = "John",
            surname = "Doe",
            ssnCode = "123456789",
            category = CategoryOptions.PROFESSIONAL.name,
            comment = "Lorem ipsum"
        ),
        skills = listOf("Java", "Kotlin", "Spring Boot"),
        employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
        geographicalLocation = "New York",
        dailyRate = 1000.0
    )

    /**
     * GET /API/PROFESSIONALS
     */

    @Test
    fun shouldGetAllProfessionals() {
        //save all professionals of the list
        professionalsList.forEach {
            professionalRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/professionals")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(professionalsList.size))
            .body("content[0].information.name", equalTo(professionalsList[0].information.name))
            .body("content[0].information.surname", equalTo(professionalsList[0].information.surname))
            .body("content[0].information.ssnCode", equalTo(professionalsList[0].information.ssnCode))
            .body("content[0].information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("content[0].employmentState", equalTo(professionalsList[0].employmentState.name))
            .body("content[0].dailyRate", equalTo(professionalsList[0].dailyRate.toFloat()))
            .body("content[0].geographicalLocation", equalTo(professionalsList[0].geographicalLocation))
            .body("content[1].information.name", equalTo(professionalsList[1].information.name))
            .body("content[1].information.surname", equalTo(professionalsList[1].information.surname))
            .body("content[1].information.ssnCode", equalTo(professionalsList[1].information.ssnCode))
            .body("content[1].information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("content[1].employmentState", equalTo(professionalsList[1].employmentState.name))
            .body("content[1].dailyRate", equalTo(professionalsList[1].dailyRate.toFloat()))
            .body("content[1].geographicalLocation", equalTo(professionalsList[1].geographicalLocation))
            .body("content[2].information.name", equalTo(professionalsList[2].information.name))
            .body("content[2].information.surname", equalTo(professionalsList[2].information.surname))
            .body("content[2].information.ssnCode", equalTo(professionalsList[2].information.ssnCode))
            .body("content[2].information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("content[2].employmentState", equalTo(professionalsList[2].employmentState.name))
            .body("content[2].dailyRate", equalTo(professionalsList[2].dailyRate.toFloat()))
            .body("content[2].geographicalLocation", equalTo(professionalsList[2].geographicalLocation))
    }

    /**
     * GET /API/PROFESSIONALS/{PROFESSIONALID}
     */

    @Test
    fun shouldGetProfessionalById() {
        val professional = professionalRepository.save(professionalsList.first())

        given()
            .contentType(ContentType.JSON)
            .get("/API/professionals/${professional.id}")
            .then()
            .statusCode(200)
            .body("professionalDTO.id", equalTo(professional.id.toInt()))
            .body("professionalDTO.information.id", equalTo(professional.information.id.toInt()))
            .body("professionalDTO.information.name", equalTo(professional.information.name))
            .body("professionalDTO.information.surname", equalTo(professional.information.surname))
            .body("professionalDTO.information.ssnCode", equalTo(professional.information.ssnCode))
            .body("professionalDTO.information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("professionalDTO.dailyRate", equalTo(professional.dailyRate.toFloat()))
            .body("professionalDTO.employmentState", equalTo(professional.employmentState.name))
            .body("professionalDTO.geographicalLocation", equalTo(professional.geographicalLocation))
            .body("jobofferDTOS.size", equalTo(emptyList<JobOffer>()))
    }

    @Test
    fun shouldNotGetProfessionalByIdProfessionalNotFound() {
        professionalRepository.deleteAll()

        given()
            .contentType(ContentType.JSON)
            .get("/API/professionals/${1}")
            .then()
            .statusCode(500)
            .body(equalTo("Error: The professional with id equal to 1 was not found!"))
    }

    /**
     * POST /API/PROFESSIONALS
     */

    @Test
    fun shouldSaveProfessional() {
        val createProfessional = createProfessionalDtoList.first()

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(201)
            .body("information.name", equalTo(createProfessional.information.name))
            .body("information.surname", equalTo(createProfessional.information.surname))
            .body("information.ssnCode", equalTo(createProfessional.information.ssnCode))
            .body("information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("employmentState", equalTo(EmploymentStateEnum.AVAILABLE_FOR_WORK.name))
            .body("dailyRate", equalTo(createProfessional.dailyRate.toFloat()))
            .body("geographicalLocation", equalTo(createProfessional.geographicalLocation))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestName() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.name = ""

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestSurname() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.surname = ""

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestSsnCode() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.ssnCode = ""

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.SSN_CODE_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalIllegalArgumentExceptionNotValidCategory() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.category = "NOT VALID"

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo("Illegal category type!"))
    }

    @Test
    fun shouldNotSaveProfessionalIllegalArgumentExceptionAnotherCategory() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.category = CategoryOptions.CUSTOMER.name

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestEmptyCategory() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.category = ""

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.CATEGORY_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestNotValidEmail() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.emails = listOf(CreateEmailDTO(email = "", comment = null))

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.EMAILS_NOT_VALID))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestNotValidTelephone() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.telephones = listOf(CreateTelephoneDTO(telephone = "", comment = null))

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.TELEPHONES_NOT_VALID))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestNotValidAddress() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.information.addresses =
            listOf(CreateAddressDTO(address = "", state = "", city = "", region = "", comment = null))

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.ADDRESSES_NOT_VALID))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestNegativeDailyRate() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.dailyRate = -1.0

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.DAILYRATE_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestEmptyGeographicalLocation() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.geographicalLocation = ""

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR))
    }

    @Test
    fun shouldNotSaveProfessionalBadRequestEmptySkills() {
        val createProfessional = createProfessionalDtoList.first()

        createProfessional.skills = listOf("")

        given()
            .contentType(ContentType.JSON)
            .body(createProfessional)
            .post("/API/professionals/")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.SKILLS_ERROR))
    }

    /**
     * PATCH /API/PROFESSIONALS
     */

    @Test
    fun shouldUpdateProfessional() {
        professionalRepository.deleteAll()
        contactRepository.deleteAll()

        val professional = professionalRepository.save(professionalsList.first())
        val updateProfessional = updateProfessionalDTO

        updateProfessional.id = professional.id
        updateProfessional.employmentState = EmploymentStateEnum.UNEMPLOYED.name
        updateProfessional.dailyRate = 100.0

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(200)
            .body("information.name", equalTo(updateProfessional.information.name))
            .body("information.surname", equalTo(updateProfessional.information.surname))
            .body("information.ssnCode", equalTo(updateProfessional.information.ssnCode))
            .body("information.category", equalTo(CategoryOptions.PROFESSIONAL.name))
            .body("employmentState", equalTo(updateProfessional.employmentState))
            .body("dailyRate", equalTo(updateProfessional.dailyRate.toFloat()))
            .body("geographicalLocation", equalTo(updateProfessional.geographicalLocation))
    }

    @Test
    fun shouldNotUpdateProfessionalNegativeProfessionalId() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.id = -1L

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.ID_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalEmptyName() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.name = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalEmptySurname() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.surname = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalEmptySsnCode() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.ssnCode = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.SSN_CODE_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalEmptyCategory() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.category = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.CATEGORY_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalNotValidCategory() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.category = "NOT VALID"

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo("Illegal category type!"))
    }

    @Test
    fun shouldNotUpdateProfessionalAnotherCategory() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.information.category = CategoryOptions.CUSTOMER.name

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalBadRquestEmptyEmploymentState() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.employmentState = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.EMPLYMENT_STATE_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalBadRquestNotValidEmploymentState() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.employmentState = "NOT VALID"

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo("Illegal Employment State type!"))
    }

    @Test
    fun shouldNotUpdateProfessionalBadRquestNotNegativeDailyRate() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.dailyRate = -100000.0

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.DAILYRATE_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalBadRquestEmptyGeographicalLocation() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.geographicalLocation = ""

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalBadRquestNotValidSkills() {
        val updateProfessional = updateProfessionalDTO

        updateProfessional.skills = listOf("")

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(400)
            .body(equalTo(ErrorsPage.SKILLS_ERROR))
    }

    @Test
    fun shouldNotUpdateProfessionalNotFoundProfessional() {
        val updateProfessional = updateProfessionalDTO

        given()
            .contentType(ContentType.JSON)
            .body(updateProfessional)
            .patch("/API/professionals/${updateProfessional.id}")
            .then()
            .statusCode(404)
            .body(equalTo("The professional with id equal to ${updateProfessional.id} was not found!"))
    }

    /**
     * DELETE /API/PROFESSIONALS
     */

    @Test
    fun shouldDeleteProfessional() {
        val professional = professionalRepository.save(professionalsList.first())

        given()
            .contentType(ContentType.JSON)
            .delete("/API/professionals/${professional.id}")
            .then()
            .statusCode(200)
            .body(equalTo(ErrorsPage.PROFESSIONAL_DELETED_SUCCESSFULLY))
    }

    @Test
    fun shouldNotDeleteProfessionalProfessionalNotFound() {
        professionalRepository.deleteAll()

        val id = 1

        given()
            .contentType(ContentType.JSON)
            .delete("/API/professionals/${id}")
            .then()
            .statusCode(404)
            .body("error", equalTo("ProfessionalService: Professional with id=${id} not found!"))
    }

    @Test
    fun shouldNotDeleteProfessionalNegativeProfessionalId() {
        val id = -1

        given()
            .contentType(ContentType.JSON)
            .delete("/API/professionals/${id}")
            .then()
            .statusCode(400)
            .body("error", equalTo(ErrorsPage.PROFESSIONAL_ID_ERROR))
    }
}