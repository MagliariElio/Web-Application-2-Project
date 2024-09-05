import { Container, Row, Col, Button, Toast, ToastContainer, Form, Alert, Pagination } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { useLocation, useNavigate } from "react-router-dom";
import { JobOffer } from "../interfaces/JobOffer.ts";
import { contractTypeList, JobOfferState, toTitleCase, workModeList } from "../utils/costants.ts";
import { fetchJobOffers } from "../apis/JobOfferRequests.ts";

// TODO: è presente un problema con elements per page (limit) il backend non restituisce il numero di job offer corretto

const HomePage = () => {
  const navigate = useNavigate();

  const [jobOffers, setJobOffers] = useState<PagedResponse<JobOffer> | null>(null);
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [filters, setFilters] = useState({
    contractType: "",
    location: "",
    workMode: "",
    status: "",
    elementsPerPage: 10,
  });

  useEffect(() => {
    if (success != null) {
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }

    const loadJobOffers = async (page: number) => {
      try {
        const result = await fetchJobOffers(page, filters.elementsPerPage);
        setJobOffers(result);
        setTotalPages(result.totalPages);
        setLoading(false);
      } catch (error) {
        setError(true);
        setLoading(false);
      }
    };

    loadJobOffers(currentPage);
  }, [success, currentPage, filters.elementsPerPage]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const filteredJobOffers =
    jobOffers?.content.filter((joboffer) => {
      return (
        (!filters.contractType || joboffer.contractType === filters.contractType) &&
        (!filters.location || joboffer.location.includes(filters.location)) &&
        (!filters.workMode || joboffer.workMode === filters.workMode) &&
        (!filters.status || joboffer.status.toLowerCase() === filters.status.toLowerCase())
      );
    }) || [];

  const changePage = (pageNumber: number) => {
    setCurrentPage(pageNumber);
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: "100vh" }}>
        <div className="spinner-border" role="status">
          <span className="sr-only"></span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ height: "100vh" }}>
        <Alert variant="danger" className="text-center w-50">
          <h5>An error occurred. Please, reload the page!</h5>
        </Alert>
      </Container>
    );
  }

  return (
    <Container fluid>
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => (success = null)}>
            <Toast.Header>
              <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col>
          <h3>Job Offers</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button className="d-flex align-items-center primaryButton" onClick={() => navigate("/ui/joboffers/add")}>
            <BsPlus size={"1.5em"} className="me-1" />
            Add Job Offer
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

      {!error && !loading && jobOffers !== null && jobOffers.totalElements === 0 && (
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
            <h5>No job offers found yet! Start adding one!</h5>
          </Col>
        </Row>
      )}

      {!error && !loading && jobOffers !== null && jobOffers.totalElements > 0 && (
        <>
          <Row>
            <Col md={8}>
              {filteredJobOffers.length === 0 ? (
                <Row className="w-100">
                  <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                    <h5>No job offers found with the selected filters!</h5>
                  </Col>
                </Row>
              ) : (
                filteredJobOffers.map((joboffer) => (
                  <div
                    key={joboffer.id}
                    className="job-offer-item mb-4 p-3"
                    onClick={() => navigate(`/ui/joboffers/${joboffer.id}`, { state: { jobOfferSelected: joboffer } })}
                  >
                    <Row className="align-items-center">
                      <Col xs={12} className="mb-2">
                        <h5 className="job-title">{joboffer.name}</h5>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-1">
                          <strong>Contract Type:</strong> {joboffer.contractType}
                        </p>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-1">
                          <strong>Location:</strong> {joboffer.location}
                        </p>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-1">
                          <strong>Work Mode:</strong> {joboffer.workMode}
                        </p>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-1">
                          <strong>Duration:</strong> {joboffer.duration} hours
                        </p>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-1">
                          <strong>Value:</strong> ${joboffer.value}
                        </p>
                      </Col>
                      <Col md={6} xs={12}>
                        <p className="mb-0">
                          <strong>Status:</strong>{" "}
                          <span className={`status ${joboffer.status.toLowerCase()}`}>{toTitleCase(joboffer.status).toLocaleUpperCase()}</span>
                        </p>
                      </Col>
                    </Row>
                  </div>
                ))
              )}
            </Col>

            <Col md={4}>
              <div className="sidebar-search p-4">
                <h5>Filter Job Offers</h5>
                <Form>
                  <Form.Group controlId="contractType" className="mb-3">
                    <Form.Label>Contract Type</Form.Label>
                    <Form.Control as="select" name="contractType" value={filters.contractType} onChange={handleFilterChange}>
                      <option value={""}>All</option>
                      {contractTypeList.map((contract, index) => (
                        <option key={index} value={contract}>
                          {toTitleCase(contract)}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>

                  <Form.Group controlId="location" className="mb-3">
                    <Form.Label>Location</Form.Label>
                    <Form.Control type="text" name="location" placeholder="Enter location" value={filters.location} onChange={handleFilterChange} />
                  </Form.Group>

                  <Form.Group controlId="workMode" className="mb-3">
                    <Form.Label>Work Mode</Form.Label>
                    <Form.Control as="select" name="workMode" value={filters.workMode} onChange={handleFilterChange}>
                      <option value="">All</option>
                      {workModeList.map((workMode, index) => (
                        <option key={index} value={workMode}>
                          {toTitleCase(workMode)}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>

                  <Form.Group controlId="status" className="mb-3">
                    <Form.Label>Status</Form.Label>
                    <Form.Control as="select" name="status" value={filters.status} onChange={handleFilterChange}>
                      <option value={""}>All</option>
                      {Object.values(JobOfferState).map((state, index) => (
                        <option key={index} value={state}>
                          {toTitleCase(state)}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>

                  <Form.Group controlId="elementsPerPage" className="mb-3">
                    <Form.Label>Job Offer Per Page</Form.Label>
                    <Form.Control
                      type="text"
                      name="elementsPerPage"
                      placeholder="Enter a number"
                      value={filters.elementsPerPage}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (/^\d*$/.test(value)) {
                          const { name, value } = e.target;
                          setFilters((prevFilters) => ({
                            ...prevFilters,
                            [name]: value,
                          }));
                        }
                      }}
                      onKeyPress={(e) => {
                        if (!/^\d*$/.test(e.key)) {
                          e.preventDefault();
                        }
                      }}
                      min="1"
                    />
                  </Form.Group>

                  <Button
                    className="secondaryButton"
                    variant="primary"
                    onClick={() => setFilters({ contractType: "", location: "", workMode: "", status: "", elementsPerPage: 10 })}
                  >
                    Clear Filters
                  </Button>
                </Form>
              </div>
            </Col>
          </Row>

          {/* Pagination */}
          <Row>
            <Col className="d-flex justify-content-center mt-4">
              <Pagination>
                <Pagination.First onClick={() => changePage(1)} disabled={currentPage === 0} />
                <Pagination.Prev onClick={() => changePage(currentPage - 1)} disabled={currentPage === 0} />

                {Array.from({ length: Math.min(5, totalPages) }, (_, index) => {
                  const startPage = Math.max(Math.min(currentPage - 2, totalPages - 5), 0);
                  const actualPage = startPage + index;

                  return (
                    <Pagination.Item key={actualPage} active={actualPage === currentPage} onClick={() => changePage(actualPage)}>
                      {actualPage + 1} 
                    </Pagination.Item>
                  );
                })}

                <Pagination.Next onClick={() => changePage(currentPage + 1)} disabled={currentPage === totalPages} />
                <Pagination.Last onClick={() => changePage(totalPages)} disabled={currentPage === totalPages} />
              </Pagination>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
};

export default HomePage;
