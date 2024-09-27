package it.polito.students.clientouth.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class CreateTelephoneDTO(
    @field:Pattern(regexp = "^[0-9]+\$", message = "Telephone has to contain only digit characters")
    var telephone: String,
    var comment: String?,
)
