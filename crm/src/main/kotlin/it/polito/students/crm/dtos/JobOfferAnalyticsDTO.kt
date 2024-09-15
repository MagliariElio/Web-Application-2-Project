package it.polito.students.crm.dtos

import it.polito.students.crm.utils.JobStatusEnum

data class JobOfferAnalyticsDTO(
    var previousState: JobStatusEnum?,
    var actualState: JobStatusEnum?,
    var date: String
)