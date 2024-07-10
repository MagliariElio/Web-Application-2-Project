package it.polito.students.document_store.entities

import jakarta.persistence.*
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Entity
class Metadata {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Int = 0

    @Column(unique = true)
    lateinit var name: String
    var size: Long = 0
    lateinit var contentType: String
    lateinit var creationTimestamp: LocalDateTime
    lateinit var modifiedTimestamp: LocalDateTime

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    lateinit var document: Document

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metadata

        if (id != other.id) return false
        if (name != other.name) return false
        if (size != other.size) return false
        if (contentType != other.contentType) return false
        if (creationTimestamp != other.creationTimestamp) return false
        if (modifiedTimestamp != other.modifiedTimestamp) return false
        if (document != other.document) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + creationTimestamp.hashCode()
        result = 31 * result + modifiedTimestamp.hashCode()
        result = 31 * result + document.hashCode()
        return result
    }
}