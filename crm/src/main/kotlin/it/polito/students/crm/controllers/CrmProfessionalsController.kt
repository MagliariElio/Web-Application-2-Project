package it.polito.students.crm.controllers

import it.polito.students.crm.dtos.CreateAddressDTO
import it.polito.students.crm.dtos.CreateProfessionalDTO
import it.polito.students.crm.dtos.ProfessionalAnalyticsDTO
import it.polito.students.crm.dtos.UpdateProfessionalDTO
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.ProfessionalNotFoundException
import it.polito.students.crm.services.ContactService
import it.polito.students.crm.services.KafkaProducerService
import it.polito.students.crm.services.ProfessionalService
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.INTERNAL_SERVER_ERROR_MESSAGE
import it.polito.students.crm.utils.ErrorsPage.Companion.PROFESSIONAL_DELETED_SUCCESSFULLY
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.regex.Pattern

@RestController
@RequestMapping("/API/professionals")
class CrmProfessionalsController(
    private val professionalService: ProfessionalService,
    private val contactService: ContactService,
    private val kafkaProducer: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(CrmProfessionalsController::class.java)

    /**
     * This endpoint handles a GET request to retrieve all professionals.
     *
     * @param pageNumber the desired page number for pagination (default value: 0).
     * @param pageSize the number of items per page (default value: 10).
     * @param skill the filter criteria for contact search by skill.
     * @param location the filter criteria for contact search by location.
     * @param employmentState the filter criteria for contact search by employment state.
     * @return a ResponseEntity containing a list of ProfessionalDTO objects representing the professionals, along with an HTTP status code.
     * @exception IllegalArgumentException if pageNumber or pageSize is negative.
     */
    @GetMapping("/", "")
    fun getAllProfessionals(
        @RequestParam(defaultValue = "0", required = false) pageNumber: Int,
        @RequestParam(defaultValue = "10", required = false) pageSize: Int,
        @RequestParam(required = false) skill: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) employmentState: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?
    ): ResponseEntity<Any> {
        if (pageNumber < 0 || pageSize < 0) {
            return ResponseEntity("The page number and the page size cannot be negative!", HttpStatus.BAD_REQUEST)
        }

        try {
            val filter = HashMap<ProfessionalEnumFields, String>().apply {
                if (!skill.isNullOrBlank()) {
                    put(ProfessionalEnumFields.SKILL, skill)
                }
                if (!location.isNullOrBlank()) {
                    put(ProfessionalEnumFields.LOCATION, location)
                }
                if (!employmentState.isNullOrBlank()) {
                    put(ProfessionalEnumFields.EMPLOYMENT_STATE, employmentState)
                }
                if (!name.isNullOrBlank()) {
                    put(ProfessionalEnumFields.NAME, name)
                }
                if (!surname.isNullOrBlank()) {
                    put(ProfessionalEnumFields.SURNAME, surname)
                }
            }

            val pageImplDto = professionalService.getAllProfessionals(pageNumber, pageSize, filter)

            val mapAnswer: Map<String, Any?> = mapOf(
                "content" to pageImplDto.content,
                "currentPage" to pageImplDto.number,
                "elementPerPage" to pageImplDto.size,
                "totalPages" to pageImplDto.totalPages,
                "totalElements" to pageImplDto.totalElements
            )

            return ResponseEntity(mapAnswer, HttpStatus.OK)
        } catch (e: Exception) {
            logger.info("Error while retrieving all professionals")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * This endpoint handles a GET request to retrieve a specific professional.
     *
     * @param professionalID the desired professional id
     * @return a ResponseEntity containing a ProfessionalDTO objects representing the professional, along with an HTTP status code.
     * @exception IllegalArgumentException if professional ID is negative.
     */
    @GetMapping("/{professionalID}", "/{professionalID}/")
    fun getProfessional(@PathVariable professionalID: Long): ResponseEntity<out Any> {
        if (professionalID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to ErrorsPage.PROFESSIONAL_ID_ERROR))
        }

        try {
            val professional = professionalService.getProfessional(professionalID)

            //return an object containing the contact and all the lists of the associated emails, telephones and addresses
            val returnedObj = professional

            return ResponseEntity(returnedObj, HttpStatus.OK)
        } catch (e: ContactNotFoundException) {
            logger.info("Error with professional id equal to ${professionalID}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error with professional id equal to ${professionalID}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/", "")
    fun storeProfessional(@RequestBody professional: CreateProfessionalDTO): ResponseEntity<out Any> {
        try {
            if (professional.information.name.isBlank() || professional.information.surname.isBlank()) {
                throw BadRequestException(ErrorsPage.NAME_SURNAME_ERROR)
            }

            if ((professional.information.ssnCode != null && professional.information.ssnCode!!.isBlank())) {
                throw BadRequestException(ErrorsPage.SSN_CODE_ERROR)
            }

            //Check that the contact is a professional
            if (professional.information.category != null && professional.information.category!!.isNotBlank()) {
                checkCategoryIsValid(professional.information.category!!.uppercase())
                val category = CategoryOptions.valueOf(professional.information.category!!.uppercase())
                if (category != CategoryOptions.PROFESSIONAL) {
                    throw IllegalArgumentException(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR)
                }
            } else {
                throw BadRequestException(ErrorsPage.CATEGORY_ERROR)
            }
            //Check validity List of emails, telephones and addresses
            if (professional.information.emails != null && professional.information.emails!!.isNotEmpty()) {
                professional.information.emails?.forEach {
                    if (!isValidEmail(it.email)) {

                        throw BadRequestException(ErrorsPage.EMAILS_NOT_VALID)
                    }
                }
            }
            if (professional.information.addresses != null && professional.information.addresses!!.isNotEmpty()) {
                professional.information.addresses?.forEach {
                    if (!isValidAddress(it)) {
                        throw BadRequestException(ErrorsPage.ADDRESSES_NOT_VALID)
                    }
                }
            }
            if (professional.information.telephones != null && professional.information.telephones!!.isNotEmpty()) {
                //If a telephone number is not valid throws an exception
                professional.information.telephones?.forEach {
                    if (!isValidPhone(it.telephone)) {
                        throw BadRequestException(ErrorsPage.TELEPHONES_NOT_VALID)
                    }
                }
            }

            //Check dailyrate
            if (professional.dailyRate < 0) {
                throw BadRequestException(ErrorsPage.DAILYRATE_ERROR)
            }

            //Check geographical location
            if (professional.geographicalLocation.isBlank()) {
                throw BadRequestException(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR)
            }

            //check skills list
            if (professional.skills != null && professional.skills.isNotEmpty()) {
                professional.skills.forEach {
                    if (it.isBlank()) {
                        throw BadRequestException(ErrorsPage.SKILLS_ERROR)
                    }
                }
            }

            val savedProfessional =
                professionalService.storeProfessional(professional, EmploymentStateEnum.AVAILABLE_FOR_WORK)
            kafkaProducer.sendProfessional(KafkaTopics.TOPIC_PROFESSIONAL, ProfessionalAnalyticsDTO(null, savedProfessional.employmentState))
            return ResponseEntity(savedProfessional, HttpStatus.CREATED)
        } catch (e: BadRequestException) {
            logger.error("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: IllegalArgumentException) {
            logger.error("Error in category or employment state: ${e.javaClass}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("Error saving a new professional: ${e}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PatchMapping("/{professionalID}", "/{professionalID}/")
    fun updateProfessional(
        @PathVariable professionalID: Long,
        @RequestBody @Valid professionalDto: UpdateProfessionalDTO
    ): ResponseEntity<out Any> {
        try {
            if (professionalID < 0) {
                throw BadRequestException(ErrorsPage.ID_ERROR)
            }

            if ((professionalDto.information.name != null && professionalDto.information.name!!.isBlank()) || (professionalDto.information.surname != null && professionalDto.information.surname!!.isBlank())) {
                throw BadRequestException(ErrorsPage.NAME_SURNAME_ERROR)
            }

            if ((professionalDto.information.ssnCode != null && professionalDto.information.ssnCode!!.isBlank())) {
                throw BadRequestException(ErrorsPage.SSN_CODE_ERROR)
            }


            //Check that the contact is a professional
            if (professionalDto.information.category != null && professionalDto.information.category!!.isNotBlank()) {
                checkCategoryIsValid(professionalDto.information.category!!.uppercase())
                val category = CategoryOptions.valueOf(professionalDto.information.category!!.uppercase())
                if (category != CategoryOptions.PROFESSIONAL) {
                    throw IllegalArgumentException(ErrorsPage.CATEGORY_PROFESSIONAL_ERROR)
                }
            } else {
                throw BadRequestException(ErrorsPage.CATEGORY_ERROR)
            }
            val category = CategoryOptions.valueOf(professionalDto.information.category!!.uppercase())

            //Check employment state
            if (professionalDto.employmentState != null && professionalDto.employmentState.isNotBlank()) {
                checkEmploymentStateIsValid(professionalDto.employmentState.uppercase())
            } else {
                throw BadRequestException(ErrorsPage.EMPLYMENT_STATE_ERROR)
            }

            //Check dailyrate
            if (professionalDto.dailyRate < 0) {
                throw BadRequestException(ErrorsPage.DAILYRATE_ERROR)
            }

            //Check geographical location
            if (professionalDto.geographicalLocation.isBlank()) {
                throw BadRequestException(ErrorsPage.GEOGRAPHICAL_LOCATION_ERROR)
            }
            //check skills list
            if (professionalDto.skills != null && professionalDto.skills.isNotEmpty()) {
                professionalDto.skills.forEach {
                    if (it.isBlank()) {
                        throw BadRequestException(ErrorsPage.SKILLS_ERROR)
                    }
                }
            }

            if (professionalDto.information.emails != null && professionalDto.information.emails.isNotEmpty()) {
                professionalDto.information.emails.forEach {
                    if (!isValidEmail(it.email)) {

                        throw BadRequestException(ErrorsPage.EMAILS_NOT_VALID)
                    }
                }
            }
            if (professionalDto.information.addresses != null && professionalDto.information.addresses.isNotEmpty()) {
                professionalDto.information.addresses.forEach {
                    if ((it.city.isNullOrBlank() || it.address.isNullOrBlank() || it.region.isNullOrBlank() || it.state.isNullOrBlank())) {
                        throw BadRequestException(ErrorsPage.ADDRESSES_NOT_VALID)
                    }
                }
            }
            if (professionalDto.information.telephones != null && professionalDto.information.telephones.isNotEmpty()) {
                //If a telephone number is not valid throws an exception
                professionalDto.information.telephones.forEach {
                    if (!isValidPhone(it.telephone)) {
                        throw BadRequestException(ErrorsPage.TELEPHONES_NOT_VALID)
                    }
                }
            }


            professionalDto.id = professionalID

            //retireve the existing professional (may throw ProfessionalNotFoundException)
            val existingProfessional = professionalService.getProfessional(professionalID)
            //Get the id of the associated contacct
            professionalDto.information.id = existingProfessional.professionalDTO.information.id
            //update the contact
            contactService.updateContact(professionalDto.information, category)
            //Get the modified contact
            val contact = contactService.getContact(professionalDto.information.id)

            val currentProfessional = professionalService.getProfessional(professionalID)
            val professionalModified = professionalService.updateProfessional(professionalDto, contact)

            kafkaProducer.sendProfessional(KafkaTopics.TOPIC_PROFESSIONAL, ProfessionalAnalyticsDTO(currentProfessional.professionalDTO.employmentState, professionalModified.employmentState))
            return ResponseEntity(professionalModified, HttpStatus.OK)

        } catch (e: IllegalArgumentException) {
            logger.info("Error: ${e.javaClass} - ${ErrorsPage.CATEGORY_PROFESSIONAL_ERROR}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: BadRequestException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: ProfessionalNotFoundException) {
            logger.info("Error with contact id equal to ${professionalID}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: ContactNotFoundException) {
            logger.info("Error with contact id equal to ${professionalID}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Failed to update a new professional detail. Details: ${e.message}")
            return ResponseEntity(
                "Failed to update a new professional detail. Details: ${e.message}",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    @DeleteMapping("/{professionalID}", "/{professionalID}/")
    fun deleteProfessionalById(
        @PathVariable professionalID: Long
    ): ResponseEntity<Any> {
        if (professionalID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to ErrorsPage.PROFESSIONAL_ID_ERROR))
        }

        try {
            professionalService.deleteProfessional(professionalID)
            return ResponseEntity(PROFESSIONAL_DELETED_SUCCESSFULLY, HttpStatus.OK)
        } catch (e: ProfessionalNotFoundException) {
            logger.info("ProfessionalController: Failed to delete the professional. Professional with id = $professionalID not found!")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("ProfessionalController: Failed to delete professional with ID $professionalID: ${e.message}")
            return ResponseEntity(mapOf("error" to INTERNAL_SERVER_ERROR_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Throws
    fun checkCategoryIsValid(categoryIn: String): CategoryOptions {
        try {
            val category = CategoryOptions.valueOf(categoryIn.uppercase())
            return category
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Illegal category type!")
        }
    }

    @Throws
    fun checkEmploymentStateIsValid(emploumentState: String): EmploymentStateEnum {
        try {
            val es = EmploymentStateEnum.valueOf(emploumentState.uppercase())
            return es
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Illegal Employment State type!")
        }
    }

    fun isValidEmail(email: String): Boolean {
        val mailRegex = getEmailRegex()
        val m: Pattern = Pattern.compile(mailRegex)
        val mail = m.matcher(email).matches()
        return mail
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = getPhoneRegex()
        val p: Pattern = Pattern.compile(phoneRegex)
        val tel = p.matcher(phone).matches()
        return tel
    }

    fun isValidAddress(a: CreateAddressDTO): Boolean {
        return !(a.city.isNullOrBlank() || a.address.isNullOrBlank() || a.region.isNullOrBlank() || a.state.isNullOrBlank())
    }
}