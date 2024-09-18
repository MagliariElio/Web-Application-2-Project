package it.polito.students.crm.dtos

import it.polito.students.crm.utils.JobStatusEnum
import it.polito.students.crm.utils.StateOptions
import java.time.LocalDateTime

data class JobOfferAnalyticsDTO (
    var previousState: JobStatusEnum?,
    var actualState: JobStatusEnum?,
    var date: String,
    var creationTime: LocalDateTime?,
    var endTime: LocalDateTime?
)