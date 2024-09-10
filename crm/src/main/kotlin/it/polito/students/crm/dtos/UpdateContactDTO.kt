package it.polito.students.crm.dtos

import jakarta.validation.constraints.Pattern

data class UpdateContactDTO(
    var id: Long,
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "The name should contain only alphabetic characters")
    var name: String?,
    @field:Pattern(regexp = "^[a-zA-Z ]+\$", message = "The surname should contain only alphabetic characters")
    var surname: String?,
    var ssnCode: String?,
    var category: String?,
    var comment: String?,
    var emails: List<EmailDTO>,
    var telephones: List<TelephoneDTO>,
    var addresses: List<AddressDTO>
)