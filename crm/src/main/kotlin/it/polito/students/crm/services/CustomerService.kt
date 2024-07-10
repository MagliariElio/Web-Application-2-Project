package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateContactDTO
import it.polito.students.crm.dtos.CustomerDTO
import it.polito.students.crm.utils.ContactEnumFields
import org.springframework.data.domain.PageImpl

interface CustomerService {
    /**
     * Retrieves a paginated list of customer DTOs based on the provided pagination parameters and optional filters.
     *
     * @param pageNumber The page number to retrieve.
     * @param pageSize The size of each page.
     * @param filterMap A map containing filter criteria for customer fields.
     * @return A PageImpl containing the paginated list of CustomerDTOs.
     */
    fun getAllCustomers(
        pageNumber: Int,
        pageSize: Int,
        filterMap: HashMap<ContactEnumFields, String>
    ): PageImpl<CustomerDTO>

    /**
     * Retrieves a customer DTO by their ID.
     *
     * @param customerId The ID of the customer to retrieve.
     * @return The CustomerDTO corresponding to the provided customer ID.
     * @throws CustomerNotFoundException if the customer with the specified ID is not found.
     */
    fun getCustomer(customerId: Long): CustomerDTO

    /**
     * Add a new customer.
     *
     * @param contact The contact information of the customer to insert.
     * @return The CustomerDTO corresponding to the provided customer ID.
     */
    fun postNewCustomer(contactInfo: CreateContactDTO): CustomerDTO

    /**
     * Updates the contact associated with a customer.
     *
     * @param customerID The ID of the customer to update.
     * @param contactID The ID of the contact to associate with the customer.
     * @throws CustomerNotFoundException if the customer is not found.
     * @throws ContactNotFoundException if the contact is not found.
     */
    fun updateCustomer(customerID: Long, contactID: Long): CustomerDTO

    /**
     * Deletes a customer by their ID.
     *
     * @param customerID The ID of the customer to delete.
     * @throws CustomerNotFoundException if the customer is not found.
     */
    fun deleteCustomer(customerID: Long)
}