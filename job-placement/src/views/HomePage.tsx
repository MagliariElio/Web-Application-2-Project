import { Container, Row, Col, Button, Toast, ToastContainer, Form, Alert, Pagination } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useRef, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { useLocation, useNavigate } from "react-router-dom";
import { JobOffer } from "../interfaces/JobOffer.ts";
import { contractTypeList, JobOfferState, toTitleCase, workModeList } from "../utils/costants.ts";
import { fetchJobOffers } from "../apis/JobOfferRequests.ts";
import { LoadingSection } from "../App.tsx";
import { Filters } from "../interfaces/Filters.ts";

const HomePage = () => {
  const navigate = useNavigate();

  const [jobOffers, setJobOffers] = useState<PagedResponse<JobOffer> | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [filters, setFilters] = useState<Filters>({
    contractType: "",
    location: "",
    workMode: "",
    status: "",
    elementsPerPage: 10,
    sortBy: "",
    sortDirection: "",
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
        const result = await fetchJobOffers(
          page,
          filters.elementsPerPage,
          filters.sortBy,
          filters.sortDirection,
          filters.contractType,
          filters.location,
          filters.status,
          filters.workMode
        );
        setJobOffers(result);
        setTotalPages(result.totalPages);
        setLoading(false);
      } catch (error) {
        if (error instanceof Error) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("An unexpected error occurred");
        }

        // Scroll to error message when it appears
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }
    };

    loadJobOffers(currentPage);
  }, [
    success,
    currentPage,
    filters.elementsPerPage,
    filters.sortBy,
    filters.sortDirection,
    filters.contractType,
    filters.location,
    filters.status,
    filters.workMode,
  ]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const changePage = (pageNumber: number) => {
    if (pageNumber >= totalPages) pageNumber = totalPages - 1;
    if (pageNumber < 0) pageNumber = 0;
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
        <Col md={4}>
          <h3>Job Offers</h3>
        </Col>
        <Col md={2} className="d-flex justify-content-end">
          <Form.Group controlId="sortBy">
            <Form.Select
              name="sortBy"
              value={filters.sortBy}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                const value = e.target.value as "duration" | "value";
                setFilters((prevFilters) => ({
                  ...prevFilters,
                  sortBy: value,
                }));
              }}
            >
              <option value="">Sort By</option>
              <option value="duration">Duration</option>
              <option value="value">Value</option>
            </Form.Select>
          </Form.Group>
        </Col>

        <Col md={2} className="d-flex justify-content-start">
          <Form.Group controlId="sortDirection">
            <Form.Select
              name="sortDirection"
              value={filters.sortDirection}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                const value = e.target.value as "asc" | "desc";
                setFilters((prevFilters) => ({
                  ...prevFilters,
                  sortDirection: value,
                }));
              }}
            >
              <option value="">Sort Direction</option>
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </Form.Select>
          </Form.Group>
        </Col>

        <Col md={2} className="d-flex justify-content-end">
          <Form.Group controlId="elementsPerPage">
            <Form.Select
              name="elementsPerPage"
              value={filters.elementsPerPage}
              onChange={(e) => {
                const value = parseInt(e.target.value, 10);
                setFilters((prevFilters) => ({
                  ...prevFilters,
                  elementsPerPage: value,
                }));
              }}
            >
              <option value="5">5 job offers</option>
              <option value="10">10 job offers</option>
              <option value="15">15 job offers</option>
              <option value="20">20 job offers</option>
              <option value="30">30 job offers</option>
              <option value="50">50 job offers</option>
              <option value="100">100 job offers</option>
            </Form.Select>
          </Form.Group>
        </Col>
        <Col md={2} className="d-flex justify-content-end">
          <Button className="d-flex align-items-center primaryButton" onClick={() => navigate("/ui/joboffers/add")}>
            <BsPlus size={"1.5em"} className="me-1" />
            Add Job Offer
          </Button>
        </Col>
      </Row>

      {errorMessage && (
        <Row className="justify-content-center" ref={errorRef}>
          <Col xs={12} md={10} lg={6}>
            <Alert variant="danger" onClose={() => setErrorMessage("")} className="d-flex mt-3 justify-content-center align-items-center" dismissible>
              {errorMessage}
            </Alert>
          </Col>
        </Row>
      )}

      {loading && <LoadingSection h={null} />}

      {!loading && jobOffers !== null && (
        <>
          <Row>
            <Col md={8}>
              {jobOffers?.content.length === 0 ? (
                <Row className="w-100">
                  <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                    <h5 className="p-5">
                      No job offers found with the selected criteria. Try adjusting the filters, or it could be that no job offers have been added
                      yet.
                    </h5>
                  </Col>
                </Row>
              ) : (
                jobOffers?.content.map((joboffer) => (
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

                  <Button
                    className="secondaryButton"
                    variant="primary"
                    onClick={() =>
                      setFilters({
                        contractType: "",
                        location: "",
                        workMode: "",
                        status: "",
                        elementsPerPage: 10,
                        sortBy: "duration",
                        sortDirection: "asc",
                      })
                    }
                  >
                    Clear Filters
                  </Button>
                </Form>
              </div>
            </Col>
          </Row>

          {/* Pagination */}
          <Row>
            <Col className="d-flex justify-content-center mt-4 custom-pagination">
              <Pagination>
                <Pagination.First onClick={() => changePage(0)} disabled={currentPage === 0} />
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

                <Pagination.Next onClick={() => changePage(currentPage + 1)} disabled={currentPage + 1 === totalPages} />
                <Pagination.Last onClick={() => changePage(totalPages - 1)} disabled={currentPage + 1 === totalPages} />
              </Pagination>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
};

export default HomePage;
