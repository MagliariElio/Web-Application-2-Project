package com.example.analytics.services

import com.example.analytics.utils.KafkaTopics
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

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

        counterService.incrementTotalMessages()
    }

    @KafkaListener(topics = [KafkaTopics.TOPIC_COMPLETED_MESSAGE], groupId = "cm-to-crm-email-message-group")
    fun consumeCompletedMessage(messageJson: String) {
        logger.info("Received a completed message:")
        logger.info(messageJson)

        counterService.incrementCompletedMessages()
    }

    @KafkaListener(topics = [KafkaTopics.TOPIC_JOB_OFFER], groupId = "cm-to-crm-email-message-group")
    fun consumeNewJobOffer(jobOfferJson: String) {
        logger.info("Received a job offer:")
        logger.info(jobOfferJson)

        counterService.incrementTotalJobOffers()
    }

    @KafkaListener(topics = [KafkaTopics.TOPIC_COMPLETED_JOB_OFFER], groupId = "cm-to-crm-email-message-group")
    fun consumeCompletedJobOffer(jobOfferJson: String) {
        logger.info("Received a completed job offer:")
        logger.info(jobOfferJson)

        counterService.incrementCompletedJobOffers()
    }
}