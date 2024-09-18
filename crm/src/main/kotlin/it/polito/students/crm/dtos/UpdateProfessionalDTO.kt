package it.polito.students.crm.dtos

data class UpdateProfessionalDTO(
    var id: Long,
    var information: UpdateContactDTO,
    var skills: List<String>,
    var employmentState: String,
    var geographicalLocation: String,
    var dailyRate: Double,

    )