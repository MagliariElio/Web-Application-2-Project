package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Telephone
import jakarta.validation.constraints.Pattern

class TelephoneDTO(
    var id: Long,
    @field:Pattern(regexp = "^[0-9]+\$", message = "Telephone has to contain only digit characters")
    var telephone: String,
    var comment: String
)

fun Telephone.toDTO(): TelephoneDTO = TelephoneDTO(
    this.id,
    this.telephone,
    this.comment
)
