package it.polito.students.integration.server

import it.polito.students.service.SendEmailService
import it.polito.students.service.SendEmailServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.spy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ComunicationManagerApplicationIntegrationServer {
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

}