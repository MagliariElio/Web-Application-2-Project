package it.polito.students.crm.controllers

import it.polito.students.crm.dtos.*
import it.polito.students.crm.exception_handlers.*
import it.polito.students.crm.services.JobOfferService
import it.polito.students.crm.services.KafkaProducerService
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.JobStatusEnum
import it.polito.students.crm.utils.JobStatusGroupEnum
import it.polito.students.crm.utils.KafkaTopics
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/API/joboffers")
class CrmJobOffersController(
    private val jobOfferService: JobOfferService,
    private val kafkaProducer: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(CrmContactsController::class.java)
    private val formatter = DateTimeFormatter.ofPattern("MMMMyyyy", Locale.ENGLISH)

    @GetMapping("", "/")
    fun getAllJobOffers(
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "30", required = false) limit: Int,
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) professionalId: Long?,
        @RequestParam(required = false) jobStatusGroup: String?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDirection: String? = "asc",
        @RequestParam(required = false) contractType: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) workMode: String?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Any> {
        try {
            val errorsList: MutableList<String> = mutableListOf()
            if (page < 0 || limit < 1) {
                errorsList.add(ErrorsPage.PAGE_AND_LIMIT_ERROR)
            }
            if ((customerId != null && customerId < 0) || (professionalId != null && professionalId < 0)) {
                errorsList.add(ErrorsPage.CUSTOMERID_PROFESSIONALID_INVALID)
            }
            if (jobStatusGroup != null) {
                try {
                    JobStatusGroupEnum.valueOf(jobStatusGroup.uppercase())
                } catch (e: IllegalArgumentException) {
                    errorsList.add(ErrorsPage.JOBSTATUSGROUP_INVALID)
                }
            }
            if (sortBy != null && sortBy !in listOf("duration", "value")) {
                errorsList.add(ErrorsPage.SORT_BY_JOB_OFFER_INVALID)
            }
            if (sortDirection != null && sortDirection !in listOf("asc", "desc")) {
                errorsList.add(ErrorsPage.SORT_DIRECTION_JOB_OFFER_INVALID)
            }

            val jobStatusEnum = status?.let {
                try {
                    JobStatusEnum.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    errorsList.add(ErrorsPage.STATUS_INVALID)
                    null
                }
            }

            if (errorsList.isNotEmpty()) {
                return ResponseEntity(mapOf("errors" to errorsList), HttpStatus.BAD_REQUEST)
            }

            val pageImplDto = jobOfferService.getAllJobOffers(
                page,
                limit,
                customerId,
                professionalId,
                jobStatusGroup?.let { JobStatusGroupEnum.valueOf(it.uppercase()) },
                sortBy,
                sortDirection,
                contractType,
                location,
                workMode,
                jobStatusEnum
            )

            val mapAnswer: Map<String, Any?> = mapOf(
                "content" to pageImplDto.content,
                "currentPage" to pageImplDto.number,
                "elementPerPage" to pageImplDto.size,
                "totalPages" to pageImplDto.totalPages,
                "totalElements" to pageImplDto.totalElements
            )

            return ResponseEntity(mapAnswer, HttpStatus.OK)
        } catch (e: Exception) {
            logger.info("Error: Internal server error, ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("", "/")
    fun storeJobOffer(@Valid @RequestBody jobOffer: CreateJobOfferDTO): ResponseEntity<out Any> {
        try {
            if (jobOffer.name.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_NAME_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.description.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_DESCRIPTION_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.location.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_LOCATION_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.workMode.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_WORK_MODE_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.contractType.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_CONTRACT_TYPE_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.requiredSkills.any { it.isBlank() }) {
                return ResponseEntity(ErrorsPage.REQUIRED_SKILLS_EMPTY_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.customerId < 0) {
                return ResponseEntity(ErrorsPage.CUSTOMER_ID_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.duration < 0) {
                return ResponseEntity(ErrorsPage.NEGATIVE_DURATION_ERROR, HttpStatus.BAD_REQUEST)
            }

            val saved = jobOfferService.storeJobOffer(jobOffer)
            kafkaProducer.sendJobOffer(KafkaTopics.TOPIC_JOB_OFFER, JobOfferAnalyticsDTO(null, saved.status, LocalDate.now().format(formatter).lowercase(), saved.creationTime, saved.endTime))
            kafkaProducer.sendJobOfferCtWm(KafkaTopics.TOPIC_JOB_OFFER_CT_WM, AnalyticsJobOfferDTO(jobOffer.contractType, jobOffer.workMode))
            return ResponseEntity(saved, HttpStatus.CREATED)
        } catch (e: CustomerNotFoundException) {
            logger.info("CustomerControl: Error with customer: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error saving a new job offer: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PatchMapping("", "/")
    fun updateJobOffer(@Valid @RequestBody jobOffer: JobOfferDTO): ResponseEntity<out Any> {
        try {
            if (jobOffer.name.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_NAME_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.description.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_DESCRIPTION_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.location.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_LOCATION_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.workMode.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_WORK_MODE_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.contractType.isBlank()) {
                return ResponseEntity(ErrorsPage.EMPTY_CONTRACT_TYPE_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.requiredSkills.any { it.isBlank() }) {
                return ResponseEntity(ErrorsPage.REQUIRED_SKILLS_EMPTY_ERROR, HttpStatus.BAD_REQUEST)
            }

            if (jobOffer.duration < 0) {
                return ResponseEntity(ErrorsPage.NEGATIVE_DURATION_ERROR, HttpStatus.BAD_REQUEST)
            }

            val saved = jobOfferService.updateJobOffer(jobOffer)
            return ResponseEntity(saved, HttpStatus.CREATED)
        } catch (e: CustomerNotFoundException) {
            logger.info("CustomerControl: Error with customer: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error saving a new job offer: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/{jobOfferId}")
    fun deleteJobOffer(
        @PathVariable jobOfferId: Long
    ): ResponseEntity<Any> {
        try {
            if (jobOfferId < 0) {
                logger.info("Error! Delete joboffer with negative Id")
                return ResponseEntity(ErrorsPage.ID_ERROR, HttpStatus.BAD_REQUEST)
            }

            jobOfferService.deleteJobOffer(jobOfferId)

            return ResponseEntity("Job offer $jobOfferId correctly deleted!", HttpStatus.OK)

        } catch (e: NoSuchElementException) {
            logger.info("Error! Cannot delete the jobOffer $jobOfferId because there is no such jobOffer in the db")
            return ResponseEntity(mapOf("error" to ErrorsPage.NO_SUCH_JOBOFFER), HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Internal server error during delete of job offer $jobOfferId")
            return ResponseEntity(ErrorsPage.INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @PatchMapping("/{jobOfferId}")
    fun changeJobOfferStatus(
        @PathVariable jobOfferId: Long,
        @RequestBody changeJobStatusDTO: ChangeJobStatusDTO
    ): ResponseEntity<Any> {

        val nextStatus = changeJobStatusDTO.nextStatus
        val professionalsId = changeJobStatusDTO.professionalsId
        val note = changeJobStatusDTO.note

        try {
            val errors = mutableListOf<String>()

            if (jobOfferId < 0) {
                logger.info("Error! Change joboffer status with negative jobOfferId")
                errors.add("Job offer id: " + ErrorsPage.ID_ERROR)
            }

            var nextStatusEnum: JobStatusEnum? = null

            try {
                nextStatusEnum = JobStatusEnum.valueOf(nextStatus.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.info("Error! Change joboffer status with status not valid! ${e.message}")
                errors.add(ErrorsPage.JOBOFFERSTATUS_INVALID)
            }

            if (!professionalsId.isNullOrEmpty() && professionalsId.all { it < 0 }) {
                logger.info("Error! Change joboffer status with negative professionalId")
                errors.add("Professional id: " + ErrorsPage.ID_ERROR)
            }

            if (errors.isNotEmpty()) {
                return ResponseEntity(mapOf("errors" to errors), HttpStatus.BAD_REQUEST)
            }

            val oldjobOffer = jobOfferService.getJobOfferById(jobOfferId)
            val editedJobOffer = jobOfferService.changeJobOfferStatus(jobOfferId, nextStatusEnum!!, professionalsId, note)
            kafkaProducer.sendJobOffer(KafkaTopics.TOPIC_JOB_OFFER, JobOfferAnalyticsDTO(oldjobOffer!!.status, editedJobOffer.status, LocalDate.now().format(formatter).lowercase(), editedJobOffer.creationTime, editedJobOffer.endTime))

            return ResponseEntity(editedJobOffer, HttpStatus.OK)
        } catch (e: IllegalJobStatusTransition) {
            logger.info("Problem during jobOffer status editing: invalid status transition")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: RequiredProfessionalIdException) {
            logger.info("Problem during jobOffer status editing: missing professionalId")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: ProfessionalNotFoundException) {
            logger.info("Problem during jobOffer status change. Professional not found")
            return ResponseEntity(mapOf("error" to ErrorsPage.PROFESSIONAL_ID_ERROR), HttpStatus.NOT_FOUND)
        } catch (e: NotAvailableProfessionalException) {
            logger.info("Problem during jobOffer status editing: professional not ready for a job")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: InconsistentProfessionalStatusTransitionException) {
            logger.info("Problem during jobOffer status editing: professional changed between steps")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: NoSuchElementException) {
            logger.info("Problem during jobOffer status editing: ${e.message}")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Problem during jobOffer status editing: ${e.message}")
            return ResponseEntity(
                mapOf("error" to ErrorsPage.INTERNAL_SERVER_ERROR_MESSAGE),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

    }

    @GetMapping("/{jobOfferId}/value", "/{jobOfferId}/value/")
    fun getJobOfferById(
        @PathVariable jobOfferId: Long,
    ): ResponseEntity<Any> {
        if (jobOfferId < 0) {
            logger.info(ErrorsPage.JOB_OFFER_ID_ERROR)
            return ResponseEntity(ErrorsPage.JOB_OFFER_ID_ERROR, HttpStatus.BAD_REQUEST)
        }

        try {
            val jobOffer = jobOfferService.getJobOfferById(jobOfferId)
            return ResponseEntity(jobOffer, HttpStatus.OK)
        } catch (e: NotFoundJobOfferException) {
            logger.info("mapOf(\"error\" to \"No such a job offer with id = $jobOfferId or value not computable\"),\n")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error: Internal server error, ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/generate", "/generate/")
    fun getGenerateJobOffer(
        @RequestBody prompt: String,
    ): ResponseEntity<Any> {
        if (prompt.isEmpty()) {
            logger.info(ErrorsPage.EMPTY_PROMPT_ERROR)
            return ResponseEntity(ErrorsPage.EMPTY_PROMPT_ERROR, HttpStatus.BAD_REQUEST)
        }

        try {
            val jobOffer = jobOfferService.getGenerateJobOffer(prompt)
            return ResponseEntity(jobOffer, HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            logger.info("Problem during job offer generation: dangerous description (${e.message})")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Error: Internal server error, ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/skills/generate", "/skills/generate/")
    fun getGenerateSkillsJobOffer(
        @RequestBody prompt: String,
    ): ResponseEntity<Any> {
        if (prompt.isEmpty()) {
            logger.info(ErrorsPage.EMPTY_PROMPT_ERROR)
            return ResponseEntity(ErrorsPage.EMPTY_PROMPT_ERROR, HttpStatus.BAD_REQUEST)
        }

        try {
            val jobOffer = jobOfferService.getGenerateSkills(prompt)
            return ResponseEntity(jobOffer, HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            logger.info("Problem during skills generation: dangerous description (${e.message})")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Error: Internal server error, ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}