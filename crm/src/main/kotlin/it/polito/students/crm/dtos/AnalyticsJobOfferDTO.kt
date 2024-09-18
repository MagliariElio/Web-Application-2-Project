package it.polito.students.crm.dtos

import it.polito.students.crm.utils.JobStatusEnum
import java.time.LocalDateTime

data class AnalyticsJobOfferDTO (
    var contractType: String,
    var workMode: String
)