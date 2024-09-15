package it.polito.students.crm.utils

fun generatePromptChatGPTJobOffer(descriptionUser: String): String {
    val sanitizedDescription = descriptionUser
        .replace("\"", "")

    return ("Given the following job description from the user, generate the corresponding job offer fields. Take into account the information provided by the user and match it to the specific fields required for the DTO:" +
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
            "    Return only the values for these fields in English and in a JSON format.")
}

fun generatePromptChatGPTSkills(descriptionUser: String): String {
    val sanitizedDescription = descriptionUser
        .replace("\"", "")

    return ("Given the following job description from the user, identify and generate a list of relevant skills required for this job. \n" +
            "The skills should be based on the details provided by the user, including specific job requirements, responsibilities, \n" +
            "and qualifications. Return only the list of skills as a JSON array of strings.\n" +
            "\n" +
            "User Job Description: $sanitizedDescription\n" +
            "\n" +
            "Return format:\n" +
            "[\n" +
            "  'Skill 1',\n" +
            "  'Skill 2',\n" +
            "  'Skill 3',\n" +
            "  ...\n" +
            "]\n")
}