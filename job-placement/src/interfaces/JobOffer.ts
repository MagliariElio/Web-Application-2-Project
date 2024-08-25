export interface JobOffer{
    id: number,
    name: string,
    description: string,
    contractType: string,
    location: string,
    workMode: string,
    status: string,
    requiredSkills: string[],
    duration: number,
    value: number,
    note: string,
    customerId: number
    professionalId: number
    candidateProfessionalIds: number[]
}