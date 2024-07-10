package it.polito.students.crm.entities

import jakarta.persistence.*

@Entity
class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @OneToOne(cascade = [(CascadeType.MERGE)], fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id", referencedColumnName = "id")
    lateinit var information: Contact

    @OneToMany(mappedBy = "customer", cascade = [(CascadeType.PERSIST)], fetch = FetchType.EAGER)
    var joboffers: MutableSet<JobOffer> = mutableSetOf()

    var deleted = false
}




