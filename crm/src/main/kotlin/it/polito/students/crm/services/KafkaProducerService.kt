package it.polito.students.crm.services

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.AnalyticsJobOfferDTO
import it.polito.students.crm.dtos.JobOfferAnalyticsDTO
import it.polito.students.crm.dtos.MessageAnalyticsDTO
import it.polito.students.crm.dtos.ProfessionalAnalyticsDTO
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

    fun sendMessage(topic: String, message: MessageAnalyticsDTO) {
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

    fun sendJobOffer(topic: String, jobOffer: JobOfferAnalyticsDTO) {
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

    fun sendJobOfferCtWm(topic: String, jobOffer: AnalyticsJobOfferDTO) {
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

    fun sendProfessional(topic: String, professional: ProfessionalAnalyticsDTO) {
        val professionalJson = objectMapper.writeValueAsString(professional)
        val result = ProducerRecord<String, String>(topic, professionalJson)

        kafkaTemplate.execute { producer ->
            producer.send(result) { metadata, exception ->
                if (exception != null) {
                    logger.error("Unable to send professional=[$professionalJson] due to : ${exception.message}")
                } else {
                    logger.info("Sent professional=[$professionalJson] with offset=[${metadata.offset()}]")
                }
            }
        }
    }
}