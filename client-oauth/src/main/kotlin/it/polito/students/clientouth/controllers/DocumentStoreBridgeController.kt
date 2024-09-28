package it.polito.students.clientouth.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.students.clientouth.dtos.CreateProfessionalDTO
import it.polito.students.clientouth.dtos.ProfessionalDTO
import it.polito.students.clientouth.dtos.ProfessionalWithAssociatedDataDTO
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/gateway")
class DocumentStoreBridgeController(
    private val authorizedClientService: OAuth2AuthorizedClientService
) {
    private val logger = LoggerFactory.getLogger(DocumentStoreBridgeController::class.java)

    @PostMapping("/createProfessional", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun handleFileUpload(
        @RequestPart("createProfessionalInfo") createProfessionalInfo: CreateProfessionalDTO,
        @RequestPart("files", required = false) files: List<MultipartFile>?
    ): ResponseEntity<String> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication is OAuth2AuthenticationToken) {
                val oauthToken = authentication as OAuth2AuthenticationToken

                val authorizedClient = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
                    oauthToken.authorizedClientRegistrationId,
                    oauthToken.name
                )

                val accessToken = authorizedClient?.accessToken?.tokenValue
                    ?: throw RuntimeException("Access token not available")

                val headers = HttpHeaders()
                headers.setBearerAuth(accessToken)
                headers.contentType = MediaType.MULTIPART_FORM_DATA

                val documentsIDs: MutableList<Long> = mutableListOf()

                files?.forEach { file ->

                    val uniqueString = "___" + LocalDateTime.now().nano


                    val fileExtension = file.originalFilename?.substringAfterLast('.', "")


                    val newFileName = if (!fileExtension.isNullOrEmpty()) {
                        "${file.originalFilename?.substringBeforeLast('.')}-$uniqueString.$fileExtension"
                    } else {
                        "${file.originalFilename}-$uniqueString"
                    }


                    val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
                    val fileResource = object : ByteArrayResource(file.bytes) {
                        override fun getFilename(): String? {
                            return newFileName
                        }
                    }
                    body.add("file", fileResource)

                    val requestEntity = HttpEntity(body, headers)

                    val res = RestTemplate().exchange(
                        "http://localhost:8081/API/documents",
                        HttpMethod.POST,
                        requestEntity,
                        String::class.java
                    )

                    if (res.statusCode.is2xxSuccessful) {
                        val trimmedJson = res.body!!.trim().removePrefix("{").removeSuffix("}")
                        val fields = trimmedJson.split(",")
                        val idField = fields.find { it.startsWith("\"id\":") }
                        val id = idField?.substringAfter(":")?.trim()?.removePrefix("\"")?.removeSuffix("\"")
                        documentsIDs.add(id!!.toLong())
                    } else {
                        throw Exception("Error during document creation")
                    }
                }

                headers.contentType = MediaType.APPLICATION_JSON

                val newObj: MutableMap<String, Any> = mutableMapOf(
                    "information" to createProfessionalInfo.information,
                    "dailyRate" to createProfessionalInfo.dailyRate,
                    "skills" to createProfessionalInfo.skills,
                    "geographicalLocation" to createProfessionalInfo.geographicalLocation,
                    "attachmentsList" to documentsIDs
                )

                // Convert the map to JSON
                val objectMapper = ObjectMapper()
                val requestBody = objectMapper.writeValueAsString(newObj)


                val requestEntity = HttpEntity(requestBody, headers)

                val res = RestTemplate().exchange(
                    "http://localhost:8082/API/professionals",
                    HttpMethod.POST,
                    requestEntity,
                    String::class.java
                )

                return ResponseEntity(res.body, HttpStatus.OK)
            } else {
                throw RuntimeException("User not authenticated!")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}")
            return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/deleteProfessional/{professionalId}")
    fun deleteProfessionalAndItsDocuments(
        @PathVariable professionalId: Long
    ): ResponseEntity<String> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication is OAuth2AuthenticationToken) {
                val oauthToken = authentication as OAuth2AuthenticationToken


                val authorizedClient = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
                    oauthToken.authorizedClientRegistrationId,
                    oauthToken.name
                )

                val accessToken = authorizedClient?.accessToken?.tokenValue
                    ?: throw RuntimeException("Access token not available")


                val headers = HttpHeaders()
                headers.setBearerAuth(accessToken)
                headers.contentType = MediaType.APPLICATION_JSON


                val professionalInfoResponse = RestTemplate().exchange(
                    "http://localhost:8082/API/professionals/$professionalId",
                    HttpMethod.GET,
                    HttpEntity<String>(headers),
                    ProfessionalWithAssociatedDataDTO::class.java
                )

                if (!professionalInfoResponse.statusCode.is2xxSuccessful || professionalInfoResponse.body == null) {
                    throw Exception("No professional found for this professional ID!")
                }


                val documentsIDs: List<Long> = professionalInfoResponse.body!!.professionalDTO.attachmentsList


                for (documentId in documentsIDs) {
                    val documentDeleteUrl = "http://localhost:8081/API/documents/$documentId"
                    val deleteRequestEntity = HttpEntity(null, headers)

                    val deleteResponse = RestTemplate().exchange(
                        documentDeleteUrl,
                        HttpMethod.DELETE,
                        deleteRequestEntity,
                        String::class.java
                    )

                    if (!deleteResponse.statusCode.is2xxSuccessful) {
                        throw Exception("Failed to delete document with ID: $documentId for professional ID: $professionalId")
                    }
                }


                val professionalDeleteUrl = "http://localhost:8082/API/professionals/$professionalId"
                val professionalDeleteResponse = RestTemplate().exchange(
                    professionalDeleteUrl,
                    HttpMethod.DELETE,
                    HttpEntity(null, headers),
                    String::class.java
                )

                if (!professionalDeleteResponse.statusCode.is2xxSuccessful) {
                    throw Exception("Failed to delete professional with ID: $professionalId")
                }

                
                return ResponseEntity("Professional and documents deleted successfully", HttpStatus.OK)

            } else {
                throw RuntimeException("User not authenticated!")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}", e)
            return ResponseEntity("Failed to delete professional: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


}




