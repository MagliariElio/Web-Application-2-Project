import { CreateProfessional } from "../interfaces/CreateProfessional";
import { EditProfessional } from "../interfaces/EditProfessional";
import { MeInterface } from "../interfaces/MeInterface";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Professional } from "../interfaces/Professional";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";

export const fetchProfessional = async (professionalId: number): Promise<ProfessionalWithAssociatedData> => {
  try {
    const response = await fetch(`/crmService/v1/API/professionals/${professionalId}`);

    if (!response.ok) {
      let errorMessage = "An error occurred while fetching the professionals.";

      try {
        const message = await response.json();
        if (message.errors && Array.isArray(message.errors)) {
          errorMessage = message.errors.join(", ");
        }
      } catch (jsonError) {
        console.error("Failed to parse JSON response:", jsonError);
      }

      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const fetchProfessionals = async (
  page: number,
  pageSize: number = 10,
  skill: string = "",
  location: string = "",
  employmentState: string = ""
): Promise<PagedResponse<Professional>> => {
  try {
    const response = await fetch(
      `/crmService/v1/API/professionals?pageNumber=${page}&pageSize=${pageSize}&skill=${skill}&location=${location}&employmentState=${employmentState}`
    );

    if (!response.ok) {
      const errorMessage = `GET /API/professionals : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching professionals:", error);
    throw error;
  }
};

export const createProfessional = async (professional: CreateProfessional, me: MeInterface): Promise<Professional> => {
  try {
    const response = await fetch("/crmService/v1/API/professionals", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": me.xsrfToken,
      },
      body: JSON.stringify(professional),
    });

    if (!response.ok) {
      const errorMessage = `POST /API/professionals : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data: Professional = await response.json();
    return data;
  } catch (error) {
    console.error("Error creating professional:", error);
    throw error;
  }
};

  export const deleteProfessional = async (professionalId: number, me: MeInterface): Promise<void> => {
    try {
      const response = await fetch(`/crmService/v1/API/professionals/${professionalId}`, {
        method: 'DELETE',
        headers: {
          'X-XSRF-Token': me.xsrfToken,
        }
      });
  
      if (!response.ok) {
        const errorMessage = `DELETE /API/professionals/${professionalId} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error("Error deleting professional:", error);
      throw error;
    }
  }


  export const updateProfessional = async (professional: EditProfessional, me: MeInterface): Promise<Professional> => {
    try {
      const response = await fetch(`/crmService/v1/API/professionals/${professional.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-Token': me.xsrfToken,
        },
        body: JSON.stringify(professional)
      });
  
      if (!response.ok) {
        const errorMessage = `PUT /API/professionals/${professional.id} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data: Professional = await response.json();
      return data;
    } catch (error) {
      console.error("Error updating professional:", error);
      throw error
    }
  }