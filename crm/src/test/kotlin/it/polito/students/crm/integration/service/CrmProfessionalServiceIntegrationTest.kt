package it.polito.students.crm.integration.service

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.InvalidUpdateException
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.*
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmProfessionalServiceIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var addressRepository: AddressRepository

    @Autowired
    private lateinit var emailRepository: EmailRepository

    @Autowired
    private lateinit var telephoneRepository: TelephoneRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var jobOfferRepository: JobOfferRepository

    @Autowired
    private lateinit var professionalRepository: ProfessionalRepository

    private val factory: Factory by lazy {
        Factory(jobOfferRepository)
    }

    private val contactService: ContactServiceImpl by lazy {
        ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)
    }

    private val messageService: MessageServiceImpl by lazy {
        MessageServiceImpl(
            messageRepository,
            historyRepository,
            contactRepository,
            emailRepository,
            telephoneRepository
        )
    }

    private val emailService: EmailServiceImpl by lazy {
        EmailServiceImpl(emailRepository, contactRepository, messageService)
    }

    private val telephoneService: TelephoneServiceImpl by lazy {
        TelephoneServiceImpl(telephoneRepository, contactRepository, messageService)
    }

    private val addressService: AddressServiceImpl by lazy {
        AddressServiceImpl(addressRepository, contactRepository, messageService)
    }

    private val customerService: CustomerServiceImpl by lazy {
        CustomerServiceImpl(customerRepository, contactService, contactRepository, jobOfferRepository, factory)
    }

    private val professionalService: ProfessionalServiceImpl by lazy {
        ProfessionalServiceImpl(professionalRepository, jobOfferRepository, contactRepository, contactService)
    }

    @BeforeEach
    fun setUp() {
        // Clean repositories before each test
        customerRepository.deleteAll()
        professionalRepository.deleteAll()
        contactRepository.deleteAll()
        addressRepository.deleteAll()
        emailRepository.deleteAll()
        telephoneRepository.deleteAll()
        professionalRepository.deleteAll()
        historyRepository.deleteAll()
        messageRepository.deleteAll()
        jobOfferRepository.deleteAll()
    }

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
    GET /API/professionals
     */

    @Test
    fun getProfessionals_goodCase() {
        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ProfessionalEnumFields, String>()

        createProfessionalDtoList.forEach {
            val createProfessional = it
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == createProfessionalDtoList.size)
        assert(result.content[0].id == professionalsListDto[0].id)
        assert(result.content[1].id == professionalsListDto[1].id)
        assert(result.content[2].id == professionalsListDto[2].id)
        assert(result.content[3].id.toInt() == 4)
    }

    @Test
    fun getProfessionals_limit() {
        val pageNumber = 0
        val pageSize = 1
        val filterMap = hashMapOf<ProfessionalEnumFields, String>()

        createProfessionalDtoList.forEach {
            val createProfessional = it
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == 1)
        assert(result.content[0].id == professionalsListDto[0].id)
    }

    @Test
    fun getProfessionals_page() {
        val pageNumber = 1
        val pageSize = 10
        val filterMap = hashMapOf<ProfessionalEnumFields, String>()

        createProfessionalDtoList.forEach {
            val createProfessional = it
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == 0)
    }

    @Transactional
    @Test
    fun getProfessionals_skillFilter() {
        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ProfessionalEnumFields, String>(
            ProfessionalEnumFields.SKILL to "Programming"
        )

        createProfessionalDtoList.forEach {
            val createProfessional = it
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == 1)
        assert(result.content[0].id == professionalsListDto[0].id)
    }

    @Test
    fun getProfessionals_locationFilter() {
        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ProfessionalEnumFields, String>(
            ProfessionalEnumFields.LOCATION to "Rome"
        )

        createProfessionalDtoList.forEach {
            val createProfessional = it
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == 1)
        assert(result.content[0].id == professionalsListDto[0].id)
    }

    @Test
    fun getProfessionals_employmentFilter() {
        val pageNumber = 0
        val pageSize = 10
        val filterMap = hashMapOf<ProfessionalEnumFields, String>(
            ProfessionalEnumFields.EMPLOYMENT_STATE to EmploymentStateEnum.AVAILABLE_FOR_WORK.name
        )

        createProfessionalDtoList.forEach {
            val createProfessional = createProfessionalDtoList[0]
            professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        }


        val result = professionalService.getAllProfessionals(pageNumber, pageSize, filterMap)

        assert(result.content.size == 4)
        assert(result.content[0].id == professionalsListDto[0].id)
        assert(result.content[1].id == professionalsListDto[1].id)
        assert(result.content[2].id == professionalsListDto[2].id)
        assert(result.content[3].id.toInt() == 4)
    }

    /*
    PATCH /API/professionals/{professionalId}
     */
    @Test
    fun patchProfessional_goodCase() {
        val createProfessional = createProfessionalDtoList[0]

        var professional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        val updateProfessional = UpdateProfessionalDTO(
            id = professional.id,
            information = UpdateContactDTO(
                id = professional.information.id,
                name = null,
                surname = null,
                ssnCode = null,
                category = null,
                comment = null
            ),
            skills = professional.skills,
            employmentState = professional.employmentState.name,
            geographicalLocation = "New location",
            dailyRate = professional.dailyRate
        )
        val result = professionalService.updateProfessional(updateProfessional, Contact().apply {
            id = professional.information.id
            name = professional.information.name
            surname = professional.information.surname
            ssnCode = professional.information.ssnCode
            category = professional.information.category
            comment = professional.information.comment
        })

        assert(result.information == professional.information)
        assert(result.skills.size == professional.skills.size)
        assert(result.geographicalLocation == "New location")
        assert(result.dailyRate == professional.dailyRate)
        assert(result.employmentState == professional.employmentState)
    }

    @Test
    fun patchProfessional_ProfessionalNotFound() {
        val createProfessional = createProfessionalDtoList[0]

        var professional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        val updateProfessional = UpdateProfessionalDTO(
            id = 100,
            information = UpdateContactDTO(
                id = professional.information.id,
                name = null,
                surname = null,
                ssnCode = null,
                category = null,
                comment = null
            ),
            skills = professional.skills,
            employmentState = professional.employmentState.name,
            geographicalLocation = "New location",
            dailyRate = professional.dailyRate
        )

        val result = assertThrows<ProfessionalNotFoundException> {
            professionalService.updateProfessional(updateProfessional, Contact().apply {
                id = professional.information.id
                name = professional.information.name
                surname = professional.information.surname
                ssnCode = professional.information.ssnCode
                category = professional.information.category
                comment = professional.information.comment
            })
        }
        val expectedMessage = "The professional with id equal to 100 was not found!"
        assert(expectedMessage == result.message)
    }


    /**
     * POST /API/PROFESSIONAL
     */
    @Test
    fun postProfessional_goodCase(){
        val professional = createProfessionalDtoList[0]

        val result = professionalService.storeProfessional(professional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        assert(result.information.name == professional.information.name)
        assert(result.information.surname == professional.information.surname)
        assert(result.information.ssnCode == professional.information.ssnCode)
        assert(result.information.category.name == professional.information.category)
        assert(result.skills.size == professional.skills.size)
        assert(result.geographicalLocation == professional.geographicalLocation)
        assert(result.dailyRate == professional.dailyRate)


    }

}