package it.polito.students.crm.utils

import it.polito.students.crm.dtos.CustomerDTO
import it.polito.students.crm.entities.*
import it.polito.students.crm.repositories.JobOfferRepository
import org.springframework.stereotype.Component

@Component
class Factory(private val jobOfferRepository: JobOfferRepository) {

    companion object {
        fun CustomerDTO.toEntity(factory: Factory): Customer {
            val emailsList = information.emailDTOs.map {
                Email().apply {
                    id = it.id
                    email = it.email
                    comment = it.comment
                }
            }.toMutableSet()

            val telephonesList = information.telephoneDTOs.map {
                Telephone().apply {
                    id = it.id
                    telephone = it.telephone
                    comment = it.comment
                }
            }.toMutableSet()

            val addressesList = information.addressDTOs.map {
                Address().apply {
                    id = it.id
                    state = it.state
                    address = it.address
                    city = it.city
                    region = it.region
                    comment = it.comment
                }
            }.toMutableSet()

            val contact = Contact().apply {
                this.id = information.contactDTO.id
                this.name = information.contactDTO.name
                this.surname = information.contactDTO.surname
                this.ssnCode = information.contactDTO.ssnCode
                this.category = information.contactDTO.category
                this.comment = information.contactDTO.comment
                this.emails = emailsList
                this.addresses = addressesList
                this.telephones = telephonesList
            }

            val jobOffersList = factory.jobOfferRepository.findAllByCustomer_Id(this@toEntity.id).toMutableSet()

            return Customer().apply {
                id = this@toEntity.id
                information = contact
                joboffers = jobOffersList
            }
        }

        fun JobOffer.copy(): JobOffer {
            return JobOffer().apply {
                id = this@copy.id
                status = this@copy.status
                requiredSkills = this@copy.requiredSkills
                duration = this@copy.duration
                value = this@copy.value
                note = this@copy.note
                customer = this@copy.customer
                professional = this@copy.professional
            }
        }

        fun Professional.copy(): Professional {
            val professional = Professional().apply {
                id = this@copy.id
                information = this@copy.information
                skills = this@copy.skills
                employmentState = this@copy.employmentState
                geographicalLocation = this@copy.geographicalLocation
                dailyRate = this@copy.dailyRate
                deleted = this@copy.deleted
            }

            professional.addJobOffers(this@copy.jobOffers)
            return professional
        }
    }

}