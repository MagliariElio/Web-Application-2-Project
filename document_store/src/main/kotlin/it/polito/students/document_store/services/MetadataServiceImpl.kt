package it.polito.students.document_store.services

import it.polito.students.document_store.dtos.*
import it.polito.students.document_store.entities.Document
import it.polito.students.document_store.entities.Metadata
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.exception_handlers.DuplicateDocumentException
import it.polito.students.document_store.repositories.MetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MetadataServiceImpl(private val metadataRepository: MetadataRepository): MetadataService {
    private val logger = LoggerFactory.getLogger(MetadataServiceImpl::class.java)
    private val messageErrorFileNameAlreadyExists = "Unable to complete the operation. The filename entered already exists in the system. Please enter a unique filename!"

    override fun getMetadata(id: Int): MetadataDTO {
        val optional = metadataRepository.findById(id)
        if(optional.isPresent) {
            val metadata = optional.get()
            return metadata.toDTO()
        } else {
            logger.info("The metadata with id $id was not found on the db")
            throw DocumentNotFoundException("The metadata with id equal to $id was not found!")
        }
    }

    override fun storeNewMetadata(metadataDto: MetadataDTO): MetadataDTO {
        // checks if the filename already exists
        if(this.checkIfFileNameAlreadyExists(metadataDto.name, -1)) {
            logger.info(messageErrorFileNameAlreadyExists +" Provided name: ${metadataDto.name}")
            throw DuplicateDocumentException(messageErrorFileNameAlreadyExists)
        }

        val document = Document().apply {
            id = metadataDto.document.id
            content = metadataDto.document.content
        }

        val metadata = Metadata().apply {
            name = metadataDto.name //+ pattern + generateFileName(extension)
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = metadataDto.creationTimestamp
            modifiedTimestamp = metadataDto.creationTimestamp
            this.document = document
        }

        val metadataSaved = metadataRepository.save(metadata).toDTO()
        logger.info("The metadata with id ${metadata.id} has been saved on the db")
        return metadataSaved
    }

    override fun updateMetadata(metadataDto: MetadataDTO): MetadataDTO {
        // checks if the filename already exists
        if(this.checkIfFileNameAlreadyExists(metadataDto.name, metadataDto.id)) {
            logger.info(messageErrorFileNameAlreadyExists +" Provided name: ${metadataDto.name}")
            throw DuplicateDocumentException(messageErrorFileNameAlreadyExists)
        }

        val metadataId = metadataDto.id
        val oldMetadataDto = getMetadata(metadataId)

        val document = Document().apply {
            id = metadataDto.document.id
            content = metadataDto.document.content
        }

        val metadata = Metadata().apply {
            id = metadataId
            name = metadataDto.name
            size = metadataDto.size
            contentType = metadataDto.contentType
            creationTimestamp = oldMetadataDto.creationTimestamp
            modifiedTimestamp = oldMetadataDto.modifiedTimestamp
            this.document = document
        }

        val oldDocument = Document().apply {
            id = oldMetadataDto.document.id
            content = oldMetadataDto.document.content
        }

        val oldMetadata = Metadata().apply {
            id = metadataId
            name = oldMetadataDto.name
            size = oldMetadataDto.size
            contentType = oldMetadataDto.contentType
            creationTimestamp = oldMetadataDto.creationTimestamp
            modifiedTimestamp = oldMetadataDto.modifiedTimestamp
            this.document = oldDocument
        }

        if(oldMetadata != metadata) {
            metadata.name = metadataDto.name
            metadata.modifiedTimestamp = LocalDateTime.now()

            val metadataSaved = metadataRepository.save(metadata).toDTO()
            logger.info("The metadata with id ${metadata.id} has been updated on the db")
            return metadataSaved
        } else {
            return oldMetadataDto
        }
    }

    override fun deleteMetadata(metadataId: Int) {
        getMetadata(metadataId) // check if metadataId is valid otherwise throws an exception
        metadataRepository.deleteById(metadataId)

        logger.info("The metadata with id $metadataId has been deleted on the db")
    }

    override fun getAllMetadata(): List<Map<String, Any?>> {
        val list = metadataRepository.findAll().map { it.toDTO() }
        return list.map {it.toMap()}
    }

    override fun getDocumentByMetadataId(metadataId: Int): DocumentDTO {
        val metadata = metadataRepository.findById(metadataId)
        if (metadata.isPresent) {
            val metadataDto = metadata.get().toDTO()
            return metadataDto.toDocument()
        } else {
            logger.info("The metadata with id $metadataId was not found on the db")
            throw DocumentNotFoundException("The metadata with id equal to $metadataId was not found!")
        }
    }

    /**
     * Gets the name to check id it already exists in db
     *
     * @param name The name to check.
     * @param id The id of the metadata.
     * @return Boolean.
     */
    fun checkIfFileNameAlreadyExists(name: String, metadataId: Int): Boolean {
        val list = metadataRepository.findAllByName(name)
        return if(list.isEmpty()) {
            false
        } else {
            list.find { it.id == metadataId } == null
        }
    }
}