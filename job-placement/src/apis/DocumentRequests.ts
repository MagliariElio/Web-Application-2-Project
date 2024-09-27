import { CreateProfessional } from "../interfaces/CreateProfessional";
import { DocumentFile } from "../interfaces/DocumentFile";
import { MeInterface } from "../interfaces/MeInterface";
import { Professional } from "../interfaces/Professional";


export const addDocument = async (document: FormData, me: MeInterface): Promise<DocumentFile> => {
  console.log("addDocument", document);
  try {
    const response = await fetch("http://localhost:8080/gateway/documentUpload", {
      method: "POST",
      headers: {
        "X-XSRF-Token": me.xsrfToken,
      },
      body: document,
    });

    if (!response.ok) {
      const errorMessage = `POST /API/documents : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error adding document:", error);
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
