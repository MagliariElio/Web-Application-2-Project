package it.polito.students.dtos

import org.springframework.web.multipart.MultipartFile

data class SendEmailRequestDTO(
    var destEmailAddr: String,
    var subject: String,
    var textBody: String,
    var multipartFile: MultipartFile?
)