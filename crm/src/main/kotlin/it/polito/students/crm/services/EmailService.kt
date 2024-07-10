package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateEmailDTO
import it.polito.students.crm.dtos.EmailDTO

interface EmailService {
    /**
     * Creates and store an Email in database
     *
     * @param contactId
     * @param emailDto
     * @return List DTO of Email created
     */
    fun storeEmailList(contactId: Long, emailDto: List<CreateEmailDTO>?): List<EmailDTO>

    /**
     * Retrieve all emails of a contact from the database
     *
     * @param contactId
     * @return List DTO of Emails
     */
    fun getEmailList(contactId: Long): List<EmailDTO>

    /**
     * Creates and store an Email in database
     *
     * @param contactId
     * @param emailDto
     * @param emailId id email
     * @return The DTO of Email created
     */
    fun modifyEmail(contactId: Long, emailDto: CreateEmailDTO, emailId: Long): EmailDTO

    /**
     * Creates and store an Email in database
     *
     * @param emailDto
     * @return The DTO of Email created
     */
    fun storeUnknownContactEmail(emailDto: EmailDTO)

    /**
     * Delete an Email in database
     *
     * @param contactId
     * @param emailId
     * @return -
     */
    fun deleteContactEmail(contactId: Long, emailId: Long)
}