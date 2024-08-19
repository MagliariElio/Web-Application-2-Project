import { ContactWithAssociatedData } from "./ContactWithAssociatedData";
import { JobOffer } from "./JobOffer";

export interface Customer{
    id: number,
    information: ContactWithAssociatedData,
    jobOffers: JobOffer[]
}