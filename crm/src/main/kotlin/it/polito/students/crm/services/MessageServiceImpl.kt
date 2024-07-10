package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.HistoryDTO
import it.polito.students.crm.dtos.MessageDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.InvalidSenderException
import it.polito.students.crm.exception_handlers.InvalidStateTransitionException
import it.polito.students.crm.exception_handlers.InvalidUpdateMessageRequestException
import it.polito.students.crm.exception_handlers.MessageNotFoundException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_UPDATE_MESSAGE_BAD_REQUEST
import it.polito.students.crm.utils.ErrorsPage.Companion.INVALID_STATE_TRANSITION_MESSAGE
import it.polito.students.crm.utils.ErrorsPage.Companion.SENDER_ERROR
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val historyRepository: HistoryRepository,
    private val contactRepository: ContactRepository,
    private val emailRepository: EmailRepository,
    private val telephoneRepository: TelephoneRepository,
) : MessageService {
    private val logger = LoggerFactory.getLogger(MessageServiceImpl::class.java)

    @Throws
    @Transactional
    override fun storeMessage(createdMessageDto: CreateMessageDTO, senderType: PhoneOrMailOption): MessageDTO {
        //Checking all fields of messageDto
        val priority = PriorityEnumOptions.valueOf(createdMessageDto.priority.uppercase())
        try {
            //Create a messageDTO
            val messageDto = MessageDTO(
                0,
                LocalDateTime.now(),
                createdMessageDto.subject,
                createdMessageDto.body,
                StateOptions.RECEIVED,
                priority,
                createdMessageDto.channel,
                createdMessageDto.sender,
            )
            // Create a message Entity from MessageDTO
            val message = Message().apply {
                date = messageDto.date
                subject = messageDto.subject
                body = messageDto.body
                actualState = messageDto.actualState
                this.priority = messageDto.priority
                channel = messageDto.channel
                sender = messageDto.sender
            }
            //Create a history Entity from MessageDTO
            val historyLine = History().apply {
                state = StateOptions.RECEIVED
                date = LocalDateTime.now()
                comment = "" // Perhaps it is missing a comment in a message
            }
            //Add historyLine to the collection in message
            message.addHistory(historyLine)

            /* ADD sender if it is not already stored in DB */
            val addContact: Contact? = when (senderType) {
                PhoneOrMailOption.PHONE -> {
                    manageTelephoneInsert(messageDto.sender)
                }

                PhoneOrMailOption.MAIL -> {
                    manageEmailInsert(messageDto.sender)
                }

                else -> throw InvalidStateTransitionException(SENDER_ERROR)
            }
            /* Save a contact */
            if (addContact != null)
                contactRepository.save(addContact)

            val messageSaved = messageRepository.save(message).toDTO()
            return messageSaved

        } catch (e: InvalidSenderException) {
            throw e
        } catch (e: Exception) {
            throw e
        }

    }

    /**
     * Check if a telephone is already stored in BD,
     * if it is not Create a blank contact and store it otherwise return null
     *
     * @param sender
     * @return blankContact or null
     */
    fun manageTelephoneInsert(sender: String): Contact? {
        val oldTelephone = telephoneRepository.findByTelephone(sender)

        // Telephone is not already stored in DB
        if (oldTelephone == null) {
            val blankContact = createBlankContact()
            val newPhone = Telephone().apply {
                this.telephone = sender
                this.comment = ""
            }
            blankContact.addTelephone(newPhone)
            return blankContact
        }
        return null
    }

    /**
     * Create a blank contact
     *
     * @return blankContact
     */
    fun createBlankContact(): Contact {
        val blackContact = Contact().apply {
            name = ""
            surname = ""
            ssnCode = ""
            category = CategoryOptions.UNKNOWN
            this.comment = ""
        }
        return blackContact
    }

    /**
     * Check if a mail is already stored in BD,
     * if it is not Create a blank contact and store it otherwise return null
     *
     * @param sender
     * @return blankContact or null
     */
    fun manageEmailInsert(sender: String): Contact? {
        val oldEmail = emailRepository.findByEmail(sender)

        if (oldEmail == null) {
            val blankContact = createBlankContact()
            val newEmail = Email().apply {
                this.email = sender
                this.comment = ""
            }
            emailRepository.save(newEmail)
            blankContact.addEmail(newEmail)
            return blankContact
        }
        return null
    }

    override fun getAllMessages(sortBy: SortOptions?, state: StateOptions?): List<MessageDTO> {
        var messageList = if (state != null) {
            messageRepository.findByActualState(state)
        } else {
            messageRepository.findAll()
        }

        sortBy?.let { option ->
            messageList = when (option) {
                SortOptions.ID_ASC -> messageList.sortedWith(compareBy { it.id })
                SortOptions.DATE_ASC -> messageList.sortedWith(compareBy { it.date })
                SortOptions.SUBJECT_ASC -> messageList.sortedWith(compareBy { it.subject })
                SortOptions.BODY_ASC -> messageList.sortedWith(compareBy { it.body })
                SortOptions.STATE_ASC -> messageList.sortedWith(compareBy { it.actualState })
                SortOptions.PRIORITY_ASC -> messageList.sortedWith(compareBy { it.priority })
                SortOptions.CHANNEL_ASC -> messageList.sortedWith(compareBy { it.channel })
                SortOptions.SENDER_ASC -> messageList.sortedWith(compareBy { it.sender })

                SortOptions.ID_DESC -> messageList.sortedWith(compareByDescending { it.id })
                SortOptions.DATE_DESC -> messageList.sortedWith(compareByDescending { it.date.toEpochSecond(ZoneOffset.UTC) })
                SortOptions.SUBJECT_DESC -> messageList.sortedWith(compareByDescending { it.subject })
                SortOptions.BODY_DESC -> messageList.sortedWith(compareByDescending { it.body })
                SortOptions.STATE_DESC -> messageList.sortedWith(compareByDescending { it.actualState })
                SortOptions.PRIORITY_DESC -> messageList.sortedWith(compareByDescending { it.priority })
                SortOptions.CHANNEL_DESC -> messageList.sortedWith(compareByDescending { it.channel })
                SortOptions.SENDER_DESC -> messageList.sortedWith(compareByDescending { it.sender })
            }
        }

        return messageList.map { it.toDTO() }
    }

    override fun getMessage(id: Long): MessageDTO {
        val optionalMessage = messageRepository.findById(id)
        if (optionalMessage.isPresent) {
            val message = optionalMessage.get()
            return message.toDTO()
        } else {
            logger.info("The message with id $id was not found on the db")
            throw MessageNotFoundException("The message with id equal to $id was not found!")
        }
    }

    override fun getMessageHistory(messageId: Long): List<HistoryDTO> {
        val historiesSteps = historyRepository.findByMessageId(messageId)
        if (historiesSteps.isNotEmpty()) {
            if (historiesSteps.size <= 4)
                return historiesSteps.map { it.toDTO() }.sortedBy { it.date }
            else {
                logger.info("The message with id $messageId contains too much histories, states graphs violated")
                throw Exception("Some error occurs on histories of message with id $messageId")
            }
        } else {
            logger.info("The message with id $messageId was not found on the db")
            throw MessageNotFoundException("The message with id equal to $messageId was not found!")
        }
    }

    @Transactional
    override fun updateMessage(
        messageID: Long,
        actualStateParam: StateOptions?,
        commentParam: String?,
        priorityParam: PriorityEnumOptions?
    ): MessageDTO {
        var result: MessageDTO? = null

        if (actualStateParam == null && priorityParam == null) {
            throw InvalidUpdateMessageRequestException(ERROR_MESSAGE_UPDATE_MESSAGE_BAD_REQUEST)
        }

        if (actualStateParam != null) {
            result = updateActualStateMessage(messageID, actualStateParam, commentParam)
        }

        if (priorityParam != null) {
            result = updatePriorityMessage(messageID, priorityParam)
        }

        return result!!
    }

    /**
     * The `updateActualStateMessage` function updates the state of a specific message.
     *
     * @param messageID A `Long` that represents the unique identifier of the message to be updated.
     * @param actualStateParam A `StateOptions` that represents the new state to be set for the message.
     * @param commentParam A `String` that represents an optional comment related to the state update.
     * @return A `MessageDTO` object that represents the updated message.
     * @throws MessageNotFoundException If the message with the specified ID is not found.
     */
    private fun updateActualStateMessage(
        messageID: Long,
        actualStateParam: StateOptions,
        commentParam: String?
    ): MessageDTO {
        val messageDto = getMessage(messageID)
        val listHistoryDto = getMessageHistory(messageID)

        if (!canTransition(messageDto.actualState, actualStateParam)) {
            logger.info("$INVALID_STATE_TRANSITION_MESSAGE: $actualStateParam")
            throw InvalidStateTransitionException(INVALID_STATE_TRANSITION_MESSAGE)
        }

        // Update the state of the message
        val message = Message().apply {
            id = messageID
            date = messageDto.date
            subject = messageDto.subject
            body = messageDto.body
            actualState = actualStateParam
            priority = messageDto.priority
            channel = messageDto.channel
            sender = messageDto.sender
        }

        //Create a history Entity from MessageDTO
        var historyLine = History().apply {
            state = actualStateParam
            date = LocalDateTime.now()
            comment = commentParam ?: ""
            this.message = message
        }
        historyLine = historyRepository.save(historyLine)

        // adding all the history to the message
        listHistoryDto.forEach {
            val history = History().apply {
                id = it.id
                state = it.state
                date = it.date
                comment = it.comment
            }
            message.addHistory(history)
        }
        message.addHistory(historyLine)     // added the new history

        val result = messageRepository.save(message)
        sendEmailNotification(
            messageDto.sender,
            "Message status changed!",
            """
        Dear ${message.sender.split(Pattern.compile("@"))[0]},

        We wanted to inform you that the status of your message has been changed.

        Message Details:
        - Date: ${message.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
        - Subject: ${message.subject}
        - Text: ${message.body}
        - New Status: ${message.actualState}
       

        If you have any questions or need further assistance, please do not hesitate to contact us.

        Best regards,
        Your Company Team
        """.trimIndent(),
            null
        )

        return result.toDTO()
    }

    private fun sendEmailNotification(
        destEmailAddr: String,
        subject: String?,
        textBody: String?,
        multipartFile: MultipartFile?
    ) {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>().apply {
            add("destEmailAddr", destEmailAddr)
            add("subject", subject)
            add("textBody", textBody)
            if (multipartFile != null && !multipartFile.isEmpty) {
                add("multipartFile", multipartFile.resource)
            }
        }

        val request = HttpEntity(body, headers)

        val response = restTemplate.exchange(
            "http://localhost:8081/API/emails/",
            HttpMethod.POST,
            request,
            String::class.java
        )

        if (!response.statusCode.is2xxSuccessful) {
            logger.error("Failed to send email notification: ${response.statusCode}")
        }
    }


    /**
     * The `updatePriorityMessage` function updates the priority of a specific message.
     *
     * @param messageID A `Long` that represents the unique identifier of the message to be updated.
     * @param priorityParam A `PriorityEnumOptions` that represents the new priority to be set for the message.
     * @return A `MessageDTO` object that represents the updated message.
     * @throws MessageNotFoundException If the message with the specified ID is not found.
     */
    private fun updatePriorityMessage(messageID: Long, priorityParam: PriorityEnumOptions): MessageDTO {
        val messageDto = getMessage(messageID)

        val message = Message().apply {
            id = messageDto.id
            date = messageDto.date
            subject = messageDto.subject
            body = messageDto.body
            actualState = messageDto.actualState
            priority = priorityParam
            channel = messageDto.channel
            sender = messageDto.sender
        }

        return messageRepository.save(message).toDTO()
    }
}