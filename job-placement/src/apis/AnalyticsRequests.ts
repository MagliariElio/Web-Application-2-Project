import { JobOfferAnalytics } from "../interfaces/JobOfferAnalytics";
import { MessageAnalytics } from "../interfaces/MessageAnalytics";
import { MessagesPerMonth } from "../interfaces/MessagesPerMonth";

export const fetchMessagesAnalytics = async (): Promise<MessageAnalytics> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/messages`
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

  export const fetchMessagesPerMonthAnalytics = async (year: number): Promise<MessagesPerMonth> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/messages/${year}`
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

  export const fetchJobOffersAnalytics = async (): Promise<JobOfferAnalytics> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/jobOffers`
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

  export const fetchJobOffersPerMonthAnalytics = async (year: number): Promise<MessagesPerMonth> => {
    try {
      const response = await fetch(
        `/analyticsService/v1/API/analytics/jobOffers/${year}`
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
