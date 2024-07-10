package it.polito.students.document_store.dtos

import it.polito.students.document_store.entities.Document
import it.polito.students.document_store.entities.Metadata

data class DocumentDTO(
    var id: Int,
    var content: ByteArray,
)

fun Document.toDTO(): DocumentDTO = DocumentDTO(this.id, this.content)