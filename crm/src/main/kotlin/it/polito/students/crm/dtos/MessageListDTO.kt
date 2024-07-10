package it.polito.students.crm.dtos

data class MessageListDTO(
    var content: List<MessageDTO>,
    var currentPage: Int,
    var elementsPerPage: Int,
    var totalPages: Int,
    var totalElements: Long
)