package it.polito.students.crm.dtos


data class CreateJobOfferDTO(
    var name: String,
    var description: String,
    var contractType: String,
    var location: String,
    var workMode: String,
    var requiredSkills: List<String>,
    var duration: Long,
    var note: String,
    var customerId: Long
)
