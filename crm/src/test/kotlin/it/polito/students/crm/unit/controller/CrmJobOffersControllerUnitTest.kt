package it.polito.students.crm.unit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.students.crm.CrmApplicationTests
import it.polito.students.crm.dtos.*
import it.polito.students.crm.exception_handlers.NotFoundJobOfferException
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.JobStatusEnum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
class CrmJobOffersControllerUnitTest(
    @Autowired val mockMvc: MockMvc
) {
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

    private val professionalsListDto = listOf(
        ProfessionalDTO(
            id = 1,
            information = ContactDTO(
                id = 1,
                name = "John",
                surname = "Doe",
                ssnCode = "123-45-6789",
                category = CategoryOptions.PROFESSIONAL,
                comment = "Lorem ipsum dolor sit amet"
            ),
            skills = listOf("Java", "Kotlin", "Spring Boot"),
            employmentState = EmploymentStateEnum.EMPLOYED,
            geographicalLocation = "New York",
            dailyRate = 500.0
        ),
        ProfessionalDTO(
            id = 2,
            information = ContactDTO(
                id = 2,
                name = "Alice",
                surname = "Smith",
                ssnCode = "987-65-4321",
                category = CategoryOptions.PROFESSIONAL,
                comment = "Consectetur adipiscing elit"
            ),
            skills = listOf("Python", "Django", "SQL"),
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK,
            geographicalLocation = "San Francisco",
            dailyRate = 600.0
        ),
        ProfessionalDTO(
            id = 3,
            information = ContactDTO(
                id = 3,
                name = "Bob",
                surname = "Johnson",
                ssnCode = "456-78-9012",
                category = CategoryOptions.PROFESSIONAL,
                comment = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
            ),
            skills = listOf("JavaScript", "React", "Node.js"),
            employmentState = EmploymentStateEnum.UNEMPLOYED,
            geographicalLocation = "Los Angeles",
            dailyRate = 450.0
        )
    )

    private val professionalWithAssociatedDataList = listOf(
        ProfessionalWithAssociatedDataDTO(
            ProfessionalDTO(
                id = 1,
                information = ContactDTO(
                    id = 1,
                    name = "John",
                    surname = "Doe",
                    ssnCode = "123-45-6789",
                    category = CategoryOptions.PROFESSIONAL,
                    comment = "Lorem ipsum dolor sit amet"
                ),
                skills = listOf("Java", "Kotlin", "Spring Boot"),
                employmentState = EmploymentStateEnum.EMPLOYED,
                geographicalLocation = "New York",
                dailyRate = 500.0
            ),
            listOf(
                JobOfferDTO(
                    id = 1,
                    status = JobStatusEnum.CONSOLIDATED,
                    requiredSkills = listOf("Java", "Spring Boot"),
                    duration = 3,
                    value = 7000.0,
                    note = "Urgent project",
                    customerId = 123,
                    professionalId = 1,
                    emptyList()
                ),
                JobOfferDTO(
                    id = 2,
                    status = JobStatusEnum.ABORT,
                    requiredSkills = listOf("Kotlin", "Spring Boot"),
                    duration = 6,
                    value = 12000.0,
                    note = "Long-term contract",
                    customerId = 456,
                    professionalId = 1,
                    emptyList()
                )
            )
        ),
        ProfessionalWithAssociatedDataDTO(
            ProfessionalDTO(
                id = 2,
                information = ContactDTO(
                    id = 2,
                    name = "Alice",
                    surname = "Smith",
                    ssnCode = "987-65-4321",
                    category = CategoryOptions.PROFESSIONAL,
                    comment = "Consectetur adipiscing elit"
                ),
                skills = listOf("Python", "Django", "SQL"),
                employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK,
                geographicalLocation = "San Francisco",
                dailyRate = 600.0
            ),
            listOf(
                JobOfferDTO(
                    id = 3,
                    status = JobStatusEnum.CANDIDATE_PROPOSAL,
                    requiredSkills = listOf("Python", "SQL"),
                    duration = 2,
                    value = 5000.0,
                    note = "Immediate start",
                    customerId = 789,
                    professionalId = 2,
                    emptyList()
                )
            )
        ),
        ProfessionalWithAssociatedDataDTO(
            ProfessionalDTO(
                id = 3,
                information = ContactDTO(
                    id = 3,
                    name = "Bob",
                    surname = "Johnson",
                    ssnCode = "456-78-9012",
                    category = CategoryOptions.PROFESSIONAL,
                    comment = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
                ),
                skills = listOf("JavaScript", "React", "Node.js"),
                employmentState = EmploymentStateEnum.UNEMPLOYED,
                geographicalLocation = "Los Angeles",
                dailyRate = 450.0
            ),
            emptyList() // No job offers associated with Bob
        )
    )

    val jobOfferList = listOf(
        JobOfferDTO(
            id = 1,
            status = JobStatusEnum.CREATED,
            requiredSkills = listOf("Java", "Kotlin", "Spring Boot"),
            duration = 20,
            value = 5000.0,
            note = "This is a job offer for a Java developer",
            customerId = 123,
            professionalId = null,
            emptyList()
        ),
        JobOfferDTO(
            id = 2,
            status = JobStatusEnum.DONE,
            requiredSkills = listOf("Python", "Django", "MySQL"),
            duration = 60,
            value = 8000.0,
            note = "We're looking for a Python developer with Django experience",
            customerId = 456,
            professionalId = null,
            emptyList()
        ),
        JobOfferDTO(
            id = 3,
            status = JobStatusEnum.CANDIDATE_PROPOSAL,
            requiredSkills = listOf("JavaScript", "React", "Node.js"),
            duration = 4,
            value = 6000.0,
            note = "A React developer is needed for a short-term project",
            customerId = 2,
            professionalId = 1,
            emptyList()
        )
    )

    val createJobOfferDTOList = listOf(
        CreateJobOfferDTO(
            requiredSkills = listOf("Java", "Kotlin", "Spring Boot"),
            duration = 30,
            note = "This is a job offer for a Java developer",
            customerId = 1
        ),
        CreateJobOfferDTO(
            requiredSkills = listOf("Python", "Django", "MySQL"),
            duration = 30,
            note = "We're looking for a Python developer with Django experience",
            customerId = 2
        ),
        CreateJobOfferDTO(
            requiredSkills = listOf("JavaScript", "React", "Node.js"),
            duration = 30,
            note = "A React developer is needed for a short-term project",
            customerId = 3
        )
    )

    /**
     * GET API/JOBOFFERS/{JOBOFFERID}/VALUE TEST CASES
     */

    @Test
    fun getJobOfferValue_statusOK() {
        val jobOfferId: Long = jobOfferList[0].id

        every { jobOfferService.getJobOfferById(jobOfferId) } returns jobOfferList[0]

        mockMvc.perform(get("/API/joboffers/${jobOfferId}/value"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(jobOfferList[0].id))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.requiredSkills.length()").value(jobOfferList[0].requiredSkills.size))
            .andExpect(jsonPath("$.duration").value(jobOfferList[0].duration))
            .andReturn()
    }

    @Test
    fun getJobOfferValue_jobOfferNotFound() {
        val jobOfferId: Long = jobOfferList[0].id

        every { jobOfferService.getJobOfferById(jobOfferId) } throws NotFoundJobOfferException(ErrorsPage.JOB_OFFER_NOT_FOUND_ERROR)

        mockMvc.perform(get("/API/joboffers/${jobOfferId}/value"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("JobOffer id not found!"))
            .andReturn()
    }

    @Test
    fun getJobOfferValue_invalidJobofferId() {
        val jobOfferId: Long = -3

        mockMvc.perform(get("/API/joboffers/${jobOfferId}/value"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("JobOffer id not valid! Please provide a valid positive integer!"))
            .andReturn()
    }

    /**
     * POST API/JOBOFFERS/ TEST CASES
     */

    @Test
    fun postNewJobOffer_statusOk() {
        val createJobOfferDTO = createJobOfferDTOList[0]
        val newJobOfferDTO = jobOfferList[0]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(createJobOfferDTO)

        every { jobOfferService.storeJobOffer(createJobOfferDTO) } returns newJobOfferDTO

        val result = mockMvc.perform(
            post("/API/joboffers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val resultJobOfferDTO = CrmApplicationTests.createJobOfferDTOFromString(result.response.contentAsString)

        Assertions.assertEquals(newJobOfferDTO.id, resultJobOfferDTO.id)
        Assertions.assertEquals(newJobOfferDTO.status, resultJobOfferDTO.status)
        Assertions.assertEquals(newJobOfferDTO.requiredSkills, resultJobOfferDTO.requiredSkills)
        Assertions.assertEquals(newJobOfferDTO.duration, resultJobOfferDTO.duration)
        Assertions.assertEquals(newJobOfferDTO.value, resultJobOfferDTO.value)
        Assertions.assertEquals(newJobOfferDTO.note, resultJobOfferDTO.note)
        Assertions.assertEquals(newJobOfferDTO.customerId, resultJobOfferDTO.customerId)
        Assertions.assertEquals(newJobOfferDTO.professionalId, resultJobOfferDTO.professionalId)
    }

    @Test
    fun postNewJobOffer_emptyRequiredSkillsString() {
        val createJobOfferDTO = createJobOfferDTOList[0]

        createJobOfferDTO.requiredSkills = listOf("", "Skill1", "")

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(createJobOfferDTO)

        mockMvc.perform(post("/API/joboffers/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The 'required skills' list cannot contain skill entries with empty strings. Please ensure all skills are properly named."))
            .andReturn()

    }

    @Test
    fun postNewJobOffer_negativeDuration() {
        val createJobOfferDTO = createJobOfferDTOList[0]

        createJobOfferDTO.duration = -20

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(createJobOfferDTO)

        mockMvc.perform(post("/API/joboffers/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The 'duration' field cannot be negative. Please ensure the duration is a positive value."))
            .andReturn()

    }

    @Test
    fun postNewJobOffer_negativeCustomerId() {
        val createJobOfferDTO = createJobOfferDTOList[0]

        createJobOfferDTO.customerId = -3

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(createJobOfferDTO)

        mockMvc.perform(post("/API/joboffers/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The provided value for customerId is not valid. Please ensure that you provide a positive integer for the customerId parameter."))
            .andReturn()
    }

    /**
     * DELETE /API/JOBOFFERS/{JOBOFFERID} TEST CASES
     */

    @Test
    fun deleteJobOffers_goodCase() {
        val jobOfferID = 1L
        val expectedMessage = "Job offer $jobOfferID correctly deleted!"

        every { jobOfferService.deleteJobOffer(jobOfferID) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/joboffers/${jobOfferID}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(expectedMessage))

        verify(exactly = 1) { jobOfferService.deleteJobOffer(jobOfferID) }
    }

    @Test
    fun deleteJobOffers_negativeJobOfferId() {
        val jobOfferID = -1L

        mockMvc.perform(
            delete("/API/joboffers/${jobOfferID}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.ID_ERROR))

        verify(exactly = 0) { jobOfferService.deleteJobOffer(jobOfferID) }
    }

    @Test
    fun deleteJobOffers_jobOfferNotFound() {
        val jobOfferID = 1L

        every { jobOfferService.deleteJobOffer(jobOfferID) } throws NoSuchElementException()

        mockMvc.perform(
            delete("/API/joboffers/${jobOfferID}")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value(ErrorsPage.NO_SUCH_JOBOFFER))

        verify(exactly = 1) { jobOfferService.deleteJobOffer(jobOfferID) }
    }

    @Test
    fun deleteJobOffers_genericExceptionHandling() {
        val jobOfferID = 1L

        every { jobOfferService.deleteJobOffer(jobOfferID) } throws Exception(ArgumentMatchers.anyString())

        mockMvc.perform(
            delete("/API/joboffers/${jobOfferID}")
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(ErrorsPage.INTERNAL_SERVER_ERROR_MESSAGE))

        verify(exactly = 1) { jobOfferService.deleteJobOffer(jobOfferID) }
    }

    /**
     * GET /API/JOBOFFES/?CUSTOMERID=CID&PROFESSIONALID=PID&STATUS=OPEN/ACCEPTERD/ABORT
     */
    @Test
    fun getJobOffer_goodCase(){

        val jobOffer: PageImpl<JobOfferDTO> = PageImpl(jobOfferList)

        every { jobOfferService.getAllJobOffers(0, 10, any(), any(), any()) } returns jobOffer

        mockMvc.perform(get("/API/joboffers?page=0&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(jobOfferList[0].id))
            .andExpect(jsonPath("$.content[0].status").value(jobOfferList[0].status.name))
            .andExpect(jsonPath("$.content[0].duration").value(jobOfferList[0].duration))
            .andExpect(jsonPath("$.content[0].value").value(jobOfferList[0].value))
            .andExpect(jsonPath("$.content[0].note").value(jobOfferList[0].note))
            .andExpect(jsonPath("$.content[0].customerId").value(jobOfferList[0].customerId))
            .andExpect(jsonPath("$.content[0].professionalId").value(jobOfferList[0].professionalId))
            .andExpect(jsonPath("$.content[0].requiredSkills.size()").value(jobOfferList[0].requiredSkills.size))
            .andExpect(jsonPath("$.content[0].requiredSkills[0]").value(jobOfferList[0].requiredSkills[0]))
            .andExpect(jsonPath("$.content[0].requiredSkills[1]").value(jobOfferList[0].requiredSkills[1]))
            .andExpect(jsonPath("$.content[0].requiredSkills[2]").value(jobOfferList[0].requiredSkills[2]))

            .andExpect(jsonPath("$.content[1].id").value(jobOfferList[1].id))
            .andExpect(jsonPath("$.content[1].status").value(jobOfferList[1].status.name))
            .andExpect(jsonPath("$.content[1].duration").value(jobOfferList[1].duration))
            .andExpect(jsonPath("$.content[1].value").value(jobOfferList[1].value))
            .andExpect(jsonPath("$.content[1].note").value(jobOfferList[1].note))
            .andExpect(jsonPath("$.content[1].customerId").value(jobOfferList[1].customerId))
            .andExpect(jsonPath("$.content[1].professionalId").value(jobOfferList[1].professionalId))
            .andExpect(jsonPath("$.content[1].requiredSkills.size()").value(jobOfferList[1].requiredSkills.size))
            .andExpect(jsonPath("$.content[1].requiredSkills[0]").value(jobOfferList[1].requiredSkills[0]))
            .andExpect(jsonPath("$.content[1].requiredSkills[1]").value(jobOfferList[1].requiredSkills[1]))
            .andExpect(jsonPath("$.content[1].requiredSkills[2]").value(jobOfferList[1].requiredSkills[2]))

            .andExpect(jsonPath("$.content[2].id").value(jobOfferList[2].id))
            .andExpect(jsonPath("$.content[2].status").value(jobOfferList[2].status.name))
            .andExpect(jsonPath("$.content[2].duration").value(jobOfferList[2].duration))
            .andExpect(jsonPath("$.content[2].value").value(jobOfferList[2].value))
            .andExpect(jsonPath("$.content[2].note").value(jobOfferList[2].note))
            .andExpect(jsonPath("$.content[2].customerId").value(jobOfferList[2].customerId))
            .andExpect(jsonPath("$.content[2].professionalId").value(jobOfferList[2].professionalId))
            .andExpect(jsonPath("$.content[2].requiredSkills.size()").value(jobOfferList[2].requiredSkills.size))
            .andExpect(jsonPath("$.content[2].requiredSkills[0]").value(jobOfferList[2].requiredSkills[0]))
            .andExpect(jsonPath("$.content[2].requiredSkills[1]").value(jobOfferList[2].requiredSkills[1]))
            .andExpect(jsonPath("$.content[2].requiredSkills[2]").value(jobOfferList[2].requiredSkills[2]))
    }

    @Test
    fun getJobOffer_pageLimitOne(){

        val jobOffer: PageImpl<JobOfferDTO> = PageImpl(jobOfferList.subList(0,1))

        every { jobOfferService.getAllJobOffers(0, 1, any(), any(), any()) } returns jobOffer

        mockMvc.perform(get("/API/joboffers?page=0&limit=1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(jobOfferList[0].id))
            .andExpect(jsonPath("$.content[0].status").value(jobOfferList[0].status.name))
            .andExpect(jsonPath("$.content[0].duration").value(jobOfferList[0].duration))
            .andExpect(jsonPath("$.content[0].value").value(jobOfferList[0].value))
            .andExpect(jsonPath("$.content[0].note").value(jobOfferList[0].note))
            .andExpect(jsonPath("$.content[0].customerId").value(jobOfferList[0].customerId))
            .andExpect(jsonPath("$.content[0].professionalId").value(jobOfferList[0].professionalId))
            .andExpect(jsonPath("$.content[0].requiredSkills.size()").value(jobOfferList[0].requiredSkills.size))
            .andExpect(jsonPath("$.content[0].requiredSkills[0]").value(jobOfferList[0].requiredSkills[0]))
            .andExpect(jsonPath("$.content[0].requiredSkills[1]").value(jobOfferList[0].requiredSkills[1]))
            .andExpect(jsonPath("$.content[0].requiredSkills[2]").value(jobOfferList[0].requiredSkills[2]))

    }

    @Test
    fun getJobOffer_withoutPage(){

        val jobOffer: PageImpl<JobOfferDTO> = PageImpl(jobOfferList)

        every { jobOfferService.getAllJobOffers( 0,10, any(), any(), any()) } returns jobOffer

        mockMvc.perform(get("/API/joboffers?limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(jobOfferList[0].id))
            .andExpect(jsonPath("$.content[0].status").value(jobOfferList[0].status.name))
            .andExpect(jsonPath("$.content[0].duration").value(jobOfferList[0].duration))
            .andExpect(jsonPath("$.content[0].value").value(jobOfferList[0].value))
            .andExpect(jsonPath("$.content[0].note").value(jobOfferList[0].note))
            .andExpect(jsonPath("$.content[0].customerId").value(jobOfferList[0].customerId))
            .andExpect(jsonPath("$.content[0].professionalId").value(jobOfferList[0].professionalId))
            .andExpect(jsonPath("$.content[0].requiredSkills.size()").value(jobOfferList[0].requiredSkills.size))
            .andExpect(jsonPath("$.content[0].requiredSkills[0]").value(jobOfferList[0].requiredSkills[0]))
            .andExpect(jsonPath("$.content[0].requiredSkills[1]").value(jobOfferList[0].requiredSkills[1]))
            .andExpect(jsonPath("$.content[0].requiredSkills[2]").value(jobOfferList[0].requiredSkills[2]))

            .andExpect(jsonPath("$.content[1].id").value(jobOfferList[1].id))
            .andExpect(jsonPath("$.content[1].status").value(jobOfferList[1].status.name))
            .andExpect(jsonPath("$.content[1].duration").value(jobOfferList[1].duration))
            .andExpect(jsonPath("$.content[1].value").value(jobOfferList[1].value))
            .andExpect(jsonPath("$.content[1].note").value(jobOfferList[1].note))
            .andExpect(jsonPath("$.content[1].customerId").value(jobOfferList[1].customerId))
            .andExpect(jsonPath("$.content[1].professionalId").value(jobOfferList[1].professionalId))
            .andExpect(jsonPath("$.content[1].requiredSkills.size()").value(jobOfferList[1].requiredSkills.size))
            .andExpect(jsonPath("$.content[1].requiredSkills[0]").value(jobOfferList[1].requiredSkills[0]))
            .andExpect(jsonPath("$.content[1].requiredSkills[1]").value(jobOfferList[1].requiredSkills[1]))
            .andExpect(jsonPath("$.content[1].requiredSkills[2]").value(jobOfferList[1].requiredSkills[2]))

            .andExpect(jsonPath("$.content[2].id").value(jobOfferList[2].id))
            .andExpect(jsonPath("$.content[2].status").value(jobOfferList[2].status.name))
            .andExpect(jsonPath("$.content[2].duration").value(jobOfferList[2].duration))
            .andExpect(jsonPath("$.content[2].value").value(jobOfferList[2].value))
            .andExpect(jsonPath("$.content[2].note").value(jobOfferList[2].note))
            .andExpect(jsonPath("$.content[2].customerId").value(jobOfferList[2].customerId))
            .andExpect(jsonPath("$.content[2].professionalId").value(jobOfferList[2].professionalId))
            .andExpect(jsonPath("$.content[2].requiredSkills.size()").value(jobOfferList[2].requiredSkills.size))
            .andExpect(jsonPath("$.content[2].requiredSkills[0]").value(jobOfferList[2].requiredSkills[0]))
            .andExpect(jsonPath("$.content[2].requiredSkills[1]").value(jobOfferList[2].requiredSkills[1]))
            .andExpect(jsonPath("$.content[2].requiredSkills[2]").value(jobOfferList[2].requiredSkills[2]))
    }

}