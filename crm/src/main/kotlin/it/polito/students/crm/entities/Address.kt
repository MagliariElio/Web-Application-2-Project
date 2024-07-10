package it.polito.students.crm.entities

import jakarta.persistence.*

@Entity
class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    lateinit var state: String
    lateinit var region: String
    lateinit var city: String
    lateinit var address: String
    lateinit var comment: String

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "address_contact",
        joinColumns = [JoinColumn(name = "address_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id")]
    )
    val contacts: MutableSet<Contact> = mutableSetOf()

    fun addContact(contact: Contact) {
        contacts.add(contact)
        contact.addresses.add(this)
    }

    fun removeContact(contact: Contact) {
        contacts.remove(contact)
        contact.addresses.remove(this)
    }
}