import { Professional } from "../interfaces/Professional";

export const fetchProfessional = async (professionalId: number): Promise<Professional> => {
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
    return data.professionalDTO;
  } catch (error) {
    console.log(error);
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
