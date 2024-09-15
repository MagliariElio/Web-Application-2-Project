package it.polito.students.crm.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.students.crm.CrmApplication
import it.polito.students.crm.dtos.CreateJobOfferDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Customer
import it.polito.students.crm.entities.JobOffer
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.NotFoundJobOfferException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.ContactServiceImpl
import it.polito.students.crm.services.CustomerServiceImpl
import it.polito.students.crm.services.JobOfferServiceImpl
import it.polito.students.crm.services.ProfessionalServiceImpl
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.Factory
import it.polito.students.crm.utils.Factory.Companion.copy
import it.polito.students.crm.utils.JobStatusEnum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest(
    classes = [CrmApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CrmJobOffersServiceUnitTest {
    private val customerRepository: CustomerRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val addressRepository: AddressRepository = mockk()
    private val emailRepository: EmailRepository = mockk()
    private val telephoneRepository: TelephoneRepository = mockk()
    private val jobOfferRepository: JobOfferRepository = mockk()
    private val professionalRepository: ProfessionalRepository = mockk()
    private val factory = Factory(jobOfferRepository)

    val contactService = ContactServiceImpl(contactRepository, emailRepository, telephoneRepository, addressRepository)

    val customerService =
        CustomerServiceImpl(customerRepository, contactService, contactRepository, jobOfferRepository, factory)

    val professionalService =
        ProfessionalServiceImpl(professionalRepository, jobOfferRepository, contactRepository, contactService)

    val jobOfferService = JobOfferServiceImpl(jobOfferRepository, customerService, professionalRepository, factory)

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
            employmentState = EmploymentStateEnum.EMPLOYED
            geographicalLocation = "New York"
            dailyRate = 500.0
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
        }
    )
    val customerList = listOf(
        Customer().apply {
            this.id = 1
            information = Contact().apply {
                name = "John"
                surname = "Doe"
                ssnCode = "1234abcdef789"
                category = CategoryOptions.CUSTOMER
                comment = "This is a comment"
            }
        },
        Customer().apply {
            this.id = 2
            information = Contact().apply {
                name = "Alice"
                surname = "Smith"
                ssnCode = "5678ghij123"
                category = CategoryOptions.CUSTOMER
                comment = "Another comment"
            }
        },
        Customer().apply {
            this.id = 3
            information = Contact().apply {
                name = "Bob"
                surname = "Johnson"
                ssnCode = "90klmnopq456"
                category = CategoryOptions.CUSTOMER
                comment = "Yet another comment"
            }
        }
    )
    val jobOfferList = listOf(
        JobOffer().apply {
            id = 1
            status = JobStatusEnum.CREATED
            requiredSkills = listOf("Java", "Kotlin", "Spring Boot")
            duration = 30
            value = 0.0
            note = "This is a job offer for a Java developer"
            customer = customerList[0]
        },
        JobOffer().apply {
            id = 2
            status = JobStatusEnum.DONE
            requiredSkills = listOf("Python", "Django", "MySQL")
            duration = 60
            value = 0.0
            note = "We're looking for a Python developer with Django experience"
            customer = customerList[1]
        },
        JobOffer().apply {
            id = 3
            status = JobStatusEnum.CONSOLIDATED
            requiredSkills = listOf("JavaScript", "React", "Node.js")
            duration = 40
            value = 200000.0
            note = "A React developer is needed for a short-term project"
            customer = customerList[2]
            professional = professionalsList[0]
        }
    )
    val createJobOfferDTOList = listOf(
        CreateJobOfferDTO(
            requiredSkills = listOf("Java", "Kotlin", "Spring Boot"),
            duration = 30,
            note = "This is a job offer for a Java developer",
            customerId = customerList[0].id
        ),
        CreateJobOfferDTO(
            requiredSkills = listOf("Python", "Django", "MySQL"),
            duration = 60,
            note = "We're looking for a Python developer with Django experience",
            customerId = customerList[1].id
        ),
        CreateJobOfferDTO(
            requiredSkills = listOf("JavaScript", "React", "Node.js"),
            duration = 40,
            note = "A React developer is needed for a short-term project",
            customerId = customerList[2].id
        )
    )


    /**
     * GET JOB OFFER VALUE TEST CASES (used in the GET /API/joboffers/{jobOfferId}/value)
     *
     * practically it just gets the entire jobOffer in the service
     *
     */

    @Test
    fun getJobOffer_goodCase() {
        val jobOffer = jobOfferList[2]

        every { jobOfferRepository.findById(jobOffer.id) } returns Optional.of(jobOffer)

        val result = jobOfferService.getJobOfferById(jobOffer.id)

        verify(exactly = 1) { jobOfferRepository.findById(jobOffer.id) }
        assertEquals(jobOffer.toDTO(), result)
    }

    @Test
    fun getJobOffer_notFound() {
        val jobOfferId: Long = -3

        every { jobOfferRepository.findById(jobOfferId) } returns Optional.empty()

        val exception = assertThrows<NotFoundJobOfferException> { jobOfferService.getJobOfferById(jobOfferId) }

        verify(exactly = 1) { jobOfferRepository.findById(jobOfferId) }
        assertEquals("JobOffer id not found!", exception.message)
    }

    @Test
    fun getJobOffer_noProfessionalAndValue() {
        val jobOffer = jobOfferList[0]

        every { jobOfferRepository.findById(jobOffer.id) } returns Optional.of(jobOffer)

        val result = jobOfferService.getJobOfferById(jobOffer.id)

        verify(exactly = 1) { jobOfferRepository.findById(jobOffer.id) }
        assertEquals(null, result)
    }


    /**
     * POST new job offer test cases
     */

    @Test
    fun postJobOffer_goodCase() {
        val createjobOffer = createJobOfferDTOList[0]

        val customer = customerList[0]
        every { customerRepository.findById(1) } returns Optional.of(customer)

        every { jobOfferRepository.findAllByCustomer_Id(1) } returns listOf()

        val mockSavedJobOffer = jobOfferList[0]
        every { jobOfferRepository.save(any()) } returns mockSavedJobOffer

        val result = jobOfferService.storeJobOffer(createjobOffer)

        verify(exactly = 1) { jobOfferRepository.save(any()) }
        assertEquals(mockSavedJobOffer.toDTO(), result)
    }


    @Test
    fun postJobOffer_customerNotFound() {
        val createjobOffer = createJobOfferDTOList[0]

        createjobOffer.customerId = 999

        every { customerRepository.findById(createjobOffer.customerId) } throws CustomerNotFoundException("Not found")

        assertThrows<CustomerNotFoundException> { jobOfferService.storeJobOffer(createjobOffer) }

    }


    /**
     * DELETE /API/JOBOFFERS/JOBOFFERID
     */

    @Test
    fun deleteJobOffer_goodCaseWithJobOffers() {
        val jobOfferBeforeDelete = jobOfferList[2].copy()
        val jobOfferNextDelete = jobOfferList[2].copy()

        jobOfferBeforeDelete.deleted = false
        jobOfferNextDelete.deleted = true
        jobOfferNextDelete.professional?.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        every { jobOfferRepository.findById(jobOfferBeforeDelete.id) } returns Optional.of(jobOfferBeforeDelete)
        every { jobOfferRepository.save(any()) } returns jobOfferNextDelete
        every { professionalRepository.save(any()) } returns jobOfferNextDelete.professional!!

        jobOfferService.deleteJobOffer(jobOfferBeforeDelete.id)

        verify(exactly = 1) { jobOfferRepository.findById(jobOfferBeforeDelete.id) }
        verify(exactly = 1) { jobOfferRepository.save(any()) }
        verify(exactly = 1) { professionalRepository.save(any()) }
    }

    @Test
    fun deleteJobOffer_goodCaseWithNoJobOffers() {
        val jobOfferBeforeDelete = jobOfferList[0].copy()
        val jobOfferNextDelete = jobOfferList[0].copy()

        jobOfferBeforeDelete.deleted = false
        jobOfferNextDelete.deleted = true
        jobOfferNextDelete.professional?.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        every { jobOfferRepository.findById(jobOfferBeforeDelete.id) } returns Optional.of(jobOfferBeforeDelete)
        every { jobOfferRepository.save(any()) } returns jobOfferNextDelete

        jobOfferService.deleteJobOffer(jobOfferBeforeDelete.id)

        verify(exactly = 1) { jobOfferRepository.findById(jobOfferBeforeDelete.id) }
        verify(exactly = 1) { jobOfferRepository.save(any()) }
        verify(exactly = 0) { professionalRepository.save(any()) }
    }

    @Test
    fun deleteJobOffer_jobOfferNotFound() {
        val jobOfferID = 1L

        every { jobOfferRepository.findById(jobOfferID) } returns Optional.empty()

        val exception = assertThrows<NoSuchElementException> {
            jobOfferService.deleteJobOffer(jobOfferID)
        }

        assertEquals(
            null,
            exception.message
        )

        verify(exactly = 1) { jobOfferRepository.findById(jobOfferID) }
    }

    /*

1. **testChangeJobOfferStatus_NoSuchJobOffer**: Questo caso di test verifica se la funzione lancia un'eccezione `NoSuchElementException` quando viene fornito un `jobOfferId` che non esiste nel repository.

2. **testChangeJobOfferStatus_InvalidStatusTransition**: Questo caso di test verifica se la funzione lancia un'eccezione `IllegalJobStatusTransition` quando viene tentata una transizione di stato non valida.

3. **testChangeJobOfferStatus_RequiredProfessionalId**: Questo caso di test verifica se la funzione lancia un'eccezione `RequiredProfessionalIdException` quando il `nextStatus` richiede un `professionalId` ma non viene fornito.

4. **testChangeJobOfferStatus_NotAvailableProfessional**: Questo caso di test verifica se la funzione lancia un'eccezione `NotAvailableProfessionalException` quando il professionista associato non è disponibile per iniziare un lavoro.

5. **testChangeJobOfferStatus_InconsistentProfessionalStatusTransition**: Questo caso di test verifica se la funzione lancia un'eccezione `InconsistentProfessionalStatusTransitionException` quando il `professionalId` fornito non corrisponde al professionista che ha superato la fase di proposta del candidato.

6. **testChangeJobOfferStatus_IllegalJobStatusTransition**: Questo caso di test verifica se la funzione lancia un'eccezione `IllegalJobStatusTransition` quando viene tentato di entrare nello stato "Create", che è illegale.

7. **testChangeJobOfferStatus_SuccessfulStatusTransition**: Questo caso di test verifica se la funzione riesce a cambiare lo stato dell'offerta di lavoro e restituisce il DTO dell'offerta di lavoro aggiornata quando tutte le condizioni sono soddisfatte.

    * */

}