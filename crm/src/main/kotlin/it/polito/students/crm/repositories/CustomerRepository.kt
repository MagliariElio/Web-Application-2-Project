package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {
    fun findAllByDeletedFalse(pageable: Pageable): Page<Customer>
}