package it.polito.students.crm.utils

enum class JobStatusEnum {
    CREATED,
    SELECTION_PHASE,
    CANDIDATE_PROPOSAL,
    CONSOLIDATED,
    DONE,
    ABORT
}

fun checkStatusTransition(oldStatus: JobStatusEnum, newStatus: JobStatusEnum): Boolean {

    return when (oldStatus) {
        JobStatusEnum.CREATED -> arrayOf(JobStatusEnum.SELECTION_PHASE, JobStatusEnum.ABORT).contains(newStatus)
        JobStatusEnum.SELECTION_PHASE -> arrayOf(JobStatusEnum.CANDIDATE_PROPOSAL, JobStatusEnum.ABORT).contains(
            newStatus
        )

        JobStatusEnum.CANDIDATE_PROPOSAL -> arrayOf(
            JobStatusEnum.SELECTION_PHASE,
            JobStatusEnum.CONSOLIDATED,
            JobStatusEnum.ABORT
        ).contains(newStatus)

        JobStatusEnum.CONSOLIDATED -> arrayOf(
            JobStatusEnum.SELECTION_PHASE,
            JobStatusEnum.DONE,
            JobStatusEnum.ABORT
        ).contains(newStatus)

        JobStatusEnum.DONE -> JobStatusEnum.SELECTION_PHASE == newStatus
        JobStatusEnum.ABORT -> false
    }

}

fun statusRequiresProfessionalId(status: JobStatusEnum): Boolean {
    return when (status) {
        JobStatusEnum.CREATED -> false
        JobStatusEnum.SELECTION_PHASE -> true
        JobStatusEnum.CANDIDATE_PROPOSAL -> false
        JobStatusEnum.CONSOLIDATED -> true
        JobStatusEnum.DONE -> true
        JobStatusEnum.ABORT -> false
    }
}