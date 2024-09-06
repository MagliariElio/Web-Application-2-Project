import { Address } from "../interfaces/Address";
import { Email } from "../interfaces/Email";
import { Telephone } from "../interfaces/Telephone";

export const fetchAllContactWhatContact = async (whatContact: string): Promise<Email[] | Telephone[] | Address[]> => {
  try {

    const response = await fetch(`/crmService/v1/API/contacts/${whatContact}`);
    if (!response.ok) {
      const errorMessage = `GET /API/contacts/${whatContact} : ${response.status} ${response.statusText}`;
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