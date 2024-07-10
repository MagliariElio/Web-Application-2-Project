package it.polito.students.crm.entities

import it.polito.students.crm.utils.PriorityEnumOptions
import it.polito.students.crm.utils.StateOptions
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    lateinit var date: LocalDateTime
    lateinit var subject: String
    lateinit var body: String
    lateinit var actualState: StateOptions
    lateinit var priority: PriorityEnumOptions
    lateinit var channel: String
    lateinit var sender: String

    @OneToMany(mappedBy = "message", cascade = [(CascadeType.PERSIST)])
    val history: MutableSet<History> = mutableSetOf()

    // Add in a set of history a message and map the other side relationship
    fun addHistory(h: History) {
        h.message = this
        this.history.add(h)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (date != other.date) return false
        if (subject != other.subject) return false
        if (body != other.body) return false
        if (actualState != other.actualState) return false
        if (priority != other.priority) return false
        if (channel != other.channel) return false
        if (sender != other.sender) return false
        if (history != other.history) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + actualState.hashCode()
        result = 31 * result + priority.hashCode()
        result = 31 * result + channel.hashCode()
        result = 31 * result + sender.hashCode()
        return result
    }
}