package it.polito.students.clientouth.dtos

data class CreateProfessionalDTO(
    var information: CreateContactDTO,
    var skills: List<String>,
    var geographicalLocation: String,
    var dailyRate: Double
)
