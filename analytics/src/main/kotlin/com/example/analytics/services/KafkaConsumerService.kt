package com.example.analytics.services

import com.example.analytics.utils.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

@Service
class KafkaConsumerService (
    private val objectMapper: ObjectMapper,
    private val counterService: CounterServiceImpl
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)

    @KafkaListener(topics = [KafkaTopics.TOPIC_MESSAGE], groupId = "cm-to-crm-email-message-group")
    fun consumeNewMessage(messageJson: String) {
        logger.info("Received a message:")
        logger.info(messageJson)

        try {
            // Parse JSON into a Map<String, Any>
            val messageMap: Map<String, Any?> = objectMapper.readValue(messageJson)

            // Access properties from the map and convert them to StateOptions enums
            val previousStateString = messageMap["previousState"] as? String
            val actualStateString = messageMap["actualState"] as? String
            val date = messageMap["date"] as? String

            val previousState = previousStateString?.let { StateOptions.valueOf(it) }
            val actualState = actualStateString?.let { StateOptions.valueOf(it) }

            logger.info("Previous State: $previousState")
            logger.info("Actual State: $actualState")

            when (previousState) {
                StateOptions.RECEIVED -> {
                    counterService.decrementMessages(StateOptionsCounters.RECEIVED_COUNTER)
                }

                StateOptions.READ -> {
                    counterService.decrementMessages(StateOptionsCounters.READ_COUNTER)
                }

                StateOptions.DISCARDED -> {
                    counterService.decrementMessages(StateOptionsCounters.DISCARDED_COUNTER)
                }

                StateOptions.PROCESSING -> {
                    counterService.decrementMessages(StateOptionsCounters.PROCESSING_COUNTER)
                }

                StateOptions.DONE -> {
                    counterService.decrementMessages(StateOptionsCounters.DONE_COUNTER)
                }

                StateOptions.FAILED -> {
                    counterService.decrementMessages(StateOptionsCounters.FAILED_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

            when (actualState) {
                StateOptions.RECEIVED -> {
                    counterService.incrementMessages(StateOptionsCounters.RECEIVED_COUNTER)
                    counterService.incrementMessages(StateOptionsCounters.TOTAL_COUNTER)
                }

                StateOptions.READ -> {
                    counterService.incrementMessages(StateOptionsCounters.READ_COUNTER)
                }

                StateOptions.DISCARDED -> {
                    counterService.incrementMessages(StateOptionsCounters.DISCARDED_COUNTER)
                }

                StateOptions.PROCESSING -> {
                    counterService.incrementMessages(StateOptionsCounters.PROCESSING_COUNTER)
                }

                StateOptions.DONE -> {
                    counterService.incrementMessages(StateOptionsCounters.DONE_COUNTER)
                    counterService.incrementMessages(date + "CompletedMessageCounter")
                }

                StateOptions.FAILED -> {
                    counterService.incrementMessages(StateOptionsCounters.FAILED_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

        } catch (e: Exception) {
            logger.error("Failed to parse the message JSON: ${e.message}", e)
        }

    }

    @KafkaListener(topics = [KafkaTopics.TOPIC_JOB_OFFER], groupId = "cm-to-crm-email-message-group")
    fun consumeNewJobOffer(jobOfferJson: String) {
        logger.info("Received a job offer:")
        logger.info(jobOfferJson)

        try {
            // Parse JSON into a Map<String, Any>
            val messageMap: Map<String, Any?> = objectMapper.readValue(jobOfferJson)

            // Access properties from the map and convert them to StateOptions enums
            val previousStateString = messageMap["previousState"] as? String
            val actualStateString = messageMap["actualState"] as? String
            val date = messageMap["date"] as? String
            val creationDateString = messageMap["creationTime"] as? String
            val endDateString = messageMap["endTime"] as? String

            val previousState = previousStateString?.let { JobStatusEnum.valueOf(it) }
            val actualState = actualStateString?.let { JobStatusEnum.valueOf(it) }

            // Use ISO_LOCAL_DATE_TIME to handle nanoseconds in the date-time string
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

            // Convert creationTime and endTime to LocalDateTime
            val creationTime = creationDateString?.let { LocalDateTime.parse(it, formatter) }
            val endTime = endDateString?.let { LocalDateTime.parse(it, formatter) }

            logger.info("Previous State: $previousState")
            logger.info("Actual State: $actualState")

            when (previousState) {
                JobStatusEnum.CREATED -> {
                    counterService.decrementMessages(JobStatusCounters.CREATED_COUNTER)
                    counterService.incrementMessages(JobStatusCounters.TOTAL_COUNTER)
                }

                JobStatusEnum.SELECTION_PHASE -> {
                    counterService.decrementMessages(JobStatusCounters.SELECTION_PHASE_COUNTER)
                }

                JobStatusEnum.CANDIDATE_PROPOSAL -> {
                    counterService.decrementMessages(JobStatusCounters.CANDIDATE_PROPOSAL_COUNTER)
                }

                JobStatusEnum.CONSOLIDATED -> {
                    counterService.decrementMessages(JobStatusCounters.CONSOLIDATED_COUNTER)
                }

                JobStatusEnum.DONE -> {
                    counterService.decrementMessages(JobStatusCounters.DONE_COUNTER)
                }

                JobStatusEnum.ABORT -> {
                    counterService.decrementMessages(JobStatusCounters.ABORT_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

            when (actualState) {
                JobStatusEnum.CREATED -> {
                    counterService.incrementMessages(JobStatusCounters.CREATED_COUNTER)
                }

                JobStatusEnum.SELECTION_PHASE -> {
                    counterService.incrementMessages(JobStatusCounters.SELECTION_PHASE_COUNTER)
                }

                JobStatusEnum.CANDIDATE_PROPOSAL -> {
                    counterService.incrementMessages(JobStatusCounters.CANDIDATE_PROPOSAL_COUNTER)
                }

                JobStatusEnum.CONSOLIDATED -> {
                    counterService.incrementMessages(JobStatusCounters.CONSOLIDATED_COUNTER)
                }

                JobStatusEnum.DONE -> {
                    counterService.incrementMessages(JobStatusCounters.DONE_COUNTER)
                    counterService.incrementMessages(date + "CompletedJobOfferCounter")
                    if (creationTime != null && endTime != null) {
                        val elapsedMinutes = Duration.between(creationTime, endTime).toMinutes()
                        counterService.incrementMessages("CompletedJobOffersForAvarage")
                        counterService.updateCompletedJobOfferAvarageValue(elapsedMinutes)
                    }
                }

                JobStatusEnum.ABORT -> {
                    counterService.incrementMessages(JobStatusCounters.ABORT_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

        } catch (e: Exception) {
            logger.error("Failed to parse the message JSON: ${e.message}", e)
        }
    }

    @KafkaListener(topics = [KafkaTopics.TOPIC_JOB_OFFER_CT_WM], groupId = "cm-to-crm-email-message-group")
    fun consumeNewJobOfferCtWm(jobOfferJson: String) {
        logger.info("Received a job offer:")
        logger.info(jobOfferJson)

        try{
            val messageMap: Map<String, Any?> = objectMapper.readValue(jobOfferJson)

            // Access properties from the map and convert them to StateOptions enums
            val contractType = messageMap["contractType"] as? String
            val workMode = messageMap["workMode"] as? String

            counterService.incrementMessages(contractType + "counter")
            counterService.incrementMessages(workMode + "counter")
        } catch (e: Exception){
            logger.error("Failed to parse the message JSON: ${e.message}", e)
        }
    }

        @KafkaListener(topics = [KafkaTopics.TOPIC_PROFESSIONAL], groupId = "cm-to-crm-email-message-group")
    fun consumeNewProfessional(professionalJson: String) {
        logger.info("Received a professional:")
        logger.info(professionalJson)

        try {
            // Parse JSON into a Map<String, Any>
            val messageMap: Map<String, Any?> = objectMapper.readValue(professionalJson)

            // Access properties from the map and convert them to StateOptions enums
            val previousStateString = messageMap["previousState"] as? String
            val actualStateString = messageMap["actualState"] as? String

            val previousState = previousStateString?.let { EmploymentStateEnum.valueOf(it) }
            val actualState = actualStateString?.let { EmploymentStateEnum.valueOf(it) }

            logger.info("Previous State: $previousState")
            logger.info("Actual State: $actualState")

            when (previousState) {
                EmploymentStateEnum.EMPLOYED -> {
                    counterService.decrementMessages(EmploymentStateCounters.EMPLOYED_COUNTER)
                }

                EmploymentStateEnum.UNEMPLOYED -> {
                    counterService.decrementMessages(EmploymentStateCounters.UNEMPLOYED_COUNTER)
                }

                EmploymentStateEnum.AVAILABLE_FOR_WORK -> {
                    counterService.decrementMessages(EmploymentStateCounters.AVAILABLE_FOR_WORK_COUNTER)
                }

                EmploymentStateEnum.NOT_AVAILABLE -> {
                    counterService.decrementMessages(EmploymentStateCounters.NOT_AVAILABLE_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

            when (actualState) {
                EmploymentStateEnum.EMPLOYED -> {
                    counterService.incrementMessages(EmploymentStateCounters.EMPLOYED_COUNTER)
                }

                EmploymentStateEnum.UNEMPLOYED -> {
                    counterService.incrementMessages(EmploymentStateCounters.UNEMPLOYED_COUNTER)
                }

                EmploymentStateEnum.AVAILABLE_FOR_WORK -> {
                    counterService.incrementMessages(EmploymentStateCounters.AVAILABLE_FOR_WORK_COUNTER)
                }

                EmploymentStateEnum.NOT_AVAILABLE -> {
                    counterService.incrementMessages(EmploymentStateCounters.NOT_AVAILABLE_COUNTER)
                }

                null -> {
                    //nothing to do
                }
            }

        } catch (e: Exception) {
            logger.error("Failed to parse the message JSON: ${e.message}", e)
        }

    }
}