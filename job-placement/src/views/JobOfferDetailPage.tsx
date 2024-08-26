import React, { useEffect, useState } from "react";
import { Card, Container, Row, Col, Button, Alert, ProgressBar } from "react-bootstrap";
import { JobOffer } from "../interfaces/JobOffer";
import JobOfferRequests from "../apis/JobOfferRequests";
import { useParams, useNavigate } from "react-router-dom";
import { FaCheckCircle, FaCircle, FaClipboardList, FaClock, FaDotCircle, FaMapMarkerAlt, FaMoneyBillWave, FaTimesCircle, FaUser, FaUserTie } from "react-icons/fa";

const JobOfferDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [jobOffer, setJobOffer] = useState<JobOffer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const states = ["CREATED", "SELECTION_PHASE", "CANDIDATE_PROPOSAL", "CONSOLIDATED", "ABORT", "DONE"];
  const abortState = "ABORT";
  
  // Determine the index of the current state in the progress flow
  const currentStepIndex = states.indexOf(jobOffer?.status || "") + 1;
  const oldStatusIndex = states.indexOf(jobOffer?.oldStatus || "");

  // Function to determine if a state is completed
  const isCompleted = (index: number) => index < currentStepIndex;
  const isCurrent = (index: number) => index === currentStepIndex;
  const isOldStatus = (index: number) => index > oldStatusIndex && jobOffer?.status === abortState;
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      const loadJobOffer = async () => {
        try {
          const result = await JobOfferRequests.fetchJobOfferById(parseInt(id));
          result.oldStatus = "CONSOLIDATED"
          result.status = "DONE"
          setJobOffer(result);
          setLoading(false);
        } catch (error) {
          setError(true);
          setLoading(false);
        }
      };

      loadJobOffer();
    }
  }, [id]);

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
    <Container className="mt-2">
      <Row className="justify-content-center align-items-center">
        <div className="progress-container">
          {states.map((state, index) => (
            state !== abortState && !isOldStatus(index) &&
            <div
              key={index}
              className={`progress-step ${isCompleted(index) ? "completed" : ""} ${isCurrent(index) ? "current" : ""}`}
            >
              {/* Render line connectors */}
              <div className="circle">
                {isCompleted(index) ? <FaCheckCircle size={24} className="text-success" /> : <FaCircle size={24} />}
              </div>
              <div className="label">{state.replace("_", " ")}</div>
            </div>
          ))}

          {/* Render the ABORT state */}
          {jobOffer?.status === abortState && (
            <div className="progress-step abort">
              <div className="circle">
                <FaTimesCircle size={24} className="text-danger" />
              </div>
              <div className="label">{abortState}</div>
            </div>
          )}
        </div>
      </Row>

      <div className="border rounded p-3 shadow-sm bg-white mt-4">
        <Row className="mb-2">
          <Col>
            <h3 className="font-weight-bold">{jobOffer?.name}</h3>
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <FaClipboardList className="mr-2" /> <strong>Description:</strong>
            <p className="d-inline ml-2">{jobOffer?.description}</p>
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col md={6} className="text-muted">
            <FaUserTie className="mr-2" /> <strong>Contract Type:</strong> {jobOffer?.contractType}
          </Col>
          <Col md={6} className="text-muted">
            <FaMapMarkerAlt className="mr-2" /> <strong>Location:</strong> {jobOffer?.location}
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col md={6} className="text-muted">
            <FaClock className="mr-2" /> <strong>Duration:</strong> {jobOffer?.duration} hours
          </Col>
          <Col md={6} className="text-muted">
            <FaMoneyBillWave className="mr-2" /> <strong>Value:</strong> ${jobOffer?.value}
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <strong>Work Mode:</strong> {jobOffer?.workMode}
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <strong>Status:</strong> {jobOffer?.status}
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <strong>Required Skills:</strong>
            <ul className="list-unstyled d-flex flex-wrap">
              {jobOffer?.requiredSkills.map((skill, index) => (
                <li key={index} className="skill-item mr-2 mb-2">
                  {skill}
                </li>
              ))}
            </ul>
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <strong>Note:</strong> {jobOffer?.note}
          </Col>
        </Row>

        <Row className="border-top pt-3 mb-2">
          <Col className="text-muted">
            <FaUser className="mr-2" /> <strong>Customer ID:</strong> {jobOffer?.customerId}
          </Col>
          <Col className="text-muted">
            <FaUser className="mr-2" /> <strong>Professional ID:</strong> {jobOffer?.professionalId}
          </Col>
        </Row>

        <Row className="border-top pt-3">
          <Col className="text-muted">
            <strong>Candidate Professionals:</strong> {jobOffer?.candidateProfessionalIds.join(", ")}
          </Col>
        </Row>

        <Row className="mt-3">
          <Col className="text-center">
            <Button variant="secondary" size="lg" onClick={() => navigate(-1)}>
              Go Back
            </Button>
          </Col>
        </Row>
      </div>
    </Container>
  );
};

export default JobOfferDetail;
