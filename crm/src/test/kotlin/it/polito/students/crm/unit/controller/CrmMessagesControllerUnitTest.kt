package it.polito.students.crm.unit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Message
import it.polito.students.crm.exception_handlers.MessageNotFoundException
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.ErrorsPage.Companion.PAGE_AND_LIMIT_ERROR
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.StateOptions
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest
class CrmMessagesControllerUnitTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var messageService: MessageService

    @MockkBean
    lateinit var jobOfferService: JobOfferService

    @MockkBean
    lateinit var customerService: CustomerService

    @MockkBean
    lateinit var professionalService: ProfessionalService

    @MockkBean
    lateinit var contactService: ContactServiceImpl

    @MockkBean
    lateinit var emailService: EmailService

    @MockkBean
    lateinit var addressService: AddressService

    @MockkBean
    lateinit var telephoneService: TelephoneService

    // Test cases
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

    /**
     * GET MESSAGES TEST CASES
     */
    @Test
    fun getMessages_statusOK() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?page=0&limit=30"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(messagesDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
            .andExpect(jsonPath("$.content[2].id").value(messagesDTOList[2].id))
            .andExpect(jsonPath("$.content[2].subject").value(messagesDTOList[2].subject))
            .andExpect(jsonPath("$.content[2].body").value(messagesDTOList[2].body))
            .andExpect(jsonPath("$.content[2].actualState").value(messagesDTOList[2].actualState.name))
            .andExpect(jsonPath("$.content[2].priority").value(messagesDTOList[2].priority.name))
            .andExpect(jsonPath("$.content[2].channel").value(messagesDTOList[2].channel))
            .andExpect(jsonPath("$.content[2].sender").value(messagesDTOList[2].sender))
    }

    @Test
    fun getMessages_statusOKLimitLow() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?page=0&limit=2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
    }

    @Test
    fun getMessages_invalidPage() {
        mockMvc.perform(get("/API/messages?page=-1&limit=30"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(PAGE_AND_LIMIT_ERROR))
    }

    @Test
    fun getMessages_invalidLimit() {
        mockMvc.perform(get("/API/messages?page=0&limit=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(PAGE_AND_LIMIT_ERROR))
    }

    @Test
    fun getMessages_invalidPageAndLimit() {
        mockMvc.perform(get("/API/messages?page=-1&limit=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(PAGE_AND_LIMIT_ERROR))
    }

    @Test
    fun getMessages_noPage() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?limit=30"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(messagesDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
            .andExpect(jsonPath("$.content[2].id").value(messagesDTOList[2].id))
            .andExpect(jsonPath("$.content[2].subject").value(messagesDTOList[2].subject))
            .andExpect(jsonPath("$.content[2].body").value(messagesDTOList[2].body))
            .andExpect(jsonPath("$.content[2].actualState").value(messagesDTOList[2].actualState.name))
            .andExpect(jsonPath("$.content[2].priority").value(messagesDTOList[2].priority.name))
            .andExpect(jsonPath("$.content[2].channel").value(messagesDTOList[2].channel))
            .andExpect(jsonPath("$.content[2].sender").value(messagesDTOList[2].sender))
    }

    @Test
    fun getMessages_noLimit() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?page=0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(messagesDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
            .andExpect(jsonPath("$.content[2].id").value(messagesDTOList[2].id))
            .andExpect(jsonPath("$.content[2].subject").value(messagesDTOList[2].subject))
            .andExpect(jsonPath("$.content[2].body").value(messagesDTOList[2].body))
            .andExpect(jsonPath("$.content[2].actualState").value(messagesDTOList[2].actualState.name))
            .andExpect(jsonPath("$.content[2].priority").value(messagesDTOList[2].priority.name))
            .andExpect(jsonPath("$.content[2].channel").value(messagesDTOList[2].channel))
            .andExpect(jsonPath("$.content[2].sender").value(messagesDTOList[2].sender))
    }

    @Test
    fun getMessages_noParameters() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(messagesDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
            .andExpect(jsonPath("$.content[2].id").value(messagesDTOList[2].id))
            .andExpect(jsonPath("$.content[2].subject").value(messagesDTOList[2].subject))
            .andExpect(jsonPath("$.content[2].body").value(messagesDTOList[2].body))
            .andExpect(jsonPath("$.content[2].actualState").value(messagesDTOList[2].actualState.name))
            .andExpect(jsonPath("$.content[2].priority").value(messagesDTOList[2].priority.name))
            .andExpect(jsonPath("$.content[2].channel").value(messagesDTOList[2].channel))
            .andExpect(jsonPath("$.content[2].sender").value(messagesDTOList[2].sender))
    }

    @Test
    fun getMessages_bigPage() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?page=1000&limit=30"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(0)) // Check if the number of items in the response is correct
    }

    @Test
    fun getMessages_bigLimit() {
        every { messageService.getAllMessages() } returns messagesDTOList

        mockMvc.perform(get("/API/messages?page=0&limit=1000"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(messagesDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(messagesDTOList[0].id))
            .andExpect(jsonPath("$.content[0].subject").value(messagesDTOList[0].subject))
            .andExpect(jsonPath("$.content[0].body").value(messagesDTOList[0].body))
            .andExpect(jsonPath("$.content[0].actualState").value(messagesDTOList[0].actualState.name))
            .andExpect(jsonPath("$.content[0].priority").value(messagesDTOList[0].priority.name))
            .andExpect(jsonPath("$.content[0].channel").value(messagesDTOList[0].channel))
            .andExpect(jsonPath("$.content[0].sender").value(messagesDTOList[0].sender))
            .andExpect(jsonPath("$.content[1].id").value(messagesDTOList[1].id))
            .andExpect(jsonPath("$.content[1].subject").value(messagesDTOList[1].subject))
            .andExpect(jsonPath("$.content[1].body").value(messagesDTOList[1].body))
            .andExpect(jsonPath("$.content[1].actualState").value(messagesDTOList[1].actualState.name))
            .andExpect(jsonPath("$.content[1].priority").value(messagesDTOList[1].priority.name))
            .andExpect(jsonPath("$.content[1].channel").value(messagesDTOList[1].channel))
            .andExpect(jsonPath("$.content[1].sender").value(messagesDTOList[1].sender))
            .andExpect(jsonPath("$.content[2].id").value(messagesDTOList[2].id))
            .andExpect(jsonPath("$.content[2].subject").value(messagesDTOList[2].subject))
            .andExpect(jsonPath("$.content[2].body").value(messagesDTOList[2].body))
            .andExpect(jsonPath("$.content[2].actualState").value(messagesDTOList[2].actualState.name))
            .andExpect(jsonPath("$.content[2].priority").value(messagesDTOList[2].priority.name))
            .andExpect(jsonPath("$.content[2].channel").value(messagesDTOList[2].channel))
            .andExpect(jsonPath("$.content[2].sender").value(messagesDTOList[2].sender))
    }

    /**
     * GET MESSAGE TEST CASES
     */

    @Test
    fun getMessage_validId() {
        val message = messagesDTOList[0]

        every { messageService.getMessage(message.id) } returns message

        val dateTimeArray = arrayOf(
            message.date.year,
            message.date.monthValue,
            message.date.dayOfMonth,
            message.date.hour,
            message.date.minute,
            message.date.second,
            message.date.nano
        ).toList()

        mockMvc.perform(get("/API/messages/${message.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(message.id))
            .andExpect(jsonPath("$.date").value(dateTimeArray))
            .andExpect(jsonPath("$.subject").value(message.subject))
            .andExpect(jsonPath("$.body").value(message.body))
            .andExpect(jsonPath("$.actualState").value(message.actualState.name))
            .andExpect(jsonPath("$.priority").value(message.priority.name))
            .andExpect(jsonPath("$.channel").value(message.channel))
            .andExpect(jsonPath("$.sender").value(message.sender))

        verify(exactly = 1) { messageService.getMessage(message.id) }
    }

    @Test
    fun getMessage_negativeId() {
        val messageId: Long = -1
        mockMvc.perform(get("/API/messages/${messageId}"))
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { messageService.getMessage(messageId) }
    }

    @Test
    fun getMessage_messageNotFound() {
        val messageId: Long = 1
        val errorMessage = "Not Found!"

        every { messageService.getMessage(messageId) } throws MessageNotFoundException(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 1) { messageService.getMessage(messageId) }
    }

    @Test
    fun getMessage_raiseGeneralError() {
        val messageId: Long = 1
        val errorMessage = "General Error!"
        val resultErrorMessage = "Error: $errorMessage"

        every { messageService.getMessage(messageId) } throws Exception(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 1) { messageService.getMessage(messageId) }
    }

    @Test
    fun getMessage_raiseNoSuchElementException() {
        val messageId: Long = 1
        val errorMessage = "No Such Element!"
        val resultErrorMessage = "Error: $errorMessage"

        every { messageService.getMessage(messageId) } throws NoSuchElementException(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 1) { messageService.getMessage(messageId) }
    }

    /**
     * GET MESSAGE HISTORY TEST CASE
     */

    @Test
    fun getMessageHistory_validId() {
        val messageId: Long = 1
        val messageHistory = messageHistoryDTOList

        every { messageService.getMessageHistory(messageId) } returns messageHistory

        val dateTimeArray = arrayOf(
            messageHistory[0].date.year,
            messageHistory[0].date.monthValue,
            messageHistory[0].date.dayOfMonth,
            messageHistory[0].date.hour,
            messageHistory[0].date.minute,
            messageHistory[0].date.second,
            messageHistory[0].date.nano
        ).toList()

        val dateTimeArray1 = arrayOf(
            messageHistory[1].date.year,
            messageHistory[1].date.monthValue,
            messageHistory[1].date.dayOfMonth,
            messageHistory[1].date.hour,
            messageHistory[1].date.minute,
            messageHistory[1].date.second,
            messageHistory[1].date.nano
        ).toList()

        val dateTimeArray2 = arrayOf(
            messageHistory[2].date.year,
            messageHistory[2].date.monthValue,
            messageHistory[2].date.dayOfMonth,
            messageHistory[2].date.hour,
            messageHistory[2].date.minute,
            messageHistory[2].date.second,
            messageHistory[2].date.nano
        ).toList()

        mockMvc.perform(get("/API/messages/${messageId}/history"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(messagesDTOList.size))
            .andExpect(jsonPath("$[0].id").value(messageHistory[0].id))
            .andExpect(jsonPath("$[0].state").value(messageHistory[0].state.name))
            .andExpect(jsonPath("$[0].date").value(dateTimeArray))
            .andExpect(jsonPath("$[0].comment").value(messageHistory[0].comment))
            .andExpect(jsonPath("$[1].id").value(messageHistory[1].id))
            .andExpect(jsonPath("$[1].state").value(messageHistory[1].state.name))
            .andExpect(jsonPath("$[1].date").value(dateTimeArray1))
            .andExpect(jsonPath("$[1].comment").value(messageHistory[1].comment))
            .andExpect(jsonPath("$[2].id").value(messageHistory[2].id))
            .andExpect(jsonPath("$[2].state").value(messageHistory[2].state.name))
            .andExpect(jsonPath("$[2].date").value(dateTimeArray2))
            .andExpect(jsonPath("$[2].comment").value(messageHistory[2].comment))

        verify(exactly = 1) { messageService.getMessageHistory(messageId) }
    }

    @Test
    fun getMessageHistory_negativeId() {
        val messageId: Long = -1

        mockMvc.perform(get("/API/messages/${messageId}/history"))
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { messageService.getMessageHistory(messageId) }
    }

    @Test
    fun getMessageHistory_messageNotFound() {
        val messageId: Long = 1
        val errorMessage = "Not Found!"

        every { messageService.getMessageHistory(messageId) } throws MessageNotFoundException(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}/history"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 1) { messageService.getMessageHistory(messageId) }
    }

    @Test
    fun getMessageHistory_raiseGeneralError() {
        val messageId: Long = 1
        val errorMessage = "General Error!"
        val resultErrorMessage = "Error: $errorMessage"

        every { messageService.getMessageHistory(messageId) } throws Exception(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}/history"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 1) { messageService.getMessageHistory(messageId) }
    }

    @Test
    fun getMessageHistory_raiseNoSuchElementException() {
        val messageId: Long = 1
        val errorMessage = "No Such Element!"
        val resultErrorMessage = "Error: $errorMessage"

        every { messageService.getMessageHistory(messageId) } throws NoSuchElementException(errorMessage)

        mockMvc.perform(get("/API/messages/${messageId}/history"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 1) { messageService.getMessageHistory(messageId) }
    }

    /**
     * POST MESSAGE TEST CASES
     */

    @Test
    fun postNewMessage_correctParameters() {

        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.subject").value(bodyValue.subject))
            .andExpect(jsonPath("$.body").value(bodyValue.body))
            .andExpect(jsonPath("$.priority").value(bodyValue.priority))
            .andExpect(jsonPath("$.actualState").value(StateOptions.RECEIVED.name))
            .andExpect(jsonPath("$.channel").value(bodyValue.channel))
            .andExpect(jsonPath("$.sender").value(bodyValue.sender))

    }

    @Test
    fun postNewMessage_emptySubject() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = ""
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Subject cannot be empty!"))
    }

    @Test
    fun postNewMessage_emptyBody() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = "",
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Body cannot be empty!"))
    }

    @Test
    fun postNewMessage_emptyPriority() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = "",
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Priority cannot be empty!"))
    }

    @Test
    fun postNewMessage_emptyChannel() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = "",
            priority = messagesDTOList[0].priority.name,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Channel cannot be empty!"))
    }

    @Test
    fun postNewMessage_emptySender() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority.name,
            sender = "",
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Sender cannot be empty!"))

    }

    @Test
    fun postNewMessage_wrongPriority() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = messagesDTOList[0].priority,
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = messagesDTOList[0].channel,
            priority = "wrong priority",
            sender = messagesDTOList[0].sender,
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Field priority has to be [HIGH, MEDIUM_HIGH, MEDIUM, MEDIUM_LOW, LOW]"))
    }

    @Test
    fun postNewMessage_wrongTelephoneNumber() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = "telephone",
            priority = messagesDTOList[0].priority,
            sender = "wrong telephone number",
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = "telephone",
            priority = messagesDTOList[0].priority.name,
            sender = "wrong telephone number",
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Sender must be a valid telephone number [+39 3321335437, 3321335437, +1 (222) 123-1234] or a valid email address [name@example.com] senderType: NONE"))
    }

    @Test
    fun postNewMessage_wrongEmail() {
        val date = LocalDateTime.now()

        every { messageService.storeMessage(any(), any()) } returns MessageDTO(
            id = 0,
            date = date,
            actualState = StateOptions.RECEIVED,
            body = messagesDTOList[0].body,
            channel = "email",
            priority = messagesDTOList[0].priority,
            sender = "wrong email",
            subject = messagesDTOList[0].subject
        )

        val bodyValue = CreateMessageDTO(
            body = messagesDTOList[0].body,
            channel = "email",
            priority = messagesDTOList[0].priority.name,
            sender = "wrong email",
            subject = messagesDTOList[0].subject
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            post("/API/messages/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Sender must be a valid telephone number [+39 3321335437, 3321335437, +1 (222) 123-1234] or a valid email address [name@example.com] senderType: NONE"))
    }

    /**
     * PATCH /API/Messages/{messageID} TEST CASES
     */
    @Test
    fun patchMessage_goodCase_State() {
        val messageID = 1
        val now = LocalDateTime.now()

        val returnData = Message().apply {
            id = 1
            date = now
            subject = "Subject 1"
            body = "Body message 1"
            actualState = StateOptions.DISCARDED
            priority = PriorityEnumOptions.HIGH
            channel = "email"
            sender = "example@exampleMail.com"
        }

        val updateState = StateOptions.DISCARDED

        every { messageService.updateMessage(returnData.id, updateState, null, null) } returns returnData.toDTO()

        val bodyValue = UpdateMessageDTO(
            actualState = updateState.name,
            comment = null,
            priority = null
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            patch("/API/messages/${messageID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.actualState").value(returnData.actualState.name))
            .andExpect(jsonPath("$.body").value(returnData.body))
            .andExpect(jsonPath("$.channel").value(returnData.channel))
            .andExpect(jsonPath("$.priority").value(returnData.priority.name))
            .andExpect(jsonPath("$.sender").value(returnData.sender))

    }

    @Test
    fun patchMessage_goodCase_Priority() {
        val messageID = 1
        val now = LocalDateTime.now()

        val returnData = Message().apply {
            id = 1
            date = now
            subject = "Subject 1"
            body = "Body message 1"
            actualState = StateOptions.DISCARDED
            priority = PriorityEnumOptions.HIGH
            channel = "email"
            sender = "example@exampleMail.com"
        }

        val updatePriority = PriorityEnumOptions.HIGH

        every { messageService.updateMessage(returnData.id, null, null, updatePriority) } returns returnData.toDTO()

        val bodyValue = UpdateMessageDTO(
            actualState = null,
            comment = null,
            priority = updatePriority.name
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            patch("/API/messages/${messageID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.actualState").value(returnData.actualState.name))
            .andExpect(jsonPath("$.body").value(returnData.body))
            .andExpect(jsonPath("$.channel").value(returnData.channel))
            .andExpect(jsonPath("$.priority").value(returnData.priority.name))
            .andExpect(jsonPath("$.sender").value(returnData.sender))

    }

    @Test
    fun patchMessage_WrongMessageId() {
        val messageID = -1

        val bodyValue = UpdateMessageDTO(
            actualState = "RECEIVED",
            comment = "Some comment",
            priority = "HIGH"
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            patch("/API/messages/${messageID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun patchMessage_PriorityWrong() {
        val messageID = 1
        val date = LocalDateTime.now()

        every { messageService.updateMessage(any(), any(), any(), any()) } returns MessageDTO(
            id = 1,
            date = date,
            subject = messagesDTOList[0].subject,
            body = messagesDTOList[0].body,
            actualState = StateOptions.RECEIVED,
            priority = messagesDTOList[0].priority,
            channel = "email",
            sender = "example@exampleMail.com"
        )

        val bodyValue = UpdateMessageDTO(
            actualState = "RECEIVED",
            comment = "Some comment",
            priority = "WRONG"
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            patch("/API/messages/${messageID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun patchMessage_StateWrong() {
        val messageID = 1
        val date = LocalDateTime.now()

        every { messageService.updateMessage(any(), any(), any(), any()) } returns MessageDTO(
            id = 1,
            date = date,
            subject = messagesDTOList[0].subject,
            body = messagesDTOList[0].body,
            actualState = StateOptions.RECEIVED,
            priority = messagesDTOList[0].priority,
            channel = "email",
            sender = "example@exampleMail.com"
        )

        val bodyValue = UpdateMessageDTO(
            actualState = "STATE_WRONG",
            comment = "Some comment",
            priority = "HIGH"
        )

        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(bodyValue)

        mockMvc.perform(
            patch("/API/messages/${messageID}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)

    }

}