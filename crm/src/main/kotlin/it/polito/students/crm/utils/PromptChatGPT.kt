package it.polito.students.crm.utils

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.owasp.esapi.ESAPI

fun generatePromptChatGPTJobOffer(descriptionUser: String): String {
    val sanitizedDescription = sanitizeAndValidateInput(descriptionUser)

    return ("Given the following job description from the user, generate the corresponding job offer fields. Ensure that each field strictly adheres to the character limits specified. Do not exceed the maximum length for any field, and truncate the text if necessary to fit within the limits." +
            "    Input from the user: $sanitizedDescription" +
            "    Populate the fields for the following job offer DTO:" +
            "    name: The title or name of the job position based on the user's description. (max 255 characters)" +
            "    description: A summary of the job, including responsibilities and requirements. (max 255 characters)" +
            "    contractType: The type of contract (Full Time, Part Time, Contract, Freelance) based on the user's input." +
            "    location: The geographical location of the job. (max 255 characters)" +
            "    workMode: The working mode (Remote, Hybrid, In-Person)." +
            "    requiredSkills: A list of the skills or competencies necessary for the job based on the user's input." +
            "    duration: The length of the job in days. (only number > 0)" +
            "    note: Additional comments or remarks about the job. (max 255 characters)" +
            "    Ensure all fields comply with the maximum character limits, and return only the values for these fields in JSON format.")
}

fun generatePromptChatGPTSkills(descriptionUser: String): String {
    val sanitizedDescription = sanitizeAndValidateInput(descriptionUser)

    return ("Given the following job description from the user, identify and generate a list of relevant skills required for this job. " +
            "   The skills should be based on the details provided by the user, including specific job requirements, responsibilities, " +
            "   and qualifications. Ensure each skill is concise and does not exceed 50 characters in length. " +
            "   Return only the list of skills as a JSON array of strings. " +
            "   The list should contain no more than 50 skills. " +
            "   User Description: $sanitizedDescription." +
            "   Return format: [\"Skill 1\", \"Skill 2\", \"Skill 3\", ...] (max 50 skills, each skill <= 50 characters)")
}

fun sanitizeAndValidateInput(descriptionUser: String): String {
    // Step 1: Sanitizzazione base con OWASP ESAPI o JSoup
    var sanitizedDescription = Jsoup.clean(descriptionUser, Safelist.basic())

    // Step 2: Rimozione caratteri potenzialmente pericolosi
    sanitizedDescription = sanitizedDescription
        .replace("\\", "")
        .replace("\n", " ")
        .replace("\t", " ")
        .replace(Regex("[^\\p{Print}]"), "")

    // Step 3: Limita la lunghezza del testo
    if (sanitizedDescription.length > 500) {
        sanitizedDescription = sanitizedDescription.substring(0, 500)
    }

    // Step 4: Protezione da SQL Injection e XSS con ESAPI
    sanitizedDescription = ESAPI.encoder().encodeForHTML(sanitizedDescription)

    // Step 5: Verifica la presenza di parole chiave dannose
    val dangerousKeywords = listOf(
        "DROP",
        "DELETE",
        "INSERT INTO",
        "SCRIPT",
        "EXEC",
        "EVAL",
        "ALERT",
        "SELECT",
        "UPDATE",
        "chmod",
        "rm -rf"
    )
    dangerousKeywords.forEach { word ->
        if (sanitizedDescription.contains(word, ignoreCase = true)) {
            throw IllegalArgumentException("The description contains potentially dangerous content: $word")
        }
    }

    return sanitizedDescription
}
