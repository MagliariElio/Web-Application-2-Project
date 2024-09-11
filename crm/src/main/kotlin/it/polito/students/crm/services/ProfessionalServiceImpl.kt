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
        val allProfessionals: List<Professional> = professionalRepository.findAll()

        var filteredList = allProfessionals.filter { !it.deleted }.map { professional ->
            professional.jobOffers = professional.jobOffers.filter { !it.deleted }.toMutableSet()
            professional
        }.map { it.toDTO() }

        filterMap.entries.forEach { filter ->
            filteredList = when (filter.key) {
                ProfessionalEnumFields.SKILL -> filteredList.filter {
                    it.skills.any { skill ->
                        skill.contains(filter.value, ignoreCase = true)
                    }
                }

                ProfessionalEnumFields.LOCATION -> filteredList.filter {
                    it.geographicalLocation.contains(filter.value, ignoreCase = true)
                }

                ProfessionalEnumFields.EMPLOYMENT_STATE -> filteredList.filter {
                    it.employmentState.name.contains(filter.value, ignoreCase = true)
                }

                ProfessionalEnumFields.NAME -> filteredList.filter {
                    it.information.name.contains(filter.value, ignoreCase = true)
                }

                ProfessionalEnumFields.SURNAME -> filteredList.filter {
                    it.information.surname.contains(filter.value, ignoreCase = true)
                }
            }
        }

        val pageable = PageRequest.of(pageNumber, pageSize)

        val start = pageNumber * pageSize
        val end = minOf(start + pageSize, filteredList.size)
        val paginatedList = if (start <= filteredList.size) filteredList.subList(start, end) else emptyList()

        return PageImpl(paginatedList, pageable, filteredList.size.toLong())
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

            if (professionalSaved.deleted) {
                logger.info("ProfessionalService: The professional with id $professionalID was not found on the db")
                throw ProfessionalNotFoundException("ProfessionalService: Professional with id=$professionalID not found!")
            }

            professionalSaved.jobOffers.forEach { jobOffer ->
                jobOffer.candidateProfessionals.removeIf { it.id == professionalID }
                jobOffer.candidatesProfessionalRejected.removeIf { it == professionalID }
                jobOffer.candidatesProfessionalRevoked.removeIf { it == professionalID }

                if (jobOffer.professional?.id == professionalID) {
                    jobOffer.oldStatus = JobStatusEnum.CREATED
                    jobOffer.status = JobStatusEnum.SELECTION_PHASE
                    jobOffer.professional = null
                    jobOffer.value = 0.0
                }

                if (jobOffer.candidateProfessionals.isEmpty()) {
                    jobOffer.oldStatus = JobStatusEnum.CREATED
                    jobOffer.status = JobStatusEnum.CREATED
                    jobOffer.value = 0.0
                }

                jobOfferRepository.save(jobOffer)
                professionalSaved.jobOffers.removeIf { it.id == jobOffer.id }
            }

            professionalSaved.deleted = true

            professionalRepository.save(professionalSaved)

        } else {
            logger.info("ProfessionalService: The professional with id $professionalID was not found on the db")
            throw ProfessionalNotFoundException("ProfessionalService: Professional with id=$professionalID not found!")
        }
    }
}