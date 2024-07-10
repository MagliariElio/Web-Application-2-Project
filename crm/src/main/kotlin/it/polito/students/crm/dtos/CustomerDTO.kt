package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Customer

data class CustomerDTO(
    var id: Long,
    var information: ContactWithAssociatedDataDTO,
    val jobOffers: List<JobOfferDTO>
)

fun Customer.toDTO(): CustomerDTO {
    return CustomerDTO(
        this.id,
        this.information.toDTOWithAssociatedData(),
        this.joboffers.map { it.toDTO() }
    )
}