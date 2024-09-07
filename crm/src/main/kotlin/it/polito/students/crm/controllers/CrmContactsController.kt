package it.polito.students.crm.controllers

import it.polito.students.crm.dtos.*
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.DetailContactNotLinkedException
import it.polito.students.crm.exception_handlers.DetailNotFoundException
import it.polito.students.crm.services.AddressService
import it.polito.students.crm.services.ContactService
import it.polito.students.crm.services.EmailService
import it.polito.students.crm.services.TelephoneService
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.ADDRESSES_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.ADDRESSES_NOT_VALID
import it.polito.students.crm.utils.ErrorsPage.Companion.ADDRESS_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.CATEGORY_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CONTACT_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CONTACT_ID_AND_DETAIL_ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CONTACT_ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.EMAILS_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.EMAILS_NOT_VALID
import it.polito.students.crm.utils.ErrorsPage.Companion.EMAIL_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_CATEGORY
import it.polito.students.crm.utils.ErrorsPage.Companion.ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.NAME_SURNAME_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.PAGE_NUMBER_CANNOT_BE_NEGATIVE_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.REQUESTED_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.TELEPHONES_BAD_FORMATTED
import it.polito.students.crm.utils.ErrorsPage.Companion.TELEPHONES_NOT_VALID
import it.polito.students.crm.utils.ErrorsPage.Companion.TELEPHONE_BAD_FORMATTED
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/contacts")
class CrmContactsController(
    private val contactService: ContactService,
    private val emailService: EmailService,
    private val addressService: AddressService,
    private val telephoneService: TelephoneService,
) {
    private val logger = LoggerFactory.getLogger(CrmContactsController::class.java)

    /**
     * This endpoint handles a GET request to retrieve all contacts.
     *
     * @param pageNumber the desired page number for pagination (default value: 0).
     * @param pageSize the number of items per page (default value: 10).
     * @param name the filter criteria for contact search by name.
     * @param surname the filter criteria for contact search by surname.
     * @param category the filter criteria for contact search by category.
     * @param ssnCode the filter criteria for contact search by ssnCode.
     * @param comment the filter criteria for contact search by comment.
     * @return a ResponseEntity containing a list of ContactDTO objects representing the contacts, along with an HTTP status code.
     * @exception IllegalArgumentException if pageNumber or pageSize is negative.
     */
    @GetMapping("/", "")
    fun getAllContacts(
        @RequestParam(defaultValue = "0", required = false) pageNumber: Int,
        @RequestParam(defaultValue = "10", required = false) pageSize: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) ssnCode: String?,
        @RequestParam(required = false) comment: String?
    ): ResponseEntity<Any> {
        if (pageNumber < 0 || pageSize < 0) {
            return ResponseEntity(PAGE_NUMBER_CANNOT_BE_NEGATIVE_ERROR, HttpStatus.BAD_REQUEST)
        }

        val filter = HashMap<ContactEnumFields, String>().apply {
            if (name != null) {
                put(ContactEnumFields.NAME, name)
            }
            if (surname != null) {
                put(ContactEnumFields.SURNAME, surname)
            }
            if (category != null) {
                put(ContactEnumFields.CATEGORY, category)
            }
            if (ssnCode != null) {
                put(ContactEnumFields.SSN_CODE, ssnCode)
            }
            if (comment != null) {
                put(ContactEnumFields.COMMENT, comment)
            }
        }

        val pageImplDto = contactService.getAllContacts(pageNumber, pageSize, filter)

        val mapAnswer: Map<String, Any?> = mapOf(
            "content" to pageImplDto.content,
            "currentPage" to pageImplDto.number,
            "elementPerPage" to pageImplDto.size,
            "totalPages" to pageImplDto.totalPages,
            "totalElements" to pageImplDto.totalElements
        )

        return ResponseEntity(mapAnswer, HttpStatus.OK)
    }

    @GetMapping("/{contactID}", "/{contactID}/")
    fun getContact(@PathVariable contactID: Long): ResponseEntity<out Any> {
        if (contactID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_ERROR))
        }

        try {
            val contact = contactService.getContact(contactID)

            //return an object containing the contact and all the lists of the associated emails, telephones and addresses
            val returnedObj = contact.toDTOWithAssociatedData()

            return ResponseEntity(returnedObj, HttpStatus.OK)
        } catch (e: ContactNotFoundException) {
            logger.info("Error with contact id equal to ${contactID}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error with contact id equal to ${contactID}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("", "/")
    fun storeNewContact(@RequestBody contact: CreateContactDTO): ResponseEntity<out Any> {
        try {
            if (contact.name.isBlank() || contact.surname.isBlank()) {
                throw BadRequestException(NAME_SURNAME_ERROR)
            }
            if (contact.category != null && contact.category!!.isNotBlank()) {
                checkCategoryIsValid(contact.category!!)
            } else {
                throw BadRequestException(CATEGORY_ERROR)
            }
            //Check validity List of emails, telephones and addresses
            if (contact.emails != null && contact.emails!!.isNotEmpty()) {
                contact.emails?.forEach {
                    if (!isValidEmail(it.email)) {

                        throw BadRequestException(EMAILS_NOT_VALID)
                    }
                }
            }
            if (contact.addresses != null && contact.addresses!!.isNotEmpty()) {
                contact.addresses?.forEach {
                    if (!isValidAddress(it)) {
                        throw BadRequestException(ADDRESSES_NOT_VALID)
                    }
                }
            }
            if (contact.telephones != null && contact.telephones!!.isNotEmpty()) {
                //If a telephone number is not valid throws an exception
                contact.telephones?.forEach {
                    if (!isValidPhone(it.telephone)) {
                        throw BadRequestException(TELEPHONES_NOT_VALID)
                    }
                }
            }
            //Pass a CategoryOptions to the server
            val category = CategoryOptions.valueOf(contact.category!!.uppercase())

            val savedContract = contactService.storeContact(contact, category)
            return ResponseEntity(savedContract, HttpStatus.CREATED)
        } catch (e: BadRequestException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: IllegalArgumentException) {
            logger.info("Error: ${e.javaClass} - ${ERROR_MESSAGE_CATEGORY}: ${e.message}")
            return ResponseEntity(ERROR_MESSAGE_CATEGORY, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Error saving a new contact: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("{contactID}/{whatContact}", "{contactID}/{whatContact}/")
    fun storeContactBy(
        @PathVariable contactID: Long,
        @PathVariable whatContact: String,
        @RequestBody contact: CreateGenericContactDTO,
    ): ResponseEntity<out Any> {
        if (contactID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_ERROR))
        }

        try {
            //Check whether whatContact is a valid one
            val wc = checkWhatContactIsValid(whatContact)

            when (wc) {
                WhatContactOptions.EMAIL -> {
                    if (contact.emails != null && contact.emails!!.isNotEmpty()) {
                        //If an email is not valid throws an exception
                        contact.emails?.forEach {
                            if (!isValidEmail(it.email)) {
                                throw BadRequestException(EMAILS_NOT_VALID)
                            }
                        }
                        val savedEmail = emailService.storeEmailList(contactID, contact.emails)
                        return ResponseEntity(savedEmail, HttpStatus.CREATED)
                    } else {
                        throw BadRequestException(EMAILS_BAD_FORMATTED)
                    }
                }

                WhatContactOptions.ADDRESS -> {
                    if (contact.addresses != null && contact.addresses!!.isNotEmpty()) {
                        //ToDo check for each address that it's valid or not
                        //If an address is not valid throws an exception: Valid at least one address field among [state, region, city, address] is not blank!
                        contact.addresses?.forEach {
                            if (!isValidAddress(it)) {
                                throw BadRequestException(ADDRESSES_NOT_VALID)
                            }
                        }
                        val savedAddress = addressService.storeAddressList(contactID, contact.addresses)
                        return ResponseEntity(savedAddress, HttpStatus.CREATED)
                    } else {
                        throw BadRequestException(ADDRESSES_BAD_FORMATTED)
                    }
                }

                WhatContactOptions.TELEPHONE -> {
                    if (contact.telephones != null && contact.telephones!!.isNotEmpty()) {
                        //If a telephone number is not valid throws an exception
                        contact.telephones?.forEach {
                            if (!isValidPhone(it.telephone)) {
                                throw BadRequestException(TELEPHONES_NOT_VALID)
                            }
                        }
                        val savedTelephone = telephoneService.storeTelephoneList(contactID, contact.telephones)
                        return ResponseEntity(savedTelephone, HttpStatus.CREATED)
                    } else {
                        throw BadRequestException(TELEPHONES_BAD_FORMATTED)
                    }
                }
                // Se il body non è ben formattato qui non ci arriva mai
                else -> throw BadRequestException(REQUESTED_BAD_FORMATTED)
            }
        } catch (e: BadRequestException) {
            logger.info("Failed to insert a new $whatContact detail. Details: Body not well formatted")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.info("Fail to insert a new $whatContact detail. Details: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.info("Failed to insert a new contact detail. Details: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("{contactID}/{whatContact}")
    fun getContactBy(
        @PathVariable contactID: Long,
        @PathVariable whatContact: String,
    ): ResponseEntity<out Any> {
        if (contactID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_ERROR))
        }

        try {
            //Check whether whatContact is a valid one
            val wc = checkWhatContactIsValid(whatContact)

            when (wc) {
                WhatContactOptions.EMAIL -> {
                    val savedTelephone = telephoneService.getTelephoneList(contactID)
                    return ResponseEntity(savedTelephone, HttpStatus.OK)
                }

                WhatContactOptions.ADDRESS -> {
                    val savedAddresses = addressService.getAddressList(contactID)
                    return ResponseEntity(savedAddresses, HttpStatus.OK)
                }

                WhatContactOptions.TELEPHONE -> {
                    val savedTelephones = telephoneService.getTelephoneList(contactID)
                    return ResponseEntity(savedTelephones, HttpStatus.OK)
                }
            }
        } catch (e: BadRequestException) {
            logger.info("Failed to retrieve $whatContact detail. Details: Contact id error")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: ContactNotFoundException) {
            logger.info("Failed to retrieve $whatContact detail. Details: Contact not found")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.info("Fail to retrieve $whatContact detail. Details: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.info("Failed to retrieve contact detail. Details: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("{contactID}/{whatContact}/{id}", "{contactID}/{whatContact}/{id}/")
    fun replaceContactByID(
        @PathVariable contactID: Long,
        @PathVariable whatContact: String,
        @PathVariable id: Long,
        @RequestBody contact: CreateContactIdDTO,
    ): ResponseEntity<out Any> {
        if (contactID < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_ERROR))
        }
        if (id < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to ID_ERROR))
        }

        try {
            //Check whether whatContact is a valid one
            val wc = checkWhatContactIsValid(whatContact)

            when (wc) {
                WhatContactOptions.EMAIL -> {
                    if (contact.email != null) {
                        //Check email valid
                        if (!isValidEmail(contact.email!!.email)) {
                            throw BadRequestException(EMAILS_NOT_VALID)
                        }
                        // Call function on email service
                        val savedEmail = emailService.modifyEmail(contactID, contact.email!!, id)
                        return ResponseEntity(savedEmail, HttpStatus.OK)
                    } else {
                        throw BadRequestException(EMAIL_BAD_FORMATTED)
                    }
                }

                WhatContactOptions.ADDRESS -> {
                    if (contact.address != null) {
                        //Check valid address
                        if (!isValidAddress(contact.address!!)) {
                            throw BadRequestException(ADDRESSES_NOT_VALID)
                        }
                        // Call function on address service
                        val savedAddress = addressService.modifyAddress(contactID, contact.address!!, id)
                        return ResponseEntity(savedAddress, HttpStatus.OK)
                    } else {
                        throw BadRequestException(ADDRESS_BAD_FORMATTED)
                    }
                }

                WhatContactOptions.TELEPHONE -> {
                    if (contact.telephone != null) {
                        //Check valid telephone number
                        if (!isValidPhone(contact.telephone!!.telephone)) {
                            throw BadRequestException(TELEPHONES_NOT_VALID)
                        }
                        // Call function on telephone service
                        val savedTelephone = telephoneService.modifyTelephone(contactID, contact.telephone!!, id)
                        return ResponseEntity(savedTelephone, HttpStatus.OK)
                    } else {
                        throw BadRequestException(TELEPHONE_BAD_FORMATTED)
                    }
                }
                // Se il body non è ben formattato qui non ci arriva mai
                else -> throw BadRequestException(REQUESTED_BAD_FORMATTED)
            }
        } catch (e: BadRequestException) {
            logger.info("Failed to insert a new $whatContact detail. Details: Body not well formatted")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: IllegalArgumentException) {
            logger.info("Fail to insert a new $whatContact detail. Details: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Failed to insert a new contact detail. Details: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PatchMapping("/{contactID}", "/{contactID}/")
    fun updateContact(
        @PathVariable contactID: Long,
        @RequestBody @Valid contactDto: UpdateContactDTO
    ): ResponseEntity<out Any> {
        try {
            if (contactDto.category != null) {
                val category = CategoryOptions.valueOf(contactDto.category!!.uppercase())

                contactDto.id = contactID
                val contactModified = contactService.updateContact(contactDto, category)
                return ResponseEntity(contactModified, HttpStatus.OK)
            } else {
                throw IllegalArgumentException(CATEGORY_ERROR)
            }
        } catch (e: IllegalArgumentException) {
            logger.info("Error: ${e.javaClass} - ${CATEGORY_ERROR}: ${e.message}")
            return ResponseEntity(CATEGORY_ERROR, HttpStatus.BAD_REQUEST)
        } catch (e: ContactNotFoundException) {
            logger.info("Error with contact id equal to ${contactID}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Failed to update a new contact detail. Details: ${e.message}")
            return ResponseEntity(
                "Failed to update a new contact detail. Details: ${e.message}",
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    @DeleteMapping("/{contactId}/{whatContact}/{id}", "/{contactId}/{whatContact}/{id}/")
    fun deleteContactDetail(
        @PathVariable contactId: Long,
        @PathVariable whatContact: String,
        @PathVariable id: Long
    ): ResponseEntity<Any> {
        if (contactId < 0 || id < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_AND_DETAIL_ID_ERROR))
        }

        try {
            val contactType = WhatContactOptions.valueOf(whatContact.uppercase())

            when (contactType) {
                WhatContactOptions.EMAIL -> emailService.deleteContactEmail(contactId, id)
                WhatContactOptions.ADDRESS -> addressService.deleteContactAddress(contactId, id)
                WhatContactOptions.TELEPHONE -> telephoneService.deleteContactTelephone(contactId, id)
            }

            return ResponseEntity("Contact deleted correctly!", HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            logger.info("Whatcontact type sent ($whatContact) is not one of the options available!")
            return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ERROR))
        } catch (e: ContactNotFoundException) {
            logger.info("Failed to delete the contact detail. Contact with id = $contactId not found!")
            return ResponseEntity(mapOf("error" to "Contact with id = $contactId not found!"), HttpStatus.NOT_FOUND)
        } catch (e: DetailNotFoundException) {
            logger.info("Failed to delete the contact detail. Detail with id = $id not found!")
            return ResponseEntity(mapOf("error" to "Detail with id = $id not found!"), HttpStatus.NOT_FOUND)
        } catch (e: DetailContactNotLinkedException) {
            logger.info("Failed to delete the contact detail! Detail with id = $id not owned by contact with id = $contactId")
            return ResponseEntity(
                mapOf("error" to "Detail with id = $id not owned by contact with id = $contactId"),
                HttpStatus.NOT_FOUND
            )
        } catch (e: Exception) {
            logger.info("Server internal error: ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal server error!"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     *  Convert in a WhatContactOptions and check whether whatContact is a valid String
     *
     *  @param whatContact
     *  @return contact
     *  @throws IllegalArgumentException if whatContact cannot be converted in WhatContactOptions
     */
    @Throws
    fun checkWhatContactIsValid(whatContact: String): WhatContactOptions {
        try {
            val contact = WhatContactOptions.valueOf(whatContact.uppercase())
            return contact
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Illegal contact type!")
        }
    }


    @GetMapping("/whatContact/{whatContact}", "/whatContact/{whatContact}/")
    fun getAllWhatContacts(
        @PathVariable whatContact: String
    ): ResponseEntity<Any> {
        try {
            val contactType = WhatContactOptions.valueOf(whatContact.uppercase())

            val result = when (contactType) {
                WhatContactOptions.EMAIL -> emailService.getAllEmails()
                WhatContactOptions.ADDRESS -> addressService.getAllAddresses()
                WhatContactOptions.TELEPHONE -> telephoneService.getAllTelephones()
            }

            return ResponseEntity(result, HttpStatus.OK)
        }
        catch (e: Exception) {
            logger.info("Server internal error: ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal server error!"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/whatContact/{whatContact}", "/whatContact/{whatContact}/")
    fun postNewWhatContact(
        @PathVariable whatContact: String,
        @RequestBody createWhatContactDTO: CreateWhatContactDTO
    ) : ResponseEntity<Any> {
        try {
            val contactType = WhatContactOptions.valueOf(whatContact.uppercase())

            val wc = checkWhatContactIsValid(whatContact)

            when (wc) {
                WhatContactOptions.EMAIL -> {

                    if (createWhatContactDTO.createEmailDTO == null || !isValidEmail(createWhatContactDTO.createEmailDTO!!.email)) {
                        throw BadRequestException(EMAILS_NOT_VALID)

                    }
                }

                WhatContactOptions.ADDRESS -> {

                    if (createWhatContactDTO.createAddressDTO == null || !isValidAddress(createWhatContactDTO.createAddressDTO!!)) {
                        throw BadRequestException(ADDRESSES_NOT_VALID)


                    }
                }


                WhatContactOptions.TELEPHONE -> {

                    if (createWhatContactDTO.createTelephoneDTO == null || !isValidPhone(createWhatContactDTO.createTelephoneDTO!!.telephone)) {
                        throw BadRequestException(TELEPHONES_NOT_VALID)
                    }
                }
            }

            val result = when (contactType) {
                WhatContactOptions.EMAIL -> emailService.storeNewEmail(createWhatContactDTO.createEmailDTO!!.email, createWhatContactDTO.createEmailDTO!!.comment)
                WhatContactOptions.ADDRESS -> addressService.storeNewAddress(createWhatContactDTO.createAddressDTO!!.address, createWhatContactDTO.createAddressDTO!!.city, createWhatContactDTO.createAddressDTO!!.region, createWhatContactDTO.createAddressDTO!!.state, createWhatContactDTO.createAddressDTO!!.comment)
                WhatContactOptions.TELEPHONE -> telephoneService.storeNewTelephone(createWhatContactDTO.createTelephoneDTO!!.telephone, createWhatContactDTO.createTelephoneDTO!!.comment)
            }

            return ResponseEntity(result, HttpStatus.OK)
        }
        catch (e: BadRequestException) {
            logger.info("Failed to insert a new $whatContact detail. Details: Body not well formatted")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
        catch (e: Exception) {
            logger.info("Server internal error: ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal server error!"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/whatContact/{whatContact}/{id}")
    fun deleteWhatContact(
        @PathVariable whatContact: String,
        @PathVariable id: Long
    ) : ResponseEntity<Any> {
        try {
            val contactType = WhatContactOptions.valueOf(whatContact.uppercase())

            val wc = checkWhatContactIsValid(whatContact)

            if (id < 0) {
                return ResponseEntity.badRequest().body(mapOf("error" to CONTACT_ID_AND_DETAIL_ID_ERROR))
            }

            val result = when (contactType) {
                WhatContactOptions.EMAIL -> emailService.deleteEmail(id)
                WhatContactOptions.ADDRESS -> addressService.deleteAddress(id)
                WhatContactOptions.TELEPHONE -> telephoneService.deleteTelephone(id)
            }

            return ResponseEntity(mapOf("result" to "Delete operation succesfully completed" ), HttpStatus.OK)
        }
        catch (e: IllegalArgumentException) {
            logger.info("Illegal argument exception: ${e.message}")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
        catch (e: Exception) {
            logger.info("Server internal error: ${e.message}")
            return ResponseEntity(mapOf("error" to "Internal server error!"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}