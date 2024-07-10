package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Message
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.StateOptions
import java.time.LocalDateTime

data class MessageDTO(
    var id: Long,
    var date: LocalDateTime,
    var subject: String,
    var body: String,
    var actualState: StateOptions,
    var priority: PriorityEnumOptions,
    var channel: String,
    var sender: String,
)

fun Message.toDTO(): MessageDTO = MessageDTO(
    this.id,
    this.date,
    this.subject,
    this.body,
    this.actualState,
    this.priority,
    this.channel,
    this.sender,
)
