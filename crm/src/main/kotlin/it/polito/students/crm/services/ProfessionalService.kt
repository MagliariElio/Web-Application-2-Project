package it.polito.students.crm.services

import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Professional
import it.polito.students.crm.utils.EmploymentStateEnum
import it.polito.students.crm.utils.ProfessionalEnumFields
import org.springframework.data.domain.PageImpl

interface ProfessionalService {
    /**
     * Gets a list of all contacts in db, paginated
     * @param pageNumber the desired page number for pagination.
     * @param pageSize the number of items per page.
     * @param filterMap filter a map containing filter criteria for professional search.
     *      The keys of the map correspond to the field names of the "Professional" entity,
     *      while the values are the values to filter by.
     *
     * @return PageImpl<ProfessionalDTO>
     */
    fun getAllProfessionals(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ProfessionalEnumFields, String>
    ): PageImpl<ProfessionalDTO>

    /**
     * Retrieves the professional with the specified ID from the database.
     *
     * @param id The ID of the professional to retrieve.
     * @return The retrieved professional.
     * @throws ProfessionalNotFoundException If the professional with the specified ID is not found.
     */
    fun getProfessional(id: Long): ProfessionalWithAssociatedDataDTO

    /**
     * Creates and store a Professional in database
     *
     * @param professional
     * @param state
     * @return The DTO of Professional created
     */
    fun storeProfessional(professional: CreateProfessionalDTO, state: EmploymentStateEnum): ProfessionalDTO

    /**
     * This function handles the update of a professional using the data provided in a DTO object.
     * It retrieves the professional from the database, then updates its fields with the non-null values provided in the DTO object.
     *
     * @param professionalDto The DTO object containing the data for updating the professional.
     * @param contact: The correct contact updated
     * @return A DTO object representing the details of the updated professional.
     * @throws ProfessionalNotFoundException If the professional with the specified ID is not found.
     * @throws DataIntegrityViolationException If an error occurs while saving the professional to the database.
     */
    fun updateProfessional(professionalDto: UpdateProfessionalDTO, contact: Contact): ProfessionalDTO

    /**
     * Deletes a professional by their ID.
     *
     * @param professionalID The ID of the professional to delete.
     * @throws ProfessionalNotFoundException if the professional is not found.
     */
    fun deleteProfessional(professionalID: Long)

}