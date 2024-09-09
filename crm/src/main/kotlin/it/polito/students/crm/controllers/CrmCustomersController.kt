package it.polito.students.crm.controllers

import it.polito.students.crm.dtos.CreateContactDTO
import it.polito.students.crm.dtos.UpdateContactDTO
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.exception_handlers.CustomerNotFoundException
import it.polito.students.crm.exception_handlers.InvalidUpdateException
import it.polito.students.crm.services.CustomerService
import it.polito.students.crm.utils.*
import it.polito.students.crm.utils.ErrorsPage.Companion.CONTACT_ID_NOT_SPECIFIED_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CUSTOMER_ID_CONTACT_ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.CUSTOMER_ID_ERROR
import it.polito.students.crm.utils.ErrorsPage.Companion.DELETED_SUCCESSFULLY
import it.polito.students.crm.utils.ErrorsPage.Companion.GENERAL_ERROR_MESSAGE_UPDATE_CUSTOMER_REQUEST
import it.polito.students.crm.utils.ErrorsPage.Companion.INTERNAL_SERVER_ERROR_MESSAGE
import it.polito.students.crm.utils.ErrorsPage.Companion.PAGE_NUMBER_CANNOT_BE_NEGATIVE_ERROR
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/customers")
class CrmCustomersController(private val customerService: CustomerService) {
    private val logger = LoggerFactory.getLogger(CrmCustomersController::class.java)

    @GetMapping("/", "")
    fun getAllCustomers(
        @RequestParam(defaultValue = "0", required = false) pageNumber: Int,
        @RequestParam(defaultValue = "10", required = false) pageSize: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) surname: String?,
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
            if (ssnCode != null) {
                put(ContactEnumFields.SSN_CODE, ssnCode)
            }
            if (comment != null) {
                put(ContactEnumFields.COMMENT, comment)
            }
        }

        val pageImplDto = customerService.getAllCustomers(pageNumber, pageSize, filter)

        val mapAnswer: Map<String, Any?> = mapOf(
            "content" to pageImplDto.content,
            "currentPage" to pageImplDto.number,
            "elementPerPage" to pageImplDto.size,
            "totalPages" to pageImplDto.totalPages,
            "totalElements" to pageImplDto.totalElements
        )

        return ResponseEntity(mapAnswer, HttpStatus.OK)
    }

    @RequestMapping("/{customerId}", "/{customerId}/")
    fun getCustomerById(@PathVariable customerId: Long): ResponseEntity<out Any> {
        if (customerId < 0) {
            return ResponseEntity.badRequest().body(mapOf("error" to CUSTOMER_ID_ERROR))
        }

        try {
            val customer = customerService.getCustomer(customerId)
            return ResponseEntity(customer, HttpStatus.OK)
        } catch (e: CustomerNotFoundException) {
            logger.info("CustomerControl: Error with customer id equal to ${customerId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("CustomerControl: Error ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/", "")
    fun postNewCustomer(
        @RequestBody contact: CreateContactDTO
    ): ResponseEntity<out Any> {
        try {
            if (contact.name.isBlank() || contact.surname.isBlank()) {
                throw BadRequestException(ErrorsPage.NAME_SURNAME_ERROR)
            }
            //Check validity List of emails, telephones and addresses
            if (contact.emails != null && contact.emails!!.isNotEmpty()) {
                contact.emails?.forEach {
                    if (!isValidEmail(it.email)) {
                        throw BadRequestException(ErrorsPage.EMAILS_NOT_VALID)
                    }
                }
            }
            if (contact.addresses != null && contact.addresses!!.isNotEmpty()) {
                contact.addresses?.forEach {
                    if (!isValidAddress(it)) {
                        throw BadRequestException(ErrorsPage.ADDRESSES_NOT_VALID)
                    }
                }
            }
            if (contact.telephones != null && contact.telephones!!.isNotEmpty()) {
                //If a telephone number is not valid throws an exception
                contact.telephones?.forEach {
                    if (!isValidPhone(it.telephone)) {
                        throw BadRequestException(ErrorsPage.TELEPHONES_NOT_VALID)
                    }
                }
            }

            val savedCustomer = customerService.postNewCustomer(contact)
            return ResponseEntity(savedCustomer, HttpStatus.CREATED)
        } catch (e: BadRequestException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.info("Error saving a new contact: ${e.message}")
            return ResponseEntity(INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PatchMapping("/{customerID}", "/{customerID}/")
    fun updateCustomer(
        @PathVariable customerID: Long,
        @RequestBody requestBody: Map<String, Long>
    ): ResponseEntity<out Any> {
        try {
            val contactID = requestBody["contactID"] ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to CONTACT_ID_NOT_SPECIFIED_ERROR))

            if (customerID < 0 || contactID < 0) {
                return ResponseEntity.badRequest().body(mapOf("error" to CUSTOMER_ID_CONTACT_ID_ERROR))
            }

            val result = customerService.updateCustomer(customerID, contactID)
            return ResponseEntity(result, HttpStatus.OK)
        } catch (e: CustomerNotFoundException) {
            logger.info("Failed to delete the customer. Customer with id = $customerID not found!")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)
        } catch (e: ContactNotFoundException) {
            logger.info("Failed to retrieve the contact. Details: $requestBody")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)
        } catch (e: InvalidUpdateException) {
            logger.info("Failed to update the contact. Details: $requestBody")
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("An error occurred while updating the customer: ${e.message}")
            return ResponseEntity(
                mapOf("error" to "$GENERAL_ERROR_MESSAGE_UPDATE_CUSTOMER_REQUEST: ${e.message}"),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    @PatchMapping("/{customerID}/contactDetails", "/{customerID}/contactDetails/")
    fun updateContactDetails(
        @PathVariable customerID: Long,
        @RequestBody newContactDetails: UpdateContactDTO
    ): ResponseEntity<out Any> {

        try {
            if (newContactDetails.name.isNullOrBlank() || newContactDetails.surname.isNullOrBlank()) {
                throw BadRequestException(ErrorsPage.NAME_SURNAME_ERROR)
            }
            //Check validity List of emails, telephones and addresses
            if (newContactDetails.emails.isNotEmpty()) {
                newContactDetails.emails.forEach {
                    if (!isValidEmail(it.email)) {
                        throw BadRequestException(ErrorsPage.EMAILS_NOT_VALID)
                    }
                }
            }
            if (newContactDetails.addresses.isNotEmpty()) {
                newContactDetails.addresses.forEach {
                    if (!(!(it.city.isNullOrBlank() || it.address.isNullOrBlank() || it.region.isNullOrBlank() || it.state.isNullOrBlank()))) {
                        throw BadRequestException(ErrorsPage.ADDRESSES_NOT_VALID)
                    }
                }
            }
            if (newContactDetails.telephones.isNotEmpty()) {
                //If a telephone number is not valid throws an exception
                newContactDetails.telephones.forEach {
                    if (!isValidPhone(it.telephone)) {
                        throw BadRequestException(ErrorsPage.TELEPHONES_NOT_VALID)
                    }
                }
            }

            val savedCustomer = customerService.updateCustomerDetails(customerID, newContactDetails)
            return ResponseEntity(savedCustomer, HttpStatus.CREATED)
        } catch (e: BadRequestException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("Error saving a new contact: ${e.message}")
            return ResponseEntity(INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @DeleteMapping("/{customerID}", "/{customerID}/")
    fun deleteCustomer(
        @PathVariable customerID: Long
    ): ResponseEntity<Any> {
        try {
            if (customerID < 0) {
                return ResponseEntity.badRequest().body(mapOf("error" to CUSTOMER_ID_ERROR))
            }

            customerService.deleteCustomer(customerID)
            return ResponseEntity(DELETED_SUCCESSFULLY, HttpStatus.OK)
        } catch (e: CustomerNotFoundException) {
            logger.info("Failed to delete the customer. Customer with id = $customerID not found!")
            return ResponseEntity(mapOf("error" to e.message), HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Server internal error: ${e.message}")
            return ResponseEntity(mapOf("error" to INTERNAL_SERVER_ERROR_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}