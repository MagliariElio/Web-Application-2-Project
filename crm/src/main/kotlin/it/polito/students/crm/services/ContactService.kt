package it.polito.students.crm.services

import it.polito.students.crm.dtos.ContactDTO
import it.polito.students.crm.dtos.ContactWithAssociatedDataDTO
import it.polito.students.crm.dtos.CreateContactDTO
import it.polito.students.crm.dtos.UpdateContactDTO
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ContactEnumFields
import org.springframework.data.domain.PageImpl

interface ContactService {

    /**
     * Gets a list of all contacts in db, paginated
     * @param pageNumber the desired page number for pagination.
     * @param pageSize the number of items per page.
     * @param filterMap filter a map containing filter criteria for contact search.
     *      The keys of the map correspond to the field names of the "Contact" entity,
     *      while the values are the values to filter by.
     *
     * @return PageImpl<ContactDTO>
     */
    fun getAllContacts(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ContactEnumFields, String>
    ): PageImpl<ContactDTO>

    /**
     * Retrieves the contact with the specified ID from the database.
     *
     * @param id The ID of the contact to retrieve.
     * @return The retrieved contact.
     * @throws ContactNotFoundException If the contact with the specified ID is not found.
     */
    fun getContact(id: Long): Contact

    /**
     * Creates and store a Contact in database
     *
     * @param contactDto
     * @return The DTO of Contact created
     */
    fun storeContact(contactDto: CreateContactDTO, category: CategoryOptions): ContactWithAssociatedDataDTO

    /**
     * This function handles the update of a contact using the data provided in a DTO object.
     * It retrieves the contact from the database, then updates its fields with the non-null values provided in the DTO object.
     *
     * @param contactDto The DTO object containing the data for updating the contact.
     * @param categoryParam: The correct category enum.
     * @return A DTO object representing the details of the updated contact.
     * @throws ContactNotFoundException If the contact with the specified ID is not found.
     * @throws DataIntegrityViolationException If an error occurs while saving the contact to the database.
     */
    fun updateContact(contactDto: UpdateContactDTO, categoryParam: CategoryOptions): ContactDTO

    /**
     * This function checks if a contact is already stored in the database
     * @param contact The DTO of the contact to check
     * @return the id of the found contact or null
     */
    fun checkExistingContact(contact: CreateContactDTO): Long?
}