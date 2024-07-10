package it.polito.students.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ComunicationManagerApplicationIntegrationController {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun testSendEmail() {
        val emailAddress = "example@gmail.com"
        val textSubject = "This is a test subject"
        val textBody = "This is a test email body"
        val noAttachment = "no-attachment"

        val requestBody = ObjectMapper().writeValueAsString(textBody)
        val url = "http://localhost:8081/API/emails?destEmailAddr=${emailAddress}&subject=${textSubject}&textBody=${textBody}"

        mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.sentEmail.destEmailAddr").value(emailAddress))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sentEmail.subject").value(textSubject))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sentEmail.textBody").value(textBody))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sentEmail.attachment").value(noAttachment))

    }
}