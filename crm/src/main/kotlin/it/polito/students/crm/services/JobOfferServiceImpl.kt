package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateJobOfferDTO
import it.polito.students.crm.dtos.JobOfferDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.JobOffer
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.exception_handlers.*
import it.polito.students.crm.repositories.JobOfferRepository
import it.polito.students.crm.repositories.ProfessionalRepository
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.Factory.Companion.toEntity
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerService: CustomerService,
    private val professionalRepository: ProfessionalRepository,
    private val factory: Factory
) : JobOfferService {

    private val profitMargin: Int = 10

    override fun getAllJobOffers(
        page: Int,
        limit: Int,
        customerId: Long?,
        professionalId: Long?,
        jobStatusGroup: JobStatusGroupEnum?,
        sortBy: String?,
        sortDirection: String?,
        contractType: String?,
        location: String?,
        workMode: String?,
        status: JobStatusEnum?
    ): PageImpl<JobOfferDTO> {
        var list = jobOfferRepository.findAll().filter { !it.deleted }

        if (customerId != null) {
            list = list.filter { it.customer.id == customerId }
        }
        if (professionalId != null) {
            list = list.filter { it.professional != null && it.professional?.id == professionalId }
        }
        if (jobStatusGroup != null) {
            list = list.filter {
                when (jobStatusGroup) {
                    JobStatusGroupEnum.OPEN -> it.status in listOf(
                        JobStatusEnum.CREATED,
                        JobStatusEnum.SELECTION_PHASE,
                        JobStatusEnum.CANDIDATE_PROPOSAL
                    )

                    JobStatusGroupEnum.ACCEPTED -> it.status in listOf(
                        JobStatusEnum.CONSOLIDATED,
                        JobStatusEnum.DONE
                    )

                    JobStatusGroupEnum.ABORTED -> it.status == JobStatusEnum.ABORT
                }
            }
        }

        if (contractType != null) {
            list = list.filter { it.contractType.equals(contractType, ignoreCase = true) }
        }

        if (location != null) {
            list = list.filter { it.location.contains(location, ignoreCase = true) }
        }

        if (workMode != null) {
            list = list.filter { it.workMode.equals(workMode, ignoreCase = true) }
        }

        if (status != null) {
            list = list.filter { it.status == status }
        }

        if (sortBy != null && sortDirection != null) {
            list = when (sortBy.lowercase()) {
                "duration" -> if (sortDirection.lowercase() == "asc") {
                    list.sortedBy { it.duration }
                } else {
                    list.sortedByDescending { it.duration }
                }

                "value" -> if (sortDirection.lowercase() == "asc") {
                    list.sortedBy { it.value }
                } else {
                    list.sortedByDescending { it.value }
                }

                else -> list
            }
        }

        val fromIndex = page * limit
        val toIndex = minOf(fromIndex + limit, list.size)
        val pagedList = if (fromIndex <= toIndex) list.subList(fromIndex, toIndex) else emptyList()

        val dtoList = pagedList.map { it.toDTO() }

        return PageImpl(dtoList, PageRequest.of(page, limit), list.size.toLong())
    }

    override fun storeJobOffer(jobOfferDto: CreateJobOfferDTO): JobOfferDTO {
        val customer = customerService.getCustomer(jobOfferDto.customerId).toEntity(factory)

        val jobOffer = JobOffer().apply {
            name = jobOfferDto.name
            description = jobOfferDto.description
            contractType = jobOfferDto.contractType
            location = jobOfferDto.location
            workMode = jobOfferDto.workMode
            creationTime = LocalDateTime.now()
            endTime = null
            status = JobStatusEnum.CREATED
            requiredSkills = jobOfferDto.requiredSkills
            duration = jobOfferDto.duration
            value = 0.0
            note = jobOfferDto.note
            this.customer = customer
        }

        val saved = jobOfferRepository.save(jobOffer)
        return saved.toDTO()
    }

    override fun updateJobOffer(jobOfferDto: JobOfferDTO): JobOfferDTO {
        // TODO: potrebbe non servire fare queste chiamate, controllare dopo

        val customer = customerService.getCustomer(jobOfferDto.customerId).toEntity(factory)
        val oldJobOffer = getJobOfferById(jobOfferDto.id) ?: throw NoSuchElementException()

        val jobOffer = JobOffer().apply {
            id = oldJobOffer.id
            name = jobOfferDto.name
            description = jobOfferDto.description
            contractType = jobOfferDto.contractType
            location = jobOfferDto.location
            workMode = jobOfferDto.workMode
            status = oldJobOffer.status
            requiredSkills = jobOfferDto.requiredSkills
            duration = jobOfferDto.duration
            value = oldJobOffer.value
            note = jobOfferDto.note
            this.customer = customer
        }

        val saved = jobOfferRepository.save(jobOffer)
        return saved.toDTO()
    }

    @Transactional
    override fun deleteJobOffer(jobOfferId: Long) {
        val jobOffer = jobOfferRepository.findById(jobOfferId)

        if (!jobOffer.isPresent || jobOffer.get().deleted) {
            throw NoSuchElementException()
        }

        val jobOfferData = jobOffer.get()

        jobOfferData.deleted = true
        jobOfferData.endTime = LocalDateTime.now()
        val professional = jobOfferData.professional
        professional?.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

        jobOfferRepository.save(jobOfferData)
        professional?.let { professionalRepository.save(it) }
    }


    @Transactional
    override fun changeJobOfferStatus(
        jobOfferId: Long,
        nextStatus: JobStatusEnum,
        professionalsId: List<Long>?,
        note: String?
    ): JobOfferDTO {
        val oldJobOfferOptional = jobOfferRepository.findById(jobOfferId)

        if (!oldJobOfferOptional.isPresent) {
            throw NoSuchElementException(ErrorsPage.NO_SUCH_JOBOFFER)
        }

        val oldJobOffer = oldJobOfferOptional.get()
        val oldStatus = oldJobOffer.status

        if (oldJobOffer.deleted) {
            throw NotFoundJobOfferException(ErrorsPage.JOB_OFFER_NOT_FOUND_ERROR)
        }

        var updateProfessionalsId = false

        // controlla che se avviene un aggiornamento della lista candidati, il check sullo stato non sia un problema
        if (professionalsId != null) {         //&& oldJobOffer.candidateProfessionals.none { professionalsId.contains(it.id) }) {
            val listId = oldJobOffer.candidateProfessionals.map { it.id }
            updateProfessionalsId =
                listId.size != professionalsId.size || !listId.containsAll(professionalsId) || !professionalsId.containsAll(
                    listId
                )
        }

        // controlla che se è stato rimosso un candidato dalla lista dei candidati proposti durante la CANDIDATE_PROPOSAL
        if (oldStatus == JobStatusEnum.CANDIDATE_PROPOSAL && professionalsId != null) {
            val listId = oldJobOffer.candidatesProposalProfessional
            updateProfessionalsId =
                listId.size != professionalsId.size || !listId.containsAll(professionalsId) || !professionalsId.containsAll(
                    listId
                )
        }

        if (!updateProfessionalsId && !checkStatusTransition(oldJobOffer.status, nextStatus)) {
            throw IllegalJobStatusTransition(ErrorsPage.INVALID_STATUS_TRANSITION)
        }

        if (statusRequiresProfessionalId(nextStatus) && professionalsId.isNullOrEmpty()) {
            throw RequiredProfessionalIdException(ErrorsPage.REQUIRED_PROFESSIONAL_ID)
        }

        when (nextStatus) {
            JobStatusEnum.SELECTION_PHASE -> {
                oldJobOffer.status = nextStatus

                // Rimozione dei precedenti candidate professional per l'aggiornamento con quelli nuovi
                oldJobOffer.candidateProfessionals.forEach { candidateProfessional ->
                    candidateProfessional.jobOffers.remove(oldJobOffer)
                }

                oldJobOffer.candidateProfessionals = professionalsId!!.map {
                    val professionalOptional: Optional<Professional>
                    var professional: Professional? = null

                    professionalOptional = professionalRepository.findById(it)
                    try {
                        professional = professionalOptional.get()

                        if (professional!!.deleted) {
                            throw ProfessionalNotFoundException("Professional not found")
                        }

                        professional!!.jobOffers.add(oldJobOffer)


                    } catch (e: Exception) {
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!
                }.toMutableList()
                oldJobOffer.professional?.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK

                if (!oldJobOffer.candidateProfessionals.contains(oldJobOffer.professional)) {
                    oldJobOffer.professional?.jobOffers?.remove(oldJobOffer)
                }

                if (oldStatus == JobStatusEnum.CONSOLIDATED) {
                    oldJobOffer.candidatesProposalProfessional = mutableListOf()

                    oldJobOffer.professional?.let { professional ->
                        val professionalId = professional.id
                        if (!oldJobOffer.candidatesProfessionalRejected.contains(professionalId)) {
                            oldJobOffer.candidatesProfessionalRevoked.add(professionalId)
                        }
                    }
                }

                oldJobOffer.professional = null
                oldJobOffer.value = 0.0
                oldJobOffer.oldStatus = JobStatusEnum.CREATED
                if (note != null) oldJobOffer.note = note
            }

            JobStatusEnum.CANDIDATE_PROPOSAL -> {
                oldJobOffer.status = nextStatus

                // Rimozione dei precedenti candidate professional per l'aggiornamento con quelli nuovi
                oldJobOffer.candidatesProposalProfessional.forEach { candidateProfessional ->
                    val professionalProposal = professionalRepository.findById(candidateProfessional).getOrNull()
                    if (professionalProposal != null) {
                        // se il professional non è all'interno della lista inviata allora è stato eliminato, quindi rejected
                        if (professionalsId?.contains(professionalProposal.id) == false &&
                            !oldJobOffer.candidatesProfessionalRejected.contains(professionalProposal.id)
                        ) {
                            oldJobOffer.candidatesProfessionalRejected.add(professionalProposal.id)
                        } else {
                            professionalProposal.jobOffers.remove(oldJobOffer)
                        }

                        //oldJobOffer.candidateProfessionals.remove(professionalProposal)
                        professionalRepository.save(professionalProposal)
                    }
                }

                var professional: Professional? = null

                professionalsId?.forEach { p ->
                    val professionalOptional: Optional<Professional> = professionalRepository.findById(p)
                    professional = professionalOptional.get()

                    if (professional!!.deleted) {
                        throw ProfessionalNotFoundException("Professional ${professional?.information?.surname} ${professional?.information?.name} not found!")
                    }

                    if (professional!!.employmentState == EmploymentStateEnum.EMPLOYED || professional!!.employmentState == EmploymentStateEnum.NOT_AVAILABLE) {
                        throw NotAvailableProfessionalException("This professional (${professional?.information?.surname} ${professional?.information?.name}) cannot start a job now!")
                    }

                    if (!updateProfessionalsId && oldJobOffer.candidateProfessionals.find { it.id == professional!!.id } == null) {
                        throw InconsistentProfessionalStatusTransitionException("This professional (${professional?.information?.surname} ${professional?.information?.name}) was not in the list of candidates!")
                    }

                    professional!!.jobOffers.add(oldJobOffer)
                    professionalRepository.save(professional!!)
                }

                //oldJobOffer.professional = professional!!
                //oldJobOffer.value = oldJobOffer.duration * professional!!.dailyRate * profitMargin
                oldJobOffer.candidatesProposalProfessional = professionalsId?.toMutableList() ?: mutableListOf()

                if (note != null) oldJobOffer.note = note
                oldJobOffer.oldStatus = JobStatusEnum.SELECTION_PHASE

                if (oldJobOffer.candidatesProposalProfessional.isEmpty()) {
                    oldJobOffer.oldStatus = JobStatusEnum.CREATED
                    oldJobOffer.status = JobStatusEnum.SELECTION_PHASE
                }
            }

            JobStatusEnum.CONSOLIDATED -> {
                oldJobOffer.status = nextStatus

                if (professionalsId!!.size > 1) {
                    throw InconsistentProfessionalStatusTransitionException("Only one professional can be consolidated")
                }
                var professional: Professional? = null

                val professionalOptional: Optional<Professional> = professionalRepository.findById(professionalsId[0])
                try {
                    professional = professionalOptional.get()
                    if (professional!!.deleted) {
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    if (professional!!.employmentState == EmploymentStateEnum.EMPLOYED || professional!!.employmentState == EmploymentStateEnum.NOT_AVAILABLE) {
                        throw NotAvailableProfessionalException("This professional cannot start a job now")
                    }

                    if (!oldJobOffer.candidatesProposalProfessional.contains(professionalsId[0]) || oldJobOffer.candidateProfessionals.none { it.id == professionalsId[0] }) {
                        throw InconsistentProfessionalStatusTransitionException("This professional is not the one that passed the candidate proposal step")
                    }

                    // se viene scelto per questa job offer, in tutte le altre in pending non viene più selezionato
                    professional?.jobOffers?.forEach {
                        if (it.id != oldJobOffer.id && it.professional?.id == professional?.id) {
                            it.oldStatus = JobStatusEnum.CREATED
                            it.status = JobStatusEnum.SELECTION_PHASE
                            it.professional = null
                            it.value = 0.0

                            if (it.candidateProfessionals.isEmpty()) {
                                it.oldStatus = JobStatusEnum.CREATED
                                it.status = JobStatusEnum.CREATED
                            }
                        }
                    }

                    oldJobOffer.professional = professional!!
                    oldJobOffer.value = oldJobOffer.duration * professional!!.dailyRate * profitMargin
                    oldJobOffer.candidatesProposalProfessional.remove(professional?.id)

                    professional!!.jobOffers.add(oldJobOffer)
                    professionalRepository.save(professional!!)
                } catch (e: Exception) {
                    throw ProfessionalNotFoundException("Professional not found")
                }

                oldJobOffer.professional!!.employmentState = EmploymentStateEnum.EMPLOYED
                oldJobOffer.professional!!.jobOffers.add(oldJobOffer)
                if (note != null) oldJobOffer.note = note
                oldJobOffer.oldStatus = JobStatusEnum.CANDIDATE_PROPOSAL
            }

            JobStatusEnum.DONE -> {
                oldJobOffer.status = nextStatus

                if (professionalsId!!.size > 1) {
                    throw InconsistentProfessionalStatusTransitionException("Only one professional can be consolidated")
                }
                var professional: Professional? = null

                val professionalOptional: Optional<Professional> = professionalRepository.findById(professionalsId[0])
                try {
                    professional = professionalOptional.get()
                    if (professional!!.deleted) {
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!.jobOffers.add(oldJobOffer)

                    professionalRepository.save(professional!!)
                } catch (e: Exception) {
                    throw ProfessionalNotFoundException("Professional not found")
                }

                if (professional!!.employmentState == EmploymentStateEnum.UNEMPLOYED || professional!!.employmentState == EmploymentStateEnum.AVAILABLE_FOR_WORK) {
                    throw Exception("Professional was consolidated but its employment state was wrong")
                }

                if (professionalsId[0] != oldJobOffer.professional!!.id) {
                    throw InconsistentProfessionalStatusTransitionException("This professional is not the one that was consolidated")
                }

                oldJobOffer.professional!!.employmentState = EmploymentStateEnum.UNEMPLOYED
                oldJobOffer.professional?.jobOffers?.remove(oldJobOffer)
                oldJobOffer.professional?.let { professionalRepository.save(it) }
                //oldJobOffer.professional = null
                //oldJobOffer.value = 0.0
                oldJobOffer.endTime = LocalDateTime.now()
                if (note != null) oldJobOffer.note = note
                oldJobOffer.oldStatus = JobStatusEnum.CONSOLIDATED
            }

            JobStatusEnum.ABORT -> {
                oldJobOffer.status = nextStatus
                oldJobOffer.professional?.employmentState = EmploymentStateEnum.UNEMPLOYED
                oldJobOffer.professional?.let { professionalRepository.save(it) }
                oldJobOffer.oldStatus = oldStatus
                oldJobOffer.endTime = LocalDateTime.now()
                if (note != null) oldJobOffer.note = note
            }

            else -> throw IllegalJobStatusTransition("Cannot enter Create status, illegal!")
        }

        oldJobOffer.professional?.let { professionalRepository.save(it) }
        oldJobOffer.candidateProfessionals.forEach { professionalRepository.save(it) }
        val newJobOffer = jobOfferRepository.save(oldJobOffer)

        return newJobOffer.toDTO()
    }

    override fun getJobOfferById(jobOfferId: Long): JobOfferDTO? {
        val jobOffer = jobOfferRepository.findById(jobOfferId)

        return if (jobOffer.isPresent) {
            val getJobOffer = jobOffer.get()

            //if (getJobOffer.professional != null && !getJobOffer.deleted) {
            if (!getJobOffer.deleted) {
                getJobOffer.toDTO()
            } else {
                null
            }
        } else {
            throw NotFoundJobOfferException(ErrorsPage.JOB_OFFER_NOT_FOUND_ERROR)
        }

    }
}