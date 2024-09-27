package it.polito.students.clientouth.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class CreateAddressDTO(
    var state: String?,
    var region: String?,
    var city: String?,
    var address: String?,
    var comment: String?,
)
