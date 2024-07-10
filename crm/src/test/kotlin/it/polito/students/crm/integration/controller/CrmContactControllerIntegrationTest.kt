package it.polito.students.crm.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.crm.dtos.*
import it.polito.students.crm.integration.IntegrationTest
import it.polito.students.crm.utils.CategoryOptions
import it.polito.students.crm.utils.ErrorsPage.Companion.CONTACT_ID_ERROR
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CrmContactControllerIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val createContactDto = CreateContactDTO(
        name = "Name 1",
        surname = "Surname 1",
        ssnCode = "ssnCode 1",
        category = "CUSTOMER",
        comment = "Comment 1",
        emails = listOf(
            CreateEmailDTO(
                email = "john.doe@example.com",
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
    )

    private val createContactDto1 = CreateContactDTO(
        name = "Name2",
        surname = "Surname2",
        ssnCode = "ssnCode 2",
        category = "CUSTOMER",
        comment = "Comment 2",
        emails = listOf(
            CreateEmailDTO(
                email = "jane.doe@example.com",
                comment = "This is another comment",
            )
        ),
        telephones = listOf(
            CreateTelephoneDTO(
                telephone = "3219877892",
                comment = "This is another comment",
            )
        ),
        addresses = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Lombardy",
                city = "Milan",
                address = "via Verdi",
                comment = "This is another comment",
            )
        )
    )

    private val createContactDto2 = CreateContactDTO(
        name = "Name 3",
        surname = "Surname 3",
        ssnCode = "ssnCode 3",
        category = "CUSTOMER",
        comment = "Comment 3",
        emails = listOf(
            CreateEmailDTO(
                email = "test@example.com",
                comment = "This is a test comment",
            )
        ),
        telephones = listOf(
            CreateTelephoneDTO(
                telephone = "3219877893",
                comment = "This is a test comment",
            )
        ),
        addresses = listOf(
            CreateAddressDTO(
                state = "Italy",
                region = "Veneto",
                city = "Venice",
                address = "via Roma",
                comment = "This is a test comment",
            )
        )
    )

    /**
     * GET ALL CONTACTS TEST CASES
     */

    fun createManyContacts() {
        val contactDtos = listOf(createContactDto, createContactDto1, createContactDto2)

        contactDtos.forEach { contactDto ->
            mockMvc.perform(
                MockMvcRequestBuilders.post("/API/contacts/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ObjectMapper().writeValueAsString(contactDto))
            )

        }
    }

    @Test
    fun getContacts_statusOK() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0&pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].category").value(contactsDTOList[2].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_pageSizeLow() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0&pageSize=2"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
    }

    @Test
    fun getContacts_invalidPage() {
        createManyContacts()

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=-1&pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getContacts_invalidPageSize() {
        createManyContacts()

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0&pageSize=-1"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getContacts_invalidPageAndPageSize() {
        createManyContacts()

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=-1&pageSize=-1"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getContacts_noPage() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageSize=10"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].category").value(contactsDTOList[2].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_noPageSize() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].category").value(contactsDTOList[2].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_noParameters() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].category").value(contactsDTOList[2].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_bigPageSize() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0&pageSize=1000"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[0].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[0].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[0].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[0].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].comment").value(contactsDTOList[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value(contactsDTOList[2].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].surname").value(contactsDTOList[2].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].ssnCode").value(contactsDTOList[2].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].category").value(contactsDTOList[2].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].comment").value(contactsDTOList[2].comment))
    }

    @Test
    fun getContacts_filtered() {
        createManyContacts()

        val contactsDTOList: List<CreateContactDTO> = listOf(createContactDto, createContactDto1, createContactDto2)

        mockMvc.perform(MockMvcRequestBuilders.get("/API/contacts?pageNumber=0&pageSize=10&surname=Surname2"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(contactsDTOList[1].name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(contactsDTOList[1].surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ssnCode").value(contactsDTOList[1].ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].category").value(contactsDTOList[1].category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].comment").value(contactsDTOList[1].comment))
    }

    /**
     * STORE NEW CONTACT TEST CASES
     */
    @Test
    fun storeNewContact_checkContact() {
        val createContact = CreateContactDTO(
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.name").value(createContact.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.surname").value(createContact.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.ssnCode").value(createContact.ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.category").value(createContact.category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.comment").value(createContact.comment))
    }

    @Test
    fun storeNewContact_checkContactnoEmail() {
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(),
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.name").value(createContact.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.surname").value(createContact.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.ssnCode").value(createContact.ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.category").value(createContact.category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.comment").value(createContact.comment))
    }

    @Test
    fun storeNewContact_checkContactNoTelephone() {
        val createContact = CreateContactDTO(
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
            telephones = listOf(),
            addresses = listOf(
                CreateAddressDTO(
                    state = "Italy",
                    region = "Lazio",
                    city = "Rome",
                    address = "via Napoli",
                    comment = "This is a comment",
                )
            )
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.name").value(createContact.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.surname").value(createContact.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.ssnCode").value(createContact.ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.category").value(createContact.category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.comment").value(createContact.comment))
    }

    @Test
    fun storeNewContact_checkContactNoAddress() {
        val createContact = CreateContactDTO(
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
            addresses = listOf()
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val id = responseContent.substringAfter("id\":").substringBefore(",").toLong()

        //Get contact
        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${id}/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.name").value(createContact.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.surname").value(createContact.surname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.ssnCode").value(createContact.ssnCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.category").value(createContact.category))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO.comment").value(createContact.comment))
    }

    /**
     * POST API/contacts/{contactID}/{whatContact} TEST CASES
     */

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

    fun createContactAndReturnItsId(): Long {
        val createContact = CreateContactDTO(
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        return responseContent.substringAfter("id\":").substringBefore(",").toLong()
    }

    @Test
    fun postContactDetailEmail() {

        val contactId = createContactAndReturnItsId()
        val whatContact = "email"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                emails = createEmails
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value(savedEmails[0].email))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].comment").value(savedEmails[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value(savedEmails[1].email))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].comment").value(savedEmails[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].email").value(savedEmails[2].email))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].comment").value(savedEmails[2].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").isNumber())

    }

    @Test
    fun postContactDetailTelephone() {
        val contactId = createContactAndReturnItsId()
        val whatContact = "telephone"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                telephones = createPhones
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].telephone").value(savedPhones[0].telephone))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].comment").value(savedPhones[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].telephone").value(savedPhones[1].telephone))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].comment").value(savedPhones[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].telephone").value(savedPhones[2].telephone))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].comment").value(savedPhones[2].comment))
    }

    @Test
    fun postContactDetailAddress() {
        val contactId = createContactAndReturnItsId()
        val whatContact = "address"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].address").value(savedAddresses[0].address))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].comment").value(savedAddresses[0].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].address").value(savedAddresses[1].address))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].comment").value(savedAddresses[1].comment))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].address").value(savedAddresses[2].address))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].comment").value(savedAddresses[2].comment))
    }             //TODO: Perchè le informazioni sull'address vengono salvate in maiuscolo?

    @Test
    fun postContactDetailInvalidContactId() {
        val contactId = -3
        val whatContact = "address"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun postContactDetail_invalidWhatContact() {
        val contactId = createContactAndReturnItsId()
        val whatContact = "invalid whatcontact"

        val requestBody = ObjectMapper().writeValueAsString(
            CreateGenericContactDTO(
                addresses = createAddresses
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun postContactDetail_wrongParametersEmails() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

    }

    @Test
    fun postContactDetail_wrongParametersTelephone() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun postContactDetail_wrongParametersAddresses() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun postContactDetail_partiallyWrongParametersEmails() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

    }

    @Test
    fun postContactDetail_partiallyWrongParametersTelephone() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun postContactDetail_partiallyWrongParametersAddresses() {
        val contactId = createContactAndReturnItsId()
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
            MockMvcRequestBuilders.post("/API/contacts/${contactId}/${whatContact}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    /**
     * PUT API/contacts/{contactID}/{whatContact}/{id} TEST CASES
     */
    @Test
    fun replaceContactById_updateEmail() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(',').toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"id\":")
        val emailId = remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"email").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/email/${emailId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            email = CreateEmailDTO(
                                "newemail@email.com",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("newemail@email.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment").value("New comment"))
    }

    @Test
    fun replaceContactById_invalidEmail() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(',').toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"id\":")
        val emailId = remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"email").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/email/${emailId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            email = CreateEmailDTO(
                                "invalid email",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$").value("Email has to be a valid format! Example: [name@example.com]")
            )
    }

    @Test
    fun replaceContactById_updateTelephone() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(",").toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"telephoneDTOs\":")
        val telephoneId =
            remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"telephone").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/telephone/${telephoneId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            telephone = CreateTelephoneDTO(
                                "3333333333",
                                "Updated comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephone").value("3333333333"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment").value("Updated comment"))
    }

    @Test
    fun replaceContactById_invalidTelephone() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(",").toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"telephoneDTOs\":")
        val telephoneId =
            remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"telephone").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/telephone/${telephoneId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            telephone = CreateTelephoneDTO(
                                "33333",
                                "Updated comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value("Telephones has to be a valid format! Example: [+39 3321335437, 3321335437, +1 (222) 123-1234]")
            )
    }

    @Test
    fun replaceContactById_updateAddress() {
        //Create new contact
        val createContact = CreateContactDTO(
            name = "Name 1",
            surname = "Surname 1",
            ssnCode = "ssnCode 1",
            category = "CUSTOMER",
            comment = "Comment 1",
            emails = listOf(
                CreateEmailDTO(
                    email = "john.doe@example.com",
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
        )

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(",").toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"addressDTOs\":")
        val addressId = remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"state").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/address/${addressId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            address = CreateAddressDTO(
                                "Italy",
                                "Sicilia",
                                "Palermo",
                                "Via Roma 11",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("Italy"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.region").value("Sicilia"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.city").value("Palermo"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.address").value("Via Roma 11"))
    }

    @Test
    fun replaceContactById_invalidAddress() {
        //Create new contact
        val createContact = createContactDto

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andReturn()

        // Extracting ID from the response content
        val responseContent = result.response.contentAsString
        val contactId = responseContent.substringAfter("\"id\":").substringBefore(",").toLong()

        val remainingContentAfterContactId = responseContent.substringAfter("\"addressDTOs\":")
        val addressId = remainingContentAfterContactId.substringAfter("\"id\":").substringBefore(",\"state").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/${contactId}/address/${addressId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            address = CreateAddressDTO(
                                "Italy",
                                "Sicilia",
                                "Palermo",
                                "",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$")
                    .value("A valid Address has at least one address field among [state, region, city, address] is not blank!")
            )
    }

    @Test
    fun replaceContactById_negativeContactId() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/-1/address/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            address = CreateAddressDTO(
                                "Italy",
                                "Sicilia",
                                "Palermo",
                                "Via Roma 11",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.error")
                    .value("ContactId value is not valid. Please provide a positive integer!")
            )
    }

    @Test
    fun replaceContactById_negativeId() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/API/contacts/1/address/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    ObjectMapper().writeValueAsString(
                        CreateContactIdDTO(
                            address = CreateAddressDTO(
                                "Italy",
                                "Sicilia",
                                "Palermo",
                                "Via Roma 11",
                                "New comment"
                            )
                        )
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.error")
                    .value("Id value is not valid. Please provide a positive integer!")
            )
    }

    /**
     * GET /API/contacts/{contactID} TEST CASES
     */

    @Test
    fun getContactById_postContactAndGetContact() {
        //Create new contact
        val createContact = createContactDto

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // Convert the response into DTO
        val responsePostContactDto: ContactWithAssociatedDataDTO = createDTOFromString(result.response.contentAsString)

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${responsePostContactDto.contactDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // Convert the response into DTO
        val responseGetContactDto: ContactWithAssociatedDataDTO =
            createDTOFromString(resultGet.response.contentAsString)

        // Asserts
        Assertions.assertEquals(responsePostContactDto.contactDTO.id, responseGetContactDto.contactDTO.id)
        Assertions.assertEquals(responsePostContactDto.contactDTO.name, responseGetContactDto.contactDTO.name)
        Assertions.assertEquals(responsePostContactDto.contactDTO.surname, responseGetContactDto.contactDTO.surname)
        Assertions.assertEquals(
            responsePostContactDto.contactDTO.category.name,
            responseGetContactDto.contactDTO.category.name
        )
        Assertions.assertEquals(responsePostContactDto.contactDTO.ssnCode, responseGetContactDto.contactDTO.ssnCode)

        for (email in responseGetContactDto.emailDTOs) {
            val emailPostDtoList = responsePostContactDto.emailDTOs.filter { it.id == email.id }

            Assertions.assertEquals(emailPostDtoList.size, 1)

            val emailPostDto = emailPostDtoList.first()

            Assertions.assertEquals(emailPostDto.email, email.email)
            Assertions.assertEquals(emailPostDto.comment, email.comment)
        }

        for (telephone in responseGetContactDto.telephoneDTOs) {
            val postDtoList = responsePostContactDto.telephoneDTOs.filter { it.id == telephone.id }

            Assertions.assertEquals(postDtoList.size, 1)

            val postDto = postDtoList.first()

            Assertions.assertEquals(postDto.telephone, telephone.telephone)
            Assertions.assertEquals(postDto.comment, telephone.comment)
        }

        for (address in responseGetContactDto.addressDTOs) {
            val postDtoList = responsePostContactDto.addressDTOs.filter { it.id == address.id }

            Assertions.assertEquals(postDtoList.size, 1)

            val postDto = postDtoList.first()

            Assertions.assertEquals(postDto.address, address.address)
            Assertions.assertEquals(postDto.comment, address.comment)
        }
    }

    @Test
    fun getContactById_postContactAndGetContactAndUpdateContact() {
        //Create new contact
        val createContact = createContactDto

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        val resultPost = mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").exists())
            .andReturn()

        // Convert the response into DTO
        val responsePostContactDto: ContactWithAssociatedDataDTO =
            createDTOFromString(resultPost.response.contentAsString)

        val resultGet = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${responsePostContactDto.contactDTO.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").exists())
            .andReturn()

        // Convert the response into DTO
        val responseGetContactDto: ContactWithAssociatedDataDTO =
            createDTOFromString(resultGet.response.contentAsString)

        // Asserts
        Assertions.assertEquals(responsePostContactDto.contactDTO.id, responseGetContactDto.contactDTO.id)
        Assertions.assertEquals(responsePostContactDto.contactDTO.name, responseGetContactDto.contactDTO.name)
        Assertions.assertEquals(responsePostContactDto.contactDTO.surname, responseGetContactDto.contactDTO.surname)
        Assertions.assertEquals(
            responsePostContactDto.contactDTO.category.name,
            responseGetContactDto.contactDTO.category.name
        )
        Assertions.assertEquals(responsePostContactDto.contactDTO.ssnCode, responseGetContactDto.contactDTO.ssnCode)

        for (email in responseGetContactDto.emailDTOs) {
            val emailPostDtoList = responsePostContactDto.emailDTOs.filter { it.id == email.id }

            Assertions.assertEquals(emailPostDtoList.size, 1)

            val emailPostDto = emailPostDtoList.first()

            Assertions.assertEquals(emailPostDto.email, email.email)
            Assertions.assertEquals(emailPostDto.comment, email.comment)
        }

        for (telephone in responseGetContactDto.telephoneDTOs) {
            val postDtoList = responsePostContactDto.telephoneDTOs.filter { it.id == telephone.id }

            Assertions.assertEquals(postDtoList.size, 1)

            val postDto = postDtoList.first()

            Assertions.assertEquals(postDto.telephone, telephone.telephone)
            Assertions.assertEquals(postDto.comment, telephone.comment)
        }

        for (address in responseGetContactDto.addressDTOs) {
            val postDtoList = responsePostContactDto.addressDTOs.filter { it.id == address.id }

            Assertions.assertEquals(postDtoList.size, 1)

            val postDto = postDtoList.first()

            Assertions.assertEquals(postDto.address, address.address)
            Assertions.assertEquals(postDto.comment, address.comment)
        }

        // Update Contact
        val contentToUpdate = responsePostContactDto.contactDTO
        contentToUpdate.category = CategoryOptions.PROFESSIONAL
        contentToUpdate.name = "Sarah"
        contentToUpdate.surname = "Doe"
        contentToUpdate.comment = "This is a new comment updated!"

        val updateContactDto = UpdateContactDTO(
            contentToUpdate.id,
            contentToUpdate.name,
            contentToUpdate.surname,
            contentToUpdate.ssnCode,
            contentToUpdate.category.name,
            contentToUpdate.comment
        )

        val requestBodyUpdate = ObjectMapper().writeValueAsString(updateContactDto)
        val responseUpdate = mockMvc.perform(
            MockMvcRequestBuilders.patch("/API/contacts/${updateContactDto.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyUpdate)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // Convert the response into DTO
        val responseUpdateContactDto: ContactDTO = createContactDTOFromString(responseUpdate.response.contentAsString)

        val resultGetUpdate = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${responseUpdateContactDto.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.contactDTO").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.telephoneDTOs").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.addressDTOs").exists())
            .andReturn()

        // Convert the response into DTO
        val responseGetUpdateContactDto: ContactWithAssociatedDataDTO =
            createDTOFromString(resultGetUpdate.response.contentAsString)

        // Asserts
        Assertions.assertEquals(responseUpdateContactDto.id, responseGetUpdateContactDto.contactDTO.id)
        Assertions.assertEquals(responseUpdateContactDto.name, responseGetUpdateContactDto.contactDTO.name)
        Assertions.assertEquals(responseUpdateContactDto.surname, responseGetUpdateContactDto.contactDTO.surname)
        Assertions.assertEquals(
            responseUpdateContactDto.category.name,
            responseGetUpdateContactDto.contactDTO.category.name
        )
        Assertions.assertEquals(responseUpdateContactDto.ssnCode, responseGetUpdateContactDto.contactDTO.ssnCode)
    }

    @Test
    fun getContactById_idNotValid() {
        //Create new contact
        val createContact = createContactDto

        val requestBody = ObjectMapper().writeValueAsString(createContact)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/contacts/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

        val invalidId = "abcd"

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/${invalidId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getContactById_negativeId() {
        val expectedMessageError = CONTACT_ID_ERROR

        mockMvc.perform(
            MockMvcRequestBuilders.get("/API/contacts/-1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(expectedMessageError))
    }
}