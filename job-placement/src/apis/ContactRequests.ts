import { Address, CreateAddress } from "../interfaces/Address";
import { CreateEmail, Email } from "../interfaces/Email";
import { MeInterface } from "../interfaces/MeInterface";
import { CreateTelephone, Telephone } from "../interfaces/Telephone";

export const fetchAllContactWhatContact = async (whatContact: string): Promise<Email[] | Telephone[] | Address[]> => {
  try {

    const response = await fetch(`/crmService/v1/API/contacts/whatContact/${whatContact}`);
    if (!response.ok) {
      const errorMessage = `GET /API/contacts/whatContact/${whatContact} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching contacts:", error);
    throw error;
  }
}

export const postNewWhatContact = async (whatContact: string, newWhatContact: any, me: MeInterface): Promise<any> => {
  try {
    const response = await fetch(`/crmService/v1/API/contacts/whatContact/${whatContact}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-Token': me.xsrfToken
      },
      body: JSON.stringify(newWhatContact)
    });

    if (!response.ok) {
      const errorMessage = `POST /API/contacts/whatContact/${whatContact} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();

    return data;

  } catch (error) {

    console.error("Error creating contact:", error);
    throw error;
  }


}

export const deleteEmail = async (emailId: number): Promise<void> => {
    try {
        const response = await fetch(`/crmService/v1/API/contacts/emails/${emailId}`, {
        method: 'DELETE'
        });
    
        if (!response.ok) {
        const errorMessage = `/crmService/v1/API/contacts/emails/${emailId} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
        }
    } catch (error) {
        console.error("Error deleting email:", error);
        throw error;
    }
    };