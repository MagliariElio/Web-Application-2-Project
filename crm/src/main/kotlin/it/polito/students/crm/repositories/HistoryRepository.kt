package it.polito.students.crm.repositories

import it.polito.students.crm.entities.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository : JpaRepository<History, Long> {
    fun findByMessageId(messageId: Long): List<History>
}