import { Professional } from "../interfaces/Professional";

export const fetchProfessional = async (professionalId: number): Promise<Professional> => {
    try {
      const response = await fetch(`/crmService/v1/API/professionals/${professionalId}`);
  
      if (!response.ok) {
        const errorMessage = `/crmService/v1/API/professionals/${professionalId} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data: Professional = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching professional:", error);
      throw error;
    }
  };