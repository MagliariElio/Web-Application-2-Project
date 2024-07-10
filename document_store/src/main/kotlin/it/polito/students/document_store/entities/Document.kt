package it.polito.students.document_store.entities

import jakarta.persistence.*

@Entity
class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Int = 0

    //@Lob
    lateinit var content: ByteArray

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var metadata: Metadata? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Document

        if (id != other.id) return false
        if (!content.contentEquals(other.content)) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + content.contentHashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }
}