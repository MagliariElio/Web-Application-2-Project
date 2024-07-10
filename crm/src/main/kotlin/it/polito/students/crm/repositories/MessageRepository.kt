package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Message
import it.polito.students.crm.utils.StateOptions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    fun findByActualState(state: StateOptions): List<Message>
}