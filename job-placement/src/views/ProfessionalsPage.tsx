import { useEffect, useState } from "react";
import {
  Button,
  Col,
  Form,
  InputGroup,
  Row,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { BsChevronLeft, BsChevronRight, BsPencilSquare, BsPlus, BsSearch, BsTrash } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Customer } from "../interfaces/Customer";
import { useLocation, useNavigate } from "react-router-dom";
import { Professional } from "../interfaces/Professional";

function ProfessionalsPage() {
  const navigate = useNavigate();

  const [professionals, setProfessionals] = useState<PagedResponse<Professional> | null>(
    null
  );
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  useEffect(() => {
    if (success != null) {
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }

    fetch("/crmService/v1/API/professionals")
      .then((res) => {
        if (!res.ok) {
          console.log(res);
          throw new Error("GET /API/professionals : Network response was not ok");
        }
        return res.json();
      })
      .then((result) => {
        console.log("professionals fetched: ", result);
        setProfessionals(result);
        setLoading(false);
      })
      .catch((error) => {
        setError(true);
        console.log(error);
        setLoading(false);
      });
  }, []);

  const [filters, setFilters] = useState({
    name: "",
    surname: "",
    ssnCode: "",
    employmentState: "",
  });

  const handleFilterChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const [sortCriteria, setSortCriteria] = useState("asc_name");

  var presentedProfessionals = professionals?.content || [];

  const [pageSize, setPageSize] = useState(10);

  return (
    <div className="w-100">
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast
            bg={success ? "success" : "danger"}
            show={success != null}
            onClose={() => (location.state = null)}
          >
            <Toast.Header>
              <img
                src="holder.js/20x20?text=%20"
                className="rounded me-2"
                alt=""
              />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>
              {success ? "Operation correctly executed!" : "Operation failed!"}
            </Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col>
          <h3>Professionals</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button
            className="d-flex align-items-center primaryButton"
            onClick={() => navigate("/ui/professionals/add")}
          >
            <BsPlus size={"1.5em"} className="me-1" />
            Add Professional
          </Button>
        </Col>
      </Row>

      {error && (
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5 text-danger">
            <h5>An error occurred. Please, reload the page!</h5>
          </Col>
        </Row>
      )}

      {loading && (
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
            <h5>Loading...</h5>
          </Col>
        </Row>
      )}

      {!error &&
        !loading &&
        professionals !== null &&
        professionals.totalElements === 0 && (
          <Row className="w-100">
            <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
              <h5>No professional found yet! Start adding one!</h5>
            </Col>
          </Row>
        )}

      {!error &&
        !loading &&
        professionals !== null &&
        professionals.totalElements > 0 && (
          <>
            <Row className="w-100 d-flex justify-content-center">
              <Col xs={12} lg={4} className="order-1 order-lg-2 mt-3">
                <div className="sidebar-search p-4">
                  <h5>Filter Professionals</h5>
                  <Form>
                    <Form.Group controlId="name" className="mb-3">
                      <Form.Label>Name</Form.Label>
                      <Form.Control
                        type="text"
                        name="name"
                        value={filters.name}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>

                    <Form.Group controlId="surname" className="mb-3">
                      <Form.Label>Surname</Form.Label>
                      <Form.Control
                        type="text"
                        name="surname"
                        value={filters.surname}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>

                    <Form.Group controlId="ssnCode" className="mb-3">
                      <Form.Label>SsN Code</Form.Label>
                      <Form.Control
                        type="text"
                        name="ssnCode"
                        value={filters.ssnCode}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>

                    <Form.Group
                      controlId="employmentState"
                      className="mb-3"
                    >
                      <Form.Label>Employment state</Form.Label>
                      <Row className="align-items-center justify-content-between">
                       
                          <Form.Control
                            as="select"
                            name="employmentState"
                            value={filters.employmentState}
                            onChange={handleFilterChange}
                          >
                            <option value="EMPLOYED">Employed</option>
                            <option value="UNEMPLOYED">Unemployed</option>
                            <option value="AVAILABLE_FOR_WORK">Available for Work</option>
                            <option value="NOT_AVAILABLE">Not Available</option>
                          </Form.Control>
                     
                        
                      </Row>
                    </Form.Group>

                    <Button
                      className="primaryButton mb-2"
                      variant="primary"
                      onClick={() => {
                        var query = "";
                        if (filters.name || filters.surname || filters.ssnCode) query += "?";
                        if (filters.name) query += `&name=${filters.name}`;
                        if (filters.surname) query += `&surname=${filters.surname}`;
                        if (filters.ssnCode) query += `&ssnCode=${filters.ssnCode}`;
                        if (filters.employmentState) query += `&employmentState=${filters.employmentState}`;
                        setLoading(true);
                        fetch(
                            `/crmService/v1/API/professionals${query}`
                            )
                            .then((res) => {
                                if (!res.ok) {
                                console.log(res);
                                setLoading(false);
                                throw new Error(
                                    "GET /API/professionals : Network response was not ok"
                                );
                                }
                                return res.json();
                            })
                            .then((result) => {
                                console.log("Professionals fetched: ", result);
                                setProfessionals(result);
                                setLoading(false);
                            })
                            .catch((error) => {
                                setError(true);
                                setLoading(false);
                                console.log(error);
                            });
                      }}
                    >
                        <BsSearch className="me-1" />
                        Filter
                    </Button>

                    <Button
                      className="secondaryButton"
                      variant="primary"
                      onClick={() => {
                        setFilters({
                          name: "",
                          surname: "",
                          ssnCode: "",
                          employmentState: "",
                        });
                        fetch("/crmService/v1/API/professionals")
                          .then((res) => {
                            if (!res.ok) {
                              console.log(res);
                              throw new Error("GET /API/professionals : Network response was not ok");
                            }
                            return res.json();
                          })
                          .then((result) => {
                            console.log("Professionals fetched: ", result);
                            setProfessionals(result);
                            presentedProfessionals = result.content;
                            setLoading(false);
                          })
                          .catch((error) => {
                            setError(true);
                            setLoading(false);
                            console.log(error);
                          });
                      }}
                    >
                      Clear Filters
                    </Button>
                  </Form>

                  <h5 className="mt-5">Sort Professionals</h5>
                  <Form>
                    <Form.Group controlId="sort" className="mb-3">
                      <Form.Control
                        as="select"
                        name="sort"
                        value={sortCriteria}
                        onChange={(e) => setSortCriteria(e.target.value)}
                      >
                        <option value="asc_name">
                          Alphabetically ascending name
                        </option>
                        <option value="asc_surname">
                          Alphabetically ascending surname
                        </option>
                        <option value="asc_ssnCode">
                          Alphabetically ascending ssnCode
                        </option>
                        <option value="desc_name">
                          Alphabetically descending name
                        </option>
                        <option value="desc_surname">
                          Alphabetically descending surname
                        </option>
                        <option value="desc_ssnCode">
                          Alphabetically descending ssnCode
                        </option>
                        <option value="employed_before">
                          Occupied before
                        </option>
                        <option value="unemployed_before">
                          Non-occupied before
                        </option>
                      </Form.Control>
                    </Form.Group>
                  </Form>
                </div>
              </Col>

              <Col xs={12} lg={8} className="order-2 order-lg-1">
                <Row className="d-flex justify-content-center">
                  <Col className="d-flex-column justify-content-center align-items-center mt-3">
                    {presentedProfessionals
                      .sort((a, b) => {
                        if (sortCriteria === "asc_name") {
                          return a.information.contactDTO.name.localeCompare(
                            b.information.contactDTO.name
                          );
                        } else if (sortCriteria === "asc_surname") {
                          return a.information.contactDTO.surname.localeCompare(
                            b.information.contactDTO.surname
                          );
                        } else if (sortCriteria === "asc_ssnCode") {
                          return a.information.contactDTO.ssnCode.localeCompare(
                            b.information.contactDTO.ssnCode
                          );
                        } else if (sortCriteria === "desc_name") {
                          return b.information.contactDTO.name.localeCompare(
                            a.information.contactDTO.name
                          );
                        } else if (sortCriteria === "desc_surname") {
                          return b.information.contactDTO.surname.localeCompare(
                            a.information.contactDTO.surname
                          );
                        } else if (sortCriteria === "desc_ssnCode") {
                          return b.information.contactDTO.ssnCode.localeCompare(
                            a.information.contactDTO.ssnCode
                          );
                        } else if (sortCriteria === "employed_before") {
                          if (a.employmentState === "EMPLOYED") {
                            return -1;
                          } else if (b.employmentState === "EMPLOYED") {
                            return 1;
                          }
                        } else if (sortCriteria === "unemployed_before") {
                          if (a.employmentState === "UNEMPLOYED") {
                            return -1;
                          } else if (b.employmentState === "UNEMPLOYED") {
                            return 1;
                          }
                        }
                        return 0;
                      })
                      .map((professional, index) => {
                        return (
                          <Row
                            key={index}
                            className="w-100 border border-dark rounded-3 p-3 mb-2 ms-1 d-flex align-items-center secondaryButton"
                            onClick={() =>
                              navigate(`/ui/professionals/${professional.id}`)
                            }
                          >
                            <Col xs={12} md={6} lg={4}>
                              <h5 className="mb-0">{`${professional.information.contactDTO.name} ${professional.information.contactDTO.surname}`}</h5>
                            </Col>
                            <Col xs={12} md={6} lg={4}>
                              <p className="mb-0 fw-light">{`${professional.information.contactDTO.ssnCode}`}</p>
                            </Col>
                            <Col
                              xs={12}
                              lg={4}
                              className="d-flex justify-content-end"
                            >
                              <p className="mb-0">
                                <span className="fw-semibold fs-5">{
                                  
                                  professional.employmentState === "EMPLOYED" ? "Employed" :
                                  professional.employmentState === "UNEMPLOYED" ? "Unemployed" :
                                  professional.employmentState === "AVAILABLE_FOR_WORK" ? "Available for Work" :
                                  professional.employmentState === "NOT_AVAILABLE" ? "Not Available" : ""
                                  
                                  }</span>
                              </p>
                            </Col>
                          </Row>
                        );
                      })}

                    {presentedProfessionals.length === 0 && (
                      <Row className="w-100">
                        <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                          <h5>No professionals found with the selected filters!</h5>
                        </Col>
                      </Row>
                    )}
                  </Col>
                </Row>
                {
                    professionals.totalPages > 1 && (
                        <Row className="w-100 d-flex justify-content-center align-items-center mt-3">
                            {
                                professionals.currentPage > 0 && (
                                    <Col xs="auto" className="d-flex align-items-center">
                                <BsChevronLeft onClick={() => {
                                    
                                    var query = "";
                                    if (filters.name) query += `&name=${filters.name}`;
                                    if (filters.surname) query += `&surname=${filters.surname}`;
                                    if (filters.ssnCode) query += `&ssnCode=${filters.ssnCode}`;
                                    if (filters.employmentState) query += `&employmentState=${filters.employmentState}`;

                                    fetch(
                                        `/crmService/v1/API/professionals?pageNumber=${professionals.currentPage - 1}${query}`
                                    )
                                    .then((res) => {
                                        if (!res.ok) {
                                        console.log(res);
                                        throw new Error(
                                            "GET /API/professionals : Network response was not ok"
                                        );
                                        }
                                        return res.json();
                                    })
                                    .then((result) => {
                                        console.log("Professionals fetched: ", result);
                                        setProfessionals(result);
                                        presentedProfessionals = result.content;
                                        setLoading(false);
                                    })
                                    .catch((error) => {
                                        setError(true);
                                        setLoading(false);
                                        console.log(error);
                                    });

                                }} style={{ cursor: 'pointer' }} />
                            </Col>
                                )
                            }
                            
                            <Col xs="auto" className="d-flex align-items-center">
                                {professionals.currentPage}
                            </Col>
                            {
                                professionals.totalPages > professionals.currentPage + 1 && (
                                    <Col xs="auto" className="d-flex align-items-center">
                                <BsChevronRight onClick={() => {

                                        var query = "";
                                        if (filters.name) query += `&name=${filters.name}`;
                                        if (filters.surname) query += `&surname=${filters.surname}`;
                                        if (filters.ssnCode) query += `&ssnCode=${filters.ssnCode}`;
                                        if (filters.employmentState) query += `&employmentState=${filters.employmentState}`;

                                        
                                        fetch(
                                            `/crmService/v1/API/professionals?pageNumber=${professionals.currentPage + 1}${query}`
                                        )
                                        .then((res) => {
                                            if (!res.ok) {
                                            console.log(res);
                                            throw new Error(
                                                "GET /API/professionals : Network response was not ok"
                                            );
                                            }
                                            return res.json();
                                        })
                                        .then((result) => {
                                            console.log("Professionals fetched: ", result);
                                            setProfessionals(result);
                                            presentedProfessionals = result.content;
                                            setLoading(false);
                                        })
                                        .catch((error) => {
                                            setError(true);
                                            setLoading(false);
                                            console.log(error);
                                        }); 
                                }} style={{ cursor: 'pointer' }} />
                            </Col>
                                )
                            }
                            
                        </Row>
                    )
                }

<Row  className="w-100 d-flex justify-content-center align-items-center mt-3">
                    <Form.Control
                        style={{ width: 'auto' }}
                        as="select"
                        name="pageSize"
                        value={pageSize}
                        onChange={(e) => {
                            setPageSize(parseInt(e.target.value));

                            var query = "";
                                        if (filters.name) query += `&name=${filters.name}`;
                                        if (filters.surname) query += `&surname=${filters.surname}`;
                                        if (filters.ssnCode) query += `&ssnCode=${filters.ssnCode}`;
                                        if (filters.employmentState) query += `&employmentState=${filters.employmentState}`;

                            fetch(
                                `/crmService/v1/API/professionals?pageSize=${e.target.value}${query}`
                            )
                            .then((res) => {
                                if (!res.ok) {
                                console.log(res);
                                throw new Error(
                                    "GET /API/professionals : Network response was not ok"
                                );
                                }
                                return res.json();
                            })
                            .then((result) => {
                                console.log("Professionals fetched: ", result);
                                setProfessionals(result);
                                presentedProfessionals = result.content;
                                setLoading(false);
                            })
                            .catch((error) => {
                                setError(true);
                                setLoading(false);
                                console.log(error);
                            });
                        }}
                    >
                        <option value="10">10 customers</option>
                        <option value="20">20 customers</option>
                        <option value="50">50 customers</option>
                        <option value="100">100 customers</option>

                        </Form.Control>
                </Row>
                
              </Col>
            </Row>
          </>
        )}
    </div>
  );
}

export default ProfessionalsPage;
