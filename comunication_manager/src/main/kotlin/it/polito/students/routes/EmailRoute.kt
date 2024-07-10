package it.polito.students.routes

import com.google.gson.Gson
import it.polito.students.dtos.CreateMessageDTO
import it.polito.students.utils.ErrorsPage.Companion.FAILED_TO_SEND_EMAIL_TO_CRM_SERVICE
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.support.DefaultMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

@Component
class EmailRoute : RouteBuilder() {
    private val logger = LoggerFactory.getLogger(EmailRoute::class.java)

    @Value("\${server.main.port}")
    private lateinit var serverMainPort: String

    @Value("\${server.crm.id}")
    private lateinit var serverCrmId: String

    @Throws(Exception::class)
    override fun configure() {
        val gson = Gson()

        errorHandler(deadLetterChannel("log:dead?level=ERROR").maximumRedeliveries(3).redeliveryDelay(2000))

        from("google-mail-stream://{{gmail-account.application_name}}?clientId={{gmail.client-id}}&clientSecret={{gmail.client-secret}}&refreshToken={{gmail.refresh-token}}")
            .routeId("email-route")
            .process { exchange ->
                // Custom processing of the email message
                val mail = exchange.getIn().getBody(Any::class.java)
                var body = ""

                if (mail.javaClass == DefaultMessage::class.java) {
                    //The email contains an attached file

                } else {
                    //No attached files
                    body = exchange.getIn().getBody(String::class.java)
                }
                val from = exchange.getIn().getHeader("CamelGoogleMailStreamFrom", String::class.java)
                var subject = exchange.getIn().getHeader("CamelGoogleMailStreamSubject", String::class.java)
                val to = exchange.getIn().getHeader("CamelGoogleMailStreamTo", String::class.java)

                val regex = Regex("<(.*?)>")
                val matchResult = regex.find(from)
                val email = matchResult?.groupValues?.get(1) ?: from

                if(subject.isBlank()) {
                    subject = "No subject"
                }

                if(body.isBlank()) {
                    body = "No body"
                }

                // Create a new CreateMessageDTO object
                val createMessageDTO = CreateMessageDTO(
                    subject = subject,
                    body = body,
                    priority = "low",
                    channel = "email",
                    sender = email ?: "Unknown"
                )

                // Convert the DTO object to JSON
                val jsonBody = gson.toJson(createMessageDTO)

                logger.info("NEW EMAIL RECEIVED: $jsonBody")

//                // Retrieve access token
//                val accessToken = getAccessToken(
//                    keycloakHost = "http://localhost:8080",
//                    realm = "lab5-g04",
//                    clientId = "lab5-g04-client",
//                    clientSecret = "5EN4Ql688ygcMWtJNU1tPUHthGuofGcQ",
//                    username = "alexsmith",
//                    password = "alexsmithPassword"
//                )
//
//                println("ACCESS TOKEN \n $accessToken")
//

                // Set the JSON body in the exchange
                exchange.message.body = jsonBody
                // Set the Content-Type header to application/json
                exchange.message.setHeader("Content-Type", "application/json")
//                exchange.message.setHeader("Authorization","Bearer "+ accessToken)
            }
            // Send the POST request to the CRM service endpoint
            .to("http://localhost:${serverMainPort}/${serverCrmId}/v1/API/messages")
            .process { exchange ->
                // Log the response message
                val response = exchange.message.getBody(String::class.java)
                logger.info("RESPONSE FROM CRM SERVICE: $response")
            }
            .onException(java.net.ConnectException::class.java)
            .maximumRedeliveries(3)
            .redeliveryDelay(2000)
            .handled(true)
            .log(FAILED_TO_SEND_EMAIL_TO_CRM_SERVICE)
    }
//
//    fun getAccessToken(
//        keycloakHost: String,
//        realm: String,
//        clientId: String,
//        clientSecret: String,
//        username: String,
//        password: String
//    ): String? {
//        val url = URL("$keycloakHost/auth/realms/$realm/protocol/openid-connect/token")
//        val postData = "client_id=$clientId&client_secret=$clientSecret&username=$username&password=$password&grant_type=password"
//
//        val connection = url.openConnection() as HttpURLConnection
//        connection.requestMethod = "POST"
//        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
//        connection.doOutput = true
//
//        connection.outputStream.use { os ->
//            val input = postData.toByteArray()
//            os.write(input, 0, input.size)
//        }
//
//        return connection.inputStream.bufferedReader().use {
//            val response = it.readText()
//            val jsonResponse = Json.parseToJsonElement(response)
//            jsonResponse.jsonObject["access_token"]?.jsonPrimitive?.content
//        }
//    }
}
