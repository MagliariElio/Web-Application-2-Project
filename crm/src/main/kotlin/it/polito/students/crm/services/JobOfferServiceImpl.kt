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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

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
        jobStatusGroup: JobStatusGroupEnum?
    ): PageImpl<JobOfferDTO> {

        val pageable = PageRequest.of(page, limit)
        val pageJobOffers: Page<JobOffer> = jobOfferRepository.findAll(pageable)
        var list = pageJobOffers.content.filter { !it.deleted }

        if (customerId != null) {
            list = list.filter { it.customer.id == customerId }
        }
        if (professionalId != null) {
            list = list.filter { it.professional != null && it.professional!!.id == professionalId }
        }
        if (jobStatusGroup != null) {
            list = when (jobStatusGroup) {
                JobStatusGroupEnum.OPEN -> list.filter {
                    arrayOf(
                        JobStatusEnum.CREATED,
                        JobStatusEnum.SELECTION_PHASE,
                        JobStatusEnum.CANDIDATE_PROPOSAL
                    ).contains(it.status)
                }

                JobStatusGroupEnum.ACCEPTED -> list.filter {
                    arrayOf(
                        JobStatusEnum.CONSOLIDATED,
                        JobStatusEnum.DONE
                    ).contains(it.status)
                }

                JobStatusGroupEnum.ABORTED -> list.filter { it.status == JobStatusEnum.ABORT }
            }
        }

        val dtoList = list.map { it.toDTO() }

        val pageImpl = PageImpl(dtoList, pageable, pageJobOffers.totalElements)
        return pageImpl
    }

    override fun storeJobOffer(jobOfferDto: CreateJobOfferDTO): JobOfferDTO {
        val customer = customerService.getCustomer(jobOfferDto.customerId).toEntity(factory)

        val jobOffer = JobOffer().apply {
            name = jobOfferDto.name
            description = jobOfferDto.description
            contractType = jobOfferDto.contractType
            location = jobOfferDto.location
            workMode = jobOfferDto.workMode
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

    @Transactional
    override fun deleteJobOffer(jobOfferId: Long) {
        val jobOffer = jobOfferRepository.findById(jobOfferId)

        if (!jobOffer.isPresent || jobOffer.get().deleted) {
            throw NoSuchElementException()
        }

        val jobOfferData = jobOffer.get()

        jobOfferData.deleted = true
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

        if (oldJobOffer.deleted){
            throw NotFoundJobOfferException(ErrorsPage.JOB_OFFER_NOT_FOUND_ERROR)
        }

        if (!checkStatusTransition(oldJobOffer.status, nextStatus)) {
            throw IllegalJobStatusTransition(ErrorsPage.INVALID_STATUS_TRANSITION)
        }

        if (statusRequiresProfessionalId(nextStatus) && professionalsId.isNullOrEmpty()) {
            throw RequiredProfessionalIdException(ErrorsPage.REQUIRED_PROFESSIONAL_ID)
        }


        when (nextStatus) {
            JobStatusEnum.SELECTION_PHASE -> {
                oldJobOffer.status = nextStatus

                oldJobOffer.candidateProfessionals = professionalsId!!.map {
                    val professionalOptional: Optional<Professional>
                    var professional: Professional? = null

                    professionalOptional = professionalRepository.findById(it)
                    try{
                        professional = professionalOptional.get()

                        if (professional!!.deleted){
                            throw ProfessionalNotFoundException("Professional not found")
                        }

                        professional!!.jobOffers.add(oldJobOffer)


                    } catch (e: Exception){
                        println(e.message)
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!
                }.toMutableList()
                oldJobOffer.professional?.employmentState = EmploymentStateEnum.AVAILABLE_FOR_WORK
                oldJobOffer.professional?.jobOffers?.remove(oldJobOffer)
                oldJobOffer.professional = null
                oldJobOffer.value = 0.0
                if (note != null) oldJobOffer.note = note
            }

            JobStatusEnum.CANDIDATE_PROPOSAL -> {
                oldJobOffer.status = nextStatus

                if (professionalsId!!.size > 1){
                    throw InconsistentProfessionalStatusTransitionException("Only one professional can be the final proposal")
                }
                var professional: Professional? = null

                val professionalOptional: Optional<Professional> = professionalRepository.findById(professionalsId[0])
                try{
                    professional = professionalOptional.get()
                    if (professional!!.deleted){
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!.jobOffers.add(oldJobOffer)

                    professionalRepository.save(professional!!)
                } catch (e: Exception){
                    throw ProfessionalNotFoundException("Professional not found")
                }

                if (professional!!.employmentState == EmploymentStateEnum.EMPLOYED || professional!!.employmentState == EmploymentStateEnum.NOT_AVAILABLE) {
                    throw NotAvailableProfessionalException("This professional cannot start a job now")
                }
                if (oldJobOffer.candidateProfessionals.find { it.id == professional!!.id } == null){
                    throw InconsistentProfessionalStatusTransitionException("This professional was not in the list of candidates")
                }

                oldJobOffer.professional = professional!!
                oldJobOffer.value = oldJobOffer.duration * professional!!.dailyRate * profitMargin
                if (note != null) oldJobOffer.note = note
            }

            JobStatusEnum.CONSOLIDATED -> {
                oldJobOffer.status = nextStatus

                if (professionalsId!!.size > 1){
                    throw InconsistentProfessionalStatusTransitionException("Only one professional can be consolidated")
                }
                var professional: Professional? = null

                val professionalOptional: Optional<Professional> = professionalRepository.findById(professionalsId[0])
                try{
                    professional = professionalOptional.get()
                    if (professional!!.deleted){
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!.jobOffers.add(oldJobOffer)

                    professionalRepository.save(professional!!)
                } catch (e: Exception){
                    throw ProfessionalNotFoundException("Professional not found")
                }

                if (professional!!.employmentState == EmploymentStateEnum.EMPLOYED || professional!!.employmentState == EmploymentStateEnum.NOT_AVAILABLE) {
                    throw NotAvailableProfessionalException("This professional cannot start a job now")
                }

                if (professionalsId[0] != oldJobOffer.professional!!.id) {
                    throw InconsistentProfessionalStatusTransitionException("This professional is not the one that passed the candidate proposal step")
                }
                oldJobOffer.professional!!.employmentState = EmploymentStateEnum.EMPLOYED
                oldJobOffer.professional!!.jobOffers.add(oldJobOffer)
                if (note != null) oldJobOffer.note = note
            }

            JobStatusEnum.DONE -> {
                oldJobOffer.status = nextStatus

                if (professionalsId!!.size > 1){
                    throw InconsistentProfessionalStatusTransitionException("Only one professional can be consolidated")
                }
                var professional: Professional? = null

                val professionalOptional: Optional<Professional> = professionalRepository.findById(professionalsId[0])
                try{
                    professional = professionalOptional.get()
                    if (professional!!.deleted){
                        throw ProfessionalNotFoundException("Professional not found")
                    }

                    professional!!.jobOffers.add(oldJobOffer)

                    professionalRepository.save(professional!!)
                } catch (e: Exception){
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
                oldJobOffer.professional = null
                oldJobOffer.value = 0.0
                if (note != null) oldJobOffer.note = note
            }

            JobStatusEnum.ABORT -> {
                oldJobOffer.status = nextStatus
                oldJobOffer.professional?.employmentState = EmploymentStateEnum.UNEMPLOYED
                oldJobOffer.professional?.let { professionalRepository.save(it) }
                if (note != null) oldJobOffer.note = note
            }

            else -> throw IllegalJobStatusTransition("Cannot enter Create status, illegal!")
        }

        oldJobOffer.professional?.let { professionalRepository.save(it) }
        oldJobOffer.candidateProfessionals.forEach{professionalRepository.save(it)}
        oldJobOffer.oldStatus = oldStatus
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