package com.example.analytics.controllers

import com.example.analytics.services.CounterService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/analytics")
class AnalyticsController(
    private  val counterService : CounterService
) {
    private val logger = LoggerFactory.getLogger(AnalyticsController::class.java)

    @GetMapping("/messages")
    fun getMessagesStates() : ResponseEntity<Any> {
        try {
            var response = counterService.getMessages()

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed messages ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/messages/{year}")
    fun getCompletedMessagesPerMonth(
        @PathVariable year: Long
    ) : ResponseEntity<Any> {
        try {
            var response = counterService.getCompletedMessagesPerMonth(year)

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed messages ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/jobOffers")
    fun getJobOffersStates() : ResponseEntity<Any> {
        try {
            var response = counterService.getJobOffers()

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed job offers ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/jobOffers/{year}")
    fun getCompletedJobOffersPerMonth(
        @PathVariable year: Long
    ) : ResponseEntity<Any> {
        try {
            var response = counterService.getCompletedJobOfferPerMonth(year)

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed messages ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/professionals")
    fun getProfessionalsStates() : ResponseEntity<Any> {
        try {
            var response = counterService.getProfessionals()

            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.info("Error retrieving completed job offers ratio ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}