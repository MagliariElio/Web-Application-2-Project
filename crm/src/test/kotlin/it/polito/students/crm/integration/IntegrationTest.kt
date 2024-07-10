package it.polito.students.crm.integration

import it.polito.students.crm.dtos.*
import it.polito.students.crm.utils.CategoryOptions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@ContextConfiguration(initializers = [IntegrationTest.Initializer::class])
abstract class IntegrationTest {
    companion object {
        private val db = PostgreSQLContainer("postgres:latest")
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            db.start()
            TestPropertyValues.of(
                "spring.datasource.url=${db.jdbcUrl}",
                "spring.datasource.username=${db.username}",
                "spring.datasource.password=${db.password}"
            ).applyTo(applicationContext.environment)
        }
    }

    fun createDTOFromString(responseContent: String): ContactWithAssociatedDataDTO {
        val contactDto = createContactDTOFromString(responseContent.substringAfter("\"contactDTO\":"))

        val emails = mutableListOf<EmailDTO>()
        val emailMatcher = Regex("\"id\":(\\d+),\"email\":\"(.*?)\",\"comment\":\"(.*?)\"")
        emailMatcher.findAll(responseContent.substringAfter("\"emailDTOs\":")).forEach { matchResult ->
            val (emailId, email, emailComment) = matchResult.destructured
            emails.add(EmailDTO(emailId.toLong(), email, emailComment))
        }

        val telephones = mutableListOf<TelephoneDTO>()
        val telephoneMatcher = Regex("\"id\":(\\d+),\"telephone\":\"(.*?)\",\"comment\":\"(.*?)\"")
        telephoneMatcher.findAll(responseContent.substringAfter("\"telephoneDTOs\":")).forEach { matchResult ->
            val (telephoneId, telephone, telephoneComment) = matchResult.destructured
            telephones.add(TelephoneDTO(telephoneId.toLong(), telephone, telephoneComment))
        }

        val addresses = mutableListOf<AddressDTO>()
        val addressMatcher =
            Regex("\"id\":(\\d+),\"state\":\"(.*?)\",\"region\":\"(.*?)\",\"city\":\"(.*?)\",\"address\":\"(.*?)\",\"comment\":\"(.*?)\"")
        addressMatcher.findAll(responseContent.substringAfter("\"addressDTOs\":")).forEach { matchResult ->
            val (addressId, state, region, city, address, addressComment) = matchResult.destructured
            addresses.add(AddressDTO(addressId.toLong(), state, region, city, address, addressComment))
        }

        return ContactWithAssociatedDataDTO(
            contactDto,
            emails,
            telephones,
            addresses
        )
    }

    fun createContactDTOFromString(responseContent: String): ContactDTO {
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(",").trim().toLong()

        val name = responseContent.substringAfter("\"name\":").substringBefore(",\"surname\"").trim('"')
        val surname = responseContent.substringAfter("\"surname\":").substringBefore(",\"ssnCode\"").trim('"')
        val ssnCode = responseContent.substringAfter("\"ssnCode\":").substringBefore(",\"category\"").trim('"')
        val category = responseContent.substringAfter("\"category\":").substringBefore(",\"comment\"").trim('"')
        val comment = responseContent.substringAfter("\"comment\":").substringBefore("},").trim('"')

        return ContactDTO(contactId, name, surname, ssnCode, CategoryOptions.valueOf(category), comment)
    }
}
