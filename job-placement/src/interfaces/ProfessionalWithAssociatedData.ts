import { JobOffer } from "./JobOffer";
import { Professional } from "./Professional";

export interface ProfessionalWithAssociatedData{
    professionalDTO: Professional,
    jobofferDTOS: JobOffer[],
}