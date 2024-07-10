package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Email

data class EmailDTO(
    var id: Long,
    var email: String,
    var comment: String
)

fun Email.toDTO(): EmailDTO = EmailDTO(
    this.id,
    this.email,
    this.comment
)