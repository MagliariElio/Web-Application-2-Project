package it.polito.students.crm.services

import it.polito.students.crm.dtos.CreateJobOfferDTO
import it.polito.students.crm.dtos.JobOfferDTO
import it.polito.students.crm.utils.JobStatusEnum
import it.polito.students.crm.utils.JobStatusGroupEnum
import org.springframework.data.domain.PageImpl

interface JobOfferService {

    /**
     * Retrieve some job offer of the system (paged).
     *
     * @param page The page number used for paging.
     * @param limit The number of Job offers per page.
     * @param customerId The Id of the customer related to the Job offer used for filtering. It can be null.
     * @param professionalId The Id of the professional related to the Job offer used for filtering. It can be null.
     * @param jobStatusGroup The status group used for filtering. It can be null.
     * @param sortBy Sort by duration or value fields. It can be null.
     * @param sortDirection Sort direction, 'asc' or 'desc'. It can be null.
     * @param contractType The contract type used for filtering. It can be null.
     * @param location The location of the Job offer used for filtering. It can be null.
     * @param workMode The work mode of the Job offer (e.g., remote, on-site) used for filtering. It can be null.
     * @param status The status of the Job offer (e.g., CREATED, SELECTION_PHASE, DONE) used for filtering. It can be null.
     * @return A PageImpl of JobOfferDTO containing the filtered and sorted list of job offers.
     */
    fun getAllJobOffers(
        page: Int,
        limit: Int,
        customerId: Long?,
        professionalId: Long?,
        jobStatusGroup: JobStatusGroupEnum?,
        sortBy: String?,
        sortDirection: String?,
        contractType: String?,
        location: String?,
        workMode: String?,
        status: JobStatusEnum?
    ): PageImpl<JobOfferDTO>

    /**
     * Stores a new job offer in the system.
     *
     * @param jobOfferDto The DTO containing information about the job offer to be stored.
     * @return The DTO representing the newly created job offer.
     * @throws CustomerNotFoundException if the customer specified in the job offer DTO is not found.
     */
    fun storeJobOffer(jobOfferDto: CreateJobOfferDTO): JobOfferDTO

    /**
     * Update an existent job offer in the system.
     *
     * @param jobOfferDto The DTO containing information about the job offer to be stored.
     * @return The DTO representing the updated job offer.
     * @throws CustomerNotFoundException if the customer specified in the job offer DTO is not found.
     */
    fun updateJobOffer(jobOfferDto: JobOfferDTO): JobOfferDTO

    /**
     * Delete a job offer in the system.
     *
     * @param jobOfferId The id of the joboffer to be deleted.
     * @return Nothing.
     * @throws NoSuchElementException if the joboffer is not present in the db
     */
    fun deleteJobOffer(jobOfferId: Long)

    /**
     * Change a job offer status in the system.
     *
     * @param jobOfferId The id of the joboffer to be changed.
     * @param nextStatus The next status to be set in the jobOffer.
     * @param professionalId The id of the professional to be deleted.
     * @param note The new note to be assigned to the job offer possibly.
     * @return The edited version of the job offer
     * @throws NoSuchElementException if the joboffer is not present in the db
     * @throws IllegalJobStatusTransition if the new status cannot be reached from the current status
     * @throws RequiredProfessionalIdException if the new status requires a professionalId not provided
     * @throws NoSuchElementException if the professionalId is not present in the db
     */
    fun changeJobOfferStatus(
        jobOfferId: Long,
        nextStatus: JobStatusEnum,
        professionalsId: List<Long>?,
        note: String?
    ): JobOfferDTO

    /**
     * Retrieve a value of job offer with its jobOfferId.
     *
     * @param jobOfferId The id of the jobOffer related to the Job offer used for filtering. It can be null.
     * @return Value of related jobOfferId it is retrieved only if job offer is bound to a professional
     */
    fun getJobOfferById(jobOfferId: Long): JobOfferDTO?
}