package it.polito.students.crm.entities

import it.polito.students.crm.utils.JobStatusEnum
import jakarta.persistence.*
import java.time.LocalDateTime

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

    var creationTime: LocalDateTime = LocalDateTime.now()
    var endTime: LocalDateTime? = null                      // in caso di abort, done o eliminazione

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
    var candidateProfessionals: MutableList<Professional> =
        mutableListOf() // lista dei candidati che sono stati selezionati nella fase di selezione

    @ElementCollection
    var candidatesProposalProfessional: MutableList<Long> =
        mutableListOf() // lista dei candidati che sono stati selezionati nella fase di candidate proposal

    @ElementCollection
    var candidatesProfessionalRejected: MutableList<Long> =
        mutableListOf() // lista dei candidati che hanno rifiutato la candidatura

    @ElementCollection
    var candidatesProfessionalRevoked: MutableList<Long> =
        mutableListOf() // lista dei candidati che hanno rifiutato la candidatura dopo aver accettato

    var deleted = false
}