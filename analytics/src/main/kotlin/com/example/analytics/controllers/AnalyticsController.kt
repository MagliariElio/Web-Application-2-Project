package com.example.analytics.controllers

import com.example.analytics.dtos.CompletedRatioDTO
import com.example.analytics.dtos.CounterDTO
import com.example.analytics.repositories.CounterRepository
import com.example.analytics.services.CounterService
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/analytics")
class AnalyticsController(
    private  val counterService : CounterService
) {
    private val logger = LoggerFactory.getLogger(AnalyticsController::class.java)

    @GetMapping("/messages")
    fun getCompletedMessagesRatio() : ResponseEntity<Any> {
        try {
            var response = counterService.getMessages()

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed messages ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/jobOffers")
    fun getCompletedJobOffersRatio() : ResponseEntity<Any> {
        try {
            var response = counterService.getJobOffers()

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed job offers ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


}