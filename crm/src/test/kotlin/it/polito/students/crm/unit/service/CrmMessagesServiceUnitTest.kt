package it.polito.students.crm.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.polito.students.crm.CrmApplication
import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.MessageDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.*
import it.polito.students.crm.exception_handlers.InvalidStateTransitionException
import it.polito.students.crm.exception_handlers.InvalidUpdateMessageRequestException
import it.polito.students.crm.exception_handlers.MessageNotFoundException
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.MessageServiceImpl
import it.polito.students.crm.utils.PhoneOrMailOption
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.SortOptions
import it.polito.students.crm.utils.StateOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(
    classes = [CrmApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CrmMessagesServiceUnitTest {
    private val messageRepository: MessageRepository = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val contactRepository: ContactRepository = mockk()
    private val emailRepository: EmailRepository = mockk()
    private val telephoneRepository: TelephoneRepository = mockk()

    val messageService = MessageServiceImpl(
        messageRepository,
        historyRepository,
        contactRepository,
        emailRepository,
        telephoneRepository
    )

    // Test cases
    val messagesDTOList: List<MessageDTO> = listOf(
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

    // Test cases
    val messagesList: List<Message> = listOf(
        Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "Subject 1"
            body = "body 1"
            actualState = StateOptions.READ
            priority = PriorityEnumOptions.HIGH
            channel = "email"
            sender = "test@unit.test"
        },
        Message().apply {
            id = 2
            date = LocalDateTime.now()
            subject = "Subject 2"
            body = "body 2"
            actualState = StateOptions.DONE
            priority = PriorityEnumOptions.MEDIUM
            channel = "email"
            sender = "test@2unit.test"
        },
        Message().apply {
            id = 3
            date = LocalDateTime.now()
            subject = "Subject 3"
            body = "body 3"
            actualState = StateOptions.FAILED
            priority = PriorityEnumOptions.LOW
            channel = "email"
            sender = "test3@unit.test"
        }
    )

    val messageHistory: List<History> = listOf(
        History().apply {
            id = 1
            state = StateOptions.RECEIVED
            date = LocalDateTime.now()
            comment = "First comment"
        },
        History().apply {
            id = 2
            state = StateOptions.READ
            date = LocalDateTime.now()
            comment = "Other second comment"
        }
    )

    /**
     * GET ALL MESSAGES TEST CASES
     */
    @Test
    fun getAllMessages_goodCase() {
        val messages = listOf(
            Message().apply {
                id = messagesDTOList[0].id
                date = messagesDTOList[0].date
                subject = messagesDTOList[0].subject
                body = messagesDTOList[0].body
                actualState = messagesDTOList[0].actualState
                priority = messagesDTOList[0].priority
                channel = messagesDTOList[0].channel
                sender = messagesDTOList[0].sender
            },
            Message().apply {
                id = messagesDTOList[1].id
                date = messagesDTOList[1].date
                subject = messagesDTOList[1].subject
                body = messagesDTOList[1].body
                actualState = messagesDTOList[1].actualState
                priority = messagesDTOList[1].priority
                channel = messagesDTOList[1].channel
                sender = messagesDTOList[1].sender
            },
            Message().apply {
                id = messagesDTOList[2].id
                date = messagesDTOList[2].date
                subject = messagesDTOList[2].subject
                body = messagesDTOList[2].body
                actualState = messagesDTOList[2].actualState
                priority = messagesDTOList[2].priority
                channel = messagesDTOList[2].channel
                sender = messagesDTOList[2].sender
            }
        )

        every { messageRepository.findAll() } returns messages

        val result = messageService.getAllMessages()

        verify(exactly = 1) { messageRepository.findAll() }
        assertEquals(messagesDTOList, result)
    }

    @Test
    fun getAllMessages_goodCaseFiltering() {
        val messages = listOf(
            Message().apply {
                id = messagesDTOList[0].id
                date = messagesDTOList[0].date
                subject = messagesDTOList[0].subject
                body = messagesDTOList[0].body
                actualState = messagesDTOList[0].actualState
                priority = messagesDTOList[0].priority
                channel = messagesDTOList[0].channel
                sender = messagesDTOList[0].sender
            },
            Message().apply {
                id = messagesDTOList[1].id
                date = messagesDTOList[1].date
                subject = messagesDTOList[1].subject
                body = messagesDTOList[1].body
                actualState = messagesDTOList[1].actualState
                priority = messagesDTOList[1].priority
                channel = messagesDTOList[1].channel
                sender = messagesDTOList[1].sender
            },
            Message().apply {
                id = messagesDTOList[2].id
                date = messagesDTOList[2].date
                subject = messagesDTOList[2].subject
                body = messagesDTOList[2].body
                actualState = messagesDTOList[2].actualState
                priority = messagesDTOList[2].priority
                channel = messagesDTOList[2].channel
                sender = messagesDTOList[2].sender
            }
        )

        every { messageRepository.findByActualState(StateOptions.DONE) } returns listOf(messages[1])

        val result = messageService.getAllMessages(state = StateOptions.DONE)

        verify(exactly = 0) { messageRepository.findAll() }
        assertEquals(listOf(messagesDTOList[1]), result)
    }

    @Test
    fun getAllMessages_goodCaseSorting() {
        val messages = listOf(
            Message().apply {
                id = messagesDTOList[0].id
                date = messagesDTOList[0].date
                subject = messagesDTOList[0].subject
                body = messagesDTOList[0].body
                actualState = messagesDTOList[0].actualState
                priority = messagesDTOList[0].priority
                channel = messagesDTOList[0].channel
                sender = messagesDTOList[0].sender
            },
            Message().apply {
                id = messagesDTOList[1].id
                date = messagesDTOList[1].date
                subject = messagesDTOList[1].subject
                body = messagesDTOList[1].body
                actualState = messagesDTOList[1].actualState
                priority = messagesDTOList[1].priority
                channel = messagesDTOList[1].channel
                sender = messagesDTOList[1].sender
            },
            Message().apply {
                id = messagesDTOList[2].id
                date = messagesDTOList[2].date
                subject = messagesDTOList[2].subject
                body = messagesDTOList[2].body
                actualState = messagesDTOList[2].actualState
                priority = messagesDTOList[2].priority
                channel = messagesDTOList[2].channel
                sender = messagesDTOList[2].sender
            }
        )

        every { messageRepository.findAll() } returns messages

        val result = messageService.getAllMessages(sortBy = SortOptions.ID_DESC)

        verify(exactly = 1) { messageRepository.findAll() }
        assertEquals(listOf(messagesDTOList[2], messagesDTOList[1], messagesDTOList[0]), result)
    }

    @Test
    fun getAllMessages_goodCaseEmptyList() {
        val messages: List<Message> = listOf()
        val expected: List<MessageDTO> = listOf()

        every { messageRepository.findAll() } returns messages

        val result = messageService.getAllMessages()

        verify(exactly = 1) { messageRepository.findAll() }
        assertEquals(expected, result)
    }

    /**
     * GET MESSAGE TEST CASES
     */

    @Test
    fun getMessage_validId() {
        val message = messagesList.first()

        every { messageRepository.findById(message.id) } returns Optional.of(message)

        val result = messageService.getMessage(message.id)

        verify(exactly = 1) { messageRepository.findById(message.id) }
        assertEquals(message.toDTO(), result)
    }

    @Test
    fun getMessage_emptyRepository() {
        val message = messagesList.first()

        every { messageRepository.findById(message.id) } returns Optional.empty()

        assertThrows<MessageNotFoundException> {
            messageService.getMessage(message.id)
        }

        verify(exactly = 1) { messageRepository.findById(message.id) }
    }

    @Test
    fun getMessage_negativeId() {
        every { messageRepository.findById(-1) } returns Optional.empty()

        assertThrows<MessageNotFoundException> {
            messageService.getMessage(-1)
        }

        verify(exactly = 1) { messageRepository.findById(-1) }
    }

    @Test
    fun getMessage_notFoundId() {
        val id: Long = 1

        every { messageRepository.findById(id) } returns Optional.empty()

        assertThrows<MessageNotFoundException> {
            messageService.getMessage(id)
        }

        verify(exactly = 1) { messageRepository.findById(id) }
    }

    /*

    POST /API/messages test cases

     */
    @Test
    fun postMessage_correctParametersFromAKnownContactEmail() {
        val messageCreated = messagesList.first()

        every { emailRepository.findByEmail("test@unit.test") } returns Email().apply {
            email = "test@unit.test"
            comment = "No comment here"
        }

        every { messageRepository.save(any()) } returns messageCreated

        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )

        val result = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        verify(exactly = 1) { emailRepository.findByEmail("test@unit.test") }
        verify(exactly = 0) { contactRepository.save(any()) }
        verify(exactly = 1) { messageRepository.save(any()) }
        assertEquals(messageCreated.toDTO(), result)

    }

    @Test
    fun postMessage_correctParametersFromAnUnknownContactEmail() {

        val messageCreated = messagesList.first()

        every { emailRepository.findByEmail("test@unit.test") } returns null

        every { messageRepository.save(any()) } returns messageCreated
        every { contactRepository.save(any()) } returns Contact()
        every { emailRepository.save(any()) } returns Email().apply { this.email = "test@unit.test" }

        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )

        val result = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        verify(exactly = 1) { emailRepository.findByEmail("test@unit.test") }
        verify(exactly = 1) { contactRepository.save(any()) }
        verify(exactly = 1) { messageRepository.save(any()) }
        assertEquals(messageCreated.toDTO(), result)

    }

    @Test
    fun postMessage_correctParametersFromAKnownContactTelephone() {
        val messageCreated = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "Subject 1"
            body = "body 1"
            actualState = StateOptions.READ
            priority = PriorityEnumOptions.HIGH
            channel = "telephone"
            sender = "3313785623"
        }

        every { telephoneRepository.findByTelephone("3313785623") } returns Telephone().apply {
            telephone = "3313785623"
            comment = "No comment here"
        }

        every { messageRepository.save(any()) } returns messageCreated

        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "telephone",
            sender = "3313785623"
        )

        val result = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.PHONE)

        verify(exactly = 1) { telephoneRepository.findByTelephone("3313785623") }
        verify(exactly = 0) { contactRepository.save(any()) }
        verify(exactly = 1) { messageRepository.save(any()) }
        assertEquals(messageCreated.toDTO(), result)

    }

    @Test
    fun postMessage_correctParametersFromAnUnknownContactTelephone() {
        val messageCreated = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "Subject 1"
            body = "body 1"
            actualState = StateOptions.READ
            priority = PriorityEnumOptions.HIGH
            channel = "telephone"
            sender = "3313785623"
        }

        every { telephoneRepository.findByTelephone("3313785623") } returns null

        every { messageRepository.save(any()) } returns messageCreated
        every { contactRepository.save(any()) } returns Contact()

        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "telephone",
            sender = "3313785623"
        )

        val result = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.PHONE)

        verify(exactly = 1) { telephoneRepository.findByTelephone("3313785623") }
        verify(exactly = 1) { contactRepository.save(any()) }
        verify(exactly = 1) { messageRepository.save(any()) }
        assertEquals(messageCreated.toDTO(), result)

    }

    /*
    PATCH /API/messages
     */

    @Test
    fun patchMessage_correctUpdateStatePriorityComment() {
        val prevMessage = messagesList.first()

        val postMessage = Message().apply {
            id = prevMessage.id
            date = prevMessage.date
            subject = prevMessage.subject
            body = prevMessage.body
            actualState = StateOptions.DISCARDED
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = prevMessage.channel
            sender = prevMessage.sender
        }

        val newHistoryLine = History().apply {
            state = StateOptions.DISCARDED
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result = messageService.updateMessage(
            prevMessage.id,
            StateOptions.DISCARDED,
            "New comment",
            PriorityEnumOptions.MEDIUM_LOW
        )

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdatePriority() {
        val prevMessage = messagesList.first()

        val postMessage = Message().apply {
            id = prevMessage.id
            date = prevMessage.date
            subject = prevMessage.subject
            body = prevMessage.body
            actualState = prevMessage.actualState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = prevMessage.channel
            sender = prevMessage.sender
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { messageRepository.save(any()) } returns postMessage

        val result = messageService.updateMessage(prevMessage.id, null, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdateStateComment() {
        val prevMessage = messagesList.first()

        val postMessage = Message().apply {
            id = prevMessage.id
            date = prevMessage.date
            subject = prevMessage.subject
            body = prevMessage.body
            actualState = StateOptions.DISCARDED
            priority = prevMessage.priority
            channel = prevMessage.channel
            sender = prevMessage.sender
        }

        val newHistoryLine = History().apply {
            state = StateOptions.DISCARDED
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result = messageService.updateMessage(prevMessage.id, StateOptions.DISCARDED, "New comment", null)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_noParameters() {
        val prevMessage = messagesList.first()

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)

        val exception = assertThrows<InvalidUpdateMessageRequestException> {
            messageService.updateMessage(
                prevMessage.id,
                null,
                "New comment",
                null
            )
        }
        assertEquals(
            "No valid fields were specified in the request body. Please include either 'actualState' or 'priority' fields to update the message.",
            exception.message
        )
    }

    @Test
    fun patchMessage_nonExistingMessage() {
        val prevMessage = messagesList.first()

        every { messageRepository.findById(prevMessage.id) } returns Optional.empty()

        val exception = assertThrows<MessageNotFoundException> {
            messageService.updateMessage(
                prevMessage.id,
                StateOptions.DISCARDED,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals("The message with id equal to 1 was not found!", exception.message)
    }

    @Test
    fun patchMessage_correctUpdateStateReceivedRead() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_wrongUpdateStateReceivedDiscarded() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateReceivedProcessing() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )
    }

    @Test
    fun patchMessage_wrongUpdateStateReceivedDone() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )
    }

    @Test
    fun patchMessage_wrongUpdateStateReceivedFailed() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateReadReceived() {
        val prevState = StateOptions.READ
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_correctUpdateStateReadDiscarded() {

        val prevState = StateOptions.READ
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdateStateReadProcessing() {

        val prevState = StateOptions.READ
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdateStateReadDone() {

        val prevState = StateOptions.READ
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdateStateReadFailed() {

        val prevState = StateOptions.READ
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedReceived() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedRead() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedProcessing() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedDone() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedFailed() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateProcessingReceived() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateProcessingRead() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }

        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateProcessingDiscarded() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_correctUpdateStateProcessingDone() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_correctUpdateStateProcessingFailed() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val postMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = newState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        val newHistoryLine = History().apply {
            state = newState
            date = LocalDateTime.now()
            comment = "New comment"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLine
        every { messageRepository.save(any()) } returns postMessage

        val result =
            messageService.updateMessage(prevMessage.id, newState, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assertEquals(postMessage.toDTO(), result)

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneReceived() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneRead() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }

        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneDiscarded() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneProcessing() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneFailed() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedReceived() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedRead() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedDiscarded() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedProcessing() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedDone() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateFailedFailed() {

        val prevState = StateOptions.FAILED
        val newState = StateOptions.FAILED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDoneDone() {

        val prevState = StateOptions.DONE
        val newState = StateOptions.DONE

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateProcessingProcessing() {

        val prevState = StateOptions.PROCESSING
        val newState = StateOptions.PROCESSING

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateDiscardedDiscarded() {

        val prevState = StateOptions.DISCARDED
        val newState = StateOptions.DISCARDED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateReadRead() {
        val prevState = StateOptions.READ
        val newState = StateOptions.READ

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }


        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )

    }

    @Test
    fun patchMessage_wrongUpdateStateReceivedReceived() {
        val prevState = StateOptions.RECEIVED
        val newState = StateOptions.RECEIVED

        val prevMessage = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "subj"
            body = "body"
            actualState = prevState
            priority = PriorityEnumOptions.MEDIUM_LOW
            channel = "email"
            sender = "test@service.mail"
        }

        every { messageRepository.findById(prevMessage.id) } returns Optional.of(prevMessage)
        every { historyRepository.findByMessageId(1) } returns messageHistory

        val exception = assertThrows<InvalidStateTransitionException> {
            messageService.updateMessage(
                prevMessage.id,
                newState,
                "New comment",
                PriorityEnumOptions.MEDIUM_LOW
            )
        }
        assertEquals(
            "Invalid state transition: The requested transition is not allowed in the current state.",
            exception.message
        )
    }

    /**
     * Get API/messages/{messageID}/history
     *
     */
    @Test
    fun getMessageHistory_goodCase() {
        val messageId: Long = 1

        every { historyRepository.findByMessageId(messageId) } returns messageHistory

        val result = messageService.getMessageHistory(messageId)

        assertEquals(listOf(messageHistory[0].toDTO(), messageHistory[1].toDTO()), result)
    }

    @Test
    fun getMessageHistory_moreThenFour() {
        val messageId: Long = 1

        val messageHistoryMoreFour: List<History> = listOf(
            History().apply {
                id = 1
                state = StateOptions.RECEIVED
                date = LocalDateTime.now()
                comment = "First comment"
            },
            History().apply {
                id = 2
                state = StateOptions.READ
                date = LocalDateTime.now()
                comment = "Other second comment"
            },
            History().apply {
                id = 3
                state = StateOptions.PROCESSING
                date = LocalDateTime.now()
                comment = "Other second comment"
            },
            History().apply {
                id = 4
                state = StateOptions.DONE
                date = LocalDateTime.now()
                comment = "Other second comment"
            },
            History().apply {
                id = 5
                state = StateOptions.DISCARDED
                date = LocalDateTime.now()
                comment = "Other second comment"
            }
        )

        every { historyRepository.findByMessageId(messageId) } returns messageHistoryMoreFour

        assertThrows<Exception> { messageService.getMessageHistory(messageId) }
    }

    @Test
    fun getMessageHistory_Sorted() {
        val messageId: Long = 1

        val messageHistorySort: List<History> = listOf(
            History().apply {
                id = 1
                state = StateOptions.RECEIVED
                date = LocalDateTime.now()
                comment = "First comment"
            },
            History().apply {
                id = 2
                state = StateOptions.DONE
                date = LocalDateTime.now().plusDays(3)
                comment = "Other second comment"
            },
            History().apply {
                id = 3
                state = StateOptions.PROCESSING
                date = LocalDateTime.now().plusDays(2)
                comment = "Other second comment"
            },
            History().apply {
                id = 4
                state = StateOptions.READ
                date = LocalDateTime.now().plusDays(1)
                comment = "Other second comment"
            }
        )

        every { historyRepository.findByMessageId(messageId) } returns messageHistorySort

        val result = messageService.getMessageHistory(messageId)

        assertEquals(
            listOf(
                messageHistorySort[0].toDTO(),
                messageHistorySort[3].toDTO(),
                messageHistorySort[2].toDTO(),
                messageHistorySort[1].toDTO()
            ), result
        )
    }

    @Test
    fun getMessageHistory_wrongMessageId() {
        val messageId: Long = 2

        every { historyRepository.findByMessageId(messageId) } returns listOf()

        assertThrows<MessageNotFoundException> { messageService.getMessageHistory(messageId) }

    }

    @Test
    fun patchMessage_newActualState() {
        val oldState = StateOptions.RECEIVED
        val newState = StateOptions.READ
        val commentParam = "comment"
        val messageId: Long = 1
        val dates = LocalDateTime.now()

        val oldMessage = Message().apply {
            id = messageId
            date = dates.minusDays(1)
            subject = "Subject"
            body = "Message body"
            actualState = oldState
            priority = PriorityEnumOptions.HIGH
            channel = "email"
            sender = "ex@example.com"
        }

        val newMessage = Message().apply {
            id = messageId
            date = dates
            subject = "Subject"
            body = "Message body"
            actualState = newState
            priority = PriorityEnumOptions.HIGH
            channel = "email"
            sender = "ex@example.com"
        }
        messageHistory.forEach {
            newMessage.addHistory(it)
        }

        val newHistoryLineBeforeSaving = History().apply {
            state = newState
            date = dates
            comment = commentParam
            this.message = newMessage
        }

        val newHistoryLineAfterSaving = History().apply {
            id = 3
            state = newState
            date = dates
            comment = commentParam
            this.message = newMessage
        }
        newMessage.addHistory(newHistoryLineAfterSaving)

        every { messageRepository.findById(messageId) } returns Optional.of(oldMessage)
        every { historyRepository.findByMessageId(messageId) } returns messageHistory
        every { historyRepository.save(any()) } returns newHistoryLineAfterSaving
        every { messageRepository.save(any()) } returns newMessage

        val result = messageService.updateMessage(messageId, newState, commentParam, null)

        assertEquals(newMessage.toDTO(), result)

        verify(exactly = 1) { messageRepository.findById(messageId) }
        verify(exactly = 1) { messageRepository.save(any()) }
        verify(exactly = 1) { historyRepository.save(any()) }
        verify(exactly = 1) { historyRepository.findByMessageId(messageId) }
    }

    @Test
    fun patchMessage_invalidActualStateParam() {
        val prior = PriorityEnumOptions.LOW

        val message = Message().apply {
            id = 1
            date = LocalDateTime.now()
            subject = "Subject"
            body = "Message body"
            actualState = StateOptions.RECEIVED
            priority = PriorityEnumOptions.LOW
            channel = "email"
            sender = "ex@example.com"
        }

        assertThrows<Exception> {
            every {
                messageService.updateMessage(
                    messageID = message.id, null, "comment", prior
                )
            } returns message.toDTO()
        }

    }
}