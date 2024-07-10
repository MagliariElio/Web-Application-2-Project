package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Telephone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TelephoneRepository : JpaRepository<Telephone, Long> {
    fun findByTelephone(telephone: String): Telephone?
}