import { MeInterface } from "../interfaces/MeInterface";
import { Message } from "../interfaces/Message";
import { MessageHistory } from "../interfaces/MessageHistory";
import { PagedResponse } from "../interfaces/PagedResponse";

export const fetchMessages = async (
    page: number,
    pageSize: number = 10,
    state: string = ""
  ): Promise<PagedResponse<Message>> => {
    try {
      var response;
      if(state !== "" && state !== undefined){
        response = await fetch(
          `/crmService/v1/API/messages?page=${page}&limit=${pageSize}&state=${state}`
        );
      }else{
        response = await fetch(
          `/crmService/v1/API/messages?page=${page}&limit=${pageSize}`
        );
      }
  
      if (!response.ok) {
        const errorMessage = `GET /API/messages : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching messages:", error);
      throw error;
    }
  };

  export const fetchMessagesHistory = async (
    id: number
  ): Promise<MessageHistory[]> => {
    try {
      
      const response = await fetch(
          `/crmService/v1/API/messages/${id}/history`
        );
      
  
      if (!response.ok) {
        const errorMessage = `GET /API/messages/history : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching message history:", error);
      throw error;
    }
  };

  export const fetchMessage = async (
    id: number
  ): Promise<Message> => {
    try {
      
      const response = await fetch(
          `/crmService/v1/API/messages/${id}`
        );
      
  
      if (!response.ok) {
        const errorMessage = `GET /API/messages/{id} : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.error("Error fetching message:", error);
      throw error;
    }
  };

  interface RequestBody {
    actualState?: string;   // Optional property
    priority?: string; // Optional property
  }

  export const updateMessage = async (me: MeInterface, id: number, state?: string, priority?: string) : Promise<Message> => {
    try {
      // Create an object to hold the data
      const bodyData: RequestBody = {};
      
      // Check if state is defined and add it to the bodyData object
      if (state !== undefined) {
        bodyData.actualState = state;
      }
      
      // Check if priority is defined and add it to the bodyData object
      if (priority !== undefined) {
        bodyData.priority = priority;
      }
      const response = await fetch(`/crmService/v1/API/messages/${id}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-Token": me.xsrfToken,
        },
        body: JSON.stringify(bodyData),
      });

      if(!response.ok){
        const errorMessage = `GET /API/messages : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.log("Error updating message")
      throw error;
    }
  }