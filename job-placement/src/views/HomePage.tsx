import { Container, Row, Card, Col, Navbar, Nav, Button, Toast, ToastContainer } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface.ts";
import "bootstrap/dist/css/bootstrap.min.css";
import { useEffect, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { useLocation, useNavigate } from "react-router-dom";
import { JobOffer } from "../interfaces/JobOffer.ts";
import JobOfferRequests from "../apis/JobOfferRequests.ts";

function HomePage() {
  const navigate = useNavigate();

  const [jobOffers, setJobOffers] = useState<PagedResponse<JobOffer> | null>(null);
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

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error loading job offers</div>;
  }

  return (
    <div>
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
        <div className="job-offer-container">
          <Row className="g-4">
            {jobOffers.content.map((joboffer) => (
              <Col key={joboffer.id} md={4} className="mb-4 d-flex align-items-stretch">
                <Card className="job-card flex-fill" onClick={() => navigate("/ui/joboffers/" + joboffer.id)}>
                  <Card.Body>
                    <Card.Title className="mb-3 job-title">{joboffer.name}</Card.Title>
                    <Card.Text>
                      <strong>Contract Type:</strong> {joboffer.contractType}
                    </Card.Text>
                    <Card.Text>
                      <strong>Location:</strong> {joboffer.location}
                    </Card.Text>
                    <Card.Text>
                      <strong>Work Mode:</strong> {joboffer.workMode}
                    </Card.Text>
                    <Card.Text>
                      <strong>Duration:</strong> {joboffer.duration} hours
                    </Card.Text>
                    <Card.Text>
                      <strong>Value:</strong> ${joboffer.value}
                    </Card.Text>
                    <Card.Text>
                      <strong>Status:</strong> <span className={`status ${joboffer.status.toLowerCase()}`}>{joboffer.status}</span>
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      )}
    </div>
  );
}

export default HomePage;
