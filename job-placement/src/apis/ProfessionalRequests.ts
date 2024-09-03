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

  export const fetchProfessionals = async (
    page: number,
    name: string = "",
    surname: string = "",
    ssnCode: string = "",
    comment: string = ""
  ): Promise<any> => {
    try {
      const response = await fetch(
        `/crmService/v1/API/professionals?pageNumber=${page}&&name=${name}&&surname=${surname}&&ssnCode=${ssnCode}&&comment=${comment}`
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