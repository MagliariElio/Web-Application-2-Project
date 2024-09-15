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

    return response.json();
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const fetchJobOffers = async (
  page: number,
  limit: number,
  sortBy: string,
  sortDirection: string,
  contractType: string,
  location: string,
  status: string,
  workMode: string
) => {
  try {
    const params: Record<string, string> = {
      page: page.toString(),
      limit: limit.toString(),
      ...(sortBy && { sortBy }),
      ...(sortDirection && { sortDirection }),
      ...(contractType && { contractType }),
      ...(location && { location }),
      ...(status && { status }),
      ...(workMode && { workMode }),
    };

    const queryString = new URLSearchParams(params).toString();
    const response = await fetch(`/crmService/v1/API/joboffers?${queryString}`);

    if (!response.ok) {
      let errorMessage = "An error occurred while fetching the job offer.";

      try {
        const message = await response.json();

        if (message.errors && Array.isArray(message.errors)) {
          errorMessage = message.errors.join(", ");
        } else if (message.error) {
          errorMessage = message.error;
        } else if (message) {
          errorMessage = message;
        }
      } catch (jsonError) {
        console.error("Failed to parse JSON response:", jsonError);
      }

      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in fetchJobOffers:", error);
    throw error;
  }
};

export const fetchJobOfferById = async (id: number) => {
  try {
    const response = await fetch(`/crmService/v1/API/joboffers/${id}/value`);

    if (!response.ok) {
      let errorMessage = "An error occurred while fetching the job offer.";

      try {
        const contentType = response.headers.get("content-type");

        if (contentType && contentType.includes("application/json")) {
          const message = await response.json();

          if (message.errors && Array.isArray(message.errors)) {
            errorMessage = message.errors.join(", ");
          } else if (message.error) {
            errorMessage = message.error;
          } else if (message) {
            errorMessage = message;
          }
        } else {
          const textMessage = await response.text();
          errorMessage = textMessage || "An unknown error occurred.";
        }
      } catch (jsonError) {
        console.error("Failed to parse JSON response:", jsonError);
      }

      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in fetchJobOfferById:", error);
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
        } else if (message.error) {
          errorMessage = message.error;
        } else if (message) {
          errorMessage = message;
        }
      } catch (jsonError) {
        console.error("Failed to parse JSON response:", jsonError);
      }

      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in goToSelectionPhase:", error);
    throw error;
  }
};

export const abortJobOffer = async (jobOfferId: number, xsrfToken: string) => {
  try {
    const jobOffer = { nextStatus: JobOfferState.ABORT };

    const response = await fetch(`/crmService/v1/API/joboffers/${jobOfferId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": xsrfToken,
      },
      body: JSON.stringify(jobOffer),
    });

    if (!response.ok) {
      let errorMessage = "An error occurred while aborting the job offer.";

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
    console.error("Error in abortJobOffer:", error);
    throw error;
  }
};

export const goToCandidateProposalPhase = async (jobOfferId: number, xsrfToken: string, candidates: number[]) => {
  try {
    const jobOffer = {
      nextStatus: JobOfferState.CANDIDATE_PROPOSAL,
      professionalsId: candidates,
    };

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

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in goToCandidateProposalPhase:", error);
    throw error;
  }
};

export const goToCondolidated = async (jobOfferId: number, xsrfToken: string, candidateId: number) => {
  try {
    const jobOffer = {
      nextStatus: JobOfferState.CONSOLIDATED,
      professionalsId: [candidateId],
    };

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

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in goToCondolidated:", error);
    throw error;
  }
};

export const doneJobOffer = async (jobOfferId: number, xsrfToken: string, candidateId: number) => {
  try {
    const jobOffer = {
      nextStatus: JobOfferState.DONE,
      professionalsId: [candidateId],
    };

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

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error in cancelCandidation:", error);
    throw error;
  }
};

export const generateJobOffer = async (prompt: string, xsrfToken: string) => {
  try {
    const response = await fetch("/crmService/v1/API/joboffers/generate", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-Token": xsrfToken,
      },
      body: JSON.stringify(prompt),
    });

    if (!response.ok) {
      let errorMessage = "An error occurred while generating the job offer. Try later, please.";

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
