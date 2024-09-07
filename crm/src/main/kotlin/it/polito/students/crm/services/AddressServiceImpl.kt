package it.polito.students.crm.services

import it.polito.students.crm.dtos.AddressDTO
import it.polito.students.crm.dtos.CreateAddressDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.Address
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.DetailContactNotLinkedException
import it.polito.students.crm.exception_handlers.DetailNotFoundException
import it.polito.students.crm.exception_handlers.InvalidContactDetailsException
import it.polito.students.crm.repositories.AddressRepository
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.utils.ErrorsPage.Companion.ADDRESS_REQUIRED_ERROR
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class AddressServiceImpl(
    private val addressRepository: AddressRepository,
    private val contactRepository: ContactRepository,
    private val messageService: MessageServiceImpl
) : AddressService {

    @Transactional
    @Throws
    override fun storeAddressList(contactId: Long, addressDto: List<CreateAddressDTO>?): List<AddressDTO> {
        //Check if contact with contactID is already in db
        val contact = contactRepository.findById(contactId)

        val listAddressesAdded = mutableListOf<Address>()

        if (addressDto.isNullOrEmpty()) {
            throw InvalidContactDetailsException(ADDRESS_REQUIRED_ERROR)
        }
        if (contact.isPresent) {
            val presentContact = contact.get()

            addressDto.forEach {
                val existedAddress: Address? = addressRepository
                    .findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
                        it.address!!,
                        it.city!!,
                        it.region!!,
                        it.state!!
                    )

                if (existedAddress == null) {
                    //Create a new Address
                    val newAddress = Address().apply {
                        state = it.state ?: ""
                        region = it.region ?: ""
                        city = it.city ?: ""
                        address = it.address ?: ""
                        comment = it.comment ?: ""
                    }
                    //Add a new address to a presentContact
                    newAddress.addContact(presentContact)
                    val addressSaved = addressRepository.save(newAddress)
                    listAddressesAdded.add(addressSaved)
                } else {
                    //Add a contact to existedAddress
                    presentContact.addAddress(existedAddress)
                    listAddressesAdded.add(existedAddress)
                }
            }

            contactRepository.save(presentContact)
            val listAddressesAddedDto = listAddressesAdded.map { it.toDTO() }
            return listAddressesAddedDto
        } else {
            throw ContactNotFoundException("Contact with id $contactId not found")
        }
    }

    override fun getAddressList(contactId: Long): List<AddressDTO> {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }

        val contact = existedContact.get()
        val existentAdd = contact.addresses

        return existentAdd.map { it.toDTO() }
    }

    override fun modifyAddress(contactId: Long, addressDto: CreateAddressDTO, id: Long): AddressDTO {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }
        val existedAddress = addressRepository.findById(id)

        // Replace what in existedEmail
        if (existedAddress.isPresent) {
            val oldAddress = existedAddress.get()

            oldAddress.state = addressDto.state!!
            oldAddress.region = addressDto.region!!
            oldAddress.city = addressDto.city!!
            oldAddress.address = addressDto.address!!
            oldAddress.comment = addressDto.comment ?: oldAddress.comment

            val addressReplaced = addressRepository.save(oldAddress)

            return addressReplaced.toDTO()
        } else {
            throw Exception("Address with id $id not found!")
        }
    }

    override fun deleteContactAddress(contactId: Long, addressId: Long) {
        val contactOpt: Optional<Contact> = contactRepository.findById(contactId)

        if (!contactOpt.isPresent)
            throw ContactNotFoundException("Contact with id=$contactId not found!")

        val addressOpt: Optional<Address> = addressRepository.findById(addressId)

        if (!addressOpt.isPresent)
            throw DetailNotFoundException("Address with id = $addressId not found!")

        val contact = contactOpt.get()
        val address = addressOpt.get()

        if (!contact.addresses.contains(address) || !address.contacts.contains(contact)) //checking the correct relation between contact and address
            throw DetailContactNotLinkedException("Contact with id=$contactId doesn't contain the address with id=$addressId!")

        contact.removeAddress(address)
        address.removeContact(contact)

        if (address.contacts.size == 0) {
            val blankContact = messageService.createBlankContact()
            blankContact.addAddress(address)
            contactRepository.save(blankContact)
        }

        contactRepository.save(contact)
        addressRepository.save(address)
    }

    override fun getAllAddresses(): List<AddressDTO> {
        return addressRepository.findAll().map { it.toDTO() }
    }

    override fun storeNewAddress(
        address: String?,
        city: String?,
        region: String?,
        state: String?,
        comment: String?
    ): AddressDTO {
        val newAddress = Address().apply {
            this.state = state ?: ""
            this.region = region ?: ""
            this.city = city ?: ""
            this.address = address ?: ""
            this.comment = comment ?: ""
        }
        val addressSaved = addressRepository.save(newAddress)

        return addressSaved.toDTO()
    }

    override fun deleteAddress(addressId: Long) {
        val optionalAddress = addressRepository.findById(addressId)

        if (optionalAddress.isPresent) {
            val address = optionalAddress.get()

            address.contacts.forEach { contact ->
                contact.addresses.removeIf { addr -> addr.id == addressId }
            }

            address.contacts.clear()

            addressRepository.delete(address)
        } else {
            throw EntityNotFoundException("Indirizzo con ID $addressId non trovato.")
        }
    }
}