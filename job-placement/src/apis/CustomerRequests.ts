import { Customer } from "../interfaces/Customer";

export const fetchCustomers = async (
  page: number,
  name: string = "",
  surname: string = "",
  ssnCode: string = "",
  comment: string = ""
): Promise<any> => {
  try {
    const response = await fetch(
      `/crmService/v1/API/customers?pageNumber=${page}&&name=${name}&&surname=${surname}&&ssnCode=${ssnCode}&&comment=${comment}`
    );

    if (!response.ok) {
      const errorMessage = `GET /API/customers : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching customers:", error);
    throw error;
  }
};

export const fetchCustomer = async (customerId: number): Promise<Customer> => {
  try {
    const response = await fetch(`/crmService/v1/API/customers/${customerId}`);

    if (!response.ok) {
      const errorMessage = `/crmService/v1/API/customers/${customerId} : ${response.status} ${response.statusText}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }

    const data: Customer = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching customer:", error);
    throw error;
  }
};
