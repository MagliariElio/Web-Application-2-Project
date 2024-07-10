package it.polito.students.document_store.dtos

import it.polito.students.document_store.entities.Metadata
import java.time.LocalDateTime

data class MetadataDTO (
    val id: Int,
    var name: String,
    var size: Long,
    var contentType: String,
    var creationTimestamp: LocalDateTime,
    var modifiedTimestamp: LocalDateTime,
    var document: DocumentDTO
)

fun Metadata.toDTO(): MetadataDTO = MetadataDTO(
    this.id,
    //toRealName(this.name),
    this.name,
    this.size,
    this.contentType,
    this.creationTimestamp,
    this.modifiedTimestamp,
    this.document.toDTO()
)

fun MetadataDTO.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "size" to size,
        "contentType" to contentType,
        "creationTimestamp" to creationTimestamp,
        "modifiedTimestamp" to modifiedTimestamp,
    )
}

fun MetadataDTO.toDocument(): DocumentDTO{
    return this.document
}


/*
fun toRealName(dirtyName: String): String{
    val pattern = "_-__"
    val cleanName = dirtyName.substringBefore(pattern)
    return cleanName
}

fun Metadata.toRealNameDTO(): MetadataDTO = MetadataDTO(
        this.id,
        toRealName(this.name),
        this.size,
        this.contentType,
        this.creationTimestamp,
        this.modifiedTimestamp,
        this.document.toDTO()
)*/

