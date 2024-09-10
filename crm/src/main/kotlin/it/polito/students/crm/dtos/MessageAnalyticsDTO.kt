package it.polito.students.crm.dtos

import it.polito.students.crm.utils.StateOptions

data class MessageAnalyticsDTO (
    var previousState: StateOptions?,
    var actualState: StateOptions
)