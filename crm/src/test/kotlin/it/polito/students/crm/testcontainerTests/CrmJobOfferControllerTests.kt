package it.polito.students.crm.testcontainerTests

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.repositories.CustomerRepository
import it.polito.students.crm.repositories.JobOfferRepository
import it.polito.students.crm.repositories.ProfessionalRepository
import it.polito.students.crm.utils.*
import jakarta.transaction.Transactional
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmJobOfferControllerTests {
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
    lateinit var jobOfferRepository: JobOfferRepository

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:$port"
        jobOfferRepository.deleteAll()
        customerRepository.deleteAll()
    }

    val jobOfferList = listOf(
        JobOffer().apply {
            id = 1
            status = JobStatusEnum.CREATED
            requiredSkills = listOf("Java", "Kotlin", "Spring Boot")
            duration = 20
            value = 5000.0
            note = "This is a job offer for a Java developer"
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
            }
            professional = null
            candidateProfessionals = mutableListOf()
        },
        JobOffer().apply {
            id = 2
            status = JobStatusEnum.DONE
            requiredSkills = listOf("Python", "Django", "MySQL")
            duration = 60
            value = 8000.0
            note = "We're looking for a Python developer with Django experience"
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
            }
            professional = null
            candidateProfessionals = mutableListOf()
        },
        JobOffer().apply {
            id = 3
            status = JobStatusEnum.CANDIDATE_PROPOSAL
            requiredSkills = listOf("JavaScript", "React", "Node.js")
            duration = 4
            value = 6000.0
            note = "A React developer is needed for a short-term project"
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
            }
            professional = null
            candidateProfessionals = mutableListOf()
        }
    )

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var professionalRepository: ProfessionalRepository


    @Transactional
    @Test
    fun getAllJobs() {
        //save all joboffes of the list
        jobOfferList.forEach {
            jobOfferRepository.save(it)
        }

        given()
            .contentType(ContentType.JSON)
            .get("/API/joboffers/")
            .then()
            .statusCode(200)
    }


    @Test
    fun storeJobOffer() {
        val jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 30,
            note = "This is a note",
            customerId = 1
        )

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        customerRepository.save(customer)

        val requestBody = ObjectMapper().writeValueAsString(jobOffer)


        //save the new customer
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/API/joboffers/")
            .then()
            .statusCode(201)
    }

    @Test
    fun storeJobOffer_CustomerNotFound() {
        val jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 30,
            note = "This is a note",
            customerId = 1
        )

        val requestBody = ObjectMapper().writeValueAsString(jobOffer)


        //save the new customer
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/API/joboffers/")
            .then()
            .statusCode(404)
    }


    @Test
    fun deleteJobOffer() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)


        //save the new customer
        given()
            .delete("/API/joboffers/${jobOffer.id}")
            .then()
            .statusCode(200)
    }

    @Test
    fun deleteJobOffer_NotFound() {
        //save the new customer
        given()
            .delete("/API/joboffers/1")
            .then()
            .statusCode(404)
            .body("error", equalTo(ErrorsPage.NO_SUCH_JOBOFFER))
    }


    @Test
    fun changeJobOfferStatus() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        jobOffer.customer = customer

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)


        val professional = Professional().apply {
            this.information = Contact().apply {
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
            this.id = 1
        }
        professionalRepository.save(professional)


        val changeJobStatus =
            ChangeJobStatusDTO(nextStatus = JobStatusEnum.SELECTION_PHASE.name, note = "", professionalsId = listOf(1))

        val requestBody = ObjectMapper().writeValueAsString(changeJobStatus)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/joboffers/${jobOffer.id}")
            .then()
            .statusCode(200)
    }

    @Test
    fun changeJobOfferStatus_RequiredProfessional() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        jobOffer.customer = customer

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)


        val professional = Professional().apply {
            this.information = Contact().apply {
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
            this.id = 1
        }
        professionalRepository.save(professional)


        val changeJobStatus =
            ChangeJobStatusDTO(nextStatus = JobStatusEnum.SELECTION_PHASE.name, note = "", professionalsId = listOf())

        val requestBody = ObjectMapper().writeValueAsString(changeJobStatus)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/joboffers/${jobOffer.id}")
            .then()
            .statusCode(400)
    }

    @Test
    fun changeJobOfferStatus_IllegaljobStatus() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        jobOffer.customer = customer

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)


        val professional = Professional().apply {
            this.information = Contact().apply {
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
            this.id = 1
        }
        professionalRepository.save(professional)


        val changeJobStatus = ChangeJobStatusDTO(
            nextStatus = JobStatusEnum.CANDIDATE_PROPOSAL.name,
            note = "",
            professionalsId = listOf(1)
        )

        val requestBody = ObjectMapper().writeValueAsString(changeJobStatus)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/joboffers/${jobOffer.id}")
            .then()
            .statusCode(400)
    }

    @Test
    fun changeJobOfferStatus_ProfessionalnotFound() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        jobOffer.customer = customer

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)


        val professional = Professional().apply {
            this.information = Contact().apply {
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
            this.id = 1
        }
        professionalRepository.save(professional)


        val changeJobStatus =
            ChangeJobStatusDTO(nextStatus = JobStatusEnum.SELECTION_PHASE.name, note = "", professionalsId = listOf(5))

        val requestBody = ObjectMapper().writeValueAsString(changeJobStatus)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .patch("/API/joboffers/${jobOffer.id}")
            .then()
            .statusCode(404)
    }

    @Test
    fun getJobValue() {
        val jobOffer = jobOfferList[0]

        val customer = Customer().apply {
            this.id = 1
            this.information = Contact().apply {
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
            this.deleted = false
            this.joboffers = mutableSetOf()
        }

        customerRepository.save(customer)
        jobOfferRepository.save(jobOffer)

        given()
            .contentType(ContentType.JSON)
            .get("/API/joboffers/1/value")
            .then()
            .statusCode(200)
    }

    @Test
    fun getJobValue_NotFound() {
        given()
            .contentType(ContentType.JSON)
            .get("/API/joboffers/1/value")
            .then()
            .statusCode(404)
            .body("error", equalTo("JobOffer id not found!"))
    }

}