package it.polito.students.unit.server

import com.google.api.services.gmail.model.Message
import it.polito.students.service.SendEmailService
import it.polito.students.service.SendEmailServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.spy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComunicationManagerApplicationUnitTest {
    @Value("\${spring.application.name:test-app-name}")
    lateinit var applicationName: String

    @Value("\${gmail.client-email:test@gmail.com}")
    lateinit var gmailClientEmail: String

    @Value("\${gmail.client-id:test-id}")
    lateinit var gmailClientId: String

    @Value("\${gmail.client-secret:test-secret}")
    lateinit var gmailClientSecret: String

    @Value("\${gmail.refresh-token:test-refresh-token}")
    lateinit var gmailClientRefreshToken: String

    private lateinit var emailService: SendEmailService

    @BeforeEach
    fun setUp() {
        emailService = spy(
            SendEmailServiceImpl(
                applicationName,
                gmailClientEmail,
                gmailClientId,
                gmailClientSecret,
                gmailClientRefreshToken
            )
        )
    }

    @Test
    fun testSendEmail_WithoutAttachment() {
        // Crea un'email di test
        val destEmailAddr = "email@test.test"
        val subject = "This is a test subject"
        val textBody = "This is a test email body"
        val file: File? = null

        // Invia l'email tramite il server
        val response: Message = emailService.sendEmail(destEmailAddr, subject, textBody, file)

        Assertions.assertNotNull(response)
    }

    @Test
    fun testSendEmail_WithAttachment() {
        // Crea un'email di test
        val destEmailAddr = "email@test.test"
        val subject = "This is a test subject"
        val textBody = "This is a test email body"
        val file = File("example_document.pdf")

        // Invia l'email tramite il server
        val response: Message = emailService.sendEmail(destEmailAddr, subject, textBody, file)

        Assertions.assertNotNull(response)
    }

    @Test
    fun testSendEmail_WithoutBody() {
        // Crea un'email di test
        val destEmailAddr = "email@test.test"
        val subject = "This is a test subject"
        val textBody = ""
        val file = File("example_document.pdf")

        // Invia l'email tramite il server
        val response: Message = emailService.sendEmail(destEmailAddr, subject, textBody, file)

        Assertions.assertNotNull(response)
    }

    @Test
    fun testSendEmail_WithSubject() {
        // Crea un'email di test
        val destEmailAddr = "email@test.test"
        val subject = ""
        val textBody = "This is a test email body"
        val file = File("example_document.pdf")

        // Invia l'email tramite il server
        val response: Message = emailService.sendEmail(destEmailAddr, subject, textBody, file)

        Assertions.assertNotNull(response)
    }

    @Test
    fun testSendEmail_WithSubjectBodyAndAttachment() {
        // Crea un'email di test
        val destEmailAddr = "email@test.test"
        val subject = ""
        val textBody = ""
        val file =  null

        // Invia l'email tramite il server
        val response: Message = emailService.sendEmail(destEmailAddr, subject, textBody, file)

        Assertions.assertNotNull(response)
    }

}