package it.polito.students.crm.dtos

import it.polito.students.crm.entities.JobOffer
import it.polito.students.crm.utils.JobStatusEnum

data class JobOfferDTO(
    var id: Long,
    var name: String,
    var description: String,
    var contractType: String,
    var location: String,
    var workMode: String,
    var oldStatus: JobStatusEnum,
    var status: JobStatusEnum,
    var requiredSkills: List<String>,
    var duration: Long,
    var value: Double,
    var note: String,
    var customerId: Long,
    var professionalId: Long?,
    var candidateProfessionalIds: List<Long>,   // lista dei candidati che sono stati selezionati nella fase di selezione
    var candidatesProposalProfessional: List<Long>, // lista dei candidati che sono stati selezionati nella fase di candidate proposal
    var candidatesProfessionalRejected: List<Long>, // lista dei candidati che hanno rifiutato la candidatura
    var candidatesProfessionalRevoked: List<Long>, // lista dei candidati che hanno rifiutato la candidatura dopo aver accettato
)

fun JobOffer.toDTO(): JobOfferDTO = JobOfferDTO(
    this.id,
    this.name,
    this.description,
    this.contractType,
    this.location,
    this.workMode,
    this.oldStatus,
    this.status,
    this.requiredSkills,
    this.duration,
    this.value,
    this.note,
    this.customer.id,
    this.professional?.id,
    this.candidateProfessionals.map { it.id },
    this.candidatesProposalProfessional,
    this.candidatesProfessionalRejected,
    this.candidatesProfessionalRevoked
)