package it.polito.students.crm.dtos

import it.polito.students.crm.utils.EmploymentStateEnum

data class ProfessionalAnalyticsDTO (
    var previousState: EmploymentStateEnum?,
    var actualState: EmploymentStateEnum
)