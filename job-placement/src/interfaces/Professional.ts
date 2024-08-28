import { Contact } from "./Contact";
import { ContactWithAssociatedData } from "./ContactWithAssociatedData";
import { JobOffer } from "./JobOffer";

export interface Professional{
    id: number,
    information: Contact,
    skills: string[],
    employmentState: 'EMPLOYED' | 'UNEMPLOYED' | 'AVAILABLE_FOR_WORK' | 'NOT_AVAILABLE';
    geographicalLocation: string,
    dailyRate: number,
}