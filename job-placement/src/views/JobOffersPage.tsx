import { Container, Row, Col, Button, Toast, ToastContainer, Form, Alert, Pagination, ButtonGroup } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useRef, useState } from "react";
import { BsPlus, BsSearch } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse.ts";
import { useLocation, useNavigate } from "react-router-dom";
import { JobOffer } from "../interfaces/JobOffer.ts";
import { contractTypeList, JobOfferState, RoleState, toTitleCase, workModeList } from "../utils/costants.ts";
import { fetchJobOffers } from "../apis/JobOfferRequests.ts";
import { Filters } from "../interfaces/Filters.ts";
import { MeInterface } from "../interfaces/MeInterface.ts";
import { convertLocalDateTimeToDate } from "../utils/checkers.ts";
import {
  FaBuilding,
  FaCalendarAlt,
  FaCheck,
  FaCheckCircle,
  FaClock,
  FaEnvelope,
  FaExchangeAlt,
  FaLaptopHouse,
  FaMapMarkerAlt,
  FaMoneyBillWave,
  FaPencilAlt,
  FaTimesCircle,
  FaUserTie,
} from "react-icons/fa";
import { FaListCheck } from "react-icons/fa6";

const JobOffersPage: React.FC<{ me: MeInterface }> = ({ me }) => {
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

  const loadJobOffers = async (page: number) => {
    try {
      setLoading(true);
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

    setLoading(false);
  };

  const handleFilterClick = () => {
    loadJobOffers(currentPage);
  };

  useEffect(() => {
    if (success != null) {
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }

    loadJobOffers(currentPage);
  }, [success, currentPage, filters.elementsPerPage, filters.sortBy, filters.sortDirection]);

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

      <Row className="d-flex flex-row p-0 mb-1 align-items-center">
        <Col xs={6} lg={3} xl={2}>
          <h3 className="title">Job Offers</h3>
        </Col>

        <Col xs={6} lg={3} xl={2} className="d-flex justify-content-end">
          <Form.Group controlId="sortBy">
            <Form.Select
              style={{ width: "auto" }}
              name="sortBy"
              value={filters.sortBy}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                const value = e.target.value as "duration" | "value" | "";
                setFilters((prevFilters) => ({
                  ...prevFilters,
                  sortBy: value,
                  sortDirection: value === "" ? "" : filters.sortDirection,
                }));
              }}
            >
              <option value="">Sort By</option>
              <option value="duration">Duration</option>
              <option value="value">Value</option>
            </Form.Select>
          </Form.Group>
        </Col>

        <Col xs={6} lg={3} xl={2} className="d-flex justify-content-start justify-content-lg-end">
          <Form.Group controlId="sortDirection">
            <Form.Select
              style={{ width: "auto" }}
              name="sortDirection"
              value={filters.sortDirection}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                const value = e.target.value as "asc" | "desc";
                setFilters((prevFilters) => ({
                  ...prevFilters,
                  sortDirection: value,
                }));
              }}
              disabled={filters.sortBy === ""}
            >
              {filters.sortBy === "" && <option value="">Sort Direction</option>}
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </Form.Select>
          </Form.Group>
        </Col>

        <Col xs={6} lg={3} xl={2} className="d-flex justify-content-end">
          <Form.Group controlId="elementsPerPage">
            <Form.Select
              style={{ width: "auto" }}
              name="elementsPerPage"
              value={filters.elementsPerPage}
              onChange={(e) => {
                setCurrentPage(0);
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
        {me.role === RoleState.OPERATOR && (
          <Col xs={12} xl={4} className="d-flex justify-content-center justify-content-xl-end">
            <Button
              className="d-flex align-items-center primaryButton me-4 me-xl-0 mt-2 mt-xl-0 mb-2 mb-xl-0"
              onClick={() => navigate("/ui/joboffers/add")}
            >
              <BsPlus size={"1.5em"} className="me-1" />
              Add Job Offer
            </Button>
          </Col>
        )}
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

      {!loading && jobOffers !== null && (
        <>
          <Row>
            <Col xs={12} lg={8} className="order-2 order-lg-1 mt-3">
              {jobOffers?.content.length === 0 ? (
                <Row className="w-100">
                  <Col className="w-100 d-flex flex-column justify-content-center align-items-center mt-5">
                    <h5 className="p-3 text-center">No job offers found with the selected criteria.</h5>
                    <h5 className="p-3 text-center">Try adjusting the filters, or it could be that no job offers have been added yet.</h5>
                  </Col>
                </Row>
              ) : (
                jobOffers?.content.map((joboffer) => (
                  <div
                    key={joboffer.id}
                    className="job-offer-item mb-4 p-3"
                    onClick={() => navigate(`/ui/joboffers/${joboffer.id}`, { state: { jobOfferSelected: joboffer } })}
                  >
                    <JobOfferCard joboffer={joboffer} />
                  </div>
                ))
              )}

              {/* Pagination */}
              {jobOffers?.content.length > 0 && (
                <Row className="mt-auto">
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
              )}
            </Col>

            <Col xs={12} lg={4} className="order-1 order-lg-2 mt-3 mb-4">
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

                  <ButtonGroup className="d-flex justify-content-center mt-4 flex-lg-column flex-xl-row w-100">
                    <Col className="text-center mb-lg-2 mb-xl-0 me-2 me-lg-0 me-xl-2 w-100 d-flex align-items-center">
                      <Button className="primaryButton w-100" variant="primary" onClick={handleFilterClick}>
                        <BsSearch className="me-1" />
                        Filter
                      </Button>
                    </Col>

                    <Col className="text-center w-100 d-flex align-items-center">
                      <Button
                        className="secondaryButton w-100"
                        variant="primary"
                        onClick={() =>
                          setFilters({
                            contractType: "",
                            location: "",
                            workMode: "",
                            status: "",
                            elementsPerPage: filters.elementsPerPage,
                            sortBy: "",
                            sortDirection: "",
                          })
                        }
                      >
                        Clear Filters
                      </Button>
                    </Col>
                  </ButtonGroup>
                </Form>
              </div>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
};

export const JobOfferCard: React.FC<{ joboffer: JobOffer }> = ({ joboffer }) => {
  return (
    <div>
      <Row className="align-items-center mb-2">
        <Col xs={12}>
          <h5 className="job-title">{joboffer.name}</h5>
        </Col>
      </Row>
      <Row className="d-flex align-items-center">
        <Col md={4} xs={12}>
          <p>
            <FaUserTie className="me-1" /> <strong>Contract Type: </strong>
            {joboffer.contractType}
          </p>
        </Col>
        <Col md={4} xs={12}>
          <p>
            <FaMapMarkerAlt className="me-1" /> <strong>Location: </strong>
            {joboffer.location}
          </p>
        </Col>
        <Col md={4} xs={12} className="d-flex align-items-center">
          <p>
            <FaClock className="me-1" /> <strong className="me-1">Creation Time: </strong>
            {convertLocalDateTimeToDate(joboffer?.creationTime).toLocaleDateString()}
          </p>
        </Col>
      </Row>
      <Row className="d-flex align-items-center">
        <Col md={4} xs={12}>
          <p>
            {joboffer?.workMode === "Remote" && <FaLaptopHouse className="me-2" />}
            {joboffer?.workMode === "Hybrid" && <FaBuilding className="me-2" />}
            {joboffer?.workMode === "In-Person" && <FaExchangeAlt className="me-2" />}
            <strong className="me-1">Work Mode:</strong>
            {joboffer.workMode}
          </p>
        </Col>
        <Col md={4} xs={12}>
          <p>
            <FaClock className="me-1" /> <strong>Duration: </strong>
            {joboffer.duration} {joboffer.duration === 1 ? "day" : "days"}
          </p>
        </Col>
        {joboffer?.endTime && (
          <Col md={4} xs={12} className="d-flex align-items-center">
            <p>
              <FaCalendarAlt className="me-2" /> <strong className="me-1">End Time: </strong>
              {convertLocalDateTimeToDate(joboffer?.endTime).toLocaleDateString()}
            </p>
          </Col>
        )}
      </Row>
      <Row className="d-flex align-items-center">
        <Col md={4} xs={12}>
          <p>
            <FaMoneyBillWave className="me-1" /> <strong>Value: </strong>
            {joboffer.value} €
          </p>
        </Col>
        <Col md={4} xs={12}>
          <p>
            {joboffer?.status === JobOfferState.CREATED && <FaPencilAlt className="me-2" />}
            {joboffer?.status === JobOfferState.SELECTION_PHASE && <FaListCheck className="me-2" />}
            {joboffer?.status === JobOfferState.CANDIDATE_PROPOSAL && <FaEnvelope className="me-2" />}
            {joboffer?.status === JobOfferState.CONSOLIDATED && <FaCheck className="me-2" />}
            {joboffer?.status === JobOfferState.DONE && <FaCheckCircle className="me-2" />}
            {joboffer?.status === JobOfferState.ABORT && <FaTimesCircle className="me-2" />}
            <strong className="me-1">Status: </strong>
            <span className={`status ${joboffer.status.toLowerCase()}`}>{toTitleCase(joboffer.status).toLocaleUpperCase()}</span>
          </p>
        </Col>
      </Row>
    </div>
  );
};

export default JobOffersPage;
