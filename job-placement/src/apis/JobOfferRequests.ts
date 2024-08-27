const submitJobOffer = async (jobOffer: any, xsrfToken: string) => {
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
      console.log("Error during joboffers post: ", response);
      throw new Error("POST /API/joboffers : Network response was not ok");
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

const JobOfferRequests = { submitJobOffer, fetchJobOffers, fetchJobOfferById, deleteJobOfferById };
export default JobOfferRequests;
