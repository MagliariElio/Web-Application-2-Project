package it.polito.students.crm.entities

import it.polito.students.crm.utils.EmploymentStateEnum
import jakarta.persistence.*

@Entity
class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @OneToOne(cascade = [(CascadeType.MERGE)], fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_id", referencedColumnName = "id")
    lateinit var information: Contact

    @ElementCollection
    lateinit var skills: List<String>
    lateinit var employmentState: EmploymentStateEnum
    lateinit var geographicalLocation: String
    var dailyRate: Double = 0.0
    @ElementCollection
    var attachmentsList: List<Long> = emptyList()

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "professional_job_offer",
        joinColumns = [JoinColumn(name = "professional_id")],
        inverseJoinColumns = [JoinColumn(name = "job_offer_id")]
    )
    var jobOffers: MutableSet<JobOffer> = mutableSetOf()

    var deleted = false

    fun addJobOffers(jobOffers: Collection<JobOffer>) {
        this.jobOffers.addAll(jobOffers)
    }
}