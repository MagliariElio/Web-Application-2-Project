package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Professional
import it.polito.students.crm.utils.EmploymentStateEnum

data class ProfessionalDTO(
    var id: Long,
    var information: ContactDTO,
    var skills: List<String>,
    var employmentState: EmploymentStateEnum,
    var geographicalLocation: String,
    var dailyRate: Double,
    var attachmentsList: List<Long>
)

fun Professional.toDTO(): ProfessionalDTO = ProfessionalDTO(
    this.id,
    this.information.toDTO(),
    this.skills,
    this.employmentState,
    this.geographicalLocation,
    this.dailyRate,
    this.attachmentsList
)