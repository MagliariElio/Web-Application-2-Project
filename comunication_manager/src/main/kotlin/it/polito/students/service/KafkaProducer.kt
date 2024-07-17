package it.polito.students.service

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.dtos.CreateMessageDTO
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    fun sendMessage(topic: String, message: CreateMessageDTO) {
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
}