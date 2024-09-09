package it.polito.students.crm.dtos

data class CreateWhatContactDTO(
    var createEmailDTO: CreateEmailDTO?,
    var createTelephoneDTO: CreateTelephoneDTO?,
    var createAddressDTO: CreateAddressDTO?
)