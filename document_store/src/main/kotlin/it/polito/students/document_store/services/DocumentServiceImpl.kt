package it.polito.students.document_store.services

import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.dtos.toDTO
import it.polito.students.document_store.entities.Document
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.repositories.DocumentRepository
import it.polito.students.document_store.repositories.MetadataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory


@Service
class DocumentServiceImpl(private val documentRepository: DocumentRepository, private val metadataRepository: MetadataRepository): DocumentService {

    var metadataService : MetadataService = MetadataServiceImpl(metadataRepository)

    private val logger = LoggerFactory.getLogger(DocumentServiceImpl::class.java)

   override fun getDocument(id: Int): DocumentDTO {
        val optionalDocument = documentRepository.findById(id)
        if(optionalDocument.isPresent) {
            val document = optionalDocument.get()
            return document.toDTO()
        } else {
            logger.info("The document with id $id was not found on the db")
            throw DocumentNotFoundException("The document with id equal to $id was not found!")
        }
    }

    @Transactional
    override fun storeNewDocument(metadataDTO: MetadataDTO): MetadataDTO {
        val metadataSaved = metadataService.storeNewMetadata(metadataDTO)

        val document = Document().apply {
            content = metadataDTO.document.content
        }

        documentRepository.save(document)
        logger.info("The document with id ${document.id} has been saved on the db")
        return metadataSaved
    }

    @Transactional
    override fun updateInformation(metadataDTO: MetadataDTO): MetadataDTO {
        val documentSavedOnDb = metadataService.getDocumentByMetadataId(metadataDTO.id)

        val document = Document().apply {
            id = documentSavedOnDb.id
            content = metadataDTO.document.content
        }

        metadataDTO.document = document.toDTO()

        if(!documentSavedOnDb.content.contentEquals(document.content)) {
            documentRepository.save(document)
            logger.info("The document with id ${document.id} has been updated on the db")
            metadataDTO.document = document.toDTO()
        }

        return metadataService.updateMetadata(metadataDTO)
    }

}