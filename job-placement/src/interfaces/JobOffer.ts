export interface JobOffer{
    id: number,
    status: string,
    requiredSkills: string[],
    duration: number,
    value: number,
    note: string,
    customerId: number
    professionalId: number
    candidateProfessionalIds: number[]
}