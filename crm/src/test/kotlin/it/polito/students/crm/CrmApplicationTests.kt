package it.polito.students.crm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.JobStatusEnum
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.io.IOException

@SpringBootTest
class CrmApplicationTests {

    @Test
    fun contextLoads() {
    }

    companion object {
        fun createContactDTOFromString(responseContent: String): ContactDTO {
            val mapper = ObjectMapper()
            val contactJson = mapper.readTree(responseContent)

            val contactId = contactJson["id"].asLong()
            val name = contactJson["name"].asText()
            val surname = contactJson["surname"].asText()
            val ssnCode = contactJson["ssnCode"].asText()
            val category = CategoryOptions.valueOf(contactJson["category"].asText()).toString()
            val comment = contactJson["comment"].asText()

            return ContactDTO(contactId, name, surname, ssnCode, CategoryOptions.valueOf(category), comment)
        }

        fun createEmailDTOListFromString(emailListDTO: JsonNode): List<EmailDTO> {
            val list = mutableListOf<EmailDTO>()

            emailListDTO.forEach { emailDTO ->
                val id = emailDTO["id"].asLong()
                val email = emailDTO["email"].asText()
                val comment = emailDTO["comment"].asText()
                list.add(EmailDTO(id, email, comment))
            }

            return list
        }

        fun createAddressDTOListFromString(addressesListDTO: JsonNode): List<AddressDTO> {
            val list = mutableListOf<AddressDTO>()

            addressesListDTO.forEach { addressDTO ->
                val id = addressDTO["id"].asLong()
                val address = addressDTO["address"].asText()
                val city = addressDTO["city"].asText()
                val region = addressDTO["region"].asText()
                val state = addressDTO["state"].asText()
                val comment = addressDTO["comment"].asText()
                list.add(AddressDTO(id, state, region, city, address, comment))
            }

            return list
        }

        fun createTelephoneDTOListFromString(telephonesListDTO: JsonNode): List<TelephoneDTO> {
            val list = mutableListOf<TelephoneDTO>()

            telephonesListDTO.forEach { telephoneDTO ->
                val id = telephoneDTO["id"].asLong()
                val telephone = telephoneDTO["telephone"].asText()
                val comment = telephoneDTO["comment"].asText()
                list.add(TelephoneDTO(id, telephone, comment))
            }

            return list
        }

        /*fun createCustomerDTOFromString(responseContent: String): CustomerDTO {
            val mapper = ObjectMapper()
            val contactJson = mapper.readTree(responseContent)

            val customerID = contactJson["id"].asLong()
            val information = createContactDTOFromString(contactJson["information"].asText())
            val jobOffers = createJobOfferDTOListFromString(contactJson["jobOffers"].asText())

            return CustomerDTO(customerID, information, jobOffers)
        }*/

        fun createProfessionalDTOFromJSONNode(professionalJson: JsonNode): ProfessionalDTO {
            val id = professionalJson["id"].asLong()
            val information = createContactDTOFromString(professionalJson["information"].toString())
            val skills = professionalJson["skills"].map { it.asText() }
            val employmentState = EmploymentStateEnum.valueOf(professionalJson["employmentState"].asText())
            val geographicalLocation = professionalJson["geographicalLocation"].asText()
            val dailyRate = professionalJson["dailyRate"].asDouble()

            return ProfessionalDTO(id, information, skills, employmentState, geographicalLocation, dailyRate)
        }

        fun createProfessionalDTOFromString(responseContent: String): ProfessionalDTO {
            val mapper = ObjectMapper()
            val responseJson = mapper.readTree(responseContent)

            lateinit var professionalJson: JsonNode
            responseJson["professionalDTO"].apply {
                professionalJson = this
            }

            val id = professionalJson["id"].asLong()
            val information = createContactDTOFromString(professionalJson["information"].toString())
            val skills = professionalJson["skills"].map { it.asText() }
            val employmentState = EmploymentStateEnum.valueOf(professionalJson["employmentState"].asText())
            val geographicalLocation = professionalJson["geographicalLocation"].asText()
            val dailyRate = professionalJson["dailyRate"].asDouble()

            return ProfessionalDTO(id, information, skills, employmentState, geographicalLocation, dailyRate)
        }

        fun createProfessionalDTOFromListString(responseContent: String): List<ProfessionalDTO> {
            val mapper = ObjectMapper()
            val responseJson = mapper.readTree(responseContent)

            val list = mutableListOf<ProfessionalDTO>()
            responseJson["content"].forEach {
                list.add(createProfessionalDTOFromJSONNode(it))
            }

            return list
        }

        fun createJobOfferDTOFromString(jobOfferString: String): JobOfferDTO {
            val mapper = ObjectMapper()
            lateinit var jobOfferJson: JsonNode

            try {
                val responseJson = mapper.readTree(jobOfferString)
                jobOfferJson = responseJson
            } catch (e: IOException) {
                throw IllegalArgumentException("Invalid JSON string: $jobOfferString")
            }

            val id = jobOfferJson["id"].asLong()
            val status = JobStatusEnum.valueOf(jobOfferJson["status"].asText())
            val requiredSkills = jobOfferJson["requiredSkills"].map { it.asText() }
            val duration = jobOfferJson["duration"].asLong()
            val value = jobOfferJson["value"].asDouble()
            val note = jobOfferJson["note"].asText()
            val customerId = jobOfferJson["customerId"].asLong()
            val professionalId =
                if (jobOfferJson.has("professionalId") && jobOfferJson["professionalId"].asLong() > 0) jobOfferJson["professionalId"].asLong() else null

            return JobOfferDTO(id, status, requiredSkills, duration, value, note, customerId, professionalId, emptyList())
        }

        /**
         * It converts a CreateContactDto to a ContactDto
         */
        fun convertCreateContactDtoToContactDto(createContactDTO: CreateContactDTO): ContactDTO {
            val category: CategoryOptions? = try {
                createContactDTO.category?.let { CategoryOptions.valueOf(it) }
            } catch (e: IllegalArgumentException) {
                CategoryOptions.CUSTOMER
            }

            val ssnCode = createContactDTO.ssnCode ?: ""
            val comment = createContactDTO.comment ?: ""

            return ContactDTO(
                id = 2,
                name = createContactDTO.name,
                surname = createContactDTO.surname,
                ssnCode = ssnCode,
                category = category!!,
                comment = comment
            )
        }
    }
}
