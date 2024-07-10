package it.polito.students.crm.dtos

import jakarta.validation.constraints.Pattern

data class CreateTelephoneDTO(
    @field:Pattern(regexp = "^[0-9]+\$", message = "Telephone has to contain only digit characters")
    var telephone: String,
    var comment: String?,
)
