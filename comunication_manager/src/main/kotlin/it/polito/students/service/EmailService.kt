package it.polito.students.service

import java.io.File

interface SendEmailService {

    /**
     * Send an email
     *
     * @param destEmailAddr
     * @param subject
     * @param textBody
     * @return DTO of the sentEmail
     */
    fun sendEmail(
        destEmailAddr: String,
        subject: String,
        textBody: String,
        file: File?
    ): com.google.api.services.gmail.model.Message

}