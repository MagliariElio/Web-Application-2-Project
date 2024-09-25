package it.polito.students.crm.controllers

import com.github.kittinunf.fuel.httpPost
import it.polito.students.crm.dtos.*
import it.polito.students.crm.exception_handlers.BadQueryParametersException
import it.polito.students.crm.exception_handlers.InvalidStateTransitionException
import it.polito.students.crm.exception_handlers.MessageNotFoundException
import it.polito.students.crm.services.KafkaProducerService
import it.polito.students.crm.services.MessageService
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.BODY_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CHANNEL_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_ACTUAL_STATE_NOT_FOUND
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_PRIORITY_NOT_FOUND
import it.polito.students.crm.utils.ErrorsPage.Companion.GENERAL_ERROR_MESSAGE_UPDATE_MESSAGE_REQUEST
import it.polito.students.crm.utils.ErrorsPage.Companion.MESSAGE_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.MESSAGE_ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.PAGE_AND_LIMIT_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.PRIORITY_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.PRIORITY_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SENDER_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SENDER_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SORT_BY_AND_STATE_REQUEST_PARAMETERS_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SUBJECT_MISSING_ERROR
import kotlinx.serialization.json.Json
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern


@RestController
@RequestMapping("/API/messages")
class CrmMessagesController(
    private val messageService: MessageService,
    private val kafkaProducer: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(CrmMessagesController::class.java)
    private val formatter = DateTimeFormatter.ofPattern("MMMMyyyy", Locale.ENGLISH)



    // TODO: aggiunto per testare la route
    @GetMapping("/auth")
    fun get(authentication: Authentication?): Map<String, Any?> {
        val authorities: Collection<GrantedAuthority>? = authentication?.authorities

        return mapOf(
            "name" to "crmService:8082",
            "principal" to authentication?.principal,
            "authorities" to authorities?.map { it.authority }
        )
    }

    @GetMapping("/", "")
    fun getMessages(
        @RequestParam page: Int = 0,
        @RequestParam limit: Int = 30,
        @RequestParam sortBy: String? = null,
        @RequestParam state: String? = null
    ): ResponseEntity<Any> {
        if (page < 0 || limit < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to PAGE_AND_LIMIT_ERROR))
        }

        try {

            val sortByValue: SortOptions? = sortBy?.uppercase()?.let { value ->

                try {
                    SortOptions.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    throw BadQueryParametersException(SORT_BY_AND_STATE_REQUEST_PARAMETERS_ERROR)
                }
            }

            val stateValue: StateOptions? = state?.uppercase()?.let { value ->
                try {
                    StateOptions.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    throw BadQueryParametersException(SORT_BY_AND_STATE_REQUEST_PARAMETERS_ERROR)
                }

            }

            val messageList = messageService.getAllMessages(sortByValue, stateValue)

            var start = page * limit
            val end = minOf(start + limit, messageList.size)

            if (start > end) {
                start = end
            }

            val pageable: Pageable = PageRequest.of(page, limit)
            val subList = messageList.subList(start, end)
            val pageImpl = PageImpl(subList, pageable, messageList.size.toLong())

            val response = MessageListDTO(
                pageImpl.content,
                pageImpl.number,
                pageImpl.size,
                pageImpl.totalPages,
                pageImpl.totalElements
            )

            val mapAnswer: Map<String, Any?> = mapOf(
                "content" to pageImpl.content,
                "currentPage" to pageImpl.number,
                "elementPerPage" to pageImpl.size,
                "totalPages" to pageImpl.totalPages,
                "totalElements" to pageImpl.totalElements
            )

            return ResponseEntity.ok(response)
        } catch (e: BadQueryParametersException) {
            logger.info("Error retrieving all messages ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Error retrieving all messages ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @PostMapping("", "/")
    fun storeNewMessage(@RequestBody message: CreateMessageDTO): ResponseEntity<out Any> {
        //Check if there are black field in request body
        if (message.sender.isEmpty() || message.sender.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to SENDER_MISSING_ERROR))
        }
        val senderType = isPhoneOrMail(message.sender)
        if (senderType == PhoneOrMailOption.NONE) {
            return ResponseEntity.badRequest().body(mapOf("error" to "$SENDER_ERROR senderType: $senderType"))
        }
        if (message.body.isEmpty() || message.body.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to BODY_MISSING_ERROR))
        }
        if (message.subject.isEmpty() || message.subject.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to SUBJECT_MISSING_ERROR))
        }
        if (message.priority.isEmpty() || message.priority.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to PRIORITY_MISSING_ERROR))
        }
        if (message.channel.isEmpty() || message.channel.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to CHANNEL_MISSING_ERROR))
        }

        try {
            checkPriorityIsValid(message.priority)
            //Save the message
            val messageSaved = messageService.storeMessage(message, senderType)
            kafkaProducer.sendMessage(
                KafkaTopics.TOPIC_MESSAGE,
                MessageAnalyticsDTO(null, messageSaved.actualState, LocalDate.now().format(formatter).lowercase())
            )
            return ResponseEntity(messageSaved, HttpStatus.CREATED)
        } catch (e: IllegalArgumentException) {
            logger.info("Error: ${e.javaClass} - ${ERROR_MESSAGE_PRIORITY_NOT_FOUND}: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to PRIORITY_ERROR))
        } catch (e: Exception) {
            logger.info(e.message)
            return ResponseEntity(MESSAGE_ERROR, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/{messageId}", "/{messageId}/")
    fun getMessage(@PathVariable messageId: Long): ResponseEntity<out Any> {
        if (messageId < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to MESSAGE_ID_ERROR))
        }

        try {
            val message = messageService.getMessage(messageId)
            return ResponseEntity(message, HttpStatus.OK)
        } catch (e: MessageNotFoundException) {
            logger.info("Error with message id equal to ${messageId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error with message id equal to ${messageId}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{messageId}/history")
    fun getMessageHistory(@PathVariable messageId: Long): ResponseEntity<Any> {
        if (messageId < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to MESSAGE_ID_ERROR))
        }

        try {
            val history: List<HistoryDTO> = messageService.getMessageHistory(messageId)
            return ResponseEntity(history, HttpStatus.OK)
        } catch (e: MessageNotFoundException) {
            logger.info("Error with message id equal to ${messageId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error with message id equal to ${messageId}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @PatchMapping("/{messageID}", "/{messageID}/")
    fun updateMessage(
        @PathVariable messageID: Long,
        @RequestBody updateMessageDTO: UpdateMessageDTO
    ): ResponseEntity<out Any> {
        if (messageID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to MESSAGE_ID_ERROR))
        }
        try {
            var state: StateOptions? = null
            var priority: PriorityEnumOptions? = null

            if (updateMessageDTO.actualState != null) {
                try {
                    state = StateOptions.valueOf(updateMessageDTO.actualState.uppercase())
                } catch (e: IllegalArgumentException) {
                    logger.info("Error: ${e.javaClass} - ${ERROR_MESSAGE_ACTUAL_STATE_NOT_FOUND}: ${e.message}")
                    return ResponseEntity(ERROR_MESSAGE_ACTUAL_STATE_NOT_FOUND, HttpStatus.BAD_REQUEST)
                }
            }

            if (updateMessageDTO.priority != null) {
                try {
                    priority = PriorityEnumOptions.valueOf(updateMessageDTO.priority.uppercase())
                } catch (e: IllegalArgumentException) {
                    logger.info("Error: ${e.javaClass} - ${ERROR_MESSAGE_PRIORITY_NOT_FOUND}: ${e.message}")
                    return ResponseEntity(ERROR_MESSAGE_PRIORITY_NOT_FOUND, HttpStatus.BAD_REQUEST)
                }
            }
            val message = messageService.getMessage(messageID)
            val result = messageService.updateMessage(messageID, state, updateMessageDTO.comment, priority)

            kafkaProducer.sendMessage(
                KafkaTopics.TOPIC_MESSAGE,
                MessageAnalyticsDTO(
                    message.actualState,
                    result.actualState,
                    LocalDate.now().format(formatter).lowercase()
                )
            )

            return ResponseEntity(result, HttpStatus.OK)
        } catch (e: MessageNotFoundException) {
            logger.info("Message not found: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: InvalidStateTransitionException) {
            logger.info("Invalid state transition: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("An error occurred while updating the message: ${e.message}")
            return ResponseEntity(
                "$GENERAL_ERROR_MESSAGE_UPDATE_MESSAGE_REQUEST: ${e.message}",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    /**
     *  Check if sender is an email or a telephone number
     *
     *  @param sender
     *  return PhoneOrMailOption
     */
    fun isPhoneOrMail(sender: String): PhoneOrMailOption {
        val mailRegex = getEmailRegex()
        val phoneRegex = getPhoneRegex()
        val m: Pattern = Pattern.compile(mailRegex)
        val mail = m.matcher(sender).matches()
        val p: Pattern = Pattern.compile(phoneRegex)
        val phone = p.matcher(sender).matches()
        if (mail) {
            return PhoneOrMailOption.MAIL
        }

        return if (phone) {
            PhoneOrMailOption.PHONE
        } else {
            PhoneOrMailOption.NONE
        }
    }

    companion object {
        /**
         *  Convert in a PriorityOptions and check whether priority is a valid String
         *
         *  @return contact
         *  @throws IllegalArgumentException if priority cannot be converted in PriorityOptions
         */
        @Throws
        fun checkPriorityIsValid(priorityIn: String): PriorityEnumOptions {
            try {
                val priority = PriorityEnumOptions.valueOf(priorityIn.uppercase())
                return priority
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException(PRIORITY_ERROR)
            }
        }
    }

}