package it.polito.students.clientouth.dtos


data class ProfessionalWithAssociatedDataDTO(
    var professionalDTO: ProfessionalDTO,
    var jobofferDTOS: List<Any>
)
data class ProfessionalDTO(
    var id: Long,
    var information: ContactDTO,
    var skills: List<String>,
    var employmentState: EmploymentStateEnum,
    var geographicalLocation: String,
    var dailyRate: Double,
    var attachmentsList: List<Long>
)

data class ContactDTO(
    var id: Long,
    var name: String,
    var surname: String,
    var ssnCode: String,
    var category: CategoryOptions,
    var comment: String
)

enum class CategoryOptions {
    CUSTOMER,
    PROFESSIONAL,
    UNKNOWN,
}

enum class EmploymentStateEnum {
    EMPLOYED,
    UNEMPLOYED,
    AVAILABLE_FOR_WORK,
    NOT_AVAILABLE
}