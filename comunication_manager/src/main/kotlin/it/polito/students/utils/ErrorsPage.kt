package it.polito.students.utils

class ErrorsPage {
    companion object {

        /**
         * CONTROLLER
         */

        const val BLANK_DEST_ADDRESS_ERROR =
            "Destination address can not be blank! Please provide correct string for recipient's address"

        const val INCORRECT_EMAIL_PARAMETER = "Wrong email address! Please provide a valid one (e.g. example@mail.com)"

        const val FAILED_TO_SEND_EMAIL_TO_CRM_SERVICE = "Failed to send message to CRM service after 3 attempts!"

        const val INTERNAL_SERVER_ERROR = "Internal server error!"

    }
}