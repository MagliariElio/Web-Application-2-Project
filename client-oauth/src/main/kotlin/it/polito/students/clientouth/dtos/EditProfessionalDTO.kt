package it.polito.students.clientouth.dtos

data class EditProfessionalDTO(
    var id: Long,
    var information: EditContactDTO,
    var skills: List<String>,
    var employmentState: String,
    var geographicalLocation: String,
    var dailyRate: Double,
    var attachmentsList: List<Long>?
)
