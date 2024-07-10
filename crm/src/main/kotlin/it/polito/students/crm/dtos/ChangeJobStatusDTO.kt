package it.polito.students.crm.dtos

data class ChangeJobStatusDTO(
    var nextStatus: String,
    var professionalsId: List<Long>?,
    var note: String?
)
