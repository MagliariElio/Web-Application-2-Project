package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateEmailDTO
import it.polito.students.crm.dtos.EmailDTO
import it.polito.students.crm.dtos.toDTO
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Email
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.DetailContactNotLinkedException
import it.polito.students.crm.exception_handlers.DetailNotFoundException
import it.polito.students.crm.exception_handlers.InvalidContactDetailsException
import it.polito.students.crm.repositories.ContactRepository
import it.polito.students.crm.repositories.EmailRepository
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage.Companion.MAIL_REQUIRED_ERROR
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class EmailServiceImpl(
    private val emailRepository: EmailRepository,
    private val contactRepository: ContactRepository,
    private val messageService: MessageServiceImpl
) : EmailService {

    //TODO function to delete
    override fun storeUnknownContactEmail(emailDto: EmailDTO) {

        //Check if sender is already in our contacts
        val emailStored = emailRepository.findByEmail(emailDto.email)

        if (emailStored == null) {
            //Contact not found create and insert a contact blank contact with email
            val newEmail = Email().apply {
                email = emailDto.email
                comment = emailDto.comment
            }
            val newContact = Contact().apply {
                name = ""
                surname = ""
                ssnCode = ""
                category = CategoryOptions.UNKNOWN
                comment = ""
            }
            newContact.addEmail(newEmail)
            contactRepository.save(newContact)
        }
    }

    /**
     *  Create a new Email contact
     *
     *  @params contactID, email, address, comment
     *  @throws InvalidContactDetailsException if email is not inserted
     */
    @Transactional
    @Throws
    override fun storeEmailList(contactId: Long, emailDto: List<CreateEmailDTO>?): List<EmailDTO> {

        //Check if contact with contactID is already in db
        val contact = contactRepository.findById(contactId)

        val listEmailsAdded = mutableListOf<Email>()

        if (emailDto.isNullOrEmpty()) {
            throw InvalidContactDetailsException(MAIL_REQUIRED_ERROR)
        }

        if (contact.isPresent) {
            val presentContact = contact.get()

            emailDto.forEach {
                //check if it is already in db before insert it
                val existedEmail = emailRepository.findByEmail(it.email)

                if (existedEmail == null) {
                    val newEmail = Email().apply {
                        email = it.email.lowercase()
                        comment = it.comment ?: ""
                    }
                    //Add a new email to a presentContact
                    newEmail.addContact(presentContact)
                    val emailSaved = emailRepository.save(newEmail)
                    listEmailsAdded.add(emailSaved)
                } else {
                    //Add a contact to existedEmail
                    presentContact.addEmail(existedEmail)
                    listEmailsAdded.add(existedEmail)
                }
            }

            contactRepository.save(presentContact)

            val listEmailAddedDto = listEmailsAdded.map { it.toDTO() }
            return listEmailAddedDto

        } else {
            throw ContactNotFoundException("Contact with id $contactId not found")
        }
    }

    override fun getEmailList(contactId: Long): List<EmailDTO> {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }

        val contact = existedContact.get()
        val existentEmails = contact.emails

        return existentEmails.map { it.toDTO() }
    }

    override fun modifyEmail(contactId: Long, emailDto: CreateEmailDTO, emailId: Long): EmailDTO {
        val existedContact = contactRepository.findById(contactId)
        if (!existedContact.isPresent) {
            throw ContactNotFoundException("Contact with id=$contactId not found!")
        }
        val existedEmail = emailRepository.findById(emailId)

        // Replace what in existedEmail
        if (existedEmail.isPresent) {
            val oldMail = existedEmail.get()

            oldMail.email = emailDto.email
            oldMail.comment = emailDto.comment ?: oldMail.comment

            val mailReplaced = emailRepository.save(oldMail)

            return mailReplaced.toDTO()
        } else {
            throw Exception("Email with id $emailId not found!")
        }
    }

    override fun deleteContactEmail(contactId: Long, emailId: Long) {
        val contactOpt: Optional<Contact> = contactRepository.findById(contactId)

        if (!contactOpt.isPresent)
            throw ContactNotFoundException("Contact with id=$contactId not found!")

        val emailOpt: Optional<Email> = emailRepository.findById(emailId)

        if (!emailOpt.isPresent)
            throw DetailNotFoundException("Email with id = $emailId not found!")

        val contact = contactOpt.get()
        val email = emailOpt.get()

        if (!contact.emails.contains(email) || !email.contacts.contains(contact)) //checking the correct relation between contact and email
            throw DetailContactNotLinkedException("Contact with id=$contactId doesn't contain the email with id=$emailId!")

        contact.removeEmail(email)
        email.removeContact(contact)

        if (email.contacts.size == 0) {
            val blankContact = messageService.createBlankContact()
            blankContact.addEmail(email)
            contactRepository.save(blankContact)
        }

        contactRepository.save(contact)
        emailRepository.save(email)
    }

    override fun getAllEmails(): List<EmailDTO> {
        return emailRepository.findAll().map { it.toDTO() }
    }

    override fun deleteEmail(emailId: Long) {
        emailRepository.deleteById(emailId)
    }

}