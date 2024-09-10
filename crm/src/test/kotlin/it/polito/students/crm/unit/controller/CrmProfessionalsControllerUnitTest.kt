package it.polito.students.crm.unit.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.students.crm.CrmApplicationTests
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.INTERNAL_SERVER_ERROR_MESSAGE
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

@WebMvcTest
class CrmProfessionalsControllerUnitTest(
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
            category = "PROFESSIONAL",
            comment = "Lorem ipsum"
        ),
        skills = listOf("Java", "Kotlin", "Spring Boot"),
        employmentState = "EMPLOYED",
        geographicalLocation = "New York",
        dailyRate = 1000.0
    )

    val updatedProfessionalDTO = ProfessionalDTO(
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
    )

    val contact = Contact().apply {
        id = 1
        name = "John"
        surname = "Doe"
        ssnCode = "123-45-6789"
        category = CategoryOptions.PROFESSIONAL
        comment = "Lorem ipsum dolor sit amet"
    }

    /**
     * GET API/PROFESSIONALS/ TEST CASES
     */

    @Test
    fun getProfessionals_statusOK() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        every { professionalService.getAllProfessionals(0, 10, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/?pageNumber=0&pageSize=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_pageSizeLow() {
        val professionals: PageImpl<ProfessionalDTO> =
            PageImpl(listOf(professionalsListDto[0], professionalsListDto[1]))

        every { professionalService.getAllProfessionals(0, 2, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/?pageNumber=0&pageSize=2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionals.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_invalidPage() {
        val errorMessage = "The page number and the page size cannot be negative!"
        mockMvc.perform(get("/API/professionals?pageNumber=-1&pageSize=10"))
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$").value(errorMessage)
            )
    }

    @Test
    fun getProfessionals_invalidPageSize() {
        val errorMessage = "The page number and the page size cannot be negative!"
        mockMvc.perform(get("/API/professionals?pageNumber=0&pageSize=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$").value(errorMessage)
            )
    }

    @Test
    fun getProfessionals_invalidPageAndPageSize() {
        val errorMessage = "The page number and the page size cannot be negative!"
        mockMvc.perform(get("/API/professionals?pageNumber=-1&pageSize=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$").value(errorMessage)
            )
    }

    @Test
    fun getProfessionals_noPage() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        every { professionalService.getAllProfessionals(0, 10, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/?pageSize=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_noPageSize() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        every { professionalService.getAllProfessionals(0, 10, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/?pageNumber=0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_noParameters() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        every { professionalService.getAllProfessionals(0, 10, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_bigPageSize() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        every { professionalService.getAllProfessionals(1000, 10, any()) } returns professionals

        val result = mockMvc.perform(get("/API/professionals/?pageNumber=1000"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_filteredByAll() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        val filter = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.SKILL, "Javascript")
            put(ProfessionalEnumFields.LOCATION, "Los Angeles")
            put(ProfessionalEnumFields.EMPLOYMENT_STATE, "UNEMPLOYED")
        }

        every { professionalService.getAllProfessionals(0, 10, filter) } returns professionals

        val result = mockMvc.perform(get("/API/professionals")
            .apply {
                param("skill", "Javascript")
                param("location", "Los Angeles")
                param("employmentState", "UNEMPLOYED")
            }
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_filteredBySkills() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        val filter = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.SKILL, "Javascript")
        }

        every { professionalService.getAllProfessionals(0, 10, filter) } returns professionals

        val result = mockMvc.perform(get("/API/professionals")
            .apply {
                param("skill", "Javascript")
            }
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_filteredByLocation() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        val filter = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.LOCATION, "Los Angeles")
        }

        every { professionalService.getAllProfessionals(0, 10, filter) } returns professionals

        val result = mockMvc.perform(get("/API/professionals")
            .apply {
                param("location", "Los Angeles")
            }
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    @Test
    fun getProfessionals_filteredByEmploymentState() {
        val professionals: PageImpl<ProfessionalDTO> = PageImpl(professionalsListDto)

        val filter = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.EMPLOYMENT_STATE, "UNEMPLOYED")
        }

        every { professionalService.getAllProfessionals(0, 10, filter) } returns professionals

        val result = mockMvc.perform(get("/API/professionals")
            .apply {
                param("employmentState", "UNEMPLOYED")
            }
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(professionalsListDto.size))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalsResult =
            CrmApplicationTests.createProfessionalDTOFromListString(result.response.contentAsString)

        professionals.forEach { professional ->
            val professionalResult = professionalsResult.first { it.id == professional.id }

            Assertions.assertEquals(professional.id, professionalResult.id)
            Assertions.assertEquals(professional.information.id, professionalResult.information.id)
            Assertions.assertEquals(professional.information.name, professionalResult.information.name)
            Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
            Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
            Assertions.assertEquals(professional.information.category, professionalResult.information.category)
            Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
            Assertions.assertEquals(professional.skills, professionalResult.skills)
            Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
            Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
            Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
        }
    }

    /**
     * GET API/PROFESSIONALS/{PROFESSIONALID} TEST CASES
     */

    @Test
    fun getProfessional_statusOK() {
        val professional = professionalsListDto[0]

        every { professionalService.getProfessional(professional.id) } returns professionalWithAssociatedDataList[0]

        val result = mockMvc.perform(get("/API/professionals/${professional.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.professionalDTO.id").value(professional.id))
            .andReturn()

        // Translation into ProfessionalDTOs
        val professionalResult = CrmApplicationTests.createProfessionalDTOFromString(result.response.contentAsString)

        Assertions.assertEquals(professional.id, professionalResult.id)
        Assertions.assertEquals(professional.information.id, professionalResult.information.id)
        Assertions.assertEquals(professional.information.name, professionalResult.information.name)
        Assertions.assertEquals(professional.information.surname, professionalResult.information.surname)
        Assertions.assertEquals(professional.information.ssnCode, professionalResult.information.ssnCode)
        Assertions.assertEquals(professional.information.category, professionalResult.information.category)
        Assertions.assertEquals(professional.information.comment, professionalResult.information.comment)
        Assertions.assertEquals(professional.skills, professionalResult.skills)
        Assertions.assertEquals(professional.employmentState, professionalResult.employmentState)
        Assertions.assertEquals(professional.geographicalLocation, professionalResult.geographicalLocation)
        Assertions.assertEquals(professional.dailyRate, professionalResult.dailyRate)
    }

    @Test
    fun getProfessional_invalidProfessionalId() {
        mockMvc.perform(get("/API/professionals/-3"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("ProfessionalId value is not valid. Please provide a positive integer!"))
            .andReturn()
    }

    @Test
    fun getProfessional_professionalNotFound() {
        val professional = professionalsListDto[0]

        every { professionalService.getProfessional(professional.id) } throws ContactNotFoundException("Not found")

        mockMvc.perform(get("/API/professionals/${professional.id}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$").value("Not found"))
            .andReturn()

    }

    /**
     * POST API/PROFESSIONALS/ TEST CASES
     */

    @Test
    fun postProfessional_goodCase() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val professionalDto: ProfessionalDTO = professionalsListDto.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        professionalDto.employmentState = employmentState

        every { professionalService.storeProfessional(createProfessional, employmentState) } returns professionalDto

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        val result = mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val professionalJson = mapper.readTree(result)
        val resultProfessional = CrmApplicationTests.createProfessionalDTOFromJSONNode(professionalJson)

        Assertions.assertEquals(resultProfessional.id, professionalDto.id)

        Assertions.assertEquals(resultProfessional.id, professionalDto.information.id)
        Assertions.assertEquals(resultProfessional.information.name, professionalDto.information.name)
        Assertions.assertEquals(resultProfessional.information.surname, professionalDto.information.surname)
        Assertions.assertEquals(resultProfessional.information.ssnCode, professionalDto.information.ssnCode)
        Assertions.assertEquals(resultProfessional.information.category, professionalDto.information.category)
        Assertions.assertEquals(resultProfessional.information.comment, professionalDto.information.comment)
        Assertions.assertEquals(resultProfessional.employmentState, professionalDto.employmentState)
        Assertions.assertEquals(resultProfessional.geographicalLocation, professionalDto.geographicalLocation)
        Assertions.assertEquals(resultProfessional.dailyRate, professionalDto.dailyRate)

        resultProfessional.skills.forEach { resultSkill ->
            val skill = professionalDto.skills.first { resultSkill == it }
            Assertions.assertEquals(resultSkill, skill)
        }

        verify(exactly = 1) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_illegalCategory() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.category = CategoryOptions.CUSTOMER.name

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactName() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.name = ""

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.NAME_SURNAME_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactSurname() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.surname = ""

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.NAME_SURNAME_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactCategory() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.category = null

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.CATEGORY_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactEmails() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.emails = listOf(CreateEmailDTO("NOT VALID", comment = null))

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.EMAILS_NOT_VALID))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactAddresses() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.addresses =
            listOf(CreateAddressDTO(address = "", city = "", state = "", region = "", comment = null))

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.ADDRESSES_NOT_VALID))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingContactTelephones() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.telephones =
            listOf(CreateTelephoneDTO(telephone = "NOT VALID", comment = null))

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.TELEPHONES_NOT_VALID))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_negativeDailyRate() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.dailyRate = -1.0

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.DAILYRATE_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_missingGeographicalLocation() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.geographicalLocation = ""

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_invalidSkills() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.skills = listOf("")

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.SKILLS_ERROR))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_invalidCategory() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        createProfessional.information.category = "INVALID"

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Illegal category type!"))

        verify(exactly = 0) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    @Test
    fun postProfessional_genericExceptionHandling() {
        val createProfessional: CreateProfessionalDTO = createProfessionalDtoList.first()
        val professionalDto: ProfessionalDTO = professionalsListDto.first()
        val employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
        val errorMessage = "General Error!"

        professionalDto.employmentState = employmentState

        every { professionalService.storeProfessional(createProfessional, employmentState) } throws Exception(
            errorMessage
        )

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)

        mockMvc.perform(
            post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 1) { professionalService.storeProfessional(createProfessional, employmentState) }
    }

    /**
     * DELETE /API/PROFESSIONALS/{PROFESSIONALID} TEST CASES
     */

    @Test
    fun deleteProfessional_goodCase() {
        val professionalID = 1L

        every { professionalService.deleteProfessional(professionalID) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/professionals/${professionalID}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(ErrorsPage.PROFESSIONAL_DELETED_SUCCESSFULLY))

        verify(exactly = 1) { professionalService.deleteProfessional(professionalID) }
    }

    @Test
    fun deleteProfessional_negativeProfessionalID() {
        val professionalID = -1L

        mockMvc.perform(
            delete("/API/professionals/${professionalID}")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorsPage.PROFESSIONAL_ID_ERROR))

        verify(exactly = 0) { professionalService.deleteProfessional(professionalID) }
    }

    @Test
    fun deleteProfessional_professionalNotFound() {
        val professionalID = 1L
        val errorMessage = "ProfessionalService: Professional with id=$professionalID not found!"

        every { professionalService.deleteProfessional(professionalID) } throws ProfessionalNotFoundException(
            errorMessage
        )

        mockMvc.perform(
            delete("/API/professionals/${professionalID}")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value(errorMessage))

        verify(exactly = 1) { professionalService.deleteProfessional(professionalID) }
    }

    @Test
    fun deleteProfessional_genericExceptionHandling() {
        val professionalID = 1L

        every { professionalService.deleteProfessional(professionalID) } throws Exception(
            INTERNAL_SERVER_ERROR_MESSAGE
        )

        mockMvc.perform(
            delete("/API/professionals/${professionalID}")
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.error").value(INTERNAL_SERVER_ERROR_MESSAGE))

        verify(exactly = 1) { professionalService.deleteProfessional(professionalID) }
    }

    /**
     * PATCH API/PROFESSIONALS/{PROFESSIONALID} TEST CASES
     */

    @Test
    fun patchProfessional_statusOk() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO

        every { professionalService.getProfessional(updateProfessionalDTO.id) } returns professionalWithAssociatedDataList[0]
        every { contactService.getContact(contact.id) } returns contact
        every {
            contactService.updateContact(
                updateProfessionalDTO.information,
                CategoryOptions.valueOf(updateProfessionalDTO.information.category!!)
            )
        } returns updatedProfessionalDTO.information
        every { professionalService.updateProfessional(any(), contact) } returns updatedProfessionalDTO

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(updatedProfessionalDTO.id))
            .andExpect(jsonPath("$.information.name").value(updatedProfessionalDTO.information.name))
            .andExpect(jsonPath("$.information.surname").value(updatedProfessionalDTO.information.surname))
            .andExpect(jsonPath("$.information.ssnCode").value(updatedProfessionalDTO.information.ssnCode))
            .andExpect(jsonPath("$.information.category").value(updatedProfessionalDTO.information.category.name))
            .andExpect(jsonPath("$.information.comment").value(updatedProfessionalDTO.information.comment))
            .andExpect(jsonPath("$.skills.size()").value(updatedProfessionalDTO.skills.size))
            .andExpect(jsonPath("$.employmentState").value(updatedProfessionalDTO.employmentState.name))
            .andExpect(jsonPath("$.geographicalLocation").value(updatedProfessionalDTO.geographicalLocation))
            .andExpect(jsonPath("$.dailyRate").value(updatedProfessionalDTO.dailyRate))
    }

    @Test
    fun patchProfessional_emptyName() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.information.name = ""

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun patchProfessional_emptySurname() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.information.surname = ""

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun patchProfessional_emptySsnCode() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.information.ssnCode = ""

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.SSN_CODE_ERROR))
    }

    @Test
    fun patchProfessional_invalidCategory() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.information.category = "invalid category"

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Illegal category type!"))
    }

    @Test
    fun patchProfessional_invalidSkills() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.skills = listOf("", "Skill1", "")

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.SKILLS_ERROR))
    }

    @Test
    fun patchProfessional_invalidEmployment() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.employmentState = "Invalid employment"

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Illegal Employment State type!"))
    }

    @Test
    fun patchProfessional_emptyGeographicalLocation() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.geographicalLocation = ""

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR))
    }

    @Test
    fun patchProfessional_invalidDailyRate() {
        val updateProfessionalDTO: UpdateProfessionalDTO = updateProfessionalDTO
        updateProfessionalDTO.dailyRate = -3.0

        val requestBody = ObjectMapper().writeValueAsString(updateProfessionalDTO)

        mockMvc.perform(
            patch("/API/professionals/${updateProfessionalDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(ErrorsPage.DAILYRATE_ERROR))
    }
}