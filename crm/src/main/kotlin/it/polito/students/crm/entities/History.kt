package it.polito.students.crm.entities

import it.polito.students.crm.utils.StateOptions
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class History {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    lateinit var state: StateOptions
    lateinit var date: LocalDateTime
    lateinit var comment: String

    @ManyToOne
    lateinit var message: Message

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as History

        if (id != other.id) return false
        if (state != other.state) return false
        if (date != other.date) return false
        if (comment != other.comment) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + comment.hashCode()
        return result
    }
}


