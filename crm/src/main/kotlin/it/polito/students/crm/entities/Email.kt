package it.polito.students.crm.entities

import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @field: Email(message = "An Email has to be valid email address")
    lateinit var email: String
    lateinit var comment: String

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "email_contact",
        joinColumns = [JoinColumn(name = "email_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id")]
    )
    val contacts: MutableSet<Contact> = mutableSetOf()

    fun addContact(contact: Contact) {
        contacts.add(contact)
        contact.emails.add(this)
    }

    fun removeContact(contact: Contact) {
        contacts.remove(contact)
        contact.emails.remove(this)
    }
}