package it.polito.students.crm.services

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.JobOfferRepository
import it.polito.students.crm.repositories.ProfessionalRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.JobStatusEnum
import it.polito.students.crm.utils.ProfessionalEnumFields
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ProfessionalServiceImpl(
    private val professionalRepository: ProfessionalRepository,
    private val jobOfferRepository: JobOfferRepository,
    private val contactRepository: ContactRepository,
    private val contactService: ContactService
) : ProfessionalService {
    private val logger = LoggerFactory.getLogger(ProfessionalServiceImpl::class.java)

    override fun getAllProfessionals(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ProfessionalEnumFields, String>
    ): PageImpl<ProfessionalDTO> {
        val pageable = PageRequest.of(pageNumber, pageSize)
        val page: Page<Professional> = professionalRepository.findAll(pageable)
        var list = page.content.filter { !it.deleted }.map { it.toDTO() }

        filterMap.entries.forEach { filter ->
            list = when (filter.key) {
                ProfessionalEnumFields.SKILL -> list.filter { it.skills.any { skill -> skill.contains(filter.value, ignoreCase = true) } }
                ProfessionalEnumFields.LOCATION -> list.filter { it.geographicalLocation.contains(filter.value, ignoreCase = true) }
                ProfessionalEnumFields.EMPLOYMENT_STATE -> list.filter { it.employmentState.name.contains(filter.value, ignoreCase = true) }
            }
        }


        val pageImpl = PageImpl(list, pageable, page.totalElements)
        return pageImpl
    }

    override fun getProfessional(id: Long): ProfessionalWithAssociatedDataDTO {
        val optionalContact = professionalRepository.findById(id)
        if (optionalContact.isPresent && !optionalContact.get().deleted) {
            val professional = optionalContact.get()
            return professional.toDTOWithAssociatedData()
        } else {
            logger.info("The professional with id $id was not found on the db")
            throw ProfessionalNotFoundException("The professional with id equal to $id was not found!")
        }
    }

    override fun storeProfessional(professional: CreateProfessionalDTO, state: EmploymentStateEnum): ProfessionalDTO {
        val newContactDTO = contactService.storeContact(professional.information, CategoryOptions.PROFESSIONAL)

        val newContact = contactRepository.findById(newContactDTO.contactDTO.id)

        val newProfessional = Professional().apply {
            information = newContact.get()
            skills = professional.skills
            employmentState = state
            geographicalLocation = professional.geographicalLocation
            dailyRate = professional.dailyRate
        }

        val professionalSaved = professionalRepository.save(newProfessional)
        return professionalSaved.toDTO()
    }

    override fun updateProfessional(professionalDto: UpdateProfessionalDTO, contact: Contact): ProfessionalDTO {
        val professionalDb = getProfessional(professionalDto.id).professionalDTO
        //Create a contact Entity from Db Contact
        val professional = Professional().apply {
            id = professionalDb.id
            information = contact
            skills = professionalDb.skills
            employmentState = professionalDb.employmentState
            geographicalLocation = professionalDb.geographicalLocation
            dailyRate = professionalDb.dailyRate
        }

        professional.employmentState = EmploymentStateEnum.valueOf(professionalDto.employmentState)
        professional.geographicalLocation = professionalDto.geographicalLocation
        professional.dailyRate = professionalDto.dailyRate
        professional.skills = professionalDto.skills

        //Update the contact in db
        val professionalSaved = professionalRepository.save(professional).toDTO()
        return professionalSaved
    }

    @Transactional
    override fun deleteProfessional(professionalID: Long) {
        val professional = professionalRepository.findById(professionalID)

        if (professional.isPresent) {
            val professionalSaved = professional.get()

            if(professionalSaved.deleted){
                logger.info("ProfessionalService: The professional with id $professionalID was not found on the db")
                throw ProfessionalNotFoundException("ProfessionalService: Professional with id=$professionalID not found!")
            }

            professionalSaved.deleted = true

            professionalSaved.jobOffers.forEach { jobOffer ->
                jobOffer.candidateProfessionals.remove(professionalSaved)
                if(jobOffer.candidateProfessionals.isEmpty()){
                    jobOffer.status = JobStatusEnum.SELECTION_PHASE
                }
            }

            professionalRepository.save(professionalSaved)

        } else {
            logger.info("ProfessionalService: The professional with id $professionalID was not found on the db")
            throw ProfessionalNotFoundException("ProfessionalService: Professional with id=$professionalID not found!")
        }
    }
}