package it.polito.students.crm.dtos


data class CreateJobOfferDTO(
    var requiredSkills: List<String>,

    var duration: Long,

    var note: String,

    var customerId: Long
)
