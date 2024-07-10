package it.polito.students.crm.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.HistoryDTO
import it.polito.students.crm.dtos.MessageDTO
import it.polito.students.crm.dtos.UpdateMessageDTO
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.utils.ErrorsPage
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.StateOptions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmMessageControllerIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private val messagesDTOList: List<MessageDTO> = listOf(
        MessageDTO(
            id = 1,
            date = LocalDateTime.now(),
            subject = "Subject 1",
            body = "body 1",
            actualState = StateOptions.READ,
            priority = PriorityEnumOptions.HIGH,
            channel = "email",
            sender = "test@unit.test"
        ),
        MessageDTO(
            id = 2,
            date = LocalDateTime.now(),
            subject = "Subject 2",
            body = "body 2",
            actualState = StateOptions.DONE,
            priority = PriorityEnumOptions.MEDIUM,
            channel = "email",
            sender = "test@2unit.test"
        ),
        MessageDTO(
            id = 3,
            date = LocalDateTime.now(),
            subject = "Subject 3",
            body = "body 3",
            actualState = StateOptions.FAILED,
            priority = PriorityEnumOptions.LOW,
            channel = "email",
            sender = "test3@unit.test"
        )
    )

    private val messageHistoryDTOList = listOf(
        HistoryDTO(
            id = 1,
            state = StateOptions.RECEIVED,
            date = LocalDateTime.now().minusDays(1),
            comment = "This is a comment!",
        ),
        HistoryDTO(
            id = 2,
            state = StateOptions.READ,
            date = LocalDateTime.now().minusDays(1),
            comment = "This is a comment!",
        ),
        HistoryDTO(
            id = 3,
            state = StateOptions.DONE,
            date = LocalDateTime.now(),
            comment = "This is a comment!",
        )
    )

    private val createMessageWithEmailDto = CreateMessageDTO(
        sender = "john.doe@example.com",
        subject = "Test Subject",
        channel = "Email",
        priority = "medium",
        body = "This is a test message."
    )

    private val createMessageWithPhoneDto = CreateMessageDTO(
        sender = "3334448889",
        subject = "Test Subject",
        channel = "SMS",
        priority = "medium",
        body = "This is a test message."
    )

    fun createAMessageAndReturnItsId(): Long {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        //Create a message
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        return responseContent.substringAfter("id\":").substringBefore(",").toLong()
    }

    /**
     * getMessage TEST CASES
     */
    @Test
    fun getMessage_goodCase() {
        val messageId = createAMessageAndReturnItsId()

        //Get message
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${messageId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(8))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(messageId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.date").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.subject").value(messagesDTOList[0].subject))
            .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(messagesDTOList[0].body))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualState").value(StateOptions.RECEIVED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.priority").value(messagesDTOList[0].priority.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.channel").value(messagesDTOList[0].channel))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sender").value(messagesDTOList[0].sender))
    }

    @Test
    fun getMessage_negativeId() {
        createAMessageAndReturnItsId()
        val messageId = -3

        //Get message
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${messageId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getMessage_messageNotFound() {
        val messageId = 999

        //Get message
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${messageId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    /**
     * getMessageHistory TEST CASES
     */
    @Test
    fun getMessageHistory_goodCase() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        //Create a message
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get message history
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${id}/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value(StateOptions.RECEIVED.name))
    }

    @Test
    fun getMessageHistory_goodCaseUpdatingTheMessage() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Update the message
        val bodyValueUpdate = UpdateMessageDTO(
            actualState = StateOptions.READ.name,
            comment = "New comment",
            priority = PriorityEnumOptions.LOW.name
        )

        val objectMapperUpdate = ObjectMapper()
        val requestBodyUpdate = objectMapperUpdate.writeValueAsString(bodyValueUpdate)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyUpdate)
        )

        //Get message history
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${id}/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value(StateOptions.RECEIVED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].state").value(StateOptions.READ.name))
    }

    @Test
    fun getMessageHistory_negativeId() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        //Create a message
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )

        //Get message history with negative id
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/-1/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.error")
                    .value("The provided value for MessageId is not valid. Please ensure that you provide a positive integer for the MessageId parameter.")
            )
    }

    @Test
    fun getMessageHistory_messageNotFound() {
        //Get message history of a non-existing message
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/1000/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$").value("The message with id equal to 1000 was not found!"))
    }

    /**
     * updateMessage TEST CASES
     */
    @Test
    fun updateMessage_newPriority() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            null,
                            "New comment",
                            PriorityEnumOptions.MEDIUM.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))

    }

    @Test
    fun updateMessage_newState() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            StateOptions.READ.name,
                            "New comment",
                            null
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualState").value(StateOptions.READ.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
    }

    @Test
    fun updateMessage_newStateAndPriority() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            StateOptions.READ.name,
                            "New comment",
                            PriorityEnumOptions.MEDIUM.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualState").value(StateOptions.READ.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
    }

    @Test
    fun updateMessage_badState() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            "BAD STATE",
                            "New comment",
                            PriorityEnumOptions.MEDIUM.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value("The specified state was not found. Please ensure that the state provided in the request body matches one of the following options: [RECEIVED, READ, DISCARDED, PROCESSING, DONE, FAILED].")
            )
    }

    @Test
    fun updateMessage_badPriority() {
        //Create the message
        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            StateOptions.READ.name,
                            "New comment",
                            "BAD PRIORITY"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value("The specified priority was not found. Please ensure that the priority provided in the request body matches one of the following options: [HIGH, MEDIUM_HIGH, MEDIUM, MEDIUM_LOW, LOW].")
            )
    }

    @Test
    fun updateMessage_messageNotFound() {
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/messages/1000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        UpdateMessageDTO(
                            StateOptions.READ.name,
                            "New comment",
                            PriorityEnumOptions.MEDIUM.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    /**
     * STORE MESSAGE TEST CASES
     */
    @Test
    fun postMessage_goodCaseEmail() {
        val bodyValue = createMessageWithEmailDto

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sender").value(bodyValue.sender))
            .andExpect(MockMvcResultMatchers.jsonPath("$.subject").value(bodyValue.subject))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualState").value(StateOptions.RECEIVED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.channel").value(bodyValue.channel))
            .andExpect(MockMvcResultMatchers.jsonPath("$.priority").value(bodyValue.priority.uppercase()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(bodyValue.body))
            .andReturn()

        // Extracting ID from the response content
        val idMessageSaved = result.response.contentAsString.substringAfter("id\":").substringBefore(",").toLong()

        //Get message history
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${idMessageSaved}/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value(StateOptions.RECEIVED.name))
    }

    @Test
    fun postMessage_goodCaseWithPhone() {
        val bodyValue = createMessageWithPhoneDto

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sender").value(bodyValue.sender))
            .andExpect(MockMvcResultMatchers.jsonPath("$.subject").value(bodyValue.subject))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actualState").value(StateOptions.RECEIVED.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.channel").value(bodyValue.channel))
            .andExpect(MockMvcResultMatchers.jsonPath("$.priority").value(bodyValue.priority.uppercase()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.body").value(bodyValue.body))
            .andReturn()

        // Extracting ID from the response content
        val idMessageSaved = result.response.contentAsString.substringAfter("id\":").substringBefore(",").toLong()

        //Get message history
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/messages/${idMessageSaved}/history")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value(StateOptions.RECEIVED.name))
    }

    @Test
    fun postMessage_badSender() {
        val bodyValue = createMessageWithPhoneDto
        bodyValue.sender = "INVALID SENDER"

        val expectedMessageError = "${ErrorsPage.SENDER_ERROR} senderType: NONE"

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

    @Test
    fun postMessage_emptySender() {
        val bodyValue = createMessageWithEmailDto
        bodyValue.sender = ""

        val expectedMessageError = ErrorsPage.SENDER_MISSING_ERROR

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

    @Test
    fun postMessage_badPriority() {
        val bodyValue = createMessageWithPhoneDto
        bodyValue.priority = "INVALID PRIORITY"

        val expectedMessageError = ErrorsPage.PRIORITY_ERROR

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

    @Test
    fun postMessage_emptySubject() {
        val bodyValue = createMessageWithEmailDto
        bodyValue.subject = ""

        val expectedMessageError = ErrorsPage.SUBJECT_MISSING_ERROR

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

    @Test
    fun postMessage_emptyChannel() {
        val bodyValue = createMessageWithEmailDto
        bodyValue.channel = ""

        val expectedMessageError = ErrorsPage.CHANNEL_MISSING_ERROR

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

    @Test
    fun postMessage_emptyBody() {
        val bodyValue = createMessageWithEmailDto
        bodyValue.body = ""

        val expectedMessageError = ErrorsPage.BODY_MISSING_ERROR

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }

}