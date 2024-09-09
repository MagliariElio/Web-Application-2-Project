package it.polito.students.crm.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.JobStatusEnum
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
class CrmProfessionalControllerIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

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

    /*
    *   GET /API/professionals/
    */

    @Test
    fun getProfessionals_goodCase() {

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(professionalsListDto[0].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(professionalsListDto[1].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").value(professionalsListDto[2].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].id").value(4))
    }

    @Test
    fun getProfessionals_limit() {

        val limit = 1

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/?pageSize=$limit")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(professionalsListDto[0].id))
    }

    @Test
    fun getProfessionals_page() {

        val page = 1

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/?pageNumber=$page")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(0))
    }

    @Test
    fun getProfessionals_skillFilter() {

        val skill = createProfessionalDtoList[0].skills[0]

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/?skill=$skill")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(professionalsListDto[0].id))

    }

    @Test
    fun getProfessionals_locationFilter() {

        val location = createProfessionalDtoList[0].geographicalLocation

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/?location=$location")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(professionalsListDto[0].id))

    }

    @Test
    fun getProfessionals_employmentFilter() {

        val employment = professionalsListDto[0].employmentState

        createProfessionalDtoList.forEach {
            val createProfessional = it

            val requestBody = ObjectMapper().writeValueAsString(createProfessional)
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/professionals/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()
        }


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/?employmentState=AVAILABLE_FOR_WORK")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(professionalsListDto[0].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(professionalsListDto[1].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").value(professionalsListDto[2].id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].id").value(4))

    }

    /*
    PATCH /API/professionals/{professionalId}
     */

    @Test
    fun patchProfessional_goodCase() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.skills.length()").value(createProfessional.skills.size))
            .andExpect(MockMvcResultMatchers.jsonPath("$.geographicalLocation").value("New location"))
    }

    @Test
    fun patchProfessional_negativeprofessionalId() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.ID_ERROR))
    }

    @Test
    fun patchProfessional_blankname() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = "",
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.NAME_SURNAME_ERROR))
    }

    @Test
    fun patchProfessional_SSNCode_error() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = "",
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.SSN_CODE_ERROR))

    }

    @Test
    fun patchProfessional_nullCateogry() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = null,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.CATEGORY_ERROR))

    }

    @Test
    fun patchProfessional_categoryNotCorrect() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = "randomcategory",
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("Illegal category type!"))

    }

    @Test
    fun patchProfessional_categoryNotProfessional() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = "CUSTOMER",
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR))

    }

    @Test
    fun patchProfessional_invalidEmploymentState() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = "random_category",
            geographicalLocation = "New location",
            dailyRate = createProfessional.dailyRate
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("Illegal Employment State type!"))

    }

    @Test
    fun patchProfessional_negativeDailyRate() {
        val createProfessional = createProfessionalDtoList[0]

        val requestBody = ObjectMapper().writeValueAsString(createProfessional)
        val professional = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        val updateProfessional = UpdateProfessionalDTO(
            id = 1,
            information = UpdateContactDTO(
                id = 1,
                name = createProfessional.information.name,
                surname = createProfessional.information.surname,
                ssnCode = createProfessional.information.ssnCode,
                category = createProfessional.information.category,
                comment = createProfessional.information.comment
            ),
            skills = createProfessional.skills,
            employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK.name,
            geographicalLocation = "New location",
            dailyRate = -1.0
        )

        val requestBody3 = ObjectMapper().writeValueAsString(updateProfessional)
        val newProfessional = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/professionals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody3)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(ErrorsPage.DAILYRATE_ERROR))

    }


    /**
     * POST /API/PROFESSIONAL
     */
    @Test
    fun post_goodTest() {
        val professional = createProfessionalDtoList[0]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.name")
                    .value(professional.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.surname")
                    .value(professional.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.ssnCode")
                    .value(professional.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.comment")
                    .value(professional.information.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.category")
                    .value(professional.information.category)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.dailyRate")
                    .value(professional.dailyRate)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.geographicalLocation")
                    .value(professional.geographicalLocation)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.skills.size()")
                    .value(professional.skills.size)
            )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").isEmpty)
    }

    @Test
    fun postProfessional_emptyNameOrSurname() {
        val professional =
            ProfessionalDTO(
                id = 1,
                information = ContactDTO(
                    id = 1,
                    name = "",
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

        val professional2 =
            ProfessionalDTO(
                id = 1,
                information = ContactDTO(
                    id = 1,
                    name = "John",
                    surname = "",
                    ssnCode = "123-45-6789",
                    category = CategoryOptions.PROFESSIONAL,
                    comment = "Lorem ipsum dolor sit amet"
                ),
                skills = listOf("Java", "Kotlin", "Spring Boot"),
                employmentState = EmploymentStateEnum.EMPLOYED,
                geographicalLocation = "New York",
                dailyRate = 500.0
            )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value(ErrorsPage.NAME_SURNAME_ERROR)
            )


        val requestBody2 = objectMapper.writeValueAsString(professional2)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value(ErrorsPage.NAME_SURNAME_ERROR)
            )
    }

    @Test
    fun postProfessional_emptySsnCode() {
        val professional =
            ProfessionalDTO(
                id = 1,
                information = ContactDTO(
                    id = 1,
                    name = "John",
                    surname = "Doe",
                    ssnCode = "",
                    category = CategoryOptions.PROFESSIONAL,
                    comment = "Lorem ipsum dolor sit amet"
                ),
                skills = listOf("Java", "Kotlin", "Spring Boot"),
                employmentState = EmploymentStateEnum.EMPLOYED,
                geographicalLocation = "New York",
                dailyRate = 500.0
            )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value(ErrorsPage.SSN_CODE_ERROR)
            )
    }

    @Test
    fun postProfessional_withTelephone() {
        val professional = createProfessionalDtoList[3]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.name")
                    .value(professional.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.surname")
                    .value(professional.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.ssnCode")
                    .value(professional.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.comment")
                    .value(professional.information.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.category")
                    .value(professional.information.category)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.dailyRate")
                    .value(professional.dailyRate)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.geographicalLocation")
                    .value(professional.geographicalLocation)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.skills.size()")
                    .value(professional.skills.size)
            )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").isEmpty)
    }

    @Test
    fun postProfessional_withEmail() {
        val professional = createProfessionalDtoList[1]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.name")
                    .value(professional.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.surname")
                    .value(professional.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.ssnCode")
                    .value(professional.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.comment")
                    .value(professional.information.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.category")
                    .value(professional.information.category)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.dailyRate")
                    .value(professional.dailyRate)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.geographicalLocation")
                    .value(professional.geographicalLocation)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.skills.size()")
                    .value(professional.skills.size)
            )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").isEmpty)
    }

    @Test
    fun postProfessional_withAddress() {
        val professional = createProfessionalDtoList[2]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.name")
                    .value(professional.information.name)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.surname")
                    .value(professional.information.surname)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.ssnCode")
                    .value(professional.information.ssnCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.comment")
                    .value(professional.information.comment)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.information.category")
                    .value(professional.information.category)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.dailyRate")
                    .value(professional.dailyRate)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.geographicalLocation")
                    .value(professional.geographicalLocation)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.skills.size()")
                    .value(professional.skills.size)
            )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").isEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").isNotEmpty)
    }

    /* DELETE */
    @Test
    fun deleteProfessionalById_idNotFound() {

        val requestBody = ObjectMapper().writeValueAsString(professionalsListDto[0])

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/${professionalsListDto[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()


        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/professionals/${professionalsListDto[1].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun deleteProfessionalById_idNegative() {

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/-1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/professionals/-1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun deleteProfessionalById_goodCase() {
        val professional = createProfessionalDtoList[0]

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(professional)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/professionals/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)


        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/professionals/${professionalsListDto[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()


        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/professionals/${professionalsListDto[0].id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

}