package it.polito.students.crm.dtos

import it.polito.students.crm.utils.JobStatusEnum
import it.polito.students.crm.utils.StateOptions

data class JobOfferAnalyticsDTO (
    var previousState: JobStatusEnum?,
    var actualState: JobStatusEnum
)