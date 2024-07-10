package it.polito.students.crm.unit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import it.polito.students.crm.dtos.*
import it.polito.students.crm.entities.Address
import it.polito.students.crm.entities.Contact
import it.polito.students.crm.entities.Email
import it.polito.students.crm.entities.Telephone
import it.polito.students.crm.exception_handlers.ContactNotFoundException
import it.polito.students.crm.services.*
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage.Companion.ERROR_MESSAGE_CATEGORY
import it.polito.students.crm.utils.ErrorsPage.Companion.NAME_SURNAME_ERROR
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
class CrmContactsControllerUnitTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var messageService: MessageService

    @MockkBean
    lateinit var jobOfferService: JobOfferService

    @MockkBean
    lateinit var customerService: CustomerService

    @MockkBean
    lateinit var professionalService: ProfessionalService

    @MockkBean
    lateinit var contactService: ContactServiceImpl

    @MockkBean
    lateinit var emailService: EmailService

    @MockkBean
    lateinit var addressService: AddressService

    @MockkBean
    lateinit var telephoneService: TelephoneService

    private val contactsDTOList: List<ContactDTO> = listOf(
        ContactDTO(
            id = 1,
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = CategoryOptions.CUSTOMER,
            comment = "Comment 1"
        ),
        ContactDTO(
            id = 2,
            name = "Name 2",
            surname = "Surname 2",
            ssnCode = "ssnCode 2",
            category = CategoryOptions.PROFESSIONAL,
            comment = "Comment 2"
        ),
        ContactDTO(
            id = 3,
            name = "Name 3",
            surname = "Surname 3",
            ssnCode = "ssnCode 3",
            category = CategoryOptions.UNKNOWN,
            comment = "Comment 3"
        )
    )

    private val createContactsList: List<CreateContactDTO> = listOf(
        CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "name@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "3219877891",
                    comment = "This is a comment",
                )
            ),
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Lazio",
                    city = "Rome",
                    address = "via Napoli",
                    comment = "This is a comment",
                )
            )
        ),
        CreateContactDTO(
            name = "Name 2",
            surname = "Surname 2",
            ssnCode = "ssnCode 2",
            category = "PROFESSIONAL",
            comment = "Comment 2",
            emails = listOf(
                CreateEmailDTO(
                    email = "name1@example.com",
                    comment = "This is a comment",
                )
            ),
            telephones = null,
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Piemonte",
                    city = "Turin",
                    address = "via Roma",
                    comment = "This is a comment"
                )
            )
        ),
        CreateContactDTO(
            name = "Name 3",
            surname = "Surname 3",
            ssnCode = "ssnCode 3",
            category = "UNKNOWN",
            comment = "Comment 3",
            emails = null,
            addresses = null,
            telephones = listOf(
                CreateTelephoneDTO(
                    telephone = "1237899871",
                    comment = "This is a comment"
                )
            )
        ),
        CreateContactDTO(
            name = "Name 4",
            surname = "Surname 4",
            ssnCode = "ssnCode 4",
            category = "CUSTOMER",
            comment = "Comment 4",
            addresses = null,
            telephones = null,
            emails = null
        ),
        CreateContactDTO(
            name = "Name 5",
            surname = "Surname 5",
            ssnCode = "ssnCode 5",
            category = "CUSTOMER",
            comment = "Comment 5",
            emails = listOf(
                CreateEmailDTO(
                    email = "name2@example.com",
                    comment = "This is a comment",
                )
            ),
            addresses = null,
            telephones = null
        ),
        CreateContactDTO(
            name = "Name 6",
            surname = "Surname 6",
            ssnCode = "ssnCode 6",
            category = "CUSTOMER",
            comment = "Comment 6",
            emails = null,
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Piemonte",
                    city = "Turin",
                    address = "via Roma",
                    comment = "This is a comment"
                )
            ),
            telephones = null
        ),
        CreateContactDTO(
            name = "",
            surname = "",
            ssnCode = "ssnCode 7",
            category = "CUSTOMER",
            comment = "Comment 7",
            emails = null,
            telephones = null,
            addresses = null
        ),
    )

    /**
     * It converts a CreateContactDto to a ContactDto
     */
    private fun convertCreateContactDtoToContactDto(createContactDTO: CreateContactDTO): ContactDTO {
        val category: CategoryOptions? = try {
            createContactDTO.category?.let { CategoryOptions.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            CategoryOptions.CUSTOMER
        }

        val ssnCode = createContactDTO.ssnCode ?: ""
        val comment = createContactDTO.comment ?: ""

        return ContactDTO(
            id = 1,
            name = createContactDTO.name,
            surname = createContactDTO.surname,
            ssnCode = ssnCode,
            category = category!!,
            comment = comment
        )

    }

    /**
     * GET CONTACTS TEST CASES
     */

    @Test
    fun getContacts_statusOK() {
        val contacts: PageImpl<ContactDTO> = PageImpl(contactsDTOList)

        every { contactService.getAllContacts(0, 10, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageNumber=0&pageSize=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(jsonPath("$.content[2].id").value(contactsDTOList[2].id))
            .andExpect(jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(jsonPath("$.content[2].category").value(contactsDTOList[2].category.name))
            .andExpect(jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_pageSizeLow() {
        val contacts: PageImpl<ContactDTO> = PageImpl(listOf(contactsDTOList[0], contactsDTOList[1]))

        every { contactService.getAllContacts(0, 2, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageNumber=0&pageSize=2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
    }

    @Test
    fun getContacts_invalidPage() {
        mockMvc.perform(get("/API/contacts?pageNumber=-1&pageSize=10"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The page number and the page size cannot be negative!"))
    }

    @Test
    fun getContacts_invalidPageSize() {
        mockMvc.perform(get("/API/contacts?pageNumber=0&pageSize=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The page number and the page size cannot be negative!"))
    }

    @Test
    fun getContacts_invalidPageAndPageSize() {
        mockMvc.perform(get("/API/contacts?pageNumber=-1&pageSize=-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("The page number and the page size cannot be negative!"))
    }

    @Test
    fun getContacts_noPage() {
        val contacts: PageImpl<ContactDTO> = PageImpl(contactsDTOList)

        every { contactService.getAllContacts(0, 10, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageSize=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(jsonPath("$.content[2].id").value(contactsDTOList[2].id))
            .andExpect(jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(jsonPath("$.content[2].category").value(contactsDTOList[2].category.name))
            .andExpect(jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_noPageSize() {
        val contacts: PageImpl<ContactDTO> = PageImpl(contactsDTOList)

        every { contactService.getAllContacts(0, 10, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageNumber=0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(jsonPath("$.content[2].id").value(contactsDTOList[2].id))
            .andExpect(jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(jsonPath("$.content[2].category").value(contactsDTOList[2].category.name))
            .andExpect(jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_noParameters() {
        val contacts: PageImpl<ContactDTO> = PageImpl(contactsDTOList)

        every { contactService.getAllContacts(0, 10, any()) } returns contacts

        mockMvc.perform(get("/API/contacts"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(jsonPath("$.content[2].id").value(contactsDTOList[2].id))
            .andExpect(jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(jsonPath("$.content[2].category").value(contactsDTOList[2].category.name))
            .andExpect(jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_bigPageSize() {
        val contacts: PageImpl<ContactDTO> = PageImpl(contactsDTOList)

        every { contactService.getAllContacts(0, 1000, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageNumber=0&pageSize=1000"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(jsonPath("$.content[1].id").value(contactsDTOList[1].id))
            .andExpect(jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(jsonPath("$.content[1].category").value(contactsDTOList[1].category.name))
            .andExpect(jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(jsonPath("$.content[2].id").value(contactsDTOList[2].id))
            .andExpect(jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(jsonPath("$.content[2].category").value(contactsDTOList[2].category.name))
            .andExpect(jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_filtered() {
        val contacts: PageImpl<ContactDTO> = PageImpl(listOf(contactsDTOList[0]))

        every { contactService.getAllContacts(0, 10, any()) } returns contacts

        mockMvc.perform(get("/API/contacts?pageNumber=0&pageSize=10&name=Name%201"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(contactsDTOList[0].id))
            .andExpect(jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(jsonPath("$.content[0].category").value(contactsDTOList[0].category.name))
            .andExpect(jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
    }

    /**
     * STORE CONTACT TEST CASES
     */
    @Test
    fun storeNewContact_success() {
        val createContact: CreateContactDTO = createContactsList[0]
        val categoryOption = CategoryOptions.valueOf(createContactsList[0].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(createContact, categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_WithoutEmail() {
        val createContact: CreateContactDTO = createContactsList[2]
        val categoryOption = CategoryOptions.valueOf(createContactsList[2].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutAddress() {
        val createContact: CreateContactDTO = createContactsList[4]
        val categoryOption = CategoryOptions.valueOf(createContactsList[4].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutTelephone() {
        val createContact: CreateContactDTO = createContactsList[5]
        val categoryOption = CategoryOptions.valueOf(createContactsList[5].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutEmailAndTelephone() {
        val createContact: CreateContactDTO = createContactsList[5]
        val categoryOption = CategoryOptions.valueOf(createContactsList[5].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutEmailAndAddress() {
        val createContact: CreateContactDTO = createContactsList[2]
        val categoryOption = CategoryOptions.valueOf(createContactsList[2].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutAddressAndTelephone() {
        val createContact: CreateContactDTO = createContactsList[4]
        val categoryOption = CategoryOptions.valueOf(createContactsList[4].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_successWithoutEmailAndAddressAndTelephone() {
        val createContact: CreateContactDTO = createContactsList[3]
        val categoryOption = CategoryOptions.valueOf(createContactsList[3].category!!)
        val contactDto = convertCreateContactDtoToContactDto(createContact)

        every { contactService.storeContact(any(), categoryOption) } returns ContactWithAssociatedDataDTO(
            contactDto,
            listOf(),
            listOf(),
            listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contactDto.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contactDto.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contactDto.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contactDto.ssnCode))
            .andExpect(jsonPath("$.contactDTO.category").value(contactDto.category.name))
            .andExpect(jsonPath("$.contactDTO.comment").value(contactDto.comment))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_nameAndSurnameBlank() {
        val createContact: CreateContactDTO = createContactsList[6]
        val categoryOption = CategoryOptions.valueOf(createContactsList[6].category!!)
        val resultErrorMessage = NAME_SURNAME_ERROR
        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 0) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_BadFormattedCategory() {
        val createContact: CreateContactDTO = createContactsList[0]
        createContact.category = "10"
        val errorMessage = ERROR_MESSAGE_CATEGORY

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 0) { contactService.storeContact(createContact, any()) }
    }

    @Test
    fun storeNewContact_NotValidCategory() {
        val createContact: CreateContactDTO = createContactsList[0]
        createContact.category = "NOT VALID"
        val resultErrorMessage = ERROR_MESSAGE_CATEGORY

        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(resultErrorMessage))

        verify(exactly = 0) { contactService.storeContact(createContact, any()) }
    }

    @Test
    fun storeNewContact_raiseGeneralError() {
        val createContact: CreateContactDTO = createContactsList[0]
        val categoryOption = CategoryOptions.valueOf(createContactsList[0].category!!)
        val errorMessage = "General Error!"

        every { contactService.storeContact(createContact, categoryOption) } throws Exception(errorMessage)
        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    @Test
    fun storeNewContact_raiseNoSuchElementException() {
        val createContact: CreateContactDTO = createContactsList[0]
        val categoryOption = CategoryOptions.valueOf(createContactsList[0].category!!)
        val errorMessage = "No Such Element!"

        every { contactService.storeContact(createContact, categoryOption) } throws NoSuchElementException(errorMessage)
        val requestBody = ObjectMapper().writeValueAsString(createContact)

        mockMvc.perform(
            post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value(errorMessage))

        verify(exactly = 1) { contactService.storeContact(createContact, categoryOption) }
    }

    /**
     * PATCH /API/contacts/{contactID} TEST CASES
     */
    @Test
    fun patchContact_goodCase() {
        val contactId = 1

        val contactNew = Contact().apply {
            id = 1
            name = "NameNew"
            surname = "SurnameNew"
            ssnCode = "ssnCodeNew"
            category = CategoryOptions.CUSTOMER
            comment = "Comment new 1"
        }

        every { contactService.updateContact(any(), any()) } returns contactNew.toDTO()

        val requestBody = ObjectMapper().writeValueAsString(
            UpdateContactDTO(
                id = 1,
                name = "NameNew",
                surname = "SurnameNew",
                ssnCode = "ssnCodeNew",
                category = CategoryOptions.CUSTOMER.toString(),
                comment = "Comment new 1"
            )
        )

        mockMvc.perform(
            patch("/API/contacts/${contactId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value(contactNew.name))
            .andExpect(jsonPath("$.surname").value(contactNew.surname))
            .andExpect(jsonPath("$.ssnCode").value(contactNew.ssnCode))
            .andExpect(jsonPath("$.category").value(contactNew.category.toString()))
            .andExpect(jsonPath("$.comment").value(contactNew.comment))
    }

    @Test
    fun patchContact_validationError() {
        val contactId = 1

        val contactNew = Contact().apply {
            id = 1
            name = "Name New 1"
            surname = "SurnameNew"
            ssnCode = "ssnCodeNew"
            category = CategoryOptions.CUSTOMER
            comment = "Comment new 1"
        }

        every { contactService.updateContact(any(), any()) } returns contactNew.toDTO()

        val requestBody = ObjectMapper().writeValueAsString(
            UpdateContactDTO(
                id = 1,
                name = "Name New 1",
                surname = "SurnameNew",
                ssnCode = "ssnCodeNew",
                category = CategoryOptions.CUSTOMER.toString(),
                comment = "Comment new 1"
            )
        )

        mockMvc.perform(
            patch("/API/contacts/${contactId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.name").value("The name should contain only alphabetic characters"))
    }

    @Test
    fun patchContact_contactNotFound() {
        val contactId = 1

        Contact().apply {
            id = 1
            name = "NameNew"
            surname = "SurnameNew"
            ssnCode = "ssnCodeNew"
            category = CategoryOptions.CUSTOMER
            comment = "Comment new 1"
        }

        every { contactService.updateContact(any(), any()) } throws ContactNotFoundException("Contact not found")

        val requestBody = ObjectMapper().writeValueAsString(
            UpdateContactDTO(
                id = 1,
                name = "NameNew",
                surname = "SurnameNew",
                ssnCode = "ssnCodeNew",
                category = CategoryOptions.CUSTOMER.toString(),
                comment = "Comment new 1"
            )
        )

        mockMvc.perform(
            patch("/API/contacts/${contactId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$").value("Contact not found"))
    }

    private var createEmails = listOf(
        CreateEmailDTO("email1@email.com", "A comment 1"),
        CreateEmailDTO("email2@email.com", "A comment 2"),
        CreateEmailDTO("email3@email.com", "A comment 3"),
    )

    private var savedEmails = listOf(
        EmailDTO(1, "email1@email.com", "A comment 1"),
        EmailDTO(2, "email2@email.com", "A comment 2"),
        EmailDTO(3, "email3@email.com", "A comment 3"),
    )

    private var createPhones = listOf(
        CreateTelephoneDTO("1234567890", "A comment 1"),
        CreateTelephoneDTO("9876543218", "A comment 2"),
        CreateTelephoneDTO("4567891232", "A comment 3"),
    )

    private var savedPhones = listOf(
        TelephoneDTO(1, "1234567890", "A comment 1"),
        TelephoneDTO(2, "9876543218", "A comment 2"),
        TelephoneDTO(3, "4567891232", "A comment 3"),
    )

    private var createAddresses = listOf(
        CreateAddressDTO("State 1", "Region 1", "City 1", "Address 1", "Comment 1"),
        CreateAddressDTO("State 2", "Region 2", "City 2", "Address 2", "Comment 2"),
        CreateAddressDTO("State 3", "Region 3", "City 3", "Address 3", "Comment 3"),
    )

    private var savedAddresses = listOf(
        AddressDTO(1, "State 1", "Region 1", "City 1", "Address 1", "Comment 1"),
        AddressDTO(2, "State 2", "Region 2", "City 2", "Address 2", "Comment 2"),
        AddressDTO(3, "State 3", "Region 3", "City 3", "Address 3", "Comment 3"),
    )

    @Test
    fun postContactDetail_correctParametersEmails() {
        val contactId = 1
        val whatContact = "email"

        every { emailService.storeEmailList(1, any()) } returns savedEmails

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                emails = createEmails
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$[0].id").value(savedEmails[0].id))
            .andExpect(jsonPath("$[0].email").value(savedEmails[0].email))
            .andExpect(jsonPath("$[0].comment").value(savedEmails[0].comment))
            .andExpect(jsonPath("$[1].id").value(savedEmails[1].id))
            .andExpect(jsonPath("$[1].email").value(savedEmails[1].email))
            .andExpect(jsonPath("$[1].comment").value(savedEmails[1].comment))
            .andExpect(jsonPath("$[2].id").value(savedEmails[2].id))
            .andExpect(jsonPath("$[2].email").value(savedEmails[2].email))
            .andExpect(jsonPath("$[2].comment").value(savedEmails[2].comment))
    }

    @Test
    fun postContactDetail_correctParametersTelephone() {
        val contactId = 1
        val whatContact = "telephone"

        every { telephoneService.storeTelephoneList(1, any()) } returns savedPhones

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                telephones = createPhones
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$[0].id").value(savedPhones[0].id))
            .andExpect(jsonPath("$[0].telephone").value(savedPhones[0].telephone))
            .andExpect(jsonPath("$[0].comment").value(savedPhones[0].comment))
            .andExpect(jsonPath("$[1].id").value(savedPhones[1].id))
            .andExpect(jsonPath("$[1].telephone").value(savedPhones[1].telephone))
            .andExpect(jsonPath("$[1].comment").value(savedPhones[1].comment))
            .andExpect(jsonPath("$[2].id").value(savedPhones[2].id))
            .andExpect(jsonPath("$[2].telephone").value(savedPhones[2].telephone))
            .andExpect(jsonPath("$[2].comment").value(savedPhones[2].comment))
    }

    @Test
    fun postContactDetail_correctParametersAddresses() {
        val contactId = 1
        val whatContact = "address"

        every { addressService.storeAddressList(1, any()) } returns savedAddresses

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$[0].id").value(savedAddresses[0].id))
            .andExpect(jsonPath("$[0].address").value(savedAddresses[0].address))
            .andExpect(jsonPath("$[0].comment").value(savedAddresses[0].comment))
            .andExpect(jsonPath("$[1].id").value(savedAddresses[1].id))
            .andExpect(jsonPath("$[1].address").value(savedAddresses[1].address))
            .andExpect(jsonPath("$[1].comment").value(savedAddresses[1].comment))
            .andExpect(jsonPath("$[2].id").value(savedAddresses[2].id))
            .andExpect(jsonPath("$[2].address").value(savedAddresses[2].address))
            .andExpect(jsonPath("$[2].comment").value(savedAddresses[2].comment))
    }

    @Test
    fun postContactDetail_invalidContactId() {
        val contactId = -3
        val whatContact = "address"

        every { addressService.storeAddressList(1, any()) } returns savedAddresses

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("ContactId value is not valid. Please provide a positive integer!"))
    }

    @Test
    fun postContactDetail_invalidWhatContact() {
        val contactId = 1
        val whatContact = "invalid whatcontact"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Illegal contact type!"))
    }

    @Test
    fun postContactDetail_wrongParametersEmails() {
        val contactId = 1
        val whatContact = "email"

        val wrongEmailList = listOf(
            CreateEmailDTO("wrong email", "A comment 1"),
            CreateEmailDTO("not acc3pt4bl3", "A comment 2"),
            CreateEmailDTO("@#[%£&&", "A comment 3"),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                emails = wrongEmailList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Email has to be a valid format! Example: [name@example.com]"))

    }

    @Test
    fun postContactDetail_wrongParametersTelephone() {
        val contactId = 1
        val whatContact = "telephone"

        val wrongTelephoneList = listOf(
            CreateTelephoneDTO("wrong telephone", "A comment 1"),
            CreateTelephoneDTO("not acc3pt4bl3", "A comment 2"),
            CreateTelephoneDTO("@#[%£&&", "A comment 3"),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                telephones = wrongTelephoneList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Telephones has to be a valid format! Example: [+39 3321335437, 3321335437, +1 (222) 123-1234]"))
    }

    @Test
    fun postContactDetail_wrongParametersAddresses() {
        val contactId = 1
        val whatContact = "address"

        val wrongAddressList = listOf(
            CreateAddressDTO("", "", "", "", ""),
            CreateAddressDTO("", "", "", "", ""),
            CreateAddressDTO("", "", "", "", ""),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = wrongAddressList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("A valid Address has at least one address field among [state, region, city, address] is not blank!"))
    }

    @Test
    fun postContactDetail_partiallyWrongParametersEmails() {
        val contactId = 1
        val whatContact = "email"

        val wrongEmailList = listOf(
            CreateEmailDTO("correct.email@gmail.com", "A comment 1"),
            CreateEmailDTO("not acc3pt4bl3", "A comment 2"),
            CreateEmailDTO("@#[%£&&", "A comment 3"),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                emails = wrongEmailList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Email has to be a valid format! Example: [name@example.com]"))

    }

    @Test
    fun postContactDetail_partiallyWrongParametersTelephone() {
        val contactId = 1
        val whatContact = "telephone"

        val wrongTelephoneList = listOf(
            CreateTelephoneDTO("3313672946", "A comment 1"),
            CreateTelephoneDTO("not acc3pt4bl3", "A comment 2"),
            CreateTelephoneDTO("@#[%£&&", "A comment 3"),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                telephones = wrongTelephoneList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Telephones has to be a valid format! Example: [+39 3321335437, 3321335437, +1 (222) 123-1234]"))
    }

    @Test
    fun postContactDetail_partiallyWrongParametersAddresses() {
        val contactId = 1
        val whatContact = "address"

        val wrongAddressList = listOf(
            CreateAddressDTO("", "", "", "", ""),
            CreateAddressDTO("State 1", "Region 1", "City 1", "Address 1", "Comment 1"),
            CreateAddressDTO("State 1", "Region 1", "City 1", "Address 1", "Comment 1"),
        )

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = wrongAddressList
            )
        )

        mockMvc.perform(
            post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("A valid Address has at least one address field among [state, region, city, address] is not blank!"))
    }

    //put contact detail test cases

    @Test
    fun putContactDetail_correctParameterEmail() {
        val contactId = 1
        val whatContact = "email"
        val id = 1

        every { emailService.modifyEmail(contactId.toLong(), any(), id.toLong()) } returns savedEmails[0]

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                email = createEmails[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedEmails[0].id))
            .andExpect(jsonPath("$.email").value(savedEmails[0].email))
            .andExpect(jsonPath("$.comment").value(savedEmails[0].comment))
    }

    @Test
    fun putContactDetail_correctParameterTelephone() {
        val contactId = 1
        val whatContact = "telephone"
        val id = 1

        every { telephoneService.modifyTelephone(contactId.toLong(), any(), id.toLong()) } returns savedPhones[0]

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                telephone = createPhones[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedPhones[0].id))
            .andExpect(jsonPath("$.telephone").value(savedPhones[0].telephone))
            .andExpect(jsonPath("$.comment").value(savedPhones[0].comment))
    }

    @Test
    fun putContactDetail_correctParameterAddress() {
        val contactId = 1
        val whatContact = "address"
        val id = 1

        every { addressService.modifyAddress(contactId.toLong(), any(), id.toLong()) } returns savedAddresses[0]

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                address = createAddresses[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedAddresses[0].id))
            .andExpect(jsonPath("$.address").value(savedAddresses[0].address))
            .andExpect(jsonPath("$.comment").value(savedAddresses[0].comment))
    }

    @Test
    fun putContactDetail_wrongContactId() {
        val contactId = -3
        val whatContact = "email"
        val id = 1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                email = createEmails[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("ContactId value is not valid. Please provide a positive integer!"))

    }

    @Test
    fun putContactDetail_wrongWhatContact() {
        val contactId = 1
        val whatContact = "wrong what contact"
        val id = 1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                email = createEmails[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Illegal contact type!"))

    }

    @Test
    fun putContactDetail_wrongId() {
        val contactId = 1
        val whatContact = "email"
        val id = -1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                email = createEmails[0]
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Id value is not valid. Please provide a positive integer!"))

    }

    @Test
    fun putContactDetail_wrongParameterEmail() {
        val contactId = 1
        val whatContact = "email"
        val id = 1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                email = CreateEmailDTO("not corr3ct 3ma1l", "no comment")
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Email has to be a valid format! Example: [name@example.com]"))

    }

    @Test
    fun putContactDetail_wrongParameterTelephone() {
        val contactId = 1
        val whatContact = "telephone"
        val id = 1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                telephone = CreateTelephoneDTO("not a correct number", "no comment")
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("Telephones has to be a valid format! Example: [+39 3321335437, 3321335437, +1 (222) 123-1234]"))
    }

    @Test
    fun putContactDetail_wrongParameterAddress() {
        val contactId = 1
        val whatContact = "address"
        val id = 1

        val requestBody = ObjectMapper().writeValueAsString(
            CreateContactIdDTO(
                address = CreateAddressDTO("", "", "", "", "")
            )
        )

        mockMvc.perform(
            put("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("A valid Address has at least one address field among [state, region, city, address] is not blank!"))
    }

    /**
     * GET /API/contacts/{contactID}
     */
    @Test
    fun getContactId_goodCase() {
        val contactId: Long = 1

        val contacts = Contact().apply {
            id = 1
            name = "Name 1"
            surname = "Surname 1"
            ssnCode = "ssnCode 1"
            category = CategoryOptions.CUSTOMER
            comment = "Comment 1"
            emails = mutableSetOf(
                Email().apply {
                    id = 1
                    email = "ex1@example.com"
                    comment = "comment 1"
                },
                Email().apply {
                    id = 2
                    email = "ex2@example.com"
                    comment = "comment 2"
                }
            )
            telephones = mutableSetOf(
                Telephone().apply {
                    id = 1
                    telephone = "0245678936"
                    comment = "comment 1"
                }
            )
            addresses = mutableSetOf(
                Address().apply {
                    id = 1
                    state = "ITALY"
                    region = "PIEMONTE"
                    city = "TORINO"
                    address = "CORSO DUCA"
                    comment = "comment 1"
                }
            )

        }

        every { contactService.getContact(contactId) } returns contacts

        mockMvc.perform(get("/API/contacts/${contactId}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contactDTO.id").value(contacts.id))
            .andExpect(jsonPath("$.contactDTO.name").value(contacts.name))
            .andExpect(jsonPath("$.contactDTO.surname").value(contacts.surname))
            .andExpect(jsonPath("$.contactDTO.ssnCode").value(contacts.ssnCode))
            .andExpect(jsonPath("$.contactDTO.comment").value(contacts.comment))
            .andExpect(jsonPath("$.emailDTOs.length()").value(contacts.emails.size))
            .andExpect(jsonPath("$.telephoneDTOs.length()").value(contacts.telephones.size))
            .andExpect(jsonPath("$.addressDTOs.length()").value(contacts.addresses.size))

    }

    @Test
    fun getContactId_IdNegative() {
        val contactId: Long = -1

        val contact = Contact().apply {
            id = 1
            name = "Contact Name"
            surname = "Contact Surname"
            ssnCode = "SSN Code"
            category = CategoryOptions.UNKNOWN
            comment = "Comment"
        }

        every { contactService.getContact(contactId) } returns contact

        mockMvc.perform(get("/API/contacts/${contactId}"))
            .andExpect(status().isBadRequest)

    }

    @Test
    fun getContactID_IdNotFound() {
        val contactId: Long = 20

        val contact = Contact().apply {
            id = 1
            name = "Contact Name"
            surname = "Contact Surname"
            ssnCode = "SSN Code"
            category = CategoryOptions.UNKNOWN
            comment = "Comment"
        }

        every { contactService.getContact(contactId) } throws ContactNotFoundException("The contact with id equal to $contactId was not found!")

        mockMvc.perform(
            get("/API/contacts/${contactId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$").value("The contact with id equal to $contactId was not found!"))
    }

    /**
     *  DELETE /API/contacts/{contactId}/{whatContact}/{id}
     */
    @Test
    fun deleteContactID_email_GoodCase() {
        val contactId: Long = 1
        val whatContact = "email"
        val id: Long = 1

        every { emailService.deleteContactEmail(contactId, id) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

    }

    @Test
    fun deleteContactID_telephone_GoodCase() {
        val contactId: Long = 1
        val whatContact = "telephone"
        val id: Long = 1

        every { telephoneService.deleteContactTelephone(contactId, id) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun deleteContactID_address_GoodCase() {
        val contactId: Long = 1
        val whatContact = "address"
        val id: Long = 1

        every { addressService.deleteContactAddress(contactId, id) } answers { callOriginal() }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun deleteContactID_idNegative() {
        val contactId: Long = 1
        val whatContact = "email"
        val id: Long = -1


        every { emailService.deleteContactEmail(contactId, id) }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun deleteContactID_contactIdNegative() {
        val contactId: Long = -1
        val whatContact = "email"
        val id: Long = 1


        every { emailService.deleteContactEmail(contactId, id) }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun deleteContactID_whatContactNotValid() {
        val contactId: Long = 1
        val whatContact = "notValid"
        val id: Long = 1



        every { emailService.deleteContactEmail(contactId, id) }

        mockMvc.perform(
            delete("/API/contacts/${contactId}/${whatContact}/${id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

}