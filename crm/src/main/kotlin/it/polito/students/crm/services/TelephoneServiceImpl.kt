package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateTelephoneDTO
import it.polito.students.crm.dtos.TelephoneDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Email
import it.polito.students.crm.entities.Telephone
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.DetailContactNotLinkedException
import it.polito.students.crm.exception_handlers.DetailNotFoundException
import it.polito.students.crm.exception_handlers.InvalidContactDetailsException
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.TelephoneRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage.Companion.TELEPHONE_REQUIRED_ERROR
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class TelephoneServiceImpl(
    private val telephoneRepository: TelephoneRepository,
    private val contactRepository: ContactRepository,
    private val messageService: MessageServiceImpl
) : TelephoneService {

    @Transactional
    @Throws
    override fun storeTelephoneList(contactId: Long, telephoneDto: List<CreateTelephoneDTO>?): List<TelephoneDTO> {
        //Check if contact with contactID is already in db
        val contact = contactRepository.findById(contactId)

        val listPhonesAdded = mutableListOf<Telephone>()

        if (telephoneDto.isNullOrEmpty()) {
            throw InvalidContactDetailsException(TELEPHONE_REQUIRED_ERROR)
        }

        if (contact.isPresent) {
            val presentContact = contact.get()
            telephoneDto.forEach {
                val existedPhone = telephoneRepository.findByTelephone(it.telephone)

                if (existedPhone == null) {
                    //Contact not create and insert a new telephone
                    val newPhone = Telephone().apply {
                        telephone = it.telephone
                        comment = it.comment ?: ""
                    }
                    //Add a new telephone to a presentContact
                    newPhone.addContact(presentContact)
                    val savedPhone = telephoneRepository.save(newPhone)
                    //Add savedPhone to a listAdded
                    listPhonesAdded.add(savedPhone)
                } else {
                    //Add a contact to existedEmail
                    presentContact.addTelephone(existedPhone)
                    listPhonesAdded.add(existedPhone)
                }
            }

            contactRepository.save(presentContact)

            val listTelephoneSavedDto = listPhonesAdded.map { it.toDTO() }
            return listTelephoneSavedDto

        } else {
            throw ContactNotFoundException("Contact with id $contactId not found")
        }
    }

    override fun getTelephoneList(contactId: Long): List<TelephoneDTO> {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }

        val contact = existedContact.get()
        val existentTels = contact.telephones

        return existentTels.map { it.toDTO() }
    }

    override fun modifyTelephone(contactId: Long, telephoneDto: CreateTelephoneDTO, id: Long): TelephoneDTO {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }
        val existedTel = telephoneRepository.findById(id)

        // Replace what in existedTel
        if (existedTel.isPresent) {
            val oldTel = existedTel.get()

            oldTel.telephone = telephoneDto.telephone
            oldTel.comment = telephoneDto.comment ?: oldTel.comment

            val telReplaced = telephoneRepository.save(oldTel)

            return telReplaced.toDTO()
        } else {
            throw Exception("Telephone with id $id not found!")
        }
    }

    override fun storeUnknownContactTelephone(telephoneDto: TelephoneDTO) {
        //Check if sender is already in our contacts
        val phoneStored = telephoneRepository.findByTelephone(telephoneDto.telephone)

        if (phoneStored == null) {
            //Contact not found create and insert a contact blank contact with telephone
            val newPhone = Telephone().apply {
                telephone = telephoneDto.telephone
                comment = telephoneDto.comment
            }
            val newContact = Contact().apply {
                name = ""
                surname = ""
                ssnCode = ""
                category = CategoryOptions.UNKNOWN
                comment = ""
            }
            newContact.addTelephone(newPhone)
            contactRepository.save(newContact)
        }
    }

    override fun deleteContactTelephone(contactId: Long, telephoneId: Long) {
        val contactOpt: Optional<Contact> = contactRepository.findById(contactId)

        if (!contactOpt.isPresent)
            throw ContactNotFoundException("Contact with id=$contactId not found!")

        val telephoneOpt: Optional<Telephone> = telephoneRepository.findById(telephoneId)

        if (!telephoneOpt.isPresent)
            throw DetailNotFoundException("Telephone with id = $telephoneId not found!")

        val contact = contactOpt.get()
        val telephone = telephoneOpt.get()

        if (!contact.telephones.contains(telephone) || !telephone.contacts.contains(contact)) //checking the correct relation between contact and telephone
            throw DetailContactNotLinkedException("Contact with id=$contactId doesn't contain the telephone number with id=$telephoneId!")

        contact.removeTelephone(telephone)
        telephone.removeContact(contact)

        if (telephone.contacts.size == 0) {
            val blankContact = messageService.createBlankContact()
            blankContact.addTelephone(telephone)
            contactRepository.save(blankContact)
        }

        contactRepository.save(contact)
        telephoneRepository.save(telephone)
    }

    override fun getAllTelephones(): List<TelephoneDTO> {
        return telephoneRepository.findAll().map { it.toDTO() }
    }

    override fun storeNewTelephone(telephone: String, comment: String?): TelephoneDTO {
        val newTelephone = Telephone().apply {
            this.telephone = telephone.lowercase()
            this.comment = comment ?: ""
        }

        return telephoneRepository.save(newTelephone).toDTO()
    }

    override fun deleteTelephone(telephoneId: Long) {
        val optionalTelephone = telephoneRepository.findById(telephoneId)

        if (optionalTelephone.isPresent) {
            val telephone = optionalTelephone.get()

            telephone.contacts.forEach { contact ->
                contact.telephones.removeIf { tel -> tel.id == telephoneId }
            }

            telephone.contacts.clear()

            telephoneRepository.delete(telephone)
        } else {
            throw EntityNotFoundException("Telefono con ID $telephoneId non trovato.")
        }
    }
}