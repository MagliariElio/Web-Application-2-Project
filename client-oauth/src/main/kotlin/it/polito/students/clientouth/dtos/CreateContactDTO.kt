package it.polito.students.clientouth.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class CreateContactDTO(
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "The name should contain only alphabetic characters")
    var name: String,
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "The surname should contain only alphabetic characters")
    var surname: String,
    var ssnCode: String?,
    var category: String?,
    var comment: String?,
    @Valid
    var emails: List<CreateEmailDTO>?,
    @Valid
    var telephones: List<CreateTelephoneDTO>?,
    @Valid
    var addresses: List<CreateAddressDTO>?,
)
