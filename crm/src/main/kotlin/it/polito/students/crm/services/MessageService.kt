package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateMessageDTO
import it.polito.students.crm.dtos.HistoryDTO
import it.polito.students.crm.dtos.MessageDTO
import it.polito.students.crm.utils.PhoneOrMailOption
import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.SortOptions
import it.polito.students.crm.utils.StateOptions

interface MessageService {

    /**
     * Retrieve all messages from the database
     *
     * @param -
     * @return The list of all messages
     */
    fun getAllMessages(sortBy: SortOptions? = null, state: StateOptions? = null): List<MessageDTO>

    /**
     * Creates and store a Message in database
     *
     * @param createdMessageDto
     * @param senderType
     * @return The DTO of Message
     */
    fun storeMessage(createdMessageDto: CreateMessageDTO, senderType: PhoneOrMailOption): MessageDTO

    /**
     * Retrieves the message with the specified ID from the database.
     *
     * @param id The ID of the message to retrieve.
     * @return The DTO representation of the retrieved message.
     * @throws MessageNotFoundException If the message with the specified ID is not found.
     */
    fun getMessage(id: Long): MessageDTO

    /**
     * Retrieves the history (state changes) of the message with the specified ID from the database.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The list of all historyDTOs.
     * @throws MessageNotFoundException If the message with the specified ID is not found.
     */
    fun getMessageHistory(messageId: Long): List<HistoryDTO>

    /**
     * The `updateMessage` function calls the priority and actual state functions to update a specific message.
     *
     * @param messageID A `Long` that represents the unique identifier of the message to be updated.
     * @param actualStateParam A `StateOptions` that represents the new state to be set for the message.
     * @param commentParam A `String` that represents an optional comment related to the state update.
     * @param priorityParam A `PriorityEnumOptions` that represents the new priority to be set for the message.
     * @return A `MessageDTO` object that represents the updated message.
     * @throws MessageNotFoundException If the message with the specified ID is not found.
     * @throws InvalidUpdateMessageRequestException If are not specified both actualState between priority fields.
     */
    fun updateMessage(
        messageID: Long,
        actualStateParam: StateOptions?,
        commentParam: String?,
        priorityParam: PriorityEnumOptions?
    ): MessageDTO
}