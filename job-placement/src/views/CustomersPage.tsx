import { useEffect, useState } from "react";
import { Button, Col, Row } from "react-bootstrap";
import { BsPlus } from "react-icons/bs";

function CustomersPage() {

    const [customers, setCustomers] = useState([]);
    const [error, setError] = useState(false);

    useEffect(() => {

        fetch("http://localhost:8082/API/customers")
            .then(res => {
                if (!res.ok) {
                    throw new Error('/API/customers : Network response was not ok');
                }
                return res.json();
            })
            .then(
                (result) => {
                
                    setCustomers(result);

                    console.log("Customerrrrrrrrrr: " + customers);
                
                }
            )
            .catch((error) => {
                setError(true);
                console.log(error);
            });

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

        { error && <Row className="w-100">
            <Col className="w-100 d-flex justify-content-center align-items-center mt-5 text-danger">
                <h5>An error occurred. Please, reload the page!</h5>
            </Col>
        </Row>}
      
    </div>
  );
}

export default CustomersPage;