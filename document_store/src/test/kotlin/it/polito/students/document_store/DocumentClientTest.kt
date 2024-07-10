package it.polito.students.document_store

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class Metadata(
    val id: Int,
    val name: String,
    val size: Long,
    val contentType: String,
    val creationTimestamp: String,
    val modifiedTimestamp: String
)

data class DocumentResponse(
    val content: List<Metadata>,
    val currentPage: Int,
    val elementsPerPage: Int,
    val totalPages: Int,
    val totalElements: Int
)

class DocumentClientTest {

    companion object {
        val url = "http://localhost:8080/API/documents/"
        val client = OkHttpClient()
        val gson = Gson()

        @JvmStatic
        fun main(args: Array<String>) {
            scenario_1()

            scenario_2()

            scenario_4()
        }

        /*
            1. Request all documents: Retrieve a list of all documents available on the server.
            2. Upload a new document: Add a new document to the collection on the server.
            3. Delete this last document: Remove the last uploaded document from the collection on the server.
         */
        fun scenario_1() {
            // Gets all file
            val requestGetAllDocuments = Request.Builder().url(url).build()

            val responseGetAllDocuments = client.newCall(requestGetAllDocuments).execute()
            if (!responseGetAllDocuments.isSuccessful) {
                throw IOException("Error: $responseGetAllDocuments")
            }

            val response = responseGetAllDocuments.body?.string()
            val documentResponse = gson.fromJson(response, DocumentResponse::class.java)
            println("All File:")
            documentResponse.content.forEach {
                println(it)
            }
            println()

            // Uploads a file
            val userDir = System.getProperty("user.dir")
            val filePath = "$userDir/document_store/src/test/kotlin/resources/Pdf File.pdf"
            val file = File(filePath)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileNameWithTimeStamp = "Pdf File_$timeStamp.pdf"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileNameWithTimeStamp,
                    file.asRequestBody("application/pdf".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val respondeDocumentUploaded = client.newCall(request).execute()
            if (!respondeDocumentUploaded.isSuccessful) {
                throw IOException("Error: $response")
            }

            val documentUploaded = gson.fromJson(respondeDocumentUploaded.body?.string(), Metadata::class.java)

            println("File Uploaded: $documentUploaded")

            // Deletes the document uploaded
            val requestDeleteDocument = Request.Builder()
                .url(url + documentUploaded.id)
                .delete()
                .build()

            client.newCall(requestDeleteDocument).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Error: $response")
                }

                println("File deleted successfully")
            }

            return
        }

        fun funGetAllDetails() {
            val requestGetAllDocuments = Request.Builder().url(url).build()

            val responseGetAllDocuments = client.newCall(requestGetAllDocuments).execute()
            if (!responseGetAllDocuments.isSuccessful) {
                throw IOException("Error: $responseGetAllDocuments")
            }

            val response = responseGetAllDocuments.body?.string()
            val documentResponse = gson.fromJson(response, DocumentResponse::class.java)
            println("All File:")
            documentResponse.content.forEach {
                println(it)
            }
        }

        fun funGetDocumentMetadata(docId: Int) {
            val requestGetDocumentMetadata = Request.Builder().url("http://localhost:8080/API/documents/"+docId).build()

            val responseGetDocumentMetadata = client.newCall(requestGetDocumentMetadata).execute()
            if (!responseGetDocumentMetadata.isSuccessful) {
                throw IOException("Error: $responseGetDocumentMetadata")
            }

            val response = responseGetDocumentMetadata.body?.string()
            val documentResponse = gson.fromJson(response, DocumentResponse::class.java)
            println("File Metadata:")
            println(documentResponse)
        }

        fun funGetDocumentData(docId: Int) {
            val requestGetDocumentMetadata = Request.Builder().url("http://localhost:8080/API/documents/${docId}/data").build()

            val responseGetDocumentMetadata = client.newCall(requestGetDocumentMetadata).execute()
            if (!responseGetDocumentMetadata.isSuccessful) {
                throw IOException("Error: $responseGetDocumentMetadata")
            }

            val response = responseGetDocumentMetadata.body?.string()
            val documentResponse = gson.fromJson(response, DocumentResponse::class.java)

        }

        fun funPost(name: String): String? {
            val userDir = System.getProperty("user.dir")
            val filePath = "$userDir/document_store/src/test/kotlin/resources/${name}"
            val file = File(filePath)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileNameWithTimeStamp = "${file.name}_$timeStamp.pdf"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileNameWithTimeStamp,
                    file.asRequestBody("application/pdf".toMediaType())
                )
                .build()

            val request = Request.Builder().url(url)
                .post(requestBody).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Error: $response")
            }


            return response.body?.string()
        }

        fun funPut(oldID:Int, name: String): String? {
            val userDir = System.getProperty("user.dir")
            val filePath = "$userDir/document_store/src/test/kotlin/resources/${name}"
            val file = File(filePath)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileNameWithTimeStamp = "${file.name}_$timeStamp.pdf"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileNameWithTimeStamp,
                    file.asRequestBody("application/pdf".toMediaType())
                )
                .build()

            val request = Request.Builder().url(url+oldID)
                .put(requestBody).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Error: $response")
            }

            return response.body?.string()
        }

        fun funDeleted(id: Int){
            // Deletes the document uploaded
            val requestDeleteDocument = Request.Builder()
                .url(url + id)
                .delete()
                .build()

            client.newCall(requestDeleteDocument).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Error: $response")
                }

                println("File deleted successfully")
            }
        }

        fun scenario_4() {
            val returnBody = funPost("Pdf File.pdf")
            println(returnBody)
            val jsonResponse = JSONObject(returnBody)
            val newDocumentID = jsonResponse.getString("id")


            println(funGetAllDetails())
            println(funGetDocumentMetadata(newDocumentID.toInt()))
            //println(funGetDocumentData(newDocumentID.toInt()));
            println(funPut(newDocumentID.toInt(), "LAB1-README.pdf"))
            println(funGetAllDetails())

        }

        fun scenario_2(){
            val returnBody = funPost("Pdf File.pdf")
            println(returnBody)
            val jsonResponse = JSONObject(returnBody)
            val newDocumentID = jsonResponse.getString("id")

            println(funGetDocumentMetadata(newDocumentID.toInt()))

            println(funDeleted(newDocumentID.toInt()))

        }
    }

}