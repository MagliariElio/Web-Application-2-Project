package it.polito.students.document_store.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.tomcat.util.http.fileupload.FileUploadException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor

class ContentTypeInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val contentType = request.contentType

        if (request.method == HttpMethod.POST.name() || request.method == HttpMethod.PUT.name()) {
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                response.status = HttpStatus.BAD_REQUEST.value()
                response.contentType = "application/json"
                response.writer.write("The content type is not recognized!")
                return false
            }
        }

        return true
    }
}