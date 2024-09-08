package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateTelephoneDTO
import it.polito.students.crm.dtos.TelephoneDTO
import org.springframework.http.ResponseEntity

interface TelephoneService {
    /**
     * Creates and store a Telephone in database
     *
     * @param contactId
     * @param telephoneDto
     * @return List DTO of Telephone created
     */
    fun storeTelephoneList(contactId: Long, telephoneDto: List<CreateTelephoneDTO>?): List<TelephoneDTO>

    /**
     * Retrieve all telephones of a contact from the database
     *
     * @param contactId
     * @return List DTO of Telephone
     */
    fun getTelephoneList(contactId: Long): List<TelephoneDTO>

    /**
     * Creates and store a Telephone in database
     *
     * @param contactId
     * @param telephoneDto
     * @return The DTO of Telephone created
     */
    fun modifyTelephone(contactId: Long, telephoneDto: CreateTelephoneDTO, id: Long): TelephoneDTO

    /**
     * Creates and store a Telephone in database
     *
     * @param telephoneDto
     * @return The DTO of Telephone created
     */
    fun storeUnknownContactTelephone(telephoneDto: TelephoneDTO)

    /**
     * Delete a Telephone in database
     *
     * @param contactId
     * @param telephoneId
     * @return -
     */
    fun deleteContactTelephone(contactId: Long, telephoneId: Long)

    /**
     * get all telephones
     *
     * @return - all telepones
     */
    fun getAllTelephones(): List<TelephoneDTO>


    /**
     * add a new telephone to book
     *
     * @param telephone
     * @param comment
     * @return - new telephone
     */
    fun storeNewTelephone(telephone: String, comment: String?): TelephoneDTO

    /**
     * Delete a Telephone in database
     *
     * @param telephoneId
     * @return -
     */
    fun deleteTelephone(telephoneId: Long)

    /**
     * edit an exisiting telephone to book
     *
     * @param telephoneId
     * @param telephone
     * @param comment
     * @return - new telephone
     */
    fun editTelephone(telephoneId: Long, telephone: String, comment: String?): TelephoneDTO

}