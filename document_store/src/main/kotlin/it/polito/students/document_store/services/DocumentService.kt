package it.polito.students.document_store.services

import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import org.springframework.transaction.annotation.Transactional


interface DocumentService {

    /**
     * Retrieves the document with the specified ID from the database.
     *
     * @param id The ID of the document to retrieve.
     * @return The DTO representation of the retrieved document.
     * @throws DocumentNotFoundException If the document with the specified ID is not found.
     */
    fun getDocument(id: Int): DocumentDTO
    /**
     * Creates and saves metadata and document entries in the database based on the provided metadata DTO.
     *
     * @param documentDto The DTO containing metadata information.
     * @return The DTO representation of the retrieved document and its metadata.
     */
    fun storeNewDocument(metadataDTO: MetadataDTO): MetadataDTO

    /**
     * Updates metadata and document entries in the database based on the provided metadata DTO.
     *
     * @param documentDto The DTO containing metadata information.
     * @return The DTO representation of the retrieved document and its metadata.
     */
    fun updateInformation(metadataDTO: MetadataDTO): MetadataDTO
}