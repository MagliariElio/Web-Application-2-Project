package it.polito.students.controller

import it.polito.students.utils.ErrorsPage
import it.polito.students.service.SendEmailService
import org.apache.commons.validator.routines.EmailValidator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream

@RestController
@RequestMapping("/API/emails")
class EmailController(
    private val sendEmailService: SendEmailService
) {
    private val logger = LoggerFactory.getLogger(EmailController::class.java)

    // TODO: aggiunto per testare la route
    @GetMapping("/auth")
    fun get(authentication: Authentication?): Map<String, Any?> {
        val authorities: Collection<GrantedAuthority>? = authentication?.authorities

        return mapOf(
            "name" to "comunicationManagerService:8083",
            "principal" to authentication?.principal,
            "authorities" to authorities?.map { it.authority }
        )
    }

    /**
     * This endpoint handles a POST request to send an email.
     *
     * @param destEmailAddr The recipient email address
     * @param subject The subject of the email
     * @param textBody The textual content of the email
     * @param multipartFile (optional) The attachment of the mail, as a document
     * @return a ResponseEntity containing the JSON representation of the email sent.
     */
    @PostMapping("", "/")
    fun sendEmail(
        @RequestParam destEmailAddr: String,
        @RequestParam subject: String?,
        @RequestParam textBody: String?,
        @RequestParam multipartFile: MultipartFile?
    ): ResponseEntity<Any> {

        if (destEmailAddr.isBlank()) {
            logger.info("POST /API/emails called with blank destination address")
            return ResponseEntity(mapOf("error" to ErrorsPage.BLANK_DEST_ADDRESS_ERROR), HttpStatus.BAD_REQUEST)
        }

        if (!isValidEmail(destEmailAddr)) {
            logger.info("POST /API/emails called with wrong recipient's email parameter")
            return ResponseEntity(mapOf("error" to ErrorsPage.INCORRECT_EMAIL_PARAMETER), HttpStatus.BAD_REQUEST)
        }

        try {
            if (multipartFile == null || multipartFile.isEmpty) {
                sendEmailService.sendEmail(destEmailAddr, subject ?: "", textBody ?: "", null)
                logger.info("Email sent successfully to $destEmailAddr")

                return ResponseEntity(
                    mapOf(
                        "sentEmail" to mapOf(
                            "destEmailAddr" to destEmailAddr,
                            "subject" to subject,
                            "textBody" to textBody,
                            "attachment" to "no-attachment"
                        )
                    ), HttpStatus.OK
                )
            } else {
                val file = File(multipartFile.originalFilename!!)
                val outputStream = FileOutputStream(file)
                outputStream.write(multipartFile.bytes)
                outputStream.close()

                sendEmailService.sendEmail(destEmailAddr, subject ?: "", textBody ?: "", file)

                logger.info("Email sent successfully to $destEmailAddr")
                return ResponseEntity(
                    mapOf(
                        "sentEmail" to mapOf(
                            "destEmailAddr" to destEmailAddr,
                            "subject" to subject,
                            "textBody" to textBody,
                            "attachment" to multipartFile.originalFilename
                        )
                    ), HttpStatus.OK
                )
            }

        } catch (e: Exception) {
            logger.info("Internal server error: " + e.message)
            return ResponseEntity(mapOf("error" to ErrorsPage.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return EmailValidator.getInstance().isValid(email)
}
