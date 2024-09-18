import { JobOfferAnalytics } from "../interfaces/JobOfferAnalytics";
import { MeInterface } from "../interfaces/MeInterface";
import { MessageAnalytics } from "../interfaces/MessageAnalytics";
import { MessagesPerMonth } from "../interfaces/MessagesPerMonth";
import { ProfessionalAnalytics } from "../interfaces/ProfessionalAnalytics";

export const fetchMessagesAnalytics = async (me: MeInterface): Promise<MessageAnalytics> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/messages`, {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-Token': me.xsrfToken,
          }
        }
      );
      if (!response.ok) {
        const errorMessage = `GET /API/analytics/messages : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching messages analytics:", error);
      throw error;
    }
  };

  export const fetchMessagesPerMonthAnalytics = async (year: number, me: MeInterface): Promise<MessagesPerMonth> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/messages/${year}`, {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-Token': me.xsrfToken,
          }
        }
      );
      if (!response.ok) {
        const errorMessage = `GET /API/analytics/messages/${year} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching messages analytics:", error);
      throw error;
    }
  };

  export const fetchJobOffersAnalytics = async (me: MeInterface): Promise<JobOfferAnalytics> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/jobOffers`, {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-Token': me.xsrfToken,
          }
        }
      );
      if (!response.ok) {
        const errorMessage = `GET /API/analytics/jobOffers : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching messages analytics:", error);
      throw error;
    }
  };

  export const fetchJobOffersPerMonthAnalytics = async (year: number, me: MeInterface): Promise<MessagesPerMonth> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/jobOffers/${year}`, {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-Token': me.xsrfToken,
          }
        }
      );
      if (!response.ok) {
        const errorMessage = `GET /API/analytics/jobOffers/${year} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching messages analytics:", error);
      throw error;
    }
  };

  export const fetchProfessionalsAnalytics = async (me: MeInterface): Promise<ProfessionalAnalytics> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/professionals`, {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-Token': me.xsrfToken,
          }
        }
      );
      if (!response.ok) {
        const errorMessage = `GET /API/analytics/professionals : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching professional analytics:", error);
      throw error;
    }
  };
