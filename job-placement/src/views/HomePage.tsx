import { Container, Row, Card, Col, Navbar, Nav, Button, Toast, ToastContainer, Form } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface.ts";
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { useLocation, useNavigate } from "react-router-dom";
import { JobOffer } from "../interfaces/JobOffer.ts";
import JobOfferRequests from "../apis/JobOfferRequests.ts";
import { contractTypeList, statesJobOffer, toTitleCase, workModeList } from "../utils/costants.ts";

function HomePage() {
  const navigate = useNavigate();

  const [jobOffers, setJobOffers] = useState<PagedResponse<JobOffer> | null>(null);
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  const [filters, setFilters] = useState({
    contractType: "",
    location: "",
    workMode: "",
    status: "",
  });

  useEffect(() => {
    if (success != null) {
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }

    const loadJobOffers = async () => {
      try {
        const result = await JobOfferRequests.fetchJobOffers();
        console.log("Job Offers fetched: ", result);
        setJobOffers(result);
        setLoading(false);
      } catch (error) {
        setError(true);
        setLoading(false);
      }
    };

    loadJobOffers();
  }, [success]);

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

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error loading job offers</div>;
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
        <Row>
          <Col md={8}>
            {filteredJobOffers.map((joboffer) => (
              <div key={joboffer.id} className="job-offer-item mb-4 p-3" onClick={() => navigate(`/ui/joboffers/${joboffer.id}`)}>
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
                      <strong>Status:</strong> <span className={`status ${joboffer.status.toLowerCase()}`}>{joboffer.status}</span>
                    </p>
                  </Col>
                </Row>
              </div>
            ))}
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
                    {statesJobOffer.map((state, index) => (
                      <option key={index} value={state}>
                        {toTitleCase(state)}
                      </option>
                    ))}
                  </Form.Control>
                </Form.Group>

                <Button variant="primary" onClick={() => setFilters({ contractType: "", location: "", workMode: "", status: "" })}>
                  Clear Filters
                </Button>
              </Form>
            </div>
          </Col>
        </Row>
      )}
    </Container>
  );
}

export default HomePage;
