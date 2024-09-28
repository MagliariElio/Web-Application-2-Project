import { CreateProfessional } from "../interfaces/CreateProfessional";
import { EditProfessional } from "../interfaces/EditProfessional";
import { MeInterface } from "../interfaces/MeInterface";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Professional } from "../interfaces/Professional";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";

export const fetchProfessional = async (
  professionalId: number
): Promise<ProfessionalWithAssociatedData> => {
  try {
    const response = await fetch(
      `/crmService/v1/API/professionals/${professionalId}`
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

export const fetchProfessionals = async (
  page: number,
  pageSize: number = 10,
  skill: string = "",
  location: string = "",
  employmentState: string = "",
  name: string = "",
  surname: string = ""
): Promise<PagedResponse<Professional>> => {
  try {
    const response = await fetch(
      `/crmService/v1/API/professionals?pageNumber=${page}&pageSize=${pageSize}&skill=${skill}&location=${location}&employmentState=${employmentState}&name=${name}&surname=${surname}`
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

export const createProfessional = async (
  professional: CreateProfessional,
  attachmentsList: File[],
  me: MeInterface
): Promise<Professional> => {
  try {
    const formData = new FormData();
    formData.append(
      "createProfessionalInfo",
      new Blob([JSON.stringify(professional)], { type: "application/json" })
    );
    if (attachmentsList.length > 0) {
      attachmentsList.forEach((file) => {
        formData.append("files", file);
      });
    }
       
    const response = await fetch(
      "http://localhost:8080/gateway/createProfessional",
      {
        method: "POST",
        headers: {
          "X-XSRF-Token": me.xsrfToken,
        },
        body: formData,
      }
    );

    if (!response.ok) {
      const errorMessage = `POST /gateway/createProfessional : ${response.status} ${response.statusText}`;
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

export const deleteProfessional = async (
  professionalId: number,
  me: MeInterface
): Promise<void> => {
  try {
    const response = await fetch(
      `/gateway/deleteProfessional/${professionalId}`,
      {
        method: "DELETE",
        headers: {
          "X-XSRF-Token": me.xsrfToken,
        },
      }
    );

    if (!response.ok) {
      const errorMessage = `DELETE /gateway/deleteProfessional/${professionalId} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }
  } catch (error) {
    console.error("Error deleting professional:", error);
    throw error;
  }
};

export const updateProfessional = async (
  professional: EditProfessional,
  addedAttachments: File[],
  removedAttachments: number[],
  me: MeInterface
): Promise<Professional> => {
  try {
    const formData = new FormData();
    formData.append(
      "updateProfessionalInfo",
      new Blob([JSON.stringify(professional)], { type: "application/json" })
    );

    if (addedAttachments.length > 0) {
      addedAttachments.forEach((file) => {
        formData.append("addedFiles", file);
      });
    }

    if (removedAttachments.length > 0) {
      formData.append(
        "removedFiles",
        new Blob([JSON.stringify(removedAttachments)], { type: "application/json" })
      );
    }

    const response = await fetch(
      "/gateway/editProfessional",
      {
        method: "PATCH",
        headers: {
          "X-XSRF-Token": me.xsrfToken,
        },
        body: formData,
      }
    );

    if (!response.ok) {
      const errorMessage = `PATCH /gateway/editProfessional : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data: Professional = await response.json();
    return data;
  } catch (error) {
    console.error("Error updating professional:", error);
    throw error;
  }
};