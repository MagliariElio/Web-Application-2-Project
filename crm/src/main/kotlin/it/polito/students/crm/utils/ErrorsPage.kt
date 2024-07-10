package it.polito.students.crm.utils

class ErrorsPage {
    companion object {
        /**
         * CONTROLLER
         */
        const val MESSAGE_ERROR = "An error occurred while processing the request."

        const val DAILYRATE_ERROR = "Daily rate must be greather or equals to 0"

        const val SKILLS_ERROR = "The skills list can't contain an empty skill"

        const val SSN_CODE_ERROR = "The ssnCode can't be blank"

        const val SKILLS_LIST_ERROR = "The skills list can't be empty"

        const val GEOGRAPHICAL_LOCATION_ERROR = "Geographical location can't be blank"

        const val PAGE_AND_LIMIT_ERROR =
            "The provided values for Page and/or Limit are not valid. Please ensure that you provide positive integers for both Page and Limit parameters."

        const val MESSAGE_ID_ERROR =
            "The provided value for MessageId is not valid. Please ensure that you provide a positive integer for the MessageId parameter."

        val ERROR_MESSAGE_ACTUAL_STATE_NOT_FOUND =
            "The specified state was not found. Please ensure that the state provided in the request body matches one of the following options: ${
                StateOptions.entries.map { it.name }
            }."

        val ERROR_MESSAGE_PRIORITY_NOT_FOUND =
            "The specified priority was not found. Please ensure that the priority provided in the request body matches one of the following options: ${
                PriorityEnumOptions.entries.map { it.name }
            }."

        const val ERROR_MESSAGE_UPDATE_MESSAGE_BAD_REQUEST =
            "No valid fields were specified in the request body. Please include either 'actualState' or 'priority' fields to update the message."

        const val GENERAL_ERROR_MESSAGE_UPDATE_MESSAGE_REQUEST =
            "An error occurred while attempting to update the message"

        const val SORT_BY_AND_STATE_REQUEST_PARAMETERS_ERROR =
            "Wrong query parameters. See documentation to use proper options values."

        const val SENDER_ERROR =
            "Sender must be a valid telephone number [+39 3321335437, 3321335437, +1 (222) 123-1234] or a valid email address [name@example.com]"

        const val PRIORITY_ERROR = "Field priority has to be [HIGH, MEDIUM_HIGH, MEDIUM, MEDIUM_LOW, LOW]"

        const val CONTACT_ID_ERROR = "ContactId value is not valid. Please provide a positive integer!"

        const val PROFESSIONAL_ID_ERROR = "ProfessionalId value is not valid. Please provide a positive integer!"

        const val CONTACT_ID_AND_DETAIL_ID_ERROR = "Contact id or detail id not valid. Provide a positive integer!"

        val CONTACT_ERROR = "Contact type should be ${WhatContactOptions.entries.map { it.name }}!"

        val CATEGORY_ERROR = "Category has to be ${CategoryOptions.entries.map { it.name }}!"

        val CATEGORY_PROFESSIONAL_ERROR = "Category has to be ${CategoryOptions.PROFESSIONAL}!"

        const val NAME_SURNAME_ERROR = "Name and Surname are mandatory!"

        val ERROR_MESSAGE_CATEGORY =
            "The specified category was not found. Please ensure that the category provided in the request body matches one of the following options: ${
                CategoryOptions.entries.map { it.name }
            }."

        const val EMAILS_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Email has to be valid email format" +
                    " and emails has to be a list of emails!"

        const val TELEPHONES_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Telephone has to be valid telephone format" +
                    "and telephones has to be a list of telephones!"

        const val ADDRESSES_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Address has to be valid [needed: name and surname] " +
                    "and addresses has to be a list of addresses!"

        const val REQUESTED_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Needed list of emails, telephones or addresses" +
                    " [emails: {email:address@mail.co, comment:comment}] or [telephones: {telephone:121212121, comment:comment} ]!"

        const val EMAIL_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Email has to be valid email format!"

        const val TELEPHONE_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Telephone has to be valid telephone format!"

        const val ADDRESS_BAD_FORMATTED =
            "Body has not been properly formatted! Details: Address has to be valid [needed: name and surname]!"

        const val EMAILS_NOT_VALID = "Email has to be a valid format! Example: [name@example.com]"

        val EMPLYMENT_STATE_ERROR = "Employment state has to be ${EmploymentStateEnum.entries.map { it.name }}!"

        const val TELEPHONES_NOT_VALID =
            "Telephones has to be a valid format! Example: [+39 3321335437, 3321335437, +1 (222) 123-1234]"

        const val ADDRESSES_NOT_VALID =
            "A valid Address has at least one address field among [state, region, city, address] is not blank!"

        const val ID_ERROR = "Id value is not valid. Please provide a positive integer!"

        const val SUBJECT_MISSING_ERROR = "Subject cannot be empty!"

        const val PRIORITY_MISSING_ERROR = "Priority cannot be empty!"

        const val CHANNEL_MISSING_ERROR = "Channel cannot be empty!"

        const val BODY_MISSING_ERROR = "Body cannot be empty!"

        const val SENDER_MISSING_ERROR = "Sender cannot be empty!"

        const val CATEGORY_MISSING_ERROR = "Category is mandatory!"

        const val CUSTOMERID_PROFESSIONALID_INVALID =
            "Customer id and/or professional id not valid. Please, provide some positive integers!"

        const val JOBSTATUSGROUP_INVALID =
            "The value provided for the jobStatusGroup field is invalid. Possible values: [OPEN, ACCEPTED, ABORTED]."

        const val JOBOFFERSTATUS_INVALID =
            "The value provided for the job status field is invalid. Possible values: [CREATED, SELECTION_PHASE, CANDIDATE_PROPOSAL, CONSOLIDATED, DONE, ABORT]"

        const val CUSTOMER_ID_ERROR =
            "The provided value for customerId is not valid. Please ensure that you provide a positive integer for the customerId parameter."

        const val CUSTOMER_ID_CONTACT_ID_ERROR =
            "The provided value for customerID or contactID is not valid. Please ensure that you provide a positive integer for parameters."

        const val CONTACT_ID_NOT_SPECIFIED_ERROR =
            "Unable to update customer. It is necessary to specify a contact id in the body request."

        const val PAGE_NUMBER_CANNOT_BE_NEGATIVE_ERROR = "The page number and the page size cannot be negative!"


        const val DELETED_SUCCESSFULLY = "Customer deleted correctly!"

        const val GENERAL_ERROR_MESSAGE_UPDATE_CUSTOMER_REQUEST =
            "An error occurred while attempting to update the customer"

        const val INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error occurred."

        const val NO_SUCH_JOBOFFER = "No such job offer in the system!"

        const val INVALID_STATUS_TRANSITION = "Cannot perform this status transition, illegal transition!"

        const val REQUIRED_PROFESSIONAL_ID =
            "In order to perform this status transition, the professionalId is mandatory"

        const val JOB_OFFER_ID_ERROR = "JobOffer id not valid! Please provide a valid positive integer!"

        const val REQUIRED_SKILLS_EMPTY_ERROR =
            "The 'required skills' list cannot contain skill entries with empty strings. Please ensure all skills are properly named."

        const val NEGATIVE_DURATION_ERROR =
            "The 'duration' field cannot be negative. Please ensure the duration is a positive value."

        /**
         * SERVICE
         */

        const val ADDRESS_REQUIRED_ERROR = "A list of addresses it is required!"

        const val MAIL_REQUIRED_ERROR = "List empty at least one email contact it's needed!"

        const val INVALID_STATE_TRANSITION_MESSAGE =
            "Invalid state transition: The requested transition is not allowed in the current state."

        const val TELEPHONE_REQUIRED_ERROR = "List empty at least one Telephone contact it's needed!"

        const val INVALID_UPDATE_CUSTOMER =
            "Unable to update contact. The requested contact's category is not a CUSTOMER."

        const val PROFESSIONAL_DELETED_SUCCESSFULLY = "Professional deleted correctly!"

        const val JOB_OFFER_NOT_FOUND_ERROR = "JobOffer id not found!"

    }
}