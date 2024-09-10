import { CreateAddress } from "./Address";
import { CreateEmail } from "./Email";
import { CreateTelephone } from "./Telephone";

export interface CreateProfessional{
    information: {
        name: string,
        surname: string,
        ssnCode: string,
        comment: string,
        category: string,
        emails: CreateEmail[],
        telephones: CreateTelephone[],
        addresses: CreateAddress[],
    },
    skills: string[],
    geographicalLocation: string,
    dailyRate: string,
}