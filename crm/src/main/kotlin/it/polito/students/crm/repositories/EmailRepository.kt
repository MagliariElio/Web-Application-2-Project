package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Email
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailRepository : JpaRepository<Email, Long> {
    fun findByEmail(email: String): Email?
}