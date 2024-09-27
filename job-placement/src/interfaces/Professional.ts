import { Contact } from "./Contact";

export interface Professional{
    id: number,
    information: Contact,
    skills: string[],
    employmentState: 'EMPLOYED' | 'UNEMPLOYED' | 'AVAILABLE_FOR_WORK' | 'NOT_AVAILABLE';
    geographicalLocation: string,
    dailyRate: string,
    attachmentsList: number[],
}