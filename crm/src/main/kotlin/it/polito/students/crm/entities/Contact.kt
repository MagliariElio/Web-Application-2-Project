package it.polito.students.crm.entities

import it.polito.students.crm.utils.CategoryOptions
import jakarta.persistence.*

@Entity
class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    lateinit var name: String
    lateinit var surname: String
    lateinit var ssnCode: String
    lateinit var category: CategoryOptions
    lateinit var comment: String

    @ManyToMany(mappedBy = "contacts", cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var emails = mutableSetOf<Email>()

    fun addEmail(email: Email) {
        emails.add(email)
        email.contacts.add(this)
    }

    fun removeEmail(email: Email) {
        emails.remove(email)
        email.contacts.remove(this)
    }

    @ManyToMany(mappedBy = "contacts", cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var addresses = mutableSetOf<Address>()

    fun addAddress(address: Address) {
        addresses.add(address)
        address.contacts.add(this)
    }

    fun removeAddress(address: Address) {
        addresses.remove(address)
        address.contacts.remove(this)
    }

    @ManyToMany(mappedBy = "contacts", cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
    var telephones = mutableSetOf<Telephone>()

    fun addTelephone(telephone: Telephone) {
        telephones.add(telephone)
        telephone.contacts.add(this)
    }

    fun removeTelephone(telephone: Telephone) {
        telephones.remove(telephone)
        telephone.contacts.remove(this)
    }
}