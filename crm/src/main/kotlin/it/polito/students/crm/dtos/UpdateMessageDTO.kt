package it.polito.students.crm.dtos

data class UpdateMessageDTO(
    val actualState: String?,
    val comment: String?,
    val priority: String?
)
