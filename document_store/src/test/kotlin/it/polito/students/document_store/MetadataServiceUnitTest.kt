package it.polito.students.document_store

import io.mockk.*
import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.dtos.toDTO
import it.polito.students.document_store.dtos.toMap
import it.polito.students.document_store.entities.Document
import it.polito.students.document_store.entities.Metadata
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.exception_handlers.DuplicateDocumentException
import it.polito.students.document_store.repositories.MetadataRepository
import it.polito.students.document_store.services.MetadataServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class MetadataServiceUnitTest {

    val metadataRepository: MetadataRepository = mockk()
    val metadataService = MetadataServiceImpl(metadataRepository)

    //getDocument test cases
    @Test
    fun getMetadata_goodCase() {
        //given
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

        every { metadataRepository.findById(id) } returns Optional.of(metadata)

        //when
        val result = metadataService.getMetadata(id)

        //then
        verify(exactly = 1) { metadataRepository.findById(id) }
        assertEquals(metadata.id, result.id)
    }

    @Test
    fun getDocument_documentNotFound() {
        // given
        val id = 1
        every { metadataRepository.findById(id) } returns Optional.empty()

        // when / then
        val exception = assertThrows(DocumentNotFoundException::class.java) {
            metadataService.getMetadata(id)
        }

        verify(exactly = 1) { metadataRepository.findById(id) }
        assertEquals("The metadata with id equal to $id was not found!", exception.message)
    }

    @Test
    fun storeNewMetadata_successful() {
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
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf()
        every { metadataRepository.save(any()) } returns Metadata().apply {
            name = metadataDto.name //+ pattern + generateFileName(extension)
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        }

        // when
        val result = metadataService.storeNewMetadata(metadataDto)

        // then
        verify(exactly = 1) { metadataRepository.save(any()) }
        assertEquals(metadataDto.name, result.name)
        assertEquals(metadataDto.size, result.size)
        assertEquals(metadataDto.contentType, result.contentType)
        assertEquals(metadataDto.creationTimestamp, result.creationTimestamp)
        assertEquals(metadataDto.creationTimestamp, result.modifiedTimestamp)
        assertEquals(metadataDto.document.id, result.document.id)
        assertEquals(metadataDto.document.content, result.document.content)
    }

    @Test
    fun storeNewMetadata_alreadyExisting() {
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
            metadataService.storeNewMetadata(metadataDto)
        }

        // then
        verify(exactly = 1) { metadataRepository.findAllByName(metadataDto.name) }
    }

    @Test
    fun updateMetadata_successful() {
        //metadata of new version of the file
        val metadataNew = mockk<Metadata>()

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

        every { metadataNew.id } returns id
        every { metadataNew.name } returns name
        every { metadataNew.size } returns size
        every { metadataNew.contentType } returns contentType
        every { metadataNew.creationTimestamp } returns creationTimestamp
        every { metadataNew.modifiedTimestamp } returns modifiedTimestamp
        every { metadataNew.document } returns  document

        val DocumentDTO = DocumentDTO(id, contentByteArray)

        val metadataDto = MetadataDTO(
            id = id,
            name = name,
            size = contentByteArray.size.toLong(),
            contentType = contentType,
            creationTimestamp = creationTimestamp,
            modifiedTimestamp = modifiedTimestamp,
            document = DocumentDTO
        )

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
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf()
        every { metadataRepository.findById(id) } returns Optional.of(Metadata().apply {
            this.name = MetadataDtoOld.name
            this.size = MetadataDtoOld.size
            this.contentType = MetadataDtoOld.contentType
            this.creationTimestamp = MetadataDtoOld.creationTimestamp
            this.modifiedTimestamp = MetadataDtoOld.creationTimestamp
            this.document = documentOld
        })
        every { metadataRepository.save(any()) } returns Metadata().apply {
            this.name = metadataDto.name
            this.size = metadataDto.size
            this.contentType = metadataDto.contentType
            this.creationTimestamp = metadataDto.creationTimestamp
            this.modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        }

        val result = metadataService.updateMetadata(metadataDto)

        // then
        verify(exactly = 1) { metadataRepository.save(any()) }
        assertEquals(metadataDto.name, result.name)
        assertEquals(metadataDto.size, result.size)
        assertEquals(metadataDto.contentType, result.contentType)
        assertEquals(metadataDto.creationTimestamp, result.creationTimestamp)
        assertEquals(metadataDto.creationTimestamp, result.modifiedTimestamp)
        assertEquals(metadataDto.document.id, result.document.id)
        assertEquals(metadataDto.document.content, result.document.content)
    }

    @Test
    fun updateMetadata_successfulChangingContentAndNotName() {
        //metadata of new version of the file
        val metadataNew = mockk<Metadata>()

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

        every { metadataNew.id } returns id
        every { metadataNew.name } returns name
        every { metadataNew.size } returns size
        every { metadataNew.contentType } returns contentType
        every { metadataNew.creationTimestamp } returns creationTimestamp
        every { metadataNew.modifiedTimestamp } returns modifiedTimestamp
        every { metadataNew.document } returns  document

        val DocumentDTO = DocumentDTO(id, contentByteArray)

        val metadataDto = MetadataDTO(
            id = id,
            name = name,
            size = contentByteArray.size.toLong(),
            contentType = contentType,
            creationTimestamp = creationTimestamp,
            modifiedTimestamp = modifiedTimestamp,
            document = DocumentDTO
        )

        //metadata of the already stored version
        val metadataOld = mockk<Metadata>()

        val contentByteArrayOld = "Old content".toByteArray()

        val idOld = 1
        val nameOld = "Test"
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
        every { metadataRepository.findAllByName(metadataDto.name) } returns listOf(Metadata().apply {
            this.id = MetadataDtoOld.id
            this.name = MetadataDtoOld.name
            this.size = MetadataDtoOld.size
            this.contentType = MetadataDtoOld.contentType
            this.creationTimestamp = MetadataDtoOld.creationTimestamp
            this.modifiedTimestamp = MetadataDtoOld.creationTimestamp
            this.document = documentOld
        })
        every { metadataRepository.findById(id) } returns Optional.of(Metadata().apply {
            this.name = MetadataDtoOld.name
            this.size = MetadataDtoOld.size
            this.contentType = MetadataDtoOld.contentType
            this.creationTimestamp = MetadataDtoOld.creationTimestamp
            this.modifiedTimestamp = MetadataDtoOld.creationTimestamp
            this.document = documentOld
        })
        every { metadataRepository.save(any()) } returns Metadata().apply {
            this.name = metadataDto.name
            this.size = metadataDto.size
            this.contentType = metadataDto.contentType
            this.creationTimestamp = metadataDto.creationTimestamp
            this.modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        }

        val result = metadataService.updateMetadata(metadataDto)

        // then
        verify(exactly = 1) { metadataRepository.save(any()) }
        assertEquals(metadataDto.name, result.name)
        assertEquals(metadataDto.size, result.size)
        assertEquals(metadataDto.contentType, result.contentType)
        assertEquals(metadataDto.creationTimestamp, result.creationTimestamp)
        assertEquals(metadataDto.creationTimestamp, result.modifiedTimestamp)
        assertEquals(metadataDto.document.id, result.document.id)
        assertEquals(metadataDto.document.content, result.document.content)
    }

    @Test
    fun updateMetadata_alreadyExistingName() {
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
            metadataService.updateMetadata(metadataDto)
        }

        // then
        verify(exactly = 1) { metadataRepository.findAllByName(metadataDto.name) }
    }

    @Test
    fun deleteMetadata_success() {
        val metadata = mockk<Metadata>()

        val contentByteArray = "Old content".toByteArray()

        val id = 1
        val name = "Test old"
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

        every { metadataRepository.findById(id) } returns Optional.of(Metadata().apply {
            this.name = MetadataDto.name
            this.size = MetadataDto.size
            this.contentType = MetadataDto.contentType
            this.creationTimestamp = MetadataDto.creationTimestamp
            this.modifiedTimestamp = MetadataDto.creationTimestamp
            this.document = document
        })
        justRun { metadataRepository.deleteById(id) }

        val result = metadataService.deleteMetadata(id)

        // then
        verify(exactly = 1) { metadataRepository.deleteById(id) }
    }

    @Test
    fun deleteMetadata_DocumentNotFound() {
        val id = 1

        every { metadataRepository.findById(id) } returns Optional.empty()

        val exception = assertThrows(DocumentNotFoundException::class.java) {
            metadataService.deleteMetadata(id)
        }

        verify(exactly = 1) { metadataRepository.findById(id) }
        assertEquals("The metadata with id equal to $id was not found!", exception.message)
    }

    @Test
    fun getAllMetadata_success() {
        val metadataDto1 = MetadataDTO(
            id = 1,
            name = "Name 1",
            size = "Content 1".toByteArray().size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = DocumentDTO(1, "Content 1".toByteArray())
        )

        val metadataDto2 = MetadataDTO(
            id = 2,
            name = "Name 2",
            size = "Content 2".toByteArray().size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = DocumentDTO(1, "Content 2".toByteArray())
        )

        val metadataDto3 = MetadataDTO(
            id = 3,
            name = "Name 3",
            size = "Content 3".toByteArray().size.toLong(),
            contentType = "text/plain",
            creationTimestamp = LocalDateTime.now(),
            modifiedTimestamp = LocalDateTime.now(),
            document = DocumentDTO(1, "Content 3".toByteArray())
        )

        val metadataList: List<Metadata> = listOf(
            Metadata().apply {
                this.id = metadataDto1.id
                this.name = metadataDto1.name
                this.size = metadataDto1.size
                this.contentType = metadataDto1.contentType
                this.creationTimestamp = metadataDto1.creationTimestamp
                this.modifiedTimestamp = metadataDto1.creationTimestamp
                this.document = Document().apply {
                    id = metadataDto1.document.id
                    content = metadataDto1.document.content
                }
            },
        Metadata().apply {
            this.id = metadataDto2.id
            this.name = metadataDto2.name
            this.size = metadataDto2.size
            this.contentType = metadataDto2.contentType
            this.creationTimestamp = metadataDto2.creationTimestamp
            this.modifiedTimestamp = metadataDto2.creationTimestamp
            this.document = Document().apply {
                id = metadataDto2.document.id
                content = metadataDto2.document.content
            }
        },
        Metadata().apply {
            this.id = metadataDto3.id
            this.name = metadataDto3.name
            this.size = metadataDto3.size
            this.contentType = metadataDto3.contentType
            this.creationTimestamp = metadataDto3.creationTimestamp
            this.modifiedTimestamp = metadataDto3.creationTimestamp
            this.document = Document().apply {
                id = metadataDto3.document.id
                content = metadataDto3.document.content
            }
        }
        )

        every { metadataRepository.findAll() } returns metadataList

        val actual = metadataService.getAllMetadata()

        // Assert the result
        assertEquals(metadataList.map { it.toDTO() }.map {it.toMap()}, actual)
    }

    @Test
    fun getDocumentByMetadataId_success() {
        val metadata = mockk<Metadata>()

        val contentByteArray = "Old content".toByteArray()

        val id = 1
        val name = "Test old"
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

        every { metadataRepository.findById(id) } returns Optional.of(Metadata().apply {
            this.id = id
            this.name = MetadataDto.name
            this.size = MetadataDto.size
            this.contentType = MetadataDto.contentType
            this.creationTimestamp = MetadataDto.creationTimestamp
            this.modifiedTimestamp = MetadataDto.creationTimestamp
            this.document = document
        })

        val result = metadataService.getDocumentByMetadataId(id)

        assertEquals(DocumentDTO, result)
    }

    @Test
    fun getDocumentByMetadataId_DocumentNotFound() {
        val id = 1

        every { metadataRepository.findById(id) } returns Optional.empty()

        val exception = assertThrows(DocumentNotFoundException::class.java) {
            metadataService.getDocumentByMetadataId(id)
        }

        verify(exactly = 1) { metadataRepository.findById(id) }
        assertEquals("The metadata with id equal to $id was not found!", exception.message)
    }

    @Test
    fun checkFileNameAlreadyExist_success() {
        val name = "Name"
        val id = 1

        every { metadataRepository.findAllByName(name) } returns listOf()

        val result = metadataService.checkIfFileNameAlreadyExists(name, id)

        assertEquals(result, false)
    }

}