package it.polito.students.crm.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.InternalPlatformDsl.toArray
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.ErrorsPage
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
class CrmJobOfferControllerIntegrationTest : IntegrationTest() {
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

    /*
    POST API/joboffers test cases
     */
    @Test
    fun storeNewJobOffer_goodCase() {
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

        //Create new customer
        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        //Get job offers
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value("CREATED"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.content[0].requiredSkills.length()")
                    .value(createJobOffer.requiredSkills.size)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].duration").value(createJobOffer.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].note").value(createJobOffer.note))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].customerId").value(createJobOffer.customerId))

    }

    @Test
    fun storeNewJobOffer_CustomerNotFound() {

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = 1
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("Customer with id = '1' not found!"))
    }

    /*
    GET /API/joboffers/{joboffersId}/value test cases
     */
    @Test
    fun getJobOfferByIdValue_goodCase() {
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

        //Create new customer
        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent2 = result2.response.contentAsString
        val jobofferId = responseContent2.substringAfter("id\":").substringBefore(",").toLong()

        val createContactProf = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
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

        val createProfessional = CreateProfessionalDTO(
            information = createContactProf,
            skills = listOf("Skill1", "Skill2"),
            geographicalLocation = "Italy",
            dailyRate = 3.0
        )

        //Create new professional
        val requestBody3 = ObjectMapper().writeValueAsString(createProfessional)
        val result3 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent3 = result3.response.contentAsString
        val idProfessional = responseContent3.substringAfter("id\":").substringBefore(",").toLong()

         //Change status of job offer
        val changeStatus = ChangeJobStatusDTO(
            nextStatus = "SELECTION_PHASE",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody4 = ObjectMapper().writeValueAsString(changeStatus)
        val result4 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody4)
        ).andReturn()

        //Change status of job offer
        val changeStatus2 = ChangeJobStatusDTO(
            nextStatus = "CANDIDATE_PROPOSAL",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody5 = ObjectMapper().writeValueAsString(changeStatus2)
        val result5 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody5)
        ).andReturn()

        val retirevedjobOffer = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/${jobofferId}/value")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANDIDATE_PROPOSAL"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.requiredSkills.length()")
                    .value(createJobOffer.requiredSkills.size)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.duration").value(createJobOffer.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.note").value(createJobOffer.note))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(createJobOffer.customerId))
    }

    @Test
    fun getJobOfferByIdValue_negativeId() {
        val jobofferId = -1

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/${jobofferId}/value")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.JOB_OFFER_ID_ERROR))


    }

    @Test
    fun getJobOfferByIdValue_jobOfferNotFound() {
        val jobofferId = 1

        val retirevedjobOffer = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/${jobofferId}/value")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.JOB_OFFER_NOT_FOUND_ERROR))
    }

    /*
    PATCH /API/joboffers/{joboffersId} test cases
     */
    @Test
    fun patchJobOffer_goodCase() {
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

        //Create new customer
        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent2 = result2.response.contentAsString
        val jobofferId = responseContent2.substringAfter("id\":").substringBefore(",").toLong()

        val createContactProf = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
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

        val createProfessional = CreateProfessionalDTO(
            information = createContactProf,
            skills = listOf("Skill1", "Skill2"),
            geographicalLocation = "Italy",
            dailyRate = 3.0
        )

        //Create new professional
        val requestBody3 = ObjectMapper().writeValueAsString(createProfessional)
        val result3 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent3 = result3.response.contentAsString
        val idProfessional = responseContent3.substringAfter("id\":").substringBefore(",").toLong()

        //Change status of job offer
        val changeStatus = ChangeJobStatusDTO(
            nextStatus = "SELECTION_PHASE",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody4 = ObjectMapper().writeValueAsString(changeStatus)
        val result4 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody4)
        ).andReturn()

        //Change status of job offer
        val changeStatus2 = ChangeJobStatusDTO(
            nextStatus = "CANDIDATE_PROPOSAL",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody5 = ObjectMapper().writeValueAsString(changeStatus2)
        val result5 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody5)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.duration").value(createJobOffer.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.note").value(createJobOffer.note))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(createJobOffer.customerId))
    }

    @Test
    fun patchJobOffer_negativeId() {

        //Change status of job offer
        val changeStatus = ChangeJobStatusDTO(
            nextStatus = "SELECTION_PHASE",
            professionalsId = listOf(1),
            note = null
        )

        val requestBody4 = ObjectMapper().writeValueAsString(changeStatus)
        val result4 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody4)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun patchJobOffer_wrongStatus() {
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

        //Create new customer
        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent2 = result2.response.contentAsString
        val jobofferId = responseContent2.substringAfter("id\":").substringBefore(",").toLong()

        val createContactProf = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
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

        val createProfessional = CreateProfessionalDTO(
            information = createContactProf,
            skills = listOf("Skill1", "Skill2"),
            geographicalLocation = "Italy",
            dailyRate = 3.0
        )

        //Create new professional
        val requestBody3 = ObjectMapper().writeValueAsString(createProfessional)
        val result3 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent3 = result3.response.contentAsString
        val idProfessional = responseContent3.substringAfter("id\":").substringBefore(",").toLong()

        //Change status of job offer
        val changeStatus = ChangeJobStatusDTO(
            nextStatus = "SELECTION_PHASE",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody4 = ObjectMapper().writeValueAsString(changeStatus)
        val result4 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody4)
        ).andReturn()

        //Change status of job offer
        val changeStatus2 = ChangeJobStatusDTO(
            nextStatus = "RANDOM_STATUS",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody5 = ObjectMapper().writeValueAsString(changeStatus2)
        val result5 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody5)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("The value provided for the job status field is invalid. Possible values: [CREATED, SELECTION_PHASE, CANDIDATE_PROPOSAL, CONSOLIDATED, DONE, ABORT]"))
    }

    @Test
    fun patchJobOffer_negativeProfessionalId() {
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

        //Create new customer
        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )

        val requestBody2 = ObjectMapper().writeValueAsString(createJobOffer)
        val result2 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent2 = result2.response.contentAsString
        val jobofferId = responseContent2.substringAfter("id\":").substringBefore(",").toLong()

        val createContactProf = CreateContactDTO(
            name = customersList[1].information.name,
            surname = customersList[1].information.surname,
            ssnCode = customersList[1].information.ssnCode,
            category = customersList[1].information.category.name,
            comment = customersList[1].information.comment,
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

        val createProfessional = CreateProfessionalDTO(
            information = createContactProf,
            skills = listOf("Skill1", "Skill2"),
            geographicalLocation = "Italy",
            dailyRate = 3.0
        )

        //Create new professional
        val requestBody3 = ObjectMapper().writeValueAsString(createProfessional)
        val result3 = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent3 = result3.response.contentAsString
        val idProfessional = responseContent3.substringAfter("id\":").substringBefore(",").toLong()

        //Change status of job offer
        val changeStatus = ChangeJobStatusDTO(
            nextStatus = "SELECTION_PHASE",
            professionalsId = listOf(idProfessional),
            note = null
        )

        val requestBody4 = ObjectMapper().writeValueAsString(changeStatus)
        val result4 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody4)
        ).andReturn()

        //Change status of job offer
        val changeStatus2 = ChangeJobStatusDTO(
            nextStatus = "CANDIDATE_PROPOSAL",
            professionalsId = listOf(-1),
            note = null
        )

        val requestBody5 = ObjectMapper().writeValueAsString(changeStatus2)
        val result5 = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/joboffers/${jobofferId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody5)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("Professional id: " + ErrorsPage.ID_ERROR))
    }

    /* DELETE */
    @Test
    fun deleteJobOffersById_goodCase() {
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

        //Create new customer
        val requestBodyContact = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyContact)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )
        val requestBodyJobOffer = ObjectMapper().writeValueAsString(createJobOffer)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andReturn()

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/joboffers/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/joboffers/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun deleteJobOffersById_JobOfferNotFound() {
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

        //Create new customer
        val requestBodyContact = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyContact)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = id
        )
        val requestBodyJobOffer = ObjectMapper().writeValueAsString(createJobOffer)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andReturn()

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/joboffers/5")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ErrorsPage.NO_SUCH_JOBOFFER))
    }

    @Test
    fun deleteJobOffersById_costumerIdNegative() {
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

        //Create new customer
        val requestBodyContact = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyContact)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = -id
        )
        val requestBodyJobOffer = ObjectMapper().writeValueAsString(createJobOffer)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/joboffers/${-id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getJobOffers_onlyByCostumerId() {
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

        //Create new customer
        val requestBodyContact = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyContact)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val cId = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Create new jobOffer
        val createJobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 3,
            note = "Note",
            customerId = cId
        )
        val requestBodyJobOffer = ObjectMapper().writeValueAsString(createJobOffer)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/joboffers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/?customerId=${cId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJobOffer)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").value("CREATED"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].duration").value(createJobOffer.duration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].value").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].note").value(createJobOffer.note))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].customerId").value(cId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].professionalId").value(null))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].candidateProfessionalIds.size()").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.elementPerPage").value(30))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(1))
    }


}