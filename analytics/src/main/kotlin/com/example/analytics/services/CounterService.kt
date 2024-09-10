package com.example.analytics.services

import com.example.analytics.utils.StateOptionsCounters
import org.springframework.transaction.annotation.Transactional

interface CounterService {

    /*
    Increment the number of messages
     */
    @Transactional
    fun incrementMessages(state : String)

    @Transactional
    fun decrementMessages(state : String)

    /*
    Return all counters values
     */
    fun getMessages(): Map<String, Long>

    /*
    Increment the number of job offers
     */
    @Transactional
    fun incrementJobOffers(state : String)

    @Transactional
    fun decrementJobOffers(state : String)


    /*
    Return all counters values
     */
    fun getJobOffers(): Map<String, Long>
}