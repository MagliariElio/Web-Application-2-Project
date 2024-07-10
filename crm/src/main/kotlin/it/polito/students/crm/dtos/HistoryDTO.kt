package it.polito.students.crm.dtos

import it.polito.students.crm.entities.History
import it.polito.students.crm.utils.StateOptions
import java.time.LocalDateTime

data class HistoryDTO(
    var id: Long,
    var state: StateOptions,
    var date: LocalDateTime,
    var comment: String,
)

fun History.toDTO(): HistoryDTO = HistoryDTO(
    this.id,
    this.state,
    this.date,
    this.comment,
)
