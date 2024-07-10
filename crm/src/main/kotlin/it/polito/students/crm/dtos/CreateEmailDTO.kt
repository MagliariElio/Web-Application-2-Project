package it.polito.students.crm.dtos

import jakarta.validation.constraints.Email

data class CreateEmailDTO(
    @field: Email(message = "An Email has to be valid email address")
    var email: String,
    var comment: String?,
)
