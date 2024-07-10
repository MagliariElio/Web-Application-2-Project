package it.polito.students.crm.dtos

data class CreateProfessionalDTO(
    var information: CreateContactDTO,
    var skills: List<String>,
    var geographicalLocation: String,
    var dailyRate: Double
)