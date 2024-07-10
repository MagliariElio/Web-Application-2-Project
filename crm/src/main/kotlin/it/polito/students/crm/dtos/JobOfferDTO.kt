package it.polito.students.crm.dtos

import it.polito.students.crm.entities.JobOffer
import it.polito.students.crm.utils.JobStatusEnum

data class JobOfferDTO(
    var id: Long,
    var status: JobStatusEnum,
    var requiredSkills: List<String>,
    var duration: Long,
    var value: Double,
    var note: String,
    var customerId: Long,
    var professionalId: Long?,
    var candidateProfessionalIds: List<Long>
)

fun JobOffer.toDTO(): JobOfferDTO = JobOfferDTO(
    this.id,
    this.status,
    this.requiredSkills,
    this.duration,
    this.value,
    this.note,
    this.customer.id,
    this.professional?.id,
    this.candidateProfessionals.map { it.id }
)