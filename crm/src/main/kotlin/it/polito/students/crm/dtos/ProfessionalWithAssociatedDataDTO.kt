package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Professional

data class ProfessionalWithAssociatedDataDTO(
    var professionalDTO: ProfessionalDTO,
    var jobofferDTOS: List<JobOfferDTO>
)

fun Professional.toDTOWithAssociatedData(): ProfessionalWithAssociatedDataDTO = ProfessionalWithAssociatedDataDTO(
    this.toDTO(),
    this.jobOffers.map { it.toDTO() }
)