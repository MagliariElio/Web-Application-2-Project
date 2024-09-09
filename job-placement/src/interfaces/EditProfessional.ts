export interface EditProfessional {
  id: number;
  information: {
    id: number;
    name: string;
    surname: string;
    ssnCode: string;
    comment: string;
    category: string;
    emails: string[];
    telephones: string[];
    addresses: string[];
  };
  skills: string[];
  employmentState: string;
  geographicalLocation: string;
  dailyRate: string;
}
