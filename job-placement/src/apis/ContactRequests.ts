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

export const deleteContactWhatContact = async (whatContact: string, id: string, me: MeInterface): Promise<any> => {
  try {
    const response = await fetch(`/crmService/v1/API/contacts/whatContact/${whatContact}/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-Token': me.xsrfToken
      }
    });

    if (!response.ok) {
      const errorMessage = `DELETE /API/contacts/whatContact/${whatContact}/${id} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();

    return data;

  } catch (error) {

    console.error("Error deleting contact:", error);
    throw error;
  }
}

export const editContactWhatContact = async (whatContact: string, id: string, updatedWhatContact: any, me: MeInterface): Promise<any> => {
  try {
    const response = await fetch(`/crmService/v1/API/contacts/whatContact/${whatContact}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-Token': me.xsrfToken
      },
      body: JSON.stringify(updatedWhatContact)
    });

    if (!response.ok) {
      const errorMessage = `PUT /API/contacts/whatContact/${whatContact}/${id} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();

    return data;

  } catch (error) {

    console.error("Error updating contact:", error);
    throw error;
  }
}