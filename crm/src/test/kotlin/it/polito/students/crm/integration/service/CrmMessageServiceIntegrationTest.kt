package it.polito.students.crm.integration.service

import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.MessageDTO
import it.polito.students.crm.entities.Email
import it.polito.students.crm.entities.Telephone
import it.polito.students.crm.exception_handlers.InvalidUpdateMessageRequestException
import it.polito.students.crm.exception_handlers.MessageNotFoundException
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.repositories.*
import it.polito.students.crm.services.MessageServiceImpl
import it.polito.students.crm.utils.PhoneOrMailOption
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.StateOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmMessageServiceIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var emailRepository: EmailRepository

    @Autowired
    private lateinit var telephoneRepository: TelephoneRepository

    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    private val messageService: MessageServiceImpl by lazy {
        MessageServiceImpl(
            messageRepository,
            historyRepository,
            contactRepository,
            emailRepository,
            telephoneRepository
        )
    }

    @BeforeEach
    fun setUp() {
        // Clean repositories before each test
        contactRepository.deleteAll()
        emailRepository.deleteAll()
        telephoneRepository.deleteAll()
        historyRepository.deleteAll()
        messageRepository.deleteAll()
    }

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

    /**
     * getMessage TEST CASES
     */
    fun createMessageAndReturnIt(): MessageDTO {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        return messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)
    }

    @Test
    fun getMessage_correctParameter() {
        val message = createMessageAndReturnIt()

        //Update the message
        val retrievedMessage = messageService.getMessage(message.id)

        assert(retrievedMessage.id == message.id)
        assert(retrievedMessage.actualState == message.actualState)
        assert(retrievedMessage.priority == message.priority)
        assert(retrievedMessage.body == message.body)
        assert(retrievedMessage.sender == message.sender)
        assert(retrievedMessage.channel == message.channel)
        assert(retrievedMessage.subject == message.subject)
    }

    @Test
    fun getMessage_messageNotInDB() {
        //Update the message
        assertThrows<MessageNotFoundException> {
            messageService.getMessage(999)
        }
    }

    /**
     * STORE MESSAGE TEST CASES
     */
    @Test
    fun postMessage_goodCaseWithPresentEmail() {
        val message = createMessageWithEmailDto

        // Check if the email is present, otherwise it adds the email
        val emailPresent = emailRepository.findByEmail(message.sender)
        if (emailPresent == null) {
            emailRepository.save(Email().apply {
                email = message.sender
            })
        }

        val messageSaved = messageService.storeMessage(message, PhoneOrMailOption.MAIL)

        // Check if the saved message correspond to the object created previously
        assertEquals(message.body, messageSaved.body)
        assertEquals(message.channel, messageSaved.channel)
        assertEquals(message.subject, messageSaved.subject)
        assertEquals(message.sender, messageSaved.sender)
        assertEquals(message.priority.uppercase(), messageSaved.priority.name)
        assertEquals(StateOptions.RECEIVED, messageSaved.actualState)

        // Get the history of the message
        val history = messageService.getMessageHistory(messageSaved.id)

        // Check if the history is the expected one
        assertEquals(history.size, 1) //The message has been only received
        assertEquals(history[0].state, StateOptions.RECEIVED)

        val emailSaved = emailRepository.findByEmail(message.sender)

        // Check if the email has been saved
        assertNotNull(emailSaved)
    }

    @Test
    fun postMessage_goodCaseWithNotPresentEmail() {
        val message = createMessageWithEmailDto

        // Check if the email is present, otherwise it deletes the email
        val emailPresent = emailRepository.findByEmail(message.sender)
        if (emailPresent != null) {
            emailRepository.delete(emailPresent)
        }

        val messageSaved = messageService.storeMessage(message, PhoneOrMailOption.MAIL)

        // Check if the saved message correspond to the object created previously
        assertEquals(message.body, messageSaved.body)
        assertEquals(message.channel, messageSaved.channel)
        assertEquals(message.subject, messageSaved.subject)
        assertEquals(message.sender, messageSaved.sender)
        assertEquals(message.priority.uppercase(), messageSaved.priority.name)
        assertEquals(StateOptions.RECEIVED, messageSaved.actualState)

        // Get the history of the message
        val history = messageService.getMessageHistory(messageSaved.id)

        // Check if the history is the expected one
        assertEquals(history.size, 1) //The message has been only received
        assertEquals(history[0].state, StateOptions.RECEIVED)

        val emailSaved = emailRepository.findByEmail(message.sender)

        // Check if the email has been saved
        assertNotNull(emailSaved)
    }

    @Test
    fun postMessage_goodCaseWithPresentPhone() {
        val message = createMessageWithPhoneDto

        // Check if the telephone is present, otherwise it adds the telephone
        val phonePresent = telephoneRepository.findByTelephone(message.sender)
        if (phonePresent == null) {
            telephoneRepository.save(Telephone().apply {
                telephone = message.sender
            })
        }

        val messageSaved = messageService.storeMessage(message, PhoneOrMailOption.PHONE)

        // Check if the saved message correspond to the object created previously
        assertEquals(message.body, messageSaved.body)
        assertEquals(message.channel, messageSaved.channel)
        assertEquals(message.subject, messageSaved.subject)
        assertEquals(message.sender, messageSaved.sender)
        assertEquals(message.priority.uppercase(), messageSaved.priority.name)
        assertEquals(StateOptions.RECEIVED, messageSaved.actualState)

        // Get the history of the message
        val history = messageService.getMessageHistory(messageSaved.id)

        // Check if the history is the expected one
        assertEquals(history.size, 1) //The message has been only received
        assertEquals(history[0].state, StateOptions.RECEIVED)
    }

    @Test
    fun postMessage_goodCaseWithNotPresentPhone() {
        val message = createMessageWithPhoneDto

        // Check if the telephone is present, otherwise it deletes the telephone
        val phonePresent = telephoneRepository.findByTelephone(message.sender)
        if (phonePresent != null) {
            telephoneRepository.delete(phonePresent)
        }

        val messageSaved = messageService.storeMessage(message, PhoneOrMailOption.PHONE)

        // Check if the saved message correspond to the object created previously
        assertEquals(message.body, messageSaved.body)
        assertEquals(message.channel, messageSaved.channel)
        assertEquals(message.subject, messageSaved.subject)
        assertEquals(message.sender, messageSaved.sender)
        assertEquals(message.priority.uppercase(), messageSaved.priority.name)
        assertEquals(StateOptions.RECEIVED, messageSaved.actualState)

        // Get the history of the message
        val history = messageService.getMessageHistory(messageSaved.id)

        // Check if the history is the expected one
        assertEquals(history.size, 1) //The message has been only received
        assertEquals(history[0].state, StateOptions.RECEIVED)
    }

    /**
     *  GET MESSAGE HISTORY TEST CASES
     */
    @Test
    fun checkMessageHistory_messageReceived() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 1) //The message has been only received
        assert(history[0].state == StateOptions.RECEIVED)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndRead() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 2) //The message has been received and read
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndDiscarded() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Discard the message
        messageService.updateMessage(message.id, StateOptions.DISCARDED, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 3) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.DISCARDED)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndDone() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Message is done
        messageService.updateMessage(message.id, StateOptions.DONE, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 3) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.DONE)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndFailed() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Message is failed
        messageService.updateMessage(message.id, StateOptions.FAILED, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 3) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.FAILED)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndProcessing() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Process the message
        messageService.updateMessage(message.id, StateOptions.PROCESSING, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 3) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.PROCESSING)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndProcessingAndDone() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Process the message
        messageService.updateMessage(message.id, StateOptions.PROCESSING, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Message is done
        messageService.updateMessage(message.id, StateOptions.DONE, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 4) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.PROCESSING)
        assert(history[3].state == StateOptions.DONE)
    }

    @Test
    fun checkMessageHistory_messageReceivedAndReadAndProcessingAndFailed() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Read the message
        messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Process the message
        messageService.updateMessage(message.id, StateOptions.PROCESSING, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Message is done
        messageService.updateMessage(message.id, StateOptions.FAILED, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        //Get the history of the message
        val history = messageService.getMessageHistory(message.id)

        //Check if the history is the expected one
        assert(history.size == 4) //The message has been received, read and discarded
        assert(history[0].state == StateOptions.RECEIVED)
        assert(history[1].state == StateOptions.READ)
        assert(history[2].state == StateOptions.PROCESSING)
        assert(history[3].state == StateOptions.FAILED)
    }

    @Test
    fun checkMessageHistory_messageNotFound() {
        assertThrows<MessageNotFoundException> {
            // Call the function with a non-existing message ID
            messageService.getMessageHistory(-1)
        }
    }

    //updateMessage test cases
    @Test
    fun updateMessage_createAndUpdate() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Update the message
        val updatedMessage =
            messageService.updateMessage(message.id, StateOptions.READ, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assert(updatedMessage.id == message.id)
        assert(updatedMessage.actualState == StateOptions.READ)
        assert(updatedMessage.priority == PriorityEnumOptions.MEDIUM_LOW)
    }

    @Test
    fun updateMessage_createAndUpdatePriority() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Update the message
        val updatedMessage =
            messageService.updateMessage(message.id, null, "New comment", PriorityEnumOptions.MEDIUM_LOW)

        assert(updatedMessage.id == message.id)
        assert(updatedMessage.actualState == StateOptions.RECEIVED)
        assert(updatedMessage.priority == PriorityEnumOptions.MEDIUM_LOW)
    }

    @Test
    fun updateMessage_createAndUpdateState() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Update the message
        val updatedMessage = messageService.updateMessage(message.id, StateOptions.READ, "New comment", null)

        assert(updatedMessage.id == message.id)
        assert(updatedMessage.actualState == StateOptions.READ)
        assert(updatedMessage.priority == PriorityEnumOptions.HIGH)
    }

    @Test
    fun updateMessage_Exception() {
        val createMessageDTO = CreateMessageDTO(
            subject = "Subject 1",
            body = "body 1",
            priority = PriorityEnumOptions.HIGH.name,
            channel = "email",
            sender = "test@unit.test"
        )
        //Create a new message
        val message = messageService.storeMessage(createMessageDTO, PhoneOrMailOption.MAIL)

        //Update the message
        assertThrows<InvalidUpdateMessageRequestException> {
            // Call the function with a non-existing message ID
            messageService.updateMessage(message.id, null, "New comment", null)

        }
    }
}