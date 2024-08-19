import { Address } from "./Address";
import { Contact } from "./Contact";
import { Email } from "./Email";
import { Telephone } from "./Telephone";

export interface ContactWithAssociatedData{
    contactDTO: Contact
    emails: Email[]
    telephones: Telephone[]
    addresses: Address[]
}