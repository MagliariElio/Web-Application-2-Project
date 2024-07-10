package it.polito.students.crm.integration.service

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.*
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.Factory
import it.polito.students.crm.utils.JobStatusEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.NoSuchElementException

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmJobOfferServiceIntegrationTest : IntegrationTest() {
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

    @Autowired
    private lateinit var factory: Factory

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

    private val professionalService : ProfessionalServiceImpl by lazy {
        ProfessionalServiceImpl(professionalRepository, jobOfferRepository, contactRepository, contactService)
    }

    private val jobOfferService: JobOfferService by lazy {
        JobOfferServiceImpl(jobOfferRepository, customerService, professionalRepository, factory)
    }

    @BeforeEach
    fun setUp() {
        // Clean repositories before each test
        contactRepository.deleteAll()
        addressRepository.deleteAll()
        emailRepository.deleteAll()
        telephoneRepository.deleteAll()
        historyRepository.deleteAll()
        messageRepository.deleteAll()
        customerRepository.deleteAll()
        jobOfferRepository.deleteAll()
        professionalRepository.deleteAll()
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
    fun storeJobOffer_goodCase() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        val jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 1,
            note = "Nota",
            customerId = newCustomer.id
        )

        val savedjob = jobOfferService.storeJobOffer(jobOffer)

        val jobOfferDTO = JobOfferDTO(
            id = savedjob.id,
            status = JobStatusEnum.CREATED,
            requiredSkills = jobOffer.requiredSkills,
            duration = jobOffer.duration,
            value = 0.0,
            note = jobOffer.note,
            customerId = newCustomer.id,
            professionalId = null,
            emptyList()
        )

        assert(savedjob.id == jobOfferDTO.id)
        assert(savedjob.status == jobOfferDTO.status)
        assert(savedjob.requiredSkills.size == jobOfferDTO.requiredSkills.size)
        assert(savedjob.duration == jobOfferDTO.duration)
        assert(savedjob.value == jobOfferDTO.value)
        assert(savedjob.note == jobOfferDTO.note)
        assert(savedjob.customerId == jobOfferDTO.customerId)
        assert(savedjob.professionalId == jobOfferDTO.professionalId)
    }

    @Test
    fun storeJobOffer_customerNotFound() {
        val jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 1,
            note = "Nota",
            customerId = 1
        )

        val result = assertThrows<CustomerNotFoundException> {
            jobOfferService.storeJobOffer(jobOffer)
        }
    }

    /*
    GET /API/joboffers/{joboffersId}/value test cases
     */
    @Test
    fun getJobOffer_goodCase() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        jobOfferService.changeJobOfferStatus(newJob.id, JobStatusEnum.SELECTION_PHASE, listOf(newProfessional.id), null)
        jobOfferService.changeJobOfferStatus(newJob.id, JobStatusEnum.CANDIDATE_PROPOSAL, listOf(newProfessional.id), null)

        val savedjob = jobOfferService.getJobOfferById(newJob.id)

        if(savedjob != null){
            val jobOfferDTO = JobOfferDTO(
                id = savedjob.id,
                status = JobStatusEnum.CANDIDATE_PROPOSAL,
                requiredSkills = jobOffer.requiredSkills,
                duration = jobOffer.duration,
                value = 0.0,
                note = jobOffer.note,
                customerId = newCustomer.id,
                professionalId = newProfessional.id,
                candidateProfessionalIds = listOf(newProfessional.id)
            )

            assert(savedjob.id == jobOfferDTO.id)
            assert(savedjob.status == jobOfferDTO.status)
            assert(savedjob.requiredSkills.size == jobOfferDTO.requiredSkills.size)
            assert(savedjob.duration == jobOfferDTO.duration)
            assert(savedjob.note == jobOfferDTO.note)
            assert(savedjob.customerId == jobOfferDTO.customerId)
            assert(savedjob.professionalId == jobOfferDTO.professionalId)
        }else{
            assert(savedjob != null) //Always false, if it reaches here test has to fail
        }
    }

    @Test
    fun getJobOffer_notFound() {

        val result = assertThrows<NotFoundJobOfferException> {
            jobOfferService.getJobOfferById(1)
        }
    }

    @Test
    fun getJobOffer_goodCaseNoProfessional() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

        val savedjob = jobOfferService.getJobOfferById(newJob.id)

        assert(savedjob == null)
    }

    /*
    PATCH /API/joboffers/{joboffersId} test cases
     */

    @Test
    fun patchJobOffer_goodCase() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        jobOfferService.changeJobOfferStatus(newJob.id, JobStatusEnum.SELECTION_PHASE, listOf(newProfessional.id), null)
        jobOfferService.changeJobOfferStatus(newJob.id, JobStatusEnum.CANDIDATE_PROPOSAL, listOf(newProfessional.id), null)

        val savedjob = jobOfferService.getJobOfferById(newJob.id)

        if(savedjob != null){
            val jobOfferDTO = JobOfferDTO(
                id = savedjob.id,
                status = JobStatusEnum.CANDIDATE_PROPOSAL,
                requiredSkills = jobOffer.requiredSkills,
                duration = jobOffer.duration,
                value = 0.0,
                note = jobOffer.note,
                customerId = newCustomer.id,
                professionalId = newProfessional.id,
                candidateProfessionalIds = listOf(newProfessional.id)
            )

            assert(savedjob.id == jobOfferDTO.id)
            assert(savedjob.status == jobOfferDTO.status)
            assert(savedjob.requiredSkills.size == jobOfferDTO.requiredSkills.size)
            assert(savedjob.duration == jobOfferDTO.duration)
            assert(savedjob.note == jobOfferDTO.note)
            assert(savedjob.customerId == jobOfferDTO.customerId)
            assert(savedjob.professionalId == jobOfferDTO.professionalId)
        }else{
            assert(savedjob != null) //Always false, if it reaches here test has to fail
        }
    }

    @Test
    fun patchJobOffer_NotExisting() {
        //Create the professional
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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        assertThrows<NoSuchElementException> {
            jobOfferService.changeJobOfferStatus(1, JobStatusEnum.SELECTION_PHASE, listOf(newProfessional.id), null)
        }
    }

    @Test
    fun patchJobOffer_deleted() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

        val deletedOffer = jobOfferService.deleteJobOffer(newJob.id)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        assertThrows<NotFoundJobOfferException> {
            jobOfferService.changeJobOfferStatus(1, JobStatusEnum.SELECTION_PHASE, listOf(newProfessional.id), null)
        }
    }

    @Test
    fun patchJobOffer_illegalState() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        assertThrows<IllegalJobStatusTransition> {
            jobOfferService.changeJobOfferStatus(1, JobStatusEnum.CONSOLIDATED, listOf(newProfessional.id), null)
        }
    }

    @Test
    fun patchJobOffer_requiredProfessionalId() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        assertThrows<RequiredProfessionalIdException> {
            jobOfferService.changeJobOfferStatus(1, JobStatusEnum.SELECTION_PHASE, listOf(), null)
        }
    }

    @Test
    fun patchJobOffer_professionalNotFound() {
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

        val newCustomer = customerService.postNewCustomer(createContact)

        var jobOffer = CreateJobOfferDTO(
            requiredSkills = listOf("Skill1", "Skill2"),
            duration = 100,
            note = "Nota",
            customerId = newCustomer.id
        )

        val newJob = jobOfferService.storeJobOffer(jobOffer)

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

        val newProfessional = professionalService.storeProfessional(createProfessional, EmploymentStateEnum.AVAILABLE_FOR_WORK)

        //CHANGE STATUS TO ADD A PROFESSIONAL
        assertThrows<ProfessionalNotFoundException> {
            jobOfferService.changeJobOfferStatus(1, JobStatusEnum.SELECTION_PHASE, listOf(5), null)
        }
    }
}