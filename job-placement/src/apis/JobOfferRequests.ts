import { JobOffer } from "../interfaces/JobOffer";
import { JobOfferState } from "../utils/costants";

export const submitJobOffer = async (jobOffer: any, xsrfToken: string) => {
  try {
    const response = await fetch("/crmService/v1/API/joboffers", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": xsrfToken,
      },
      body: JSON.stringify(jobOffer),
    });

    if (!response.ok) {
      let errorMessage = "An error occurred while updating the job offer.";

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

    return response.json();
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const updateJobOffer = async (jobOffer: JobOffer, xsrfToken: string) => {
  try {
    const response = await fetch("/crmService/v1/API/joboffers", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": xsrfToken,
      },
      body: JSON.stringify(jobOffer),
    });

    if (!response.ok) {
      let errorMessage = "An error occurred while updating the job offer.";

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

    return response.json();
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const fetchJobOffers = async () => {
  try {
    const response = await fetch("/crmService/v1/API/joboffers");
    if (!response.ok) {
      throw new Error("GET /API/joboffers : Network response was not ok");
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching job offers:", error);
    throw error;
  }
};

export const fetchJobOfferById = async (id: number) => {
  try {
    const response = await fetch("/crmService/v1/API/joboffers/" + id + "/value");
    if (!response.ok) {
      throw new Error("GET /API/joboffers/" + id + "/value : Network response was not ok");
    }
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching job offer:", error);
    throw error;
  }
};

export const deleteJobOfferById = async (jobOfferId: number, xsrfToken: string) => {
  try {
    const response = await fetch(`/crmService/v1/API/joboffers/${jobOfferId}`, {
      method: "DELETE",
      headers: {
        "X-XSRF-Token": xsrfToken,
      },
    });

    if (!response.ok) {
      throw new Error(`DELETE /API/joboffers/${jobOfferId} : Network response was not ok`);
    }

    return { success: true, message: "Job offer deleted successfully." };
  } catch (error) {
    console.error("Error deleting job offer:", error);
    throw error;
  }
};

export const goToSelectionPhase = async (jobOfferId: number, xsrfToken: string, jobOffer: any) => {
  try {
    jobOffer.nextStatus = JobOfferState.SELECTION_PHASE;

    const response = await fetch(`/crmService/v1/API/joboffers/${jobOfferId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": xsrfToken,
      },
      body: JSON.stringify(jobOffer),
    });

    if (!response.ok) {
      let errorMessage = "An error occurred while updating the job offer.";

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

    return { success: true, message: "Job offer updated successfully." };
  } catch (error) {
    console.error("Error in goToSelectionPhase:", error);
    throw error;
  }
};
