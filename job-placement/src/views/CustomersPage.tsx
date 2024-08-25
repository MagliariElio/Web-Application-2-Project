import { useEffect, useState } from "react";
import { Button, Col, Form, InputGroup, Row, Toast, ToastContainer } from "react-bootstrap";
import { BsPencilSquare, BsPlus, BsSearch, BsTrash } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Customer } from "../interfaces/Customer";
import { useLocation, useNavigate } from "react-router-dom";

function CustomersPage() {

    const navigate = useNavigate();

    const [customers, setCustomers] = useState<PagedResponse<Customer> | null>(null);
    const [error, setError] = useState(false);
    const [loading, setLoading] = useState(true);

    const location = useLocation();
    var { success } = location.state || {};
    const [showAlert, setShowAlert] = useState(false);

    useEffect(() => {

        if (success != null){
            setShowAlert(true);
            setTimeout(() => {
                setShowAlert(false);
            }, 3000);
        }

        fetch("/crmService/v1/API/customers")
            .then(res => {
                if (!res.ok) {
                    console.log(res);
                    throw new Error('GET /API/customers : Network response was not ok');
                }
                return res.json();
            })
            .then(
                (result) => {
                    console.log("Customers fetched: ", result);
                    setCustomers(result);
                    setLoading(false);
                
                }
            )
            .catch((error) => {
                setError(true);
                console.log(error);
                setLoading(false);
            });

    }, []);

    const [searchTerm, setSearchTerm] = useState('');
    const [sortField, setSortField] = useState('');

    const handleSearchChange = () => {
    };

    const handleSort: React.ChangeEventHandler<HTMLSelectElement> = (event) => {
        setSortField(event.target.value);
    };


  return (
    <div className="w-100">
        { showAlert &&
            <ToastContainer position="top-end" className="p-3">
            <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => location.state = null}>
              <Toast.Header>
                <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
                <strong className="me-auto">JobConnect</strong>
                <small>now</small>
              </Toast.Header>
              <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
            </Toast>
          </ToastContainer>
        }
        
        <Row className="d-flex flex-row p-0 mb-3 align-items-center">
            <Col>
                <h3>Customers</h3> 
            </Col>
            <Col className="d-flex justify-content-end">
                <Button className="d-flex align-items-center primaryButton" onClick={() => navigate("/ui/customers/add") } >
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

        { loading && <Row className="w-100">
            <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                <h5>Loading...</h5>
            </Col>
        </Row>}

        {
            !error && !loading && customers!== null && customers.totalElements === 0 && <Row className="w-100">
                <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                    <h5>No customers found yet! Start adding one!</h5>
                </Col>
            </Row>
        }

        {
            !error && !loading && customers!== null && customers.totalElements > 0 && <>

            <Row className="mb-3">
                <Col className="d-flex flex-row align-items-center">
                    <InputGroup className="flex-grow-1 me-4">
                        <Form.Control
                            type="text"
                            placeholder="Cerca per nome o codice SSN"
                            value={searchTerm}
                            onChange={handleSearchChange}
                        />
                            <Button className="secondaryButton d-flex justify-content-center align-items-center" variant="outline-secondary" onClick={handleSearchChange}>
                                <BsSearch />
                            </Button>
                        
                    </InputGroup>
                    <Form.Select className="ml-2 me-4" onChange={handleSort}>
                        <option value="">Ordina per...</option>
                        <option value="name">Nome</option>
                        <option value="ssnCode">Codice SSN</option>
                    </Form.Select>
                </Col>
            </Row>

            <Row className="w-100 d-flex justify-content-center">
                <Col className="d-flex-column justify-content-center align-items-center mt-3">
                    {
                    customers.content.map((customer, index) => {
                        return (
                            <Row key={index} className="w-100 border border-dark rounded-3 p-3 mb-2 ms-1 d-flex align-items-center secondaryButton"
                                onClick={() => navigate(`/ui/customers/${customer.id}`)}
                            >
                                <Col xs={12} md={6} lg={3}>
                                    <h5 className="mb-0">{`${customer.information.contactDTO.name} ${customer.information.contactDTO.surname}`}</h5>
                                </Col>
                                <Col xs={12} md={6} lg={3}>
                                    <p className="mb-0 fw-light">{`${customer.information.contactDTO.ssnCode}`}</p>
                                </Col>
                                <Col xs={12} md={6} lg={3}>
                                    <p className="mb-0"><span className="fw-semibold fs-5">{`${customer.jobOffers.length} `}</span>job offers</p>
                                </Col>
                                
                            </Row>
                        )
                    })
                }

                </Col>
            </Row>
            </>

        }


      
    </div>
  );
}

export default CustomersPage;