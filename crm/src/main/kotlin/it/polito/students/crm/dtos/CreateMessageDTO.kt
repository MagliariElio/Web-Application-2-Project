package it.polito.students.crm.dtos

data class CreateMessageDTO(
    var subject: String,
    var body: String,
    var priority: String,
    var channel: String,
    var sender: String,
)
