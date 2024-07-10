package it.polito.students.document_store

import io.mockk.*
import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.entities.Document
import it.polito.students.document_store.entities.Metadata
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.exception_handlers.DuplicateDocumentException
import it.polito.students.document_store.repositories.DocumentRepository
import it.polito.students.document_store.repositories.MetadataRepository
import it.polito.students.document_store.services.DocumentServiceImpl
import it.polito.students.document_store.services.MetadataService
import it.polito.students.document_store.services.MetadataServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import java.time.LocalDateTime
import java.util.*


@SpringBootTest(
    classes = arrayOf(DocumentStoreApplication::class),
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentServiceUnitTest {

    val documentRepository: DocumentRepository = mockk()
    val metadataRepository: MetadataRepository = mockk()

    val documentService = DocumentServiceImpl(documentRepository, metadataRepository)

    //getDocument test cases
    @Test
    fun getDocument_goodCase() {
        //given
        val document = mockk<Document>()
        val contentByteArray = "Sample content".toByteArray()
        val id = 1
        every { document.content } returns contentByteArray
        every { document.id } returns id
        every { documentRepository.findById(id) } returns Optional.of(document)

        //when
        val result = documentService.getDocument(id)

        //then
        verify(exactly = 1) { documentRepository.findById(id) }
        assertEquals(DocumentDTO(1, contentByteArray), result)
    }

    @Test
    fun getDocument_documentNotFound() {
        // given
        val id = 1
        every { documentRepository.findById(id) } returns Optional.empty()

        // when / then
        val exception = assertThrows(DocumentNotFoundException::class.java) {
            documentService.getDocument(id)
        }

        verify(exactly = 1) { documentRepository.findById(id) }
        assertEquals("The document with id equal to $id was not found!", exception.message)
    }

    @Test
    fun storeNewDocument_success() {
        val contentByteArray = "New content".toByteArray()
        val newDocumentDTO = DocumentDTO(0, contentByteArray)

        val metadataDto = MetadataDTO(
            id = 0,
            name = "Test",
            size = contentByteArray.size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = newDocumentDTO
        )
        val document = Document().apply {
            //id = metadataDto.document.id
            content = metadataDto.document.content
        }

        // mock behavior
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf()
        every { metadataRepository.save(any()) } returns Metadata().apply {
            name = metadataDto.name //+ pattern + generateFileName(extension)
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        }

        every { documentRepository.save(document) } returns document

        val result = documentService.storeNewDocument(metadataDto)

        assertEquals(metadataDto, result)
    }

    @Test
    fun storeNewDocument_alreadyExisting() {
        val contentByteArray = "New content".toByteArray()
        val newDocumentDTO = DocumentDTO(1, contentByteArray)

        val metadataDto = MetadataDTO(
            id = 1,
            name = "Test",
            size = contentByteArray.size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = newDocumentDTO
        )
        val document = Document().apply {
            id = metadataDto.document.id
            content = metadataDto.document.content
        }

        // mock behavior
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf(Metadata().apply {
            name = metadataDto.name //+ pattern + generateFileName(extension)
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        })

        // when
        val exception = assertThrows(DuplicateDocumentException::class.java) {
            documentService.storeNewDocument(metadataDto)
        }
    }

    @Test
    fun updateInformation_success() {
        val metadata = mockk<Metadata>()

        val contentByteArray = "Sample content".toByteArray()
        val id = 1
        val name = "Test"
        val size = contentByteArray.size.toLong()
        val contentType = "text/plain"
        val creationTimestamp = LocalDateTime.now()
        val modifiedTimestamp = LocalDateTime.now()

        val document = mockk<Document>()
        every { document.content } returns contentByteArray
        every { document.id } returns id

        every { metadata.id } returns id
        every { metadata.name } returns name
        every { metadata.size } returns size
        every { metadata.contentType } returns contentType
        every { metadata.creationTimestamp } returns creationTimestamp
        every { metadata.modifiedTimestamp } returns modifiedTimestamp
        every { metadata.document } returns  document

        val DocumentDTO = DocumentDTO(1, contentByteArray)

        val MetadataDto = MetadataDTO(
            id = id,
            name = name,
            size = contentByteArray.size.toLong(),
            contentType = contentType,
            creationTimestamp = creationTimestamp,
            modifiedTimestamp = modifiedTimestamp,
            document = DocumentDTO
        )

        every { metadataRepository.findById(id) } returns Optional.of(metadata)

        every { documentRepository.save(document) } returns document

        //metadata of the already stored version
        val metadataOld = mockk<Metadata>()

        val contentByteArrayOld = "Old content".toByteArray()

        val idOld = 1
        val nameOld = "Test old"
        val sizeOld = contentByteArrayOld.size.toLong()
        val contentTypeOld = "text/plain"
        val creationTimestampOld = LocalDateTime.now()
        val modifiedTimestampOld = LocalDateTime.now()

        val documentOld = mockk<Document>()
        every { documentOld.content } returns contentByteArrayOld
        every { documentOld.id } returns id

        every { metadataOld.id } returns idOld
        every { metadataOld.name } returns nameOld
        every { metadataOld.size } returns sizeOld
        every { metadataOld.contentType } returns contentTypeOld
        every { metadataOld.creationTimestamp } returns creationTimestampOld
        every { metadataOld.modifiedTimestamp } returns modifiedTimestampOld
        every { metadataOld.document } returns  documentOld

        val DocumentDTOOld = DocumentDTO(1, contentByteArray)

        val MetadataDtoOld = MetadataDTO(
            id = 1,
            name = nameOld,
            size = contentByteArrayOld.size.toLong(),
            contentType = contentTypeOld,
            creationTimestamp = creationTimestampOld,
            modifiedTimestamp = modifiedTimestampOld,
            document = DocumentDTOOld
        )

        // mock behavior
        every { metadataRepository.findAllByName(MetadataDto.name) } returns listOf()
        every { metadataRepository.findById(id) } returns Optional.of(Metadata().apply {
            this.name = MetadataDtoOld.name
            this.size = MetadataDtoOld.size
            this.contentType = MetadataDtoOld.contentType
            this.creationTimestamp = MetadataDtoOld.creationTimestamp
            this.modifiedTimestamp = MetadataDtoOld.creationTimestamp
            this.document = documentOld
        })
        every { metadataRepository.save(any()) } returns Metadata().apply {
            this.name = MetadataDto.name
            this.size = MetadataDto.size
            this.contentType = MetadataDto.contentType
            this.creationTimestamp = MetadataDto.creationTimestamp
            this.modifiedTimestamp = MetadataDto.creationTimestamp
            this.document = document
        }

        val result = documentService.updateInformation(MetadataDto)

        verify(exactly = 1) { documentRepository.save(any()) }
        assertEquals(MetadataDto.name, result.name)
        assertEquals(MetadataDto.size, result.size)
        assertEquals(MetadataDto.contentType, result.contentType)
        assertEquals(MetadataDto.creationTimestamp, result.creationTimestamp)
        assertEquals(MetadataDto.creationTimestamp, result.modifiedTimestamp)
        assertEquals(MetadataDto.document.id, result.document.id)
        assertEquals(MetadataDto.document.content, result.document.content)
    }

    @Test
    fun updateInformation_alreadyExistingName() {
        // given
        val contentByteArray = "New content".toByteArray()
        val newDocumentDTO = DocumentDTO(1, contentByteArray)

        val metadataDto = MetadataDTO(
            id = 1,
            name = "Test",
            size = contentByteArray.size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = newDocumentDTO
        )
        val document = Document().apply {
            id = metadataDto.document.id
            content = metadataDto.document.content
        }

        // mock behavior
        every { metadataRepository.findById(metadataDto.id) } returns Optional.of(Metadata().apply {
            id = metadataDto.id
            name = metadataDto.name
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.modifiedTimestamp
            this.document = document
        })
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf(Metadata().apply {
            id = 2
            name = metadataDto.name //+ pattern + generateFileName(extension)
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        })

        // when
        val exception = assertThrows(DuplicateDocumentException::class.java) {
            documentService.updateInformation(metadataDto)
        }

        // then
        verify(exactly = 1) { metadataRepository.findAllByName(metadataDto.name) }
    }
}
