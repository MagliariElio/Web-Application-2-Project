package com.example.analytics.services

import com.example.analytics.entities.CounterEntity
import com.example.analytics.repositories.CounterRepository
import com.example.analytics.utils.EmploymentStateCounters
import com.example.analytics.utils.JobStatusCounters
import com.example.analytics.utils.StateOptionsCounters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CounterServiceImpl(
    private val counterRepository: CounterRepository
) : CounterService {
    private val logger = LoggerFactory.getLogger(CounterServiceImpl::class.java)

    @Transactional
    override fun incrementMessages(state: String) {
        val totalCounter = counterRepository.findByType(state) ?: CounterEntity(state, 0)
        totalCounter.count += 1
        counterRepository.save(totalCounter)
    }

    @Transactional
    override fun decrementMessages(state: String) {
        val totalCounter = counterRepository.findByType(state) ?: CounterEntity(state, 0)
        totalCounter.count -= 1
        counterRepository.save(totalCounter)
    }

    override fun getMessages(): Map<String, Long> {
        // Create a mutable map to store counter name as key and its value as the value
        val countersMap = mutableMapOf<String, Long>()

        // Retrieve each counter from the repository and add it to the map
        val totalCounter = counterRepository.findByType(StateOptionsCounters.TOTAL_COUNTER) ?: CounterEntity(StateOptionsCounters.TOTAL_COUNTER, 0)
        countersMap[StateOptionsCounters.TOTAL_COUNTER] = totalCounter.count

        val receivedCounter = counterRepository.findByType(StateOptionsCounters.RECEIVED_COUNTER) ?: CounterEntity(StateOptionsCounters.RECEIVED_COUNTER, 0)
        countersMap[StateOptionsCounters.RECEIVED_COUNTER] = receivedCounter.count

        val readCounter = counterRepository.findByType(StateOptionsCounters.READ_COUNTER) ?: CounterEntity(StateOptionsCounters.READ_COUNTER, 0)
        countersMap[StateOptionsCounters.READ_COUNTER] = readCounter.count

        val discardedCounter = counterRepository.findByType(StateOptionsCounters.DISCARDED_COUNTER) ?: CounterEntity(StateOptionsCounters.DISCARDED_COUNTER, 0)
        countersMap[StateOptionsCounters.DISCARDED_COUNTER] = discardedCounter.count

        val processingCounter = counterRepository.findByType(StateOptionsCounters.PROCESSING_COUNTER) ?: CounterEntity(StateOptionsCounters.PROCESSING_COUNTER, 0)
        countersMap[StateOptionsCounters.PROCESSING_COUNTER] = processingCounter.count

        val doneCounter = counterRepository.findByType(StateOptionsCounters.DONE_COUNTER) ?: CounterEntity(StateOptionsCounters.DONE_COUNTER, 0)
        countersMap[StateOptionsCounters.DONE_COUNTER] = doneCounter.count

        val failedCounter = counterRepository.findByType(StateOptionsCounters.FAILED_COUNTER) ?: CounterEntity(StateOptionsCounters.FAILED_COUNTER, 0)
        countersMap[StateOptionsCounters.FAILED_COUNTER] = failedCounter.count

        // Return the map as a JSON response
        return countersMap
    }

    override fun getCompletedMessagesPerMonth(year: Long): Map<String, Long> {
        var monthMap = mutableMapOf<String, Long>()
        val months = arrayOf("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")

        for (month in months) {
           val counter = counterRepository.findByType(month + year + "CompletedMessageCounter") ?: CounterEntity(month + year + "CompletedMessageCounter", 0)
            monthMap[month] = counter.count
        }

        return monthMap
    }

    @Transactional
    override fun incrementJobOffers(state: String) {
        val totalCounter = counterRepository.findByType(state) ?: CounterEntity(state, 0)
        totalCounter.count += 1
        counterRepository.save(totalCounter)
    }

    @Transactional
    override fun decrementJobOffers(state: String) {
        val totalCounter = counterRepository.findByType(state) ?: CounterEntity(state, 0)
        totalCounter.count -= 1
        counterRepository.save(totalCounter)
    }

    override fun getJobOffers(): Map<String, Long> {
        // Create a mutable map to store counter name as key and its value as the value
        val countersMap = mutableMapOf<String, Long>()

        // Retrieve each counter from the repository and add it to the map
        val totalCounter = counterRepository.findByType(JobStatusCounters.TOTAL_COUNTER) ?: CounterEntity(StateOptionsCounters.TOTAL_COUNTER, 0)
        countersMap[JobStatusCounters.TOTAL_COUNTER] = totalCounter.count

        val receivedCounter = counterRepository.findByType(JobStatusCounters.CREATED_COUNTER) ?: CounterEntity(StateOptionsCounters.RECEIVED_COUNTER, 0)
        countersMap[JobStatusCounters.CREATED_COUNTER] = receivedCounter.count

        val readCounter = counterRepository.findByType(JobStatusCounters.SELECTION_PHASE_COUNTER) ?: CounterEntity(StateOptionsCounters.READ_COUNTER, 0)
        countersMap[JobStatusCounters.SELECTION_PHASE_COUNTER] = readCounter.count

        val discardedCounter = counterRepository.findByType(JobStatusCounters.CANDIDATE_PROPOSAL_COUNTER) ?: CounterEntity(StateOptionsCounters.DISCARDED_COUNTER, 0)
        countersMap[JobStatusCounters.CANDIDATE_PROPOSAL_COUNTER] = discardedCounter.count

        val processingCounter = counterRepository.findByType(JobStatusCounters.CONSOLIDATED_COUNTER) ?: CounterEntity(StateOptionsCounters.PROCESSING_COUNTER, 0)
        countersMap[JobStatusCounters.CONSOLIDATED_COUNTER] = processingCounter.count

        val doneCounter = counterRepository.findByType(JobStatusCounters.DONE_COUNTER) ?: CounterEntity(StateOptionsCounters.DONE_COUNTER, 0)
        countersMap[JobStatusCounters.DONE_COUNTER] = doneCounter.count

        val failedCounter = counterRepository.findByType(JobStatusCounters.ABORT_COUNTER) ?: CounterEntity(StateOptionsCounters.FAILED_COUNTER, 0)
        countersMap[JobStatusCounters.ABORT_COUNTER] = failedCounter.count

        val avarageCounter = counterRepository.findByType("AvarageJobOfferCompletionTime") ?: CounterEntity("AvarageJobOfferCompletionTime", 0)
        countersMap["AvarageJobOfferCompletionTime"] = avarageCounter.count

        val fulltimeCounter = counterRepository.findByType("Full Timecounter") ?: CounterEntity("Full Timecounter", 0)
        countersMap["fullTimeCounter"] = fulltimeCounter.count

        val parttimeCounter = counterRepository.findByType("Part Timecounter") ?: CounterEntity("Part Timecounter", 0)
        countersMap["partTimeCounter"] = parttimeCounter.count

        val contractCounter = counterRepository.findByType("Contractcounter") ?: CounterEntity("Contractcounter", 0)
        countersMap["contractCounter"] = contractCounter.count

        val freelanceCounter = counterRepository.findByType("Freelancecounter") ?: CounterEntity("Freelancecounter", 0)
        countersMap["freelanceCounter"] = freelanceCounter.count

        val remoteCounter = counterRepository.findByType("Remotecounter") ?: CounterEntity("Remotecounter", 0)
        countersMap["remoteCounter"] = freelanceCounter.count

        val hybridCounter = counterRepository.findByType("Hybridcounter") ?: CounterEntity("Hybridcounter", 0)
        countersMap["hybridCounter"] = hybridCounter.count

        val inPersonCounter = counterRepository.findByType("In-Personcounter") ?: CounterEntity("In-Personcounter", 0)
        countersMap["inPersonCounter"] = inPersonCounter.count

        // Return the map as a JSON response
        return countersMap
    }

    override fun getCompletedJobOfferPerMonth(year: Long): Map<String, Long> {
        var monthMap = mutableMapOf<String, Long>()
        val months = arrayOf("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")

        for (month in months) {
            val counter = counterRepository.findByType(month + year + "CompletedJobOfferCounter") ?: CounterEntity(month + year + "CompletedJobOfferCounter", 0)
            monthMap[month] = counter.count
        }

        return monthMap
    }

    override fun updateCompletedJobOfferAvarageValue(newValue: Long) {
        val completedJobOffers = counterRepository.findByType("CompletedJobOffersForAvarage") ?: CounterEntity("CompletedJobOffersForAvarage", 0)
        val avarage = counterRepository.findByType("AvarageJobOfferCompletionTime") ?: CounterEntity("AvarageJobOfferCompletionTime", 0)
        if(completedJobOffers.count <= 0){
            avarage.count = newValue
        }else{
            var newAvarage = avarage.count*completedJobOffers.count
            newAvarage += newValue
            newAvarage = newAvarage/(completedJobOffers.count + 1)
            avarage.count = newAvarage
        }
        counterRepository.save(avarage)
        completedJobOffers.count += 1
        counterRepository.save(completedJobOffers)
    }

    override fun getProfessionals(): Map<String, Long> {
        // Create a mutable map to store counter name as key and its value as the value
        val countersMap = mutableMapOf<String, Long>()

        // Retrieve each counter from the repository and add it to the map
        val employedCounter = counterRepository.findByType(EmploymentStateCounters.EMPLOYED_COUNTER) ?: CounterEntity(EmploymentStateCounters.EMPLOYED_COUNTER, 0)
        countersMap[EmploymentStateCounters.EMPLOYED_COUNTER] = employedCounter.count

        val unemployedCounter = counterRepository.findByType(EmploymentStateCounters.UNEMPLOYED_COUNTER) ?: CounterEntity(EmploymentStateCounters.UNEMPLOYED_COUNTER, 0)
        countersMap[EmploymentStateCounters.UNEMPLOYED_COUNTER] = unemployedCounter.count

        val availableCounter = counterRepository.findByType(EmploymentStateCounters.AVAILABLE_FOR_WORK_COUNTER) ?: CounterEntity(EmploymentStateCounters.AVAILABLE_FOR_WORK_COUNTER, 0)
        countersMap[EmploymentStateCounters.AVAILABLE_FOR_WORK_COUNTER] = availableCounter.count

        val notAvailableCounter = counterRepository.findByType(EmploymentStateCounters.NOT_AVAILABLE_COUNTER) ?: CounterEntity(EmploymentStateCounters.NOT_AVAILABLE_COUNTER, 0)
        countersMap[EmploymentStateCounters.NOT_AVAILABLE_COUNTER] = notAvailableCounter.count

        // Return the map as a JSON response
        return countersMap
    }
}