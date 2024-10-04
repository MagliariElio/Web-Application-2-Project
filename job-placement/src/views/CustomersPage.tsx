import { useEffect, useState } from "react";
import { Button, ButtonGroup, Col, Form, Pagination, Row, Toast, ToastContainer } from "react-bootstrap";
import { BsPlus, BsSearch } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Customer } from "../interfaces/Customer";
import { useLocation, useNavigate } from "react-router-dom";
import { fetchCustomers } from "../apis/CustomerRequests";
import { MeInterface } from "../interfaces/MeInterface";
import { RoleState } from "../utils/costants";

interface CustomersPageProps {
  me: MeInterface;
}

function CustomersPage(props: CustomersPageProps) {
  const navigate = useNavigate();

  const [customers, setCustomers] = useState<PagedResponse<Customer> | null>(null);
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

    fetchCustomers(0)
      .then((result) => {
        console.log("Customers fetched: ", result);
        setCustomers(result);
        setLoading(false);
      })
      .catch((error) => {
        setError(true);
        console.log(error);
        setLoading(false);
        throw new Error("GET /API/customers : Network response was not ok");
      });
  }, []);

  const [filters, setFilters] = useState({
    name: "",
    surname: "",
    ssnCode: "",
    jobOffersNumberFrom: 0,
    jobOffersNumberTo: 10000,
  });

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const [sortCriteria, setSortCriteria] = useState("asc_name");

  var presentedCustomers =
    customers?.content.filter((customer) => {
      return customer.jobOffers.length >= filters.jobOffersNumberFrom && customer.jobOffers.length <= filters.jobOffersNumberTo;
    }) || [];

  const [pageSize, setPageSize] = useState(10);

  const changePage = (pageNumber: number) => {
    if (customers?.totalPages && pageNumber >= customers?.totalPages) pageNumber = customers?.totalPages - 1;
    if (pageNumber < 0) pageNumber = 0;

    fetchCustomers(pageNumber, filters.name, filters.surname, filters.ssnCode, undefined, pageSize)
      .then((result) => {
        console.log("Customers fetched: ", result);
        setCustomers(result);
        presentedCustomers = result.content;
        setLoading(false);
      })
      .catch((error) => {
        setError(true);
        setLoading(false);
        console.log(error);
        throw new Error("GET /API/customers : Network response was not ok");
      });
  };

  return (
    <div className="w-100">
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => (location.state = null)}>
            <Toast.Header>
              <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="d-flex flex-row p-0 mb-1 align-items-center">
        <Col xs={6}>
          <h3 className="title">Customers</h3>
        </Col>

        <Col xs={6} lg={2} className="d-flex justify-content-end">
          <Form.Group controlId="elementsPerPage text-center text-md-end">
            <Form.Select
              style={{ width: "auto" }}
              name="pageSize"
              value={pageSize}
              onChange={(e) => {
                setPageSize(parseInt(e.target.value));

                fetchCustomers(0, filters.name, filters.surname, filters.ssnCode, undefined, parseInt(e.target.value))
                  .then((result) => {
                    console.log("Customers fetched: ", result);
                    setCustomers(result);
                    presentedCustomers = result.content;
                    setLoading(false);
                  })
                  .catch((error) => {
                    setError(true);
                    setLoading(false);
                    console.log(error);
                    throw new Error("GET /API/customers : Network response was not ok");
                  });
              }}
            >
              <option value="10">10 customers</option>
              <option value="20">20 customers</option>
              <option value="50">50 customers</option>
              <option value="100">100 customers</option>
            </Form.Select>
          </Form.Group>
        </Col>
        {props.me.role === RoleState.OPERATOR && (
          <Col xs={12} lg={4} className="d-flex justify-content-center justify-content-lg-end">
            <Button className="d-flex align-items-center primaryButton me-4 mt-2 mt-lg-0" onClick={() => navigate("/ui/customers/add")}>
              <BsPlus size={"1.5em"} className="me-1" />
              Add Customer
            </Button>
          </Col>
        )}
      </Row>

      {error && (
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5 text-danger">
            <h5>An error occurred. Please, reload the page!</h5>
          </Col>
        </Row>
      )}

      {loading && (
        <Row>
          <Col md={8}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
          <Col md={4}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!error && !loading && customers !== null && (
        <Row className="w-100 d-flex justify-content-center">
          <Col xs={12} lg={4} className="order-1 order-lg-2 mt-3">
            <div className="sidebar-search p-4">
              <h5>Filter Customers</h5>
              <Form>
                <Form.Group controlId="name" className="mb-3">
                  <Form.Label>Name</Form.Label>
                  <Form.Control type="text" name="name" value={filters.name} onChange={handleFilterChange} />
                </Form.Group>

                <Form.Group controlId="surname" className="mb-3">
                  <Form.Label>Surname</Form.Label>
                  <Form.Control type="text" name="surname" value={filters.surname} onChange={handleFilterChange} />
                </Form.Group>

                <Form.Group controlId="ssnCode" className="mb-3">
                  <Form.Label>SSN Code</Form.Label>
                  <Form.Control type="text" name="ssnCode" value={filters.ssnCode} onChange={handleFilterChange} />
                </Form.Group>

                <Form.Group controlId="jobOffersNumberFromTo" className="mb-3">
                  <Form.Label>Job Offers range</Form.Label>
                  <Row className="align-items-center justify-content-between">
                    <Col xs={5}>
                      <Form.Control
                        type="number"
                        name="jobOffersNumberFrom"
                        value={filters.jobOffersNumberFrom}
                        onChange={handleFilterChange}
                        min={0}
                      />
                    </Col>
                    <Col className="d-flex justify-content-center">
                      <span>-</span>
                    </Col>
                    <Col xs={5}>
                      <Form.Control
                        type="number"
                        name="jobOffersNumberTo"
                        value={filters.jobOffersNumberTo}
                        onChange={handleFilterChange}
                        max={10000}
                      />
                    </Col>
                  </Row>
                </Form.Group>

                <ButtonGroup className="d-flex justify-content-center mt-4 flex-lg-column flex-xl-row w-100">
                  <Col className="text-center mb-lg-2 mb-xl-0 me-2 me-lg-0 me-xl-2 w-100 d-flex align-items-center">
                    <Button
                      className="primaryButton w-100"
                      variant="primary"
                      onClick={() => {
                        setLoading(true);
                        fetchCustomers(0, filters.name, filters.surname, filters.ssnCode)
                          .then((result) => {
                            console.log("Customers fetched: ", result);
                            setCustomers(result);
                            setLoading(false);
                          })
                          .catch((error) => {
                            setError(true);
                            setLoading(false);
                            console.log(error);
                            throw new Error("GET /API/customers : Network response was not ok");
                          });
                      }}
                    >
                      <BsSearch className="me-1" />
                      Filter
                    </Button>
                  </Col>

                  <Col className="text-center w-100 d-flex align-items-center">
                    <Button
                      className="secondaryButton w-100"
                      variant="primary"
                      onClick={() => {
                        setFilters({
                          name: "",
                          surname: "",
                          ssnCode: "",
                          jobOffersNumberFrom: 0,
                          jobOffersNumberTo: 10000,
                        });
                        fetchCustomers(0)
                          .then((result) => {
                            console.log("Customers fetched: ", result);
                            setCustomers(result);
                            presentedCustomers = result.content;
                            setLoading(false);
                          })
                          .catch((error) => {
                            setError(true);
                            setLoading(false);
                            console.log(error);
                            throw new Error("GET /API/customers : Network response was not ok");
                          });
                      }}
                    >
                      Clear Filters
                    </Button>
                  </Col>
                </ButtonGroup>
              </Form>

              <h5 className="mt-3">Sort Customers</h5>
              <Form>
                <Form.Group controlId="sort" className="mb-3">
                  <Form.Select name="sort" value={sortCriteria} onChange={(e) => setSortCriteria(e.target.value)}>
                    <option value="asc_name">Alphabetically ascending name</option>
                    <option value="asc_surname">Alphabetically ascending surname</option>
                    <option value="asc_ssnCode">Alphabetically ascending ssnCode</option>
                    <option value="asc_jobOffersNumber">Number of job offers ascending</option>
                    <option value="desc_name">Alphabetically descending name</option>
                    <option value="desc_surname">Alphabetically descending surname</option>
                    <option value="desc_ssnCode">Alphabetically descending ssnCode</option>
                    <option value="desc_jobOffersNumber">Number of job offers descending</option>
                  </Form.Select>
                </Form.Group>
              </Form>
            </div>
          </Col>

          <Col xs={12} lg={8} className="order-2 order-lg-1">
            <Row className="d-flex justify-content-center">
              <Col className="d-flex-column justify-content-center align-items-center mt-3">
                {presentedCustomers
                  .sort((a, b) => {
                    if (sortCriteria === "asc_name") {
                      return a.information.contactDTO.name.localeCompare(b.information.contactDTO.name);
                    } else if (sortCriteria === "asc_surname") {
                      return a.information.contactDTO.surname.localeCompare(b.information.contactDTO.surname);
                    } else if (sortCriteria === "asc_ssnCode") {
                      return a.information.contactDTO.ssnCode.localeCompare(b.information.contactDTO.ssnCode);
                    } else if (sortCriteria === "asc_jobOffersNumber") {
                      return a.jobOffers.length - b.jobOffers.length;
                    } else if (sortCriteria === "desc_name") {
                      return b.information.contactDTO.name.localeCompare(a.information.contactDTO.name);
                    } else if (sortCriteria === "desc_surname") {
                      return b.information.contactDTO.surname.localeCompare(a.information.contactDTO.surname);
                    } else if (sortCriteria === "desc_ssnCode") {
                      return b.information.contactDTO.ssnCode.localeCompare(a.information.contactDTO.ssnCode);
                    } else if (sortCriteria === "desc_jobOffersNumber") {
                      return b.jobOffers.length - a.jobOffers.length;
                    }
                    return 0;
                  })
                  .map((customer, index) => {
                    return (
                      <Row
                        key={index}
                        className="w-100 border border-dark rounded-3 p-3 mb-2 ms-1 d-flex align-items-center secondaryButton"
                        onClick={() => navigate(`/ui/customers/${customer.id}`)}
                      >
                        <Col xs={12} md={6} lg={4}>
                          <h5 className="mb-0">{`${customer.information.contactDTO.name} ${customer.information.contactDTO.surname}`}</h5>
                        </Col>
                        <Col xs={12} md={6} lg={4}>
                          <p className="mb-0 fw-light">{`${customer.information.contactDTO.ssnCode}`}</p>
                        </Col>
                        <Col xs={12} lg={4} className="d-flex justify-content-end">
                          <p className="mb-0">
                            <span className="fw-semibold fs-5">{`${customer.jobOffers.length} `}</span>
                            job offers
                          </p>
                        </Col>
                      </Row>
                    );
                  })}

                {presentedCustomers.length === 0 && (
                  <Row className="w-100">
                    <Col className="w-100 d-flex flex-column justify-content-center align-items-center mt-5">
                      <h5 className="p-3 text-center">No customers found with the selected criteria.</h5>
                      <h5 className="p-3 text-center">Try adjusting the filters, or it could be that no customers have been added yet.</h5>
                    </Col>
                  </Row>
                )}
              </Col>
            </Row>

            {/* Pagination */}
            {customers?.content.length > 0 && (
              <Row>
                <Col className="d-flex justify-content-center mt-4 justify-self-center">
                  <Pagination className="custom-pagination">
                    <Pagination.First onClick={() => changePage(0)} disabled={customers.currentPage === 0} />
                    <Pagination.Prev onClick={() => changePage(customers.currentPage - 1)} disabled={customers.currentPage === 0} />

                    {Array.from({ length: Math.min(5, customers.totalPages) }, (_, index) => {
                      const startPage = Math.max(Math.min(customers.currentPage - 2, customers.totalPages - 5), 0);
                      const actualPage = startPage + index;

                      return (
                        <Pagination.Item key={actualPage} active={actualPage === customers.currentPage} onClick={() => changePage(actualPage)}>
                          {actualPage + 1}
                        </Pagination.Item>
                      );
                    })}

                    <Pagination.Next
                      onClick={() => changePage(customers.currentPage + 1)}
                      disabled={customers.currentPage + 1 === customers.totalPages}
                    />
                    <Pagination.Last
                      onClick={() => changePage(customers.totalPages - 1)}
                      disabled={customers.currentPage + 1 === customers.totalPages}
                    />
                  </Pagination>
                </Col>
              </Row>
            )}
          </Col>
        </Row>
      )}
    </div>
  );
}

export default CustomersPage;
