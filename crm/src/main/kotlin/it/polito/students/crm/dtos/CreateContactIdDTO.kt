package it.polito.students.crm.dtos

import jakarta.validation.Valid

data class CreateContactIdDTO(
    @Valid
    var email: CreateEmailDTO? = null,
    @Valid
    var telephone: CreateTelephoneDTO? = null,
    @Valid
    var address: CreateAddressDTO? = null,
)
