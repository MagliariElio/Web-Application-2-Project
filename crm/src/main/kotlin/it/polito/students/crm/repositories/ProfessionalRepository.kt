package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Professional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfessionalRepository : JpaRepository<Professional, Long> {
}