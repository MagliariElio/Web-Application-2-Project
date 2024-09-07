package it.polito.students.crm.services

import it.polito.students.crm.dtos.AddressDTO
import it.polito.students.crm.dtos.CreateAddressDTO

interface AddressService {
    /**
     * Creates and store an Address in database
     *
     * @param contactId
     * @param addressDto
     * @return List DTO of Address created
     */
    fun storeAddressList(contactId: Long, addressDto: List<CreateAddressDTO>?): List<AddressDTO>

    /**
     * Retrieve all addresses of a contact from the database
     *
     * @param contactId
     * @return List DTO of Addresses
     */
    fun getAddressList(contactId: Long): List<AddressDTO>

    /**
     * Creates and store an Address in database
     *
     * @param contactId
     * @param addressDto
     * @return The DTO of Address created
     */
    fun modifyAddress(contactId: Long, addressDto: CreateAddressDTO, id: Long): AddressDTO

    /**
     * Delete an Address in database
     *
     * @param contactId
     * @param addressId
     * @return -
     */
    fun deleteContactAddress(contactId: Long, addressId: Long)


    /**
     * get all addresses in db
     *
     * @return - all addresses
     */
    fun getAllAddresses(): List<AddressDTO>

    /**
     * add a new address to address book
     *
     * @param address
     * @param city
     * @param region
     * @param state
     * @param comment
     * @return - the new address
     */
    fun storeNewAddress(address: String?, city: String?, region: String?, state: String?, comment: String?): AddressDTO
}