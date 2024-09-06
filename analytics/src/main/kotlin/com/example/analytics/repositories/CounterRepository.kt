package com.example.analytics.repositories

import com.example.analytics.entities.CounterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CounterRepository : JpaRepository<CounterEntity, String> {
    fun findByType(type: String): CounterEntity?
}