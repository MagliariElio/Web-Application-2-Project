package it.polito.students.crm.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.students.crm.CrmApplication
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.ContactServiceImpl
import it.polito.students.crm.services.ProfessionalServiceImpl
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.Factory
import it.polito.students.crm.utils.Factory.Companion.copy
import it.polito.students.crm.utils.ProfessionalEnumFields
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@SpringBootTest(
    classes = [CrmApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CrmProfessionalsServiceUnitTest {
    private val customerRepository: CustomerRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val addressRepository: AddressRepository = mockk()
    private val emailRepository: EmailRepository = mockk()
    private val telephoneRepository: TelephoneRepository = mockk()
    private val jobOfferRepository: JobOfferRepository = mockk()
    private val professionalRepository: ProfessionalRepository = mockk()
    private val factory = Factory(jobOfferRepository)

    val contactService =
        ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)

    val professionalService =
        ProfessionalServiceImpl(professionalRepository, jobOfferRepository, contactRepository, contactService)

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

    val updatedProfessional = Professional().apply {
        id = updatedProfessionalDTO.id
        information = Contact().apply {
            id = updatedProfessionalDTO.information.id
            name = updatedProfessionalDTO.information.name
            surname = updatedProfessionalDTO.information.surname
            ssnCode = updatedProfessionalDTO.information.ssnCode
            category = updatedProfessionalDTO.information.category
            comment = updatedProfessionalDTO.information.comment
        }
        skills = updatedProfessionalDTO.skills
        employmentState = updatedProfessionalDTO.employmentState
        geographicalLocation = updatedProfessionalDTO.geographicalLocation
        dailyRate = updatedProfessionalDTO.dailyRate
    }


    val createProfessional = CreateProfessionalDTO(
        information = CreateContactDTO(
            name = "John",
            surname = "Doe",
            ssnCode = "123-45-6789",
            category = CategoryOptions.PROFESSIONAL.name,
            comment = "Lorem ipsum dolor sit amet",
            emails = mutableListOf(),
            addresses = mutableListOf(),
            telephones = mutableListOf()
            /*emails = mutableListOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
                    comment = "This is a comment"
                )
            ),
            addresses = mutableListOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Lazio",
                    city = "Rome",
                    address = "via Napoli",
                    comment = "This is a comment"
                )
            ),
            telephones = mutableListOf(
                CreateTelephoneDTO(
                    telephone = "3219877891",
                    comment = "This is a comment"
                )
            )*/
        ),
        skills = listOf("Java", "Kotlin", "Spring Boot"),
        geographicalLocation = "New York",
        dailyRate = 500.0
    )

    val createProfessionalToSave = Professional().apply {
        information = Contact().apply {
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
    }

    /**
     * GET ALL PROFESSIONAL TEST CASES
     */

    @Test
    fun getAllProfessionals_goodCase() {
        val professionals: Page<Professional> = PageImpl(professionalsList)
        val list = professionals.content.map { it.toDTO() }
        val pageNumber = 0
        val pageSize = 10

        val map = HashMap<ProfessionalEnumFields, String>()

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    @Test
    fun getAllProfessionals_goodCaseEmptyList() {
        val professionals: Page<Professional> = PageImpl(emptyList())
        val map = HashMap<ProfessionalEnumFields, String>()
        val list = professionals.content.map { it.toDTO() }
        val pageNumber = 0
        val pageSize = 10

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    @Test
    fun getAllProfessionals_goodCaseFilteringByAll() {
        val professionals: Page<Professional> = PageImpl(professionalsList)
        val pageNumber = 0
        val pageSize = 10
        var list = professionals.content.map { it.toDTO() }

        val map = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.SKILL, "Javascript")
            put(ProfessionalEnumFields.LOCATION, "Los Angeles")
            put(ProfessionalEnumFields.EMPLOYMENT_STATE, "UNEMPLOYED")
        }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ProfessionalEnumFields.SKILL -> list.filter { it.skills.contains(filter.value) }
                ProfessionalEnumFields.LOCATION -> list.filter { it.geographicalLocation == filter.value }
                ProfessionalEnumFields.EMPLOYMENT_STATE -> list.filter { it.employmentState.name == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    @Test
    fun getAllProfessionals_goodCaseFilteringBySkills() {
        val professionals: Page<Professional> = PageImpl(professionalsList)
        val pageNumber = 0
        val pageSize = 10
        var list = professionals.content.map { it.toDTO() }

        val map = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.SKILL, "Javascript")
        }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ProfessionalEnumFields.SKILL -> list.filter { it.skills.contains(filter.value) }
                ProfessionalEnumFields.LOCATION -> list.filter { it.geographicalLocation == filter.value }
                ProfessionalEnumFields.EMPLOYMENT_STATE -> list.filter { it.employmentState.name == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    @Test
    fun getAllProfessionals_goodCaseFilteringByLocation() {
        val professionals: Page<Professional> = PageImpl(professionalsList)
        val pageNumber = 0
        val pageSize = 10
        var list = professionals.content.map { it.toDTO() }

        val map = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.LOCATION, "Los Angeles")
        }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ProfessionalEnumFields.SKILL -> list.filter { it.skills.contains(filter.value) }
                ProfessionalEnumFields.LOCATION -> list.filter { it.geographicalLocation == filter.value }
                ProfessionalEnumFields.EMPLOYMENT_STATE -> list.filter { it.employmentState.name == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    @Test
    fun getAllProfessionals_goodCaseFilteringByEmploymentState() {
        val professionals: Page<Professional> = PageImpl(professionalsList)
        val pageNumber = 0
        val pageSize = 10
        var list = professionals.content.map { it.toDTO() }

        val map = HashMap<ProfessionalEnumFields, String>().apply {
            put(ProfessionalEnumFields.EMPLOYMENT_STATE, "UNEMPLOYED")
        }

        map.entries.forEach { filter ->
            list = when (filter.key) {
                ProfessionalEnumFields.SKILL -> list.filter { it.skills.contains(filter.value) }
                ProfessionalEnumFields.LOCATION -> list.filter { it.geographicalLocation == filter.value }
                ProfessionalEnumFields.EMPLOYMENT_STATE -> list.filter { it.employmentState.name == filter.value }
            }
        }

        val expectedPage = PageImpl(list, PageRequest.of(pageNumber, pageSize), professionals.totalElements)

        every { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) } returns professionals

        val result = professionalService.getAllProfessionals(pageNumber, pageSize, map)

        assertEquals(expectedPage, result)

        verify(exactly = 1) { professionalRepository.findAll(PageRequest.of(pageNumber, pageSize)) }
    }

    /**
     * GET PROFESSIONAL TEST CASES
     */

    @Test
    fun getProfessional_goodCase() {
        val professional = professionalsList[0]
        val professionalDTO = professional.toDTOWithAssociatedData()

        every { professionalRepository.findById(professional.id) } returns Optional.of(professional)

        val result = professionalService.getProfessional(professional.id)

        verify(exactly = 1) { professionalRepository.findById(professional.id) }
        assertEquals(professionalDTO, result)
    }

    @Test
    fun getProfessional_professionalNotFound() {
        val professional = professionalsList[0]

        every { professionalRepository.findById(professional.id) } returns Optional.empty()

        val exception = assertThrows<ProfessionalNotFoundException> {
            professionalService.getProfessional(professional.id)
        }

        verify(exactly = 1) { professionalRepository.findById(professional.id) }
        assertEquals(
            "The professional with id equal to ${professional.id} was not found!",
            exception.message
        )
    }


    /**
     * GET PROFESSIONAL TEST CASES
     */

    @Test
    fun patchProfessional_goodCase() {
        every { professionalRepository.findById(updateProfessionalDTO.id) } returns Optional.of(professionalsList[0])
        every { professionalRepository.save(any()) } returns updatedProfessional

        val result = professionalService.updateProfessional(updateProfessionalDTO, contact)


        verify(exactly = 1) { professionalRepository.save(any()) }
        assertEquals(updatedProfessional.toDTO(), result)
    }

    @Test
    fun patchProfessional_ProfessionalNotFound() {
        every { professionalRepository.findById(updateProfessionalDTO.id) } returns Optional.empty()
        every { professionalRepository.save(any()) } returns updatedProfessional

        val result = assertThrows<ProfessionalNotFoundException> {
            professionalService.updateProfessional(
                updateProfessionalDTO,
                contact
            )
        }

        verify(exactly = 0) { professionalRepository.save(any()) }
        assertEquals(
            "The professional with id equal to ${updateProfessionalDTO.id} was not found!",
            result.message
        )
    }

    /**
     * POST PROFESSIONAL TEST CASES
     */

    @Test
    fun storeProfessional_goodCase() {
        val professional = createProfessional
        val professionalSaved = professionalsList.first()

        every { contactRepository.save(any<Contact>()) } returns professionalSaved.information
        every {
            contactService.storeContact(
                professional.information,
                CategoryOptions.PROFESSIONAL
            )
        } returns any()
        every { contactRepository.findById(professionalSaved.information.id) } returns Optional.of(professionalSaved.information)
        every { professionalRepository.save(any<Professional>()) } returns professionalSaved

        val result = professionalService.storeProfessional(professional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        assertEquals(professionalSaved.toDTO(), result)

        verify(exactly = 1) { contactRepository.findById(professionalSaved.information.id) }
        verify(exactly = 1) { professionalRepository.save(any<Professional>()) }
        verify(exactly = 1) { contactRepository.save(any<Contact>()) }
    }

    /**
     * DELETE /API/PROFESSIONALS/PROFESSIONALID
     */

    @Test
    fun deleteProfessional_goodCase() {
        val professionalBeforeStore = professionalsList.first().copy()
        val professionalNextStore = professionalsList.first().copy()
        professionalBeforeStore.deleted = false
        professionalNextStore.deleted = true

        every { professionalRepository.findById(professionalBeforeStore.id) } returns Optional.of(
            professionalBeforeStore
        )
        every { professionalRepository.save(professionalBeforeStore) } answers { professionalNextStore }

        professionalService.deleteProfessional(professionalBeforeStore.id)

        verify(exactly = 1) { professionalRepository.findById(professionalBeforeStore.id) }
        verify(exactly = 1) { professionalRepository.save(professionalBeforeStore) }
    }

    @Test
    fun deleteProfessional_professionalNotFound() {
        val professional = professionalsList.first()

        every { professionalRepository.findById(professional.id) } returns Optional.empty()

        val exception = assertThrows<ProfessionalNotFoundException> {
            professionalService.deleteProfessional(professional.id)
        }

        assertEquals(
            "ProfessionalService: Professional with id=${professional.id} not found!",
            exception.message
        )

        verify(exactly = 1) { professionalRepository.findById(professional.id) }
    }

    @Test
    fun deleteProfessional_professionalAlreadyDeleted() {
        val professional = professionalsList.first().copy()
        professional.deleted = true

        every { professionalRepository.findById(professional.id) } returns Optional.of(professional)

        val exception = assertThrows<ProfessionalNotFoundException> {
            professionalService.deleteProfessional(professional.id)
        }

        assertEquals(
            "ProfessionalService: Professional with id=${professional.id} not found!",
            exception.message
        )

        verify(exactly = 1) { professionalRepository.findById(professional.id) }
    }
}