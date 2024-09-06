package it.polito.students.crm.services

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.CreateJobOfferDTO
import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.JobOfferDTO
import it.polito.students.crm.dtos.MessageDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    fun sendMessage(topic: String, message: MessageDTO) {
        val messageJson = objectMapper.writeValueAsString(message)
        val result = ProducerRecord<String, String>(topic, messageJson)

        kafkaTemplate.execute { producer ->
            producer.send(result) { metadata, exception ->
                if (exception != null) {
                    logger.error("Unable to send message=[$messageJson] due to : ${exception.message}")
                } else {
                    logger.info("Sent message=[$messageJson] with offset=[${metadata.offset()}]")
                }
            }
        }
    }

    fun sendCompletedMessage(topic: String, message: MessageDTO) {
        val messageJson = objectMapper.writeValueAsString(message)
        val result = ProducerRecord<String, String>(topic, messageJson)

        kafkaTemplate.execute { producer ->
            producer.send(result) { metadata, exception ->
                if (exception != null) {
                    logger.error("Unable to send message=[$messageJson] due to : ${exception.message}")
                } else {
                    logger.info("Sent message=[$messageJson] with offset=[${metadata.offset()}]")
                }
            }
        }
    }

    fun sendJobOffer(topic: String, jobOffer: JobOfferDTO) {
        val jobOfferJson = objectMapper.writeValueAsString(jobOffer)
        val result = ProducerRecord<String, String>(topic, jobOfferJson)

        kafkaTemplate.execute { producer ->
            producer.send(result) { metadata, exception ->
                if (exception != null) {
                    logger.error("Unable to send job offer=[$jobOfferJson] due to : ${exception.message}")
                } else {
                    logger.info("Sent job offer=[$jobOfferJson] with offset=[${metadata.offset()}]")
                }
            }
        }
    }

    fun sendCompletedJobOffer(topic: String, jobOffer: JobOfferDTO) {
        val jobOfferJson = objectMapper.writeValueAsString(jobOffer)
        val result = ProducerRecord<String, String>(topic, jobOfferJson)

        kafkaTemplate.execute { producer ->
            producer.send(result) { metadata, exception ->
                if (exception != null) {
                    logger.error("Unable to send job offer=[$jobOfferJson] due to : ${exception.message}")
                } else {
                    logger.info("Sent job offer=[$jobOfferJson] with offset=[${metadata.offset()}]")
                }
            }
        }
    }
}