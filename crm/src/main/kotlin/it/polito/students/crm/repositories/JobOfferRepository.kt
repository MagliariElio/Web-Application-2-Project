package it.polito.students.crm.repositories

import it.polito.students.crm.entities.JobOffer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobOfferRepository : JpaRepository<JobOffer, Long> {
    fun findAllByCustomer_Id(customerId: Long): List<JobOffer>
}