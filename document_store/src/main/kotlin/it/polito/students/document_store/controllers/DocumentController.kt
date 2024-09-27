package it.polito.students.document_store.controllers

import it.polito.students.document_store.dtos.DocumentDTO
import it.polito.students.document_store.dtos.MetadataDTO
import it.polito.students.document_store.dtos.toMap

import it.polito.students.document_store.exception_handlers.DocumentNotFoundException
import it.polito.students.document_store.exception_handlers.DuplicateDocumentException
import it.polito.students.document_store.services.DocumentService
import it.polito.students.document_store.services.MetadataService
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.*

import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/API/documents")
class DocumentController(private val documentService: DocumentService, private val metadataService: MetadataService) {
    private val logger = LoggerFactory.getLogger(DocumentController::class.java)
    private val messageErrorInvalidMetadataId = "Error: Invalid metadataId. Please make sure to enter a valid metadataId!"
    private val messageErrorUploadFile = "Please upload a file, or ensure that a file is attached using the keyword \"file\" during upload!"
    private val messageErrorFileNameNotAccepted = "Invalid file name. Ensure it contains only alphanumeric characters, dots, underscores, or hyphens, and its length is between 1 and 255 characters!"

    // TODO: aggiunto per testare la route
    @GetMapping("/auth")
    fun get(authentication: Authentication?): Map<String, Any?> {
        val authorities: Collection<GrantedAuthority>? = authentication?.authorities

        return mapOf(
            "name" to "documentStoreService:8081",
            "principal" to authentication?.principal,
            "authorities" to authorities?.map { it.authority }
        )
    }

    @GetMapping("/auth/public")
    fun getPublic(authentication: Authentication?): Map<String, Any?> {
        val authorities: Collection<GrantedAuthority>? = authentication?.authorities

        return mapOf(
            "name" to "documentStoreService:8081",
            "principal" to authentication?.principal,
            "authorities" to authorities?.map { it.authority }
        )
    }

    @GetMapping("/", "")
    fun getAllDocuments(
        @RequestParam(defaultValue = "0") page: Int,      //page number
        @RequestParam(defaultValue = "10") limit: Int,     //how many document there are for each page
    ): ResponseEntity<Map<String, Any?>>{
        if (page < 0 || limit < 0) {
            return ResponseEntity.badRequest().build()
        }

        val listMetadataDTO = metadataService.getAllMetadata()

        var start = page * limit
        val end = minOf(start + limit, listMetadataDTO.size)

        if(start > end) {
            start = end
        }

        val pageable : Pageable = PageRequest.of(page, limit)
        val subList = listMetadataDTO.subList(start, end)
        val pageImpl = PageImpl(subList, pageable, listMetadataDTO.size.toLong())

        //Prepare answer for respond
        val mapAnswer : Map<String, Any?> = mapOf(
            "content" to pageImpl.content,
            "currentPage" to pageImpl.number,
            "elementPerPage" to pageImpl.size,
            "totalPages" to pageImpl.totalPages,
            "totalElements" to pageImpl.totalElements
        )

        return ResponseEntity.ok(mapAnswer)
    }

    @GetMapping("/{metadataId}", "/{metadataId}/")
    fun getDocumentDetails(@PathVariable metadataId: Int): ResponseEntity<out Any> {
        if(metadataId < 0){
            return ResponseEntity(messageErrorInvalidMetadataId, HttpStatus.BAD_REQUEST)
        }

        try {
            val metadata = metadataService.getMetadata(metadataId)
            val map = metadata.toMap()  // in this way it is not considered the content of the document
            return ResponseEntity(map, HttpStatus.OK)
        } catch (e: DocumentNotFoundException) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{metadataId}/data", "/{metadataId}/data/")
    fun getDocumentData(
        @PathVariable metadataId: Int
    ): ResponseEntity<out Any> {
        if(metadataId < 0){
            return ResponseEntity(messageErrorInvalidMetadataId, HttpStatus.BAD_REQUEST)
        }

        try{
            val document = metadataService.getDocumentByMetadataId(metadataId)
            val metadata = metadataService.getMetadata(metadataId)

            // Returning content data
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${metadata.name}\"")
                .header(HttpHeaders.CONTENT_TYPE, metadata.contentType)
                .body( ByteArrayResource(document.content) )

        }catch(e: DocumentNotFoundException) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/", "")
    fun storeNewDocument(@RequestParam file: MultipartFile?): ResponseEntity<out Any> {
        if(file == null || file.isEmpty) {
            return ResponseEntity(messageErrorUploadFile, HttpStatus.BAD_REQUEST)
        }

        try {
            val documentDto = DocumentDTO(0, file.bytes)
            val metadataDto = MetadataDTO(
                0, file.originalFilename.orEmpty(), file.size, file.contentType!!,
                LocalDateTime.now(), LocalDateTime.now(), documentDto
            )

            // checks regex on the filename
            if (!checkFileName(metadataDto.name)) {
                logger.info(messageErrorFileNameNotAccepted + " Provided name: ${metadataDto.name}")
                return ResponseEntity(messageErrorFileNameNotAccepted, HttpStatus.BAD_REQUEST)
            }

            val documentSaved = documentService.storeNewDocument(metadataDto)
            return ResponseEntity(documentSaved.toMap(), HttpStatus.OK)
        } catch(e: DuplicateDocumentException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity("${e.message}", HttpStatus.CONFLICT)
        } catch(e: Exception) {
            logger.info("Error: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping("/{metadataId}", "/{metadataId}/")
    fun editDocument(@PathVariable metadataId: Int, @RequestBody file: MultipartFile?): ResponseEntity<out Any> {
        if(file == null || file.isEmpty) {
            return ResponseEntity(messageErrorUploadFile, HttpStatus.BAD_REQUEST)
        }

        if(metadataId < 0){
            return ResponseEntity(messageErrorInvalidMetadataId, HttpStatus.BAD_REQUEST)
        }

        try {
            //val creationData = metadataService.getMetadata(metadataId).creationTimestamp
            val documentDto = DocumentDTO(0, file.bytes)
            val metadataDto = MetadataDTO(metadataId, file.originalFilename.orEmpty(), file.size, file.contentType!!,
                LocalDateTime.now(), LocalDateTime.now(), documentDto)

            // check regex on the filename
            if(!checkFileName(metadataDto.name)) {
                logger.info(messageErrorFileNameNotAccepted +" Provided name: ${metadataDto.name}")
                return ResponseEntity(messageErrorFileNameNotAccepted, HttpStatus.BAD_REQUEST)
            }

            val documentSaved = documentService.updateInformation(metadataDto)
            return ResponseEntity(documentSaved.toMap(), HttpStatus.OK)
        } catch(e: DuplicateDocumentException) {
            logger.info("Error: ${e.message}")
            return ResponseEntity("${e.message}", HttpStatus.CONFLICT)
        } catch (e: DocumentNotFoundException) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        } catch(e: Exception) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/{metadataId}", "/{metadataId}/")
    fun deleteDocument(
        @PathVariable metadataId: Int
    ): ResponseEntity<String>{
        if(metadataId < 0){
            return ResponseEntity(messageErrorInvalidMetadataId, HttpStatus.BAD_REQUEST)
        }

        try{
            metadataService.deleteMetadata(metadataId)
            return ResponseEntity("The file has been removed successfully!", HttpStatus.OK)
        } catch (e: DocumentNotFoundException) {
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        }catch (e: Exception){
            logger.info("Error with metadata id equal to ${metadataId}: ${e.message}")
            return ResponseEntity("Error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Checks whether a file name is valid for database registration.
     *
     * This function validates the provided file name against a regular expression pattern.
     * The file name is considered valid if it contains only alphanumeric characters,
     * dots ('.'), underscores ('_'), or hyphens ('-'), and its length is between 1 and 255 characters.
     *
     * @param name The file name to be validated.
     * @return true if the file name is valid, false otherwise.
     */
    fun checkFileName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9._ -]{1,255}$"))
    }
}