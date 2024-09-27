package it.polito.students.clientouth.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class CreateEmailDTO(
    @field: Email(message = "An Email has to be valid email address")
    var email: String,
    var comment: String?,
)
