package it.polito.students.document_store

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.services.DocumentServiceImpl
import it.polito.students.document_store.services.MetadataServiceImpl
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime


@WebMvcTest
class DocumentControllerUnitTest(@Autowired val mockMvc: MockMvc) {

    val mapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    @MockkBean
    lateinit var documentService: DocumentServiceImpl

    @MockkBean
    lateinit var metadataService: MetadataServiceImpl

    //getAllDocuments test cases
    val metadataDTOList: List<Map<String, Any?>> = listOf(
        mapOf(
            "id" to 1,
            "name" to "metadata1",
            "size" to 1024L,
            "contentType" to "application/pdf",
            "creationTimestamp" to LocalDateTime.now(),
            "modifiedTimestamp" to LocalDateTime.now(),
            "document" to DocumentDTO(1, "content1".toByteArray())
        ),
        mapOf(
            "id" to 2,
            "name" to "metadata2",
            "size" to 2048L,
            "contentType" to "text/plain",
            "creationTimestamp" to LocalDateTime.now(),
            "modifiedTimestamp" to LocalDateTime.now(),
            "document" to DocumentDTO(2, "content2".toByteArray())
        ),
        mapOf(
            "id" to 3,
            "name" to "metadata3",
            "size" to 4096L,
            "contentType" to "image/jpeg",
            "creationTimestamp" to LocalDateTime.now(),
            "modifiedTimestamp" to LocalDateTime.now(),
            "document" to DocumentDTO(3, "content3".toByteArray())
        )
    )

    @Test
    fun getAllDocuments_statusOK() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?page=0&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(metadataDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches
            .andExpect(jsonPath("$.content[2].id").value(metadataDTOList[2]["id"])) // Check if the third metadata ID matches
            .andExpect(jsonPath("$.content[2].name").value(metadataDTOList[2]["name"])) // Check if the third metadata name matches
            .andExpect(jsonPath("$.content[2].size").value(metadataDTOList[2]["size"])) // Check if the third metadata size matches
            .andExpect(jsonPath("$.content[2].contentType").value(metadataDTOList[2]["contentType"])) // Check if the third metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(10)) // Check if elementPerPage is 10
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_statusOKLimitLow() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?page=0&limit=2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(2)) // Check if elementPerPage is 2
            .andExpect(jsonPath("$.totalPages").value(2)) // Check if totalPages is 2
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_invalidPage() {
        mockMvc.perform(get("/API/documents?page=-1&limit=10"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getAllDocuments_invalidLimit() {
        mockMvc.perform(get("/API/documents?page=0&limit=-1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getAllDocuments_invalidPageAndLimit() {
        mockMvc.perform(get("/API/documents?page=-1&limit=-1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getAllDocuments_noPage() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(metadataDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches
            .andExpect(jsonPath("$.content[2].id").value(metadataDTOList[2]["id"])) // Check if the third metadata ID matches
            .andExpect(jsonPath("$.content[2].name").value(metadataDTOList[2]["name"])) // Check if the third metadata name matches
            .andExpect(jsonPath("$.content[2].size").value(metadataDTOList[2]["size"])) // Check if the third metadata size matches
            .andExpect(jsonPath("$.content[2].contentType").value(metadataDTOList[2]["contentType"])) // Check if the third metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(10)) // Check if elementPerPage is 10
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_noLimit() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?page=0"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(metadataDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches
            .andExpect(jsonPath("$.content[2].id").value(metadataDTOList[2]["id"])) // Check if the third metadata ID matches
            .andExpect(jsonPath("$.content[2].name").value(metadataDTOList[2]["name"])) // Check if the third metadata name matches
            .andExpect(jsonPath("$.content[2].size").value(metadataDTOList[2]["size"])) // Check if the third metadata size matches
            .andExpect(jsonPath("$.content[2].contentType").value(metadataDTOList[2]["contentType"])) // Check if the third metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(10)) // Check if elementPerPage is 10
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_noParameters() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(metadataDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches
            .andExpect(jsonPath("$.content[2].id").value(metadataDTOList[2]["id"])) // Check if the third metadata ID matches
            .andExpect(jsonPath("$.content[2].name").value(metadataDTOList[2]["name"])) // Check if the third metadata name matches
            .andExpect(jsonPath("$.content[2].size").value(metadataDTOList[2]["size"])) // Check if the third metadata size matches
            .andExpect(jsonPath("$.content[2].contentType").value(metadataDTOList[2]["contentType"])) // Check if the third metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(10)) // Check if elementPerPage is 10
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_bigPage() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?page=1000&limit=10"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(0)) // Check if the number of items in the response is correct

            .andExpect(jsonPath("$.currentPage").value(1000)) // Check if currentPage is 1000
            .andExpect(jsonPath("$.elementPerPage").value(10)) // Check if elementPerPage is 10
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    @Test
    fun getAllDocuments_bigLimit() {
        every { metadataService.getAllMetadata() } returns metadataDTOList

        mockMvc.perform(get("/API/documents?page=0&limit=1000"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(metadataDTOList.size)) // Check if the number of items in the response is correct
            .andExpect(jsonPath("$.content[0].id").value(metadataDTOList[0]["id"])) // Check if the first metadata ID matches
            .andExpect(jsonPath("$.content[0].name").value(metadataDTOList[0]["name"])) // Check if the first metadata name matches
            .andExpect(jsonPath("$.content[0].size").value(metadataDTOList[0]["size"])) // Check if the first metadata size matches
            .andExpect(jsonPath("$.content[0].contentType").value(metadataDTOList[0]["contentType"])) // Check if the first metadata contentType matches
            .andExpect(jsonPath("$.content[1].id").value(metadataDTOList[1]["id"])) // Check if the second metadata ID matches
            .andExpect(jsonPath("$.content[1].name").value(metadataDTOList[1]["name"])) // Check if the second metadata name matches
            .andExpect(jsonPath("$.content[1].size").value(metadataDTOList[1]["size"])) // Check if the second metadata size matches
            .andExpect(jsonPath("$.content[1].contentType").value(metadataDTOList[1]["contentType"])) // Check if the second metadata contentType matches
            .andExpect(jsonPath("$.content[2].id").value(metadataDTOList[2]["id"])) // Check if the third metadata ID matches
            .andExpect(jsonPath("$.content[2].name").value(metadataDTOList[2]["name"])) // Check if the third metadata name matches
            .andExpect(jsonPath("$.content[2].size").value(metadataDTOList[2]["size"])) // Check if the third metadata size matches
            .andExpect(jsonPath("$.content[2].contentType").value(metadataDTOList[2]["contentType"])) // Check if the third metadata contentType matches

            .andExpect(jsonPath("$.currentPage").value(0)) // Check if currentPage is 0
            .andExpect(jsonPath("$.elementPerPage").value(1000)) // Check if elementPerPage is 1000
            .andExpect(jsonPath("$.totalPages").value(1)) // Check if totalPages is 1
            .andExpect(jsonPath("$.totalElements").value(3)) // Check if totalElements is 3
    }

    //getDocumentDetails test cases
    val metadataResult = MetadataDTO(
        id = 1,
        name = "example_metadata",
        size = 1024,
        contentType = "application/pdf",
        creationTimestamp = LocalDateTime.now(),
        modifiedTimestamp = LocalDateTime.now(),
        document = DocumentDTO(
            id = 1,
            content = "example_document_content".toByteArray()
        )
    )

    @Test
    fun getDocumentDetails_statusOK() {
        every { metadataService.getMetadata(1) } returns metadataResult

        mockMvc.perform(get("/API/documents/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("example_metadata"))
            .andExpect(jsonPath("$.size").value(1024))
            .andExpect(jsonPath("$.contentType").value("application/pdf"))
    }

    @Test
    fun getDocumentDetails_invalidMetadataId() {
        val invalidMetadataId = -1
        every { metadataService.getMetadata(invalidMetadataId) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(get("/API/documents/$invalidMetadataId"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getDocumentDetails_stringMetadataId() {
        val invalidMetadataId = "fail"

        mockMvc.perform(get("/API/documents/$invalidMetadataId"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getDocumentDetails_highMetadataId() {
        val invalidMetadataId = 1000
        every { metadataService.getMetadata(invalidMetadataId) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(get("/API/documents/$invalidMetadataId"))
            .andExpect(status().isNotFound)
    }

    //getDocumentData test cases
    val expectedDocument = DocumentDTO(1, "document1 content".toByteArray())

    @Test
    fun getDocumentData_statusOK() {
        every { metadataService.getDocumentByMetadataId(1) } returns expectedDocument
        every { metadataService.getMetadata(1) } returns metadataResult

        mockMvc.perform(get("/API/documents/1/data"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
    }

    @Test
    fun getDocumentData_invalidMetadataId() {
        every { metadataService.getDocumentByMetadataId(-1) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(get("/API/documents/-1/data"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getDocumentData_stringMetadataId() {
        val invalidMetadataId = "fail"

        mockMvc.perform(get("/API/documents/$invalidMetadataId/data"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getDocumentData_highMetadataId() {
        val invalidMetadataId = 1000
        every { metadataService.getDocumentByMetadataId(invalidMetadataId) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(get("/API/documents/$invalidMetadataId"))
            .andExpect(status().isInternalServerError)
    }

    //storeNewDocument test cases

    val multipartFile = MockMultipartFile("file", "example_document.pdf", "application/pdf", "example_document_content".toByteArray())

    @Test
    fun storeNewDocument_validParameters() {
        every { documentService.storeNewDocument(any()) } returns metadataResult

        // Perform the request to store a new document
        mockMvc.perform(
            multipart("/API/documents")
            .file(multipartFile))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("example_metadata"))
            .andExpect(jsonPath("$.size").value(1024))
            .andExpect(jsonPath("$.contentType").value("application/pdf"))


    }

    @Test
    fun storeNewDocument_missingFile() {
        mockMvc.perform(multipart("/API/documents"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Please upload a file, or ensure that a file is attached using the keyword \"file\" during upload!"))
    }

    val multipartFileWrongParameterName =  MockMultipartFile("invalid field name", "example_document.pdf", "application/pdf", "example_document_content".toByteArray())
    @Test
    fun storeNewDocument_wrongParameterName() {
        mockMvc.perform(
            multipart("/API/documents")
            .file(multipartFileWrongParameterName))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Please upload a file, or ensure that a file is attached using the keyword \"file\" during upload!"))
    }


    val multipartFileWrongFileName =  MockMultipartFile("file", "../../incorrectexample_document.pdf", "application/pdf", "example_document_content".toByteArray())
    @Test
    fun storeNewDocument_wrongFileName() {
        mockMvc.perform(
            multipart("/API/documents")
            .file(multipartFileWrongFileName))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Invalid file name. Ensure it contains only alphanumeric characters, dots, underscores, or hyphens, and its length is between 1 and 255 characters!"))
    }

    val multipartFileWrongContentType =  MockMultipartFile("file", "example_document.pdf", "incorrect/pdf", "example_document_content".toByteArray())
    @Test
    fun storeNewDocument_wrongContentType() {
        mockMvc.perform(
            multipart("/API/documents")
            .file(multipartFileWrongContentType))
            .andExpect(status().isInternalServerError)
    }

    val multipartFileEmptyFile =  MockMultipartFile("file", "example_document.pdf", "application/pdf", "".toByteArray())
    @Test
    fun storeNewDocument_EmptyFile() {
        mockMvc.perform(
            multipart("/API/documents")
            .file(multipartFileEmptyFile))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Please upload a file, or ensure that a file is attached using the keyword \"file\" during upload!"))
    }

    //editDocument test cases

    @Test
    fun editDocument_validParameters(){
        every { documentService.updateInformation(any()) } returns metadataResult

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/1")
            .file(multipartFile)
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("example_metadata"))
            .andExpect(jsonPath("$.size").value(1024))
            .andExpect(jsonPath("$.contentType").value("application/pdf"))
    }

    @Test
    fun editDocument_negativeDocumentID() {

        every { documentService.updateInformation(any()) } throws Exception("The metadata with id equal to -3 was not found!")

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/-3")
            .file(multipartFile)
        ).andExpect(status().isBadRequest)
        .andExpect(content().string("Error: Invalid metadataId. Please make sure to enter a valid metadataId!"))

    }

    @Test
    fun editDocument_invalidDocumentID() {

        every { documentService.updateInformation(any()) } throws Exception("The metadata with id equal to 4 was not found!")

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/incorrect")
            .file(multipartFile)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun editDocument_withoutDocument() {

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/1")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun editDocument_incorrectFilename() {

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/1")
            .file(multipartFileWrongFileName)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun editDocument_incorrectContentType() {

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/1")
            .file(multipartFileWrongContentType)
        ).andExpect(status().isInternalServerError)
    }

    @Test
    fun editDocument_emptyFile() {

        mockMvc.perform( multipart(HttpMethod.PUT, "/API/documents/1")
            .file(multipartFileEmptyFile)
        ).andExpect(status().isBadRequest)
    }


    //deleteDocument test cases
    @Test
    fun deleteDocument_statusOK() {
        every { metadataService.deleteMetadata(1) } just runs

        mockMvc.perform(MockMvcRequestBuilders.delete("/API/documents/1"))
            .andExpect(status().isOk)
    }

    @Test
    fun deleteDocument_invalidMetadataId() {
        every { metadataService.deleteMetadata(-1) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(MockMvcRequestBuilders.delete("/API/documents/-1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun deleteDocument_stringMetadataId() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/API/documents/pippo"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun deleteDocument_highMetadataId() {
        every { metadataService.deleteMetadata(1000) } throws DocumentNotFoundException("Document not found")

        mockMvc.perform(MockMvcRequestBuilders.delete("/API/documents/1000"))
            .andExpect(status().isNotFound)

    }
}