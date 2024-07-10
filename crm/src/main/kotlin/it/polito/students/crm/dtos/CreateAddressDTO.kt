package it.polito.students.crm.dtos

data class CreateAddressDTO(
    var state: String?,
    var region: String?,
    var city: String?,
    var address: String?,
    var comment: String?,
)
