package com.example.analytics.services

import com.example.analytics.entities.CounterEntity
import com.example.analytics.repositories.CounterRepository
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CounterServiceImpl(
    private val counterRepository: CounterRepository
) : CounterService {
    private val logger = LoggerFactory.getLogger(CounterServiceImpl::class.java)

    @Transactional
    override fun incrementTotalMessages() {
        val totalCounter = counterRepository.findByType("totalMessages") ?: CounterEntity("totalMessages", 0)
        totalCounter.count += 1
        counterRepository.save(totalCounter)
    }

    @Transactional
    override fun incrementCompletedMessages() {
        val completedCounter = counterRepository.findByType("completedMessages") ?: CounterEntity("completedMessages", 0)
        completedCounter.count += 1
        counterRepository.save(completedCounter)
    }

    override fun getMessagesCompletionPercentage(): Double {
        val totalCounter = counterRepository.findByType("totalMessages") ?: CounterEntity("totalMessages", 0)
        val completedCounter = counterRepository.findByType("completedMessages") ?: CounterEntity("completedMessages", 0)

        val total = totalCounter.count
        val completed = completedCounter.count

        return if (total == 0L) 0.0 else (completed.toDouble() / total) * 100
    }

    @Transactional
    override fun incrementTotalJobOffers() {
        val totalCounter = counterRepository.findByType("totalJobOffers") ?: CounterEntity("totalJobOffers", 0)
        totalCounter.count += 1
        counterRepository.save(totalCounter)
    }

    @Transactional
    override fun incrementCompletedJobOffers() {
        val completedCounter = counterRepository.findByType("completedJobOffers") ?: CounterEntity("completedJobOffers", 0)
        completedCounter.count += 1
        counterRepository.save(completedCounter)
    }

    override fun getJobOffersCompletionPercentage(): Double {
        val totalCounter = counterRepository.findByType("totalJobOffers") ?: CounterEntity("totalJobOffers", 0)
        val completedCounter = counterRepository.findByType("completedJobOffers") ?: CounterEntity("completedJobOffers", 0)

        val total = totalCounter.count
        val completed = completedCounter.count

        return if (total == 0L) 0.0 else (completed.toDouble() / total) * 100
    }
}