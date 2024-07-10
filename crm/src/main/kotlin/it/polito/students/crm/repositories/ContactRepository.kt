package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : JpaRepository<Contact, Long> {
    fun findContactByName(name: String): Contact?
}