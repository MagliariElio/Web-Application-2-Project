package it.polito.students.crm.entities

import jakarta.persistence.*
import jakarta.validation.constraints.Pattern

@Entity
class Telephone {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @field:Pattern(
        regexp = "^(\\+\\d{1,2}\\s?)?1?-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$",
        message = "Telephone number has to contain [+39 3321335437, 3321335437, +1 (222) 123-1234] "
    )
    lateinit var telephone: String
    lateinit var comment: String

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "telephone_contact",
        joinColumns = [JoinColumn(name = "telephone_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id")]
    )
    val contacts: MutableSet<Contact> = mutableSetOf()

    fun addContact(contact: Contact) {
        contacts.add(contact)
        contact.telephones.add(this)
    }

    fun removeContact(contact: Contact) {
        contacts.remove(contact)
        contact.telephones.remove(this)
    }
}