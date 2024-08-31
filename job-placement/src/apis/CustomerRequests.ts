import { Customer } from "../interfaces/Customer";

export const fetchCustomers = async (): Promise<Customer[]> => {
    try {
      const response = await fetch("/crmService/v1/API/customers");
  
      if (!response.ok) {
        const errorMessage = `GET /API/customers : ${response.status} ${response.statusText}`;
        console.error(errorMessage);
        throw new Error(errorMessage);
      }
  
      const data = await response.json();
      return data.content;
    } catch (error) {
      console.error("Error fetching customers:", error);
      throw error;
    }
  };
  