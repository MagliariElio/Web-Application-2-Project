import { useEffect, useState } from "react";
import { Button, Col, Row } from "react-bootstrap";
import { BsPlus } from "react-icons/bs";

function CustomersPage() {

    const [customers, setCustomers] = useState([]);

    useEffect(() => {

        fetch("http://localhost:8082/API/customers")
            .then(res => res.json())
            .then(
                (result) => {
                
                    setCustomers(result);

                    console.log(customers);
                
                }
            )

    }, []);


  return (
    <div>
        <Row className="d-flex flex-row p-0 mb-3 align-items-center">
            <Col>
                <h3>Customers</h3> 
            </Col>
            <Col className="d-flex justify-content-end">
                <Button className="d-flex align-items-center primaryButton" >
                    <BsPlus size={"1.5em"} className="me-1" />
                    Add Customer
                </Button>
            </Col>


            

            

        </Row>
      
    </div>
  );
}

export default CustomersPage;