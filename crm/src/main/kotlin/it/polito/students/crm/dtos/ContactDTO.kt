package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Contact
import it.polito.students.crm.utils.CategoryOptions

data class ContactDTO(
    var id: Long,
    var name: String,
    var surname: String,
    var ssnCode: String,
    var category: CategoryOptions,
    var comment: String
)

fun Contact.toDTO(): ContactDTO = ContactDTO(
    this.id,
    this.name,
    this.surname,
    this.ssnCode,
    this.category,
    this.comment
)