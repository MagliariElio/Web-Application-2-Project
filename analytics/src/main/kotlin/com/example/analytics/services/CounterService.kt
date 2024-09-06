package com.example.analytics.services

import org.springframework.transaction.annotation.Transactional

interface CounterService {

    /*
    Increment the number of total messages received
     */
    @Transactional
    fun incrementTotalMessages()

    /*
    Increment the number of completed messages
     */
    @Transactional
    fun incrementCompletedMessages()

    /*
    Return the ratio between completed messages/total messages
     */
    fun getMessagesCompletionPercentage(): Double

    /*
    Increment the number of total job offers received
     */
    @Transactional
    fun incrementTotalJobOffers()

    /*
    Increment the number of completed job offers
     */
    @Transactional
    fun incrementCompletedJobOffers()

    /*
    Return the ratio between completed job offers/total job offers
     */
    fun getJobOffersCompletionPercentage(): Double
}