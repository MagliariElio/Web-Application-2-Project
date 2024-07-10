package it.polito.students.unit.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ComunicationManagerApplicationUnitTest(
    @Autowired val mockMvc: MockMvc
) {

    @Test
    fun testSuccessPost_WithoutAttachment() {

        val responseExpected = """
            {
                "sentEmail":{
                    "destEmailAddr": "test@test.com",
                    "subject": "This a test Subject",
                    "textBody": "This is a test body",
                    "attachment": "no-attachment"
                }
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .param("destEmailAddr", "test@test.com")
                .param("subject", "This a test Subject")
                .param("textBody", "This is a test body")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithAttach() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "sentEmail":{
                    "destEmailAddr": "test@test.com",
                    "subject": "This a test Subject",
                    "textBody": "This is a test body",
                    "attachment": "example_document.pdf"
                }
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "test@test.com")
                .param("subject", "This a test Subject")
                .param("textBody", "This is a test body")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithBody() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "sentEmail":{
                    "destEmailAddr": "test@test.com",
                    "subject": "This a test Subject",
                    "textBody": null,
                    "attachment": "example_document.pdf"
                }
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "test@test.com")
                .param("subject", "This a test Subject")
                .param("textBody", null)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithSubject() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "sentEmail":{
                    "destEmailAddr": "test@test.com",
                    "subject": null,
                    "textBody": "This is a test body",
                    "attachment": "example_document.pdf"
                }
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "test@test.com")
                .param("subject", null)
                .param("textBody", "This is a test body")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithSubjectAndBody() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "sentEmail":{
                    "destEmailAddr": "test@test.com",
                    "subject": null,
                    "textBody": null,
                    "attachment": "example_document.pdf"
                }
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "test@test.com")
                .param("subject", null)
                .param("textBody", null)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithBlankDestinationAddress() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "error": "Destination address can not be blank! Please provide correct string for recipient's address"
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "")
                .param("subject", "This a test Subject")
                .param("textBody", "This is a test body")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(content().json(responseExpected))
    }

    @Test
    fun testSuccessPost_WithIncorrectDestinationAddress() {

        val multipartFile = MockMultipartFile(
            "multipartFile",
            "example_document.pdf",
            "application/pdf",
            "example_document.pdf".toByteArray()
        )

        val responseExpected = """
            {
                "error": "Wrong email address! Please provide a valid one (e.g. example@mail.com)"
            }"""

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/API/emails")
                .file(multipartFile)
                .param("destEmailAddr", "test.test")
                .param("subject", "This a test Subject")
                .param("textBody", "This is a test body")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(content().json(responseExpected))
    }

}
