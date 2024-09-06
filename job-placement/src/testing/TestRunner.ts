import { createCustomer } from "../apis/CustomerRequests";
import { submitJobOffer } from "../apis/JobOfferRequests";
import { createProfessional } from "../apis/ProfessionalRequests";
import { CreateCustomer } from "../interfaces/CreateCustomer";
import { MeInterface } from "../interfaces/MeInterface";
import { testCustomers } from "./CustomerTests";
import { testJobOffers } from "./JobOfferTests";
import { testProfessionals } from "./ProfessionalsTest";

export const runCustomerTests = async (me: MeInterface) => {
  for (const customer of testCustomers) {
    const c: CreateCustomer = {
      name: customer.name,
      surname: customer.surname,
      ssnCode: customer.ssnCode,
      comment: customer.comment,
      category: customer.category,

      emails: customer.emailDTOs.map((emailDTO) => ({
        email: emailDTO.email,
        comment: emailDTO.comment,
      })),

      telephones: customer.telephoneDTOs.map((telephoneDTO) => ({
        telephone: telephoneDTO.telephone,
        comment: telephoneDTO.comment,
      })),

      addresses: customer.addressDTOs.map((addressDTO) => ({
        state: addressDTO.state,
        region: addressDTO.region,
        city: addressDTO.city,
        address: addressDTO.address,
        comment: addressDTO.comment,
      })),
    };

    try {
      await createCustomer(c, me);
      console.log(`Customer ${customer.name} ${customer.surname} created successfully.`);
    } catch (error) {
      console.error(`Failed to create customer ${customer.name} ${customer.surname}:`, error);
    }
  }
};

export const runProfessionalTests = async (me: MeInterface) => {
  for (const professional of testProfessionals) {
    await createProfessional(professional, me);
  }
};

export const runJobOfferTests = async (me: MeInterface) => {
  for (const jobOffer of testJobOffers) {
    await submitJobOffer(jobOffer, me.xsrfToken);
  }
};
