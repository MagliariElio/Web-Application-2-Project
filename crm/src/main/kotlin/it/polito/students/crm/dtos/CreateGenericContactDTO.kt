package it.polito.students.crm.dtos

import jakarta.validation.Valid

data class CreateGenericContactDTO(
    @Valid
    var emails: List<CreateEmailDTO>? = null,
    @Valid
    var telephones: List<CreateTelephoneDTO>? = null,
    @Valid
    var addresses: List<CreateAddressDTO>? = null,
)
