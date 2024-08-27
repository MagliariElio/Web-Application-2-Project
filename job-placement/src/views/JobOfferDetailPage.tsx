import React, { useEffect, useState } from "react";
import { Container, Row, Col, Button, Alert, Form, Modal } from "react-bootstrap";
import { JobOffer } from "../interfaces/JobOffer";
import JobOfferRequests from "../apis/JobOfferRequests";
import { useParams, useNavigate } from "react-router-dom";
import { FaCheckCircle, FaCircle, FaClock, FaMapMarkerAlt, FaMoneyBillWave, FaPen, FaTimesCircle, FaTrash, FaUser, FaUserTie } from "react-icons/fa";
import { contractTypeList, statesJobOffer, toTitleCase, workModeList } from "../utils/costants";
import { MeInterface } from "../interfaces/MeInterface";

const JobOfferDetail = ({ me }: { me: MeInterface }) => {
  const { id } = useParams<{ id: string }>();
  const [jobOffer, setJobOffer] = useState<JobOffer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const abortState = "ABORT";

  // Determine the index of the current state in the progress flow
  const currentStepIndex = statesJobOffer.indexOf(jobOffer?.status || "") + 1;
  const oldStatusIndex = statesJobOffer.indexOf(jobOffer?.oldStatus || "");

  // Function to determine if a state is completed
  const isCompleted = (index: number) => index < currentStepIndex;
  const isCurrent = (index: number) => index === currentStepIndex;
  const isOldStatus = (index: number) => index > oldStatusIndex && jobOffer?.status === abortState;

  const [isEditing, setIsEditing] = useState(false);
  const [formDataJobOffer, setFormDataJobOffer] = useState<JobOffer | null>(null);
  const [singleRequiredSkill, setSingleRequiredSkill] = useState<string>("");

  const [showModalDeleteConfirmation, setShowModalDeleteConfirmation] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      const loadJobOffer = async () => {
        try {
          const result = await JobOfferRequests.fetchJobOfferById(parseInt(id));
          setJobOffer(result);
          setFormDataJobOffer(result);
          setLoading(false);
        } catch (error) {
          setError(true);
          setLoading(false);
        }
      };

      loadJobOffer();
    }
  }, [id]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormDataJobOffer({
      ...formDataJobOffer,
      [e.target.name]: e.target.value,
    } as JobOffer);
  };

  const handleInputSelectChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormDataJobOffer({
      ...formDataJobOffer,
      [e.target.name]: e.target.value,
    } as JobOffer);
  };

  const handleSave = async () => {
    if (formDataJobOffer) {
      try {
        //await JobOfferRequests.updateJobOfferById(parseInt(id), formData); // Assuming there's an API method to update
        setJobOffer(formDataJobOffer);
        setIsEditing(false);
      } catch (error) {
        setError(true);
      }
    }
  };

  const handleDeleteJobOffer = async () => {
    try {
      await JobOfferRequests.deleteJobOfferById(jobOffer?.id ? jobOffer.id : -1, me.xsrfToken);
      navigate("/ui", { state: { success: true } });
    } catch (error) {
      console.error("Failed to delete job offer:", error);
      navigate("/ui", { state: { success: false } });
    }
  };

  const handleCloseModal = () => {
    setShowModalDeleteConfirmation(false);
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
    <Container className="mt-2">
      <ConfirmDeleteModal show={showModalDeleteConfirmation} handleClose={handleCloseModal} handleConfirm={handleDeleteJobOffer} />

      <Row className="justify-content-center align-items-center">
        <div className="progress-container">
          {statesJobOffer.map(
            (state, index) =>
              state !== abortState &&
              !isOldStatus(index) && (
                <div key={index} className={`progress-step ${isCompleted(index) ? "completed" : ""} ${isCurrent(index) ? "current" : ""}`}>
                  {/* Render line connectors */}
                  <div className="circle">{isCompleted(index) ? <FaCheckCircle size={24} className="text-success" /> : <FaCircle size={24} />}</div>
                  <div className="label">{state.replace("_", " ")}</div>
                </div>
              )
          )}

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
        <Form>
          {/* Job Offer Name */}
          <Row className="pt-3 mb-3">
            {!isEditing && (
              <>
                <Col md={9}>
                  <h3 className="font-weight-bold">{jobOffer?.name}</h3>
                </Col>
                <Col md={2} className="d-flex justify-content-end">
                  <Button variant="danger" onClick={() => setShowModalDeleteConfirmation(true)}>
                    <FaTrash /> Delete
                  </Button>
                </Col>
                <Col md={1} className="d-flex justify-content-end">
                  <Button variant="primary" onClick={() => setIsEditing(true)}>
                    <FaPen /> Edit
                  </Button>
                </Col>
              </>
            )}
            {isEditing && (
              <Form.Group as={Row} controlId="jobName" className="d-flex align-items-center">
                <Form.Label column xs={12} sm={2} className="mb-0 fw-bold">
                  Job Offer Name
                </Form.Label>
                <Col xs={12} sm={10}>
                  <Form.Control
                    type="text"
                    name="name"
                    placeholder="Enter a name"
                    value={formDataJobOffer?.name || ""}
                    onChange={handleInputChange}
                    required
                  />
                </Col>
              </Form.Group>
            )}
          </Row>

          {/* Description */}
          <Row className="border-top pt-3 mb-3">
            <Col md={12}>
              {!isEditing ? (
                <div>
                  <strong>Description: </strong>
                  {jobOffer?.description}
                </div>
              ) : (
                <Form.Group as={Row} controlId="jobDescription" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={2} className="mb-0 fw-bold">
                    Description
                  </Form.Label>
                  <Col xs={12} sm={10}>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      name="description"
                      placeholder="Enter a description"
                      value={formDataJobOffer?.description || ""}
                      onChange={handleInputChange}
                      required
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Contract Type and Location */}
          <Row className="border-top pt-3 mb-3">
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaUserTie className="mr-2" /> <strong>Contract Type: </strong>
                  {jobOffer?.contractType}
                </div>
              ) : (
                <Form.Group as={Row} controlId="contractType" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} className="mb-0 fw-bold">
                    Contract Type
                  </Form.Label>
                  <Col xs={12} sm={8}>
                    <Form.Select name="contractType" value={formDataJobOffer?.contractType || ""} onChange={handleInputSelectChange} required>
                      {contractTypeList.map((contract, index) => (
                        <option key={index} value={contract}>
                          {toTitleCase(contract)}
                        </option>
                      ))}
                    </Form.Select>
                  </Col>
                </Form.Group>
              )}
            </Col>
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaMapMarkerAlt className="mr-2" /> <strong>Location: </strong>
                  {jobOffer?.location}
                </div>
              ) : (
                <Form.Group as={Row} controlId="location" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={3} className="mb-0 fw-bold">
                    Location
                  </Form.Label>
                  <Col xs={12} sm={9}>
                    <Form.Control
                      type="text"
                      name="location"
                      placeholder="Enter a location"
                      value={formDataJobOffer?.location || ""}
                      onChange={handleInputChange}
                      required
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Duration and Value */}
          <Row className="border-top pt-3 mb-3">
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaClock className="mr-2" /> <strong>Duration: </strong>
                  {jobOffer?.duration} hours
                </div>
              ) : (
                <Form.Group as={Row} controlId="duration" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} className="mb-0 fw-bold">
                    Duration (hours)
                  </Form.Label>
                  <Col xs={12} sm={8}>
                    <Form.Control
                      type="number"
                      name="duration"
                      placeholder="Duration in hours"
                      value={formDataJobOffer?.duration}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (/^\d*$/.test(value)) {
                          setFormDataJobOffer({
                            ...formDataJobOffer,
                            [e.target.name]: value,
                          } as JobOffer);
                        }
                      }}
                      onKeyPress={(e) => {
                        if (!/^\d*$/.test(e.key)) {
                          e.preventDefault();
                        }
                      }}
                      min="0"
                      required
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaMoneyBillWave className="mr-2" /> <strong>Value: </strong>${jobOffer?.value}
                </div>
              ) : (
                <Form.Group as={Row} controlId="value" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={3} className="mb-0 fw-bold">
                    Value ($)
                  </Form.Label>
                  <Col xs={12} sm={9}>
                    <Form.Control type="text" name="value" value={formDataJobOffer?.value || "0"} disabled />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Work Mode and Status */}
          <Row className="border-top pt-3 mb-3">
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <strong>Work Mode: </strong>
                  {jobOffer?.workMode}
                </div>
              ) : (
                <Form.Group as={Row} controlId="workMode" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} className="mb-0 fw-bold">
                    Work Mode
                  </Form.Label>
                  <Col xs={12} sm={8}>
                    <Form.Select name="workMode" value={formDataJobOffer?.workMode || ""} onChange={handleInputSelectChange} required>
                      {workModeList.map((workMode, index) => (
                        <option key={index} value={workMode}>
                          {toTitleCase(workMode)}
                        </option>
                      ))}
                    </Form.Select>
                  </Col>
                </Form.Group>
              )}
            </Col>
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <strong>Status: </strong>
                  {jobOffer?.status}
                </div>
              ) : (
                <Form.Group as={Row} controlId="status" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={3} className="mb-0 fw-bold">
                    Status
                  </Form.Label>
                  <Col xs={12} sm={9}>
                    <Form.Control type="text" name="status" value={formDataJobOffer?.status || ""} disabled />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Required Skills */}
          <Row className="border-top pt-4">
            <Col>
              <Form.Group as={Row} controlId="status" className="d-flex align-items-center">
                <Form.Label column xs={12} sm={2} className="mb-0 fw-bold">
                  Required Skills
                </Form.Label>
                <Col xs={12} sm={10}>
                  <ul className="list-unstyled d-flex flex-wrap">
                    {formDataJobOffer?.requiredSkills.map((skill, index) => (
                      <li key={index} className="skill-item mb-3 d-flex justify-content-between align-items-center">
                        <span className="skill-text">{skill}</span>
                        {isEditing && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => {
                              setFormDataJobOffer(
                                (prevData) =>
                                  ({
                                    ...prevData,
                                    requiredSkills: prevData?.requiredSkills.filter((_, i) => i !== index),
                                  } as JobOffer)
                              );
                            }}
                          >
                            <FaTrash />
                          </Button>
                        )}
                      </li>
                    ))}
                  </ul>
                </Col>
              </Form.Group>
            </Col>
          </Row>

          {/* Add Skill */}
          {isEditing && (
            <Row className="mb-3 justify-content-end">
              <Col xs={12} md={8} lg={6}>
                <Form.Control
                  placeholder="Add a skill"
                  value={singleRequiredSkill}
                  onChange={(e) => {
                    setSingleRequiredSkill(e.target.value);
                  }}
                  className="mb-2"
                />
              </Col>
              <Col xs={12} md={4} lg={3}>
                <Button
                  variant="primary"
                  onClick={() => {
                    setFormDataJobOffer(
                      (prevData) =>
                        ({
                          ...prevData,
                          requiredSkills: [...(prevData?.requiredSkills || []), singleRequiredSkill],
                        } as JobOffer)
                    );
                    setSingleRequiredSkill("");
                  }}
                >
                  Add Skill
                </Button>
              </Col>
            </Row>
          )}

          {/* Note */}
          <Row className="border-top pt-3 mb-3">
            <Col md={12}>
              {!isEditing ? (
                <div>
                  <strong>Note: </strong>
                  {jobOffer?.note}
                </div>
              ) : (
                <Form.Group as={Row} controlId="note" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={2} className="mb-0 fw-bold">
                    Note
                  </Form.Label>
                  <Col xs={12} sm={10}>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      name="note"
                      placeholder="Enter a note"
                      value={formDataJobOffer?.note || ""}
                      onChange={handleInputChange}
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Customer ID and Professional ID */}
          <Row className="border-top pt-3 mb-3">
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaUser className="mr-2" /> <strong>Customer ID: </strong>
                  {jobOffer?.customerId}
                </div>
              ) : (
                <Form.Group as={Row} controlId="customerId" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} className="mb-0 fw-bold">
                    Customer ID
                  </Form.Label>
                  <Col xs={12} sm={8}>
                    <Form.Control type="text" name="customerId" value={formDataJobOffer?.customerId || ""} onChange={handleInputChange} disabled />
                  </Col>
                </Form.Group>
              )}
            </Col>
            <Col md={6}>
              {!isEditing ? (
                <div>
                  <FaUser className="mr-2" /> <strong>Professional ID: </strong>
                  {jobOffer?.professionalId}
                </div>
              ) : (
                <Form.Group as={Row} controlId="professionalId" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={3} className="mb-0 fw-bold">
                    Professional ID
                  </Form.Label>
                  <Col xs={12} sm={9}>
                    <Form.Control
                      type="text"
                      name="professionalId"
                      value={formDataJobOffer?.professionalId || ""}
                      onChange={handleInputChange}
                      disabled
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Candidate Professionals */}
          <Row className="border-top pt-3 mb-3">
            <Col md={12}>
              {!isEditing ? (
                <div>
                  <strong>Candidate Professionals: </strong>
                  {jobOffer?.candidateProfessionalIds.join(", ")}
                </div>
              ) : (
                <Form.Group as={Row} controlId="candidateProfessionalIds" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} md={2} className="mb-0 fw-bold">
                    Candidate Professionals
                  </Form.Label>
                  <Col xs={12} sm={8} md={10}>
                    <Form.Control
                      type="text"
                      name="candidateProfessionalIds"
                      value={formDataJobOffer?.candidateProfessionalIds.join(", ") || ""}
                      disabled
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {isEditing && (
            <Row className="mt-5 mb-3">
              <Col className="text-center">
                <Button
                  variant="danger"
                  size="lg"
                  onClick={() => {
                    setFormDataJobOffer(jobOffer);
                    setIsEditing(false);
                  }}
                >
                  Cancel
                </Button>
              </Col>
              <Col className="text-center">
                <Button variant="secondary" size="lg" onClick={handleSave}>
                  Save
                </Button>
              </Col>
            </Row>
          )}

          {!isEditing && (
            <Row className="mt-5">
              <Col className="text-center">
                <Button variant="secondary" size="lg" onClick={() => navigate(-1)}>
                  Go Back
                </Button>
              </Col>
            </Row>
          )}
        </Form>
      </div>
    </Container>
  );
};

const ConfirmDeleteModal: React.FC<{
  show: boolean;
  handleClose: () => void;
  handleConfirm: () => void;
}> = ({ show, handleClose, handleConfirm }) => {
  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>Confirm Delete</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>
          Are you sure you want to delete this job offer? This action is
          <strong> irreversible</strong>.
        </p>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Cancel
        </Button>
        <Button variant="danger" onClick={handleConfirm}>
          Confirm
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default JobOfferDetail;
