package it.polito.students.document_store.services

import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.exception_handlers.DocumentNotFoundException

interface MetadataService {

    /**
     * Retrieves the metadata of the document with the specified ID from the database.
     *
     * @param id The ID of the metadata to retrieve.
     * @return The DTO representation of the retrieved metadata.
     * @throws DocumentNotFoundException If the metadata with the specified ID is not found.
     */
    fun getMetadata(id: Int): MetadataDTO

    /**
     * Creates and saves metadata entry in the database.
     *
     * @param metadataDto The DTO containing metadata information.
     */
    fun storeNewMetadata(metadataDto: MetadataDTO): MetadataDTO

    /**
     * Updates metadata entry in the database.
     *
     * @param metadataDto The DTO containing metadata information.
     */
    fun updateMetadata(metadataDto: MetadataDTO): MetadataDTO

    /**
     * Deletes document in the database.
     *
     * @param metadataId The metadataId containing id of metadata information.
     * @throws DocumentNotFoundException
     */
    fun deleteMetadata(metadataId: Int)

    /**
     * Gets a list of all metadata with detailes in db, paginated
     *
     * @return List<MetadataDTO>.
     */
    fun getAllMetadata(): List<Map<String, Any?>>

    /**
     * Gets a document starting by the metadataId
     *
     * @param metadataId The metadataId containing id of metadata information.
     * @return DocumentDTO.
     */
    fun getDocumentByMetadataId(metadataId: Int): DocumentDTO
}