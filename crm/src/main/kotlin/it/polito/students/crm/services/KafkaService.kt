package it.polito.students.crm.services

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.controllers.CrmMessagesController
import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.utils.ErrorsPage.Companion.BODY_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CHANNEL_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_PRIORITY_NOT_FOUND
import it.polito.students.crm.utils.ErrorsPage.Companion.PRIORITY_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SENDER_MISSING_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.SUBJECT_MISSING_ERROR
import it.polito.students.crm.utils.PhoneOrMailOption
import it.polito.students.crm.utils.SuccessPage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val messageService: MessageService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)

    @KafkaListener(topics = ["email_message_cm_to_crm"], groupId = "cm-to-crm-email-message-group")
    fun consume(messageJson: String) {
        logger.info(SuccessPage.MESSAGE_RECEIVED)

        logger.info(messageJson)

        try {
            val message: CreateMessageDTO = objectMapper.readValue(messageJson, CreateMessageDTO::class.java)
            val errors = validateMessage(message)
            if (errors.isNotEmpty()) {
                errors.forEach { logger.error(it) }
                return
            }

            val senderType = PhoneOrMailOption.MAIL
            CrmMessagesController.checkPriorityIsValid(message.priority)
            messageService.storeMessage(message, senderType)
            logger.info(SuccessPage.MESSAGE_SENT_SUCCESSFULLY)
        } catch (e: IllegalArgumentException) {
            logger.error("Error: ${e.javaClass} - $ERROR_MESSAGE_PRIORITY_NOT_FOUND: ${e.message}")
        } catch (e: Exception) {
            logger.error("An error occurred during JSON deserialization: ${e.message}")
        }
    }

    private fun validateMessage(message: CreateMessageDTO): List<String> {
        val errors = mutableListOf<String>()

        if (message.sender.isBlank()) {
            errors.add(SENDER_MISSING_ERROR)
        }
        if (message.body.isBlank()) {
            errors.add(BODY_MISSING_ERROR)
        }
        if (message.subject.isBlank()) {
            errors.add(SUBJECT_MISSING_ERROR)
        }
        if (message.priority.isBlank()) {
            errors.add(PRIORITY_MISSING_ERROR)
        }
        if (message.channel.isBlank()) {
            errors.add(CHANNEL_MISSING_ERROR)
        }

        return errors
    }

}