package it.polito.students.crm.utils

class KafkaTopics {
    companion object {
        const val TOPIC_MESSAGE = "message_crm_to_analytics"
        const val TOPIC_COMPLETED_MESSAGE = "completed_message_crm_to_analytics"
        const val TOPIC_JOB_OFFER = "job_offer_crm_to_analytics"
        const val TOPIC_COMPLETED_JOB_OFFER = "completed_job_offer_crm_to_analytics"
    }
}