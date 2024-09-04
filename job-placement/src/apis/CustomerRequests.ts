import { CreateCustomer } from "../interfaces/CreateCustomer";
import { Customer } from "../interfaces/Customer";
import { MeInterface } from "../interfaces/MeInterface";
import { PagedResponse } from "../interfaces/PagedResponse";

export const fetchCustomers = async (
  page: number,
  name: string = "",
  surname: string = "",
  ssnCode: string = "",
  comment: string = "",
  pageSize: number = 10,
): Promise<PagedResponse<Customer>> => {
  try {
    const response = await fetch(
      `/crmService/v1/API/customers?pageNumber=${page}&&pageSize=${pageSize}&&name=${name}&&surname=${surname}&&ssnCode=${ssnCode}&&comment=${comment}`
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


export const createCustomer = async (customer: CreateCustomer, me: MeInterface ): Promise<Customer> => {

  try {
    const response = await fetch("/crmService/v1/API/customers", {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json',
          'X-XSRF-Token': me.xsrfToken
      },
      body: JSON.stringify(customer)
    });

    if (!response.ok) {
      const errorMessage = `POST /API/customers : ${response.status} ${response.statusText}`;
      console.error("Error message: ", errorMessage);
      throw new Error(errorMessage);
    }

    const data: Customer = await response.json();
    console.log("Created customer: ", data);
    return data;
  } catch (error) {
    console.error("Error creating customer:", error);
    throw error;
  }
  
}

export const deleteCustomer = async (customerId: number, me: MeInterface): Promise<any> => {
  try {
    const response = await fetch("/crmService/v1/API/customers/" + customerId, {
      method: "DELETE",
      headers: {
          'X-XSRF-Token': me.xsrfToken,
      },
    });

    if (response.ok) {
      return response.text();
    } else {
      throw new Error(
        `DELETE /API/customers/${customerId} : Network response was not ok`
      );
    }
  } catch (error) {
    console.error("Error:", error);
    throw error;
  }
}