package it.polito.students.service

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.UserCredentials
import jakarta.activation.DataHandler
import jakarta.activation.DataSource
import jakarta.activation.FileDataSource
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.apache.commons.codec.binary.Base64
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class SendEmailServiceImpl(
    @Value("\${spring.application.name}") val applicationName: String,
    @Value("\${gmail.client-email}") val gmailClientEmail: String,
    @Value("\${gmail.client-id}") val gmailClientId: String,
    @Value("\${gmail.client-secret}") val gmailClientSecret: String,
    @Value("\${gmail.refresh-token}") val gmailClientRefreshToken: String
) : SendEmailService {

    override fun sendEmail(
        destEmailAddr: String,
        subject: String,
        textBody: String,
        file: File?
    ): com.google.api.services.gmail.model.Message {
        val props = Properties()
        val session: Session = Session.getDefaultInstance(props, null)
        val mimeEmail = MimeMessage(session)

        mimeEmail.setFrom(InternetAddress(gmailClientEmail))
        mimeEmail.addRecipient(Message.RecipientType.TO, InternetAddress(destEmailAddr))
        mimeEmail.subject = subject

        val multipart: Multipart = MimeMultipart()

        if (file != null) {
            var mimeBodyPart = MimeBodyPart()
            mimeBodyPart.setContent(textBody, "text/plain")
            multipart.addBodyPart(mimeBodyPart)

            mimeBodyPart = MimeBodyPart()
            val source: DataSource = FileDataSource(file)
            mimeBodyPart.dataHandler = DataHandler(source)
            mimeBodyPart.fileName = file.name
            multipart.addBodyPart(mimeBodyPart)
        } else {
            val textPart = MimeBodyPart()
            textPart.setText(textBody)
            multipart.addBodyPart(textPart)
        }

        mimeEmail.setContent(multipart)

        val buffer = ByteArrayOutputStream()
        mimeEmail.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = Base64.encodeBase64URLSafeString(bytes)
        var message = com.google.api.services.gmail.model.Message()
        message.setRaw(encodedEmail)

        val credentials = UserCredentials.newBuilder()
            .setClientId(gmailClientId)
            .setClientSecret(gmailClientSecret)
            .setRefreshToken(gmailClientRefreshToken)
            .build()
        val requestInitializer = HttpCredentialsAdapter(credentials)
        val service = Gmail.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), requestInitializer)
            .setApplicationName(applicationName).build()

        message = service.users().messages().send("me", message).execute()

        return message
    }

}