package it.polito.students.crm.entities

import it.polito.students.crm.utils.JobStatusEnum
import jakarta.persistence.*

@Entity
class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    lateinit var name: String

    lateinit var description: String

    lateinit var contractType: String

    lateinit var location: String

    lateinit var workMode: String

    var oldStatus: JobStatusEnum = JobStatusEnum.CREATED    // traccia lo stato precedente a status
    var status: JobStatusEnum = JobStatusEnum.CREATED

    @ElementCollection(fetch = FetchType.EAGER)
    lateinit var requiredSkills: List<String>

    var duration: Long = 1

    @Column(name = "offer_value") // specify custom column name
    var value: Double = 0.0
    lateinit var note: String

    @ManyToOne
    lateinit var customer: Customer

    @ManyToOne
    var professional: Professional? = null

    @ManyToMany(mappedBy = "jobOffers", fetch = FetchType.EAGER)
    var candidateProfessionals: MutableList<Professional> = mutableListOf()

    @ElementCollection
    var candidatesProfessionalRefused: MutableList<Long>  = mutableListOf() // lista dei candidati che hanno rifiutato

    var deleted = false
}