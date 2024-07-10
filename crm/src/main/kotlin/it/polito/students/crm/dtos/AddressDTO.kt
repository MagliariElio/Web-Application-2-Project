package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Address

data class AddressDTO(
    var id: Long,
    var state: String,
    var region: String,
    var city: String,
    var address: String,
    var comment: String
)

fun Address.toDTO(): AddressDTO = AddressDTO(
    this.id,
    this.state,
    this.region,
    this.city,
    this.address,
    this.comment
)

