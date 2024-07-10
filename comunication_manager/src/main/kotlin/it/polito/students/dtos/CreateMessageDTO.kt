package it.polito.students.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageDTO(
    var subject: String,
    var body: String,
    var priority: String,
    var channel: String,
    var sender: String,
)
