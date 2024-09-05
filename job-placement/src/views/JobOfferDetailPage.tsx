import React, { useEffect, useRef, useState } from "react";
import { Container, Row, Col, Button, Alert, Form, Modal, Table, Pagination, ButtonGroup } from "react-bootstrap";
import { JobOffer } from "../interfaces/JobOffer";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import {
  FaCheck,
  FaCheckCircle,
  FaCircle,
  FaClock,
  FaMapMarkerAlt,
  FaMoneyBillWave,
  FaPen,
  FaTimesCircle,
  FaTrash,
  FaUser,
  FaUserTie,
} from "react-icons/fa";
import { contractTypeList, JobOfferState, toTitleCase, workModeList } from "../utils/costants";
import { MeInterface } from "../interfaces/MeInterface";
import { fetchCustomer } from "../apis/CustomerRequests";
import { Customer } from "../interfaces/Customer";
import { fetchProfessional, fetchProfessionals } from "../apis/ProfessionalRequests";
import { Professional } from "../interfaces/Professional";
import {
  abortJobOffer,
  cancelCandidation,
  deleteJobOfferById,
  doneJobOffer,
  fetchJobOfferById,
  goToCandidateProposalPhase,
  goToCondolidated,
  goToSelectionPhase,
  updateJobOffer,
} from "../apis/JobOfferRequests";
import { LoadingSection } from "../App";

const JobOfferDetail = ({ me }: { me: MeInterface }) => {
  const { id } = useParams<{ id: string }>();
  const [jobOffer, setJobOffer] = useState<JobOffer | null>(null);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [professional, setProfessional] = useState<Professional | null>(null);
  const [candidateProfessionalList, setCandidateProfessionalList] = useState<Professional[]>([]);
  const [loadingCandidateProfessional, setLoadingCandidateProfessional] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const abortState = "ABORT";

  const location = useLocation();
  //const { jobOfferSelected } = location.state || { jobOfferSelected: null };

  // Determine the index of the current state in the progress flow
  const currentStepIndex = Object.values(JobOfferState).indexOf(jobOffer?.status as JobOfferState) + 1;
  const oldStatusIndex = Object.values(JobOfferState).indexOf(jobOffer?.oldStatus as JobOfferState);

  // Function to determine if a state is completed
  const isCompleted = (index: number) => index < currentStepIndex;
  const isCurrent = (index: number) => index === currentStepIndex;
  const isOldStatus = (index: number) => index > oldStatusIndex && jobOffer?.status === abortState;

  // Job Offer Props
  const [isEditing, setIsEditing] = useState(false);
  const [formDataJobOffer, setFormDataJobOffer] = useState<JobOffer | null>(null);
  const [singleRequiredSkill, setSingleRequiredSkill] = useState<string>("");
  const [errorMessage, setErrorMessage] = useState("");

  // Delete Job Offer
  const [showModalDeleteConfirmation, setShowModalDeleteConfirmation] = useState(false);

  // Add Professional Candidate
  const [showProfessionalCandidateModal, setProfessionalCandidateModal] = useState(false);
  const handleOpenProfessionalCandidateModal = () => setProfessionalCandidateModal(true);
  const handleCloseProfessionalCandidateModal = () => setProfessionalCandidateModal(false);
  const handleAddCandidate = (professional: Professional) => {
    setCandidateProfessionalList((prevList) => [...prevList, professional]);
  };

  const errorRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();

  const loadCandidateProfessionals = async (candidateList: number[]) => {
    try {
      setLoadingCandidateProfessional(true);
      setCandidateProfessionalList([]);

      const resultList: Professional[] = await Promise.all(
        candidateList.map(async (id) => {
          const result = await fetchProfessional(id);
          return result;
        })
      );

      setCandidateProfessionalList(resultList);
      setLoadingCandidateProfessional(false);
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

      setLoadingCandidateProfessional(false);
      setCandidateProfessionalList([]);
    }
  };

  const loadJobOffer = async (jobOfferSelected: JobOffer | null) => {
    try {
      var result = null;
      setLoading(true);

      if (jobOfferSelected) {
        result = jobOfferSelected;
      } else if (id) {
        result = await fetchJobOfferById(parseInt(id));
      } else {
        return;
      }

      setJobOffer(result);
      setFormDataJobOffer(result);

      const resultCustomer = await fetchCustomer(result?.customerId);
      setCustomer(resultCustomer);

      setProfessional(null);
      if (result?.professionalId) {
        const resultProfessional = await fetchProfessional(result?.professionalId);
        setProfessional(resultProfessional);
      }

      loadCandidateProfessionals(result?.candidateProfessionalIds);

      setLoading(false);
    } catch (error) {
      console.error(error);
      setError(true);
      setLoading(false);
    }
  };

  useEffect(() => {
    //loadJobOffer(jobOfferSelected);
    loadJobOffer(null);
  }, [id]);

  const handleAddSkill = () => {
    if (singleRequiredSkill.trim() === "") {
      setErrorMessage("Please enter a skill before adding.");

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }

      return;
    }

    setFormDataJobOffer(
      (prevData) =>
        ({
          ...prevData,
          requiredSkills: [...(prevData?.requiredSkills || []), singleRequiredSkill],
        } as JobOffer)
    );

    setSingleRequiredSkill("");
    setErrorMessage("");
  };

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
      if (formDataJobOffer?.name.trim() === "") {
        setErrorMessage("Job offer name cannot be empty or just spaces.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        return;
      }

      if (formDataJobOffer?.description.trim() === "") {
        setErrorMessage("Description cannot be empty or just spaces.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        return;
      }

      if (formDataJobOffer?.location.trim() === "") {
        setErrorMessage("Location cannot be empty or just spaces.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        return;
      }

      if (formDataJobOffer?.requiredSkills.length === 0) {
        setErrorMessage("You must add at least one required skill before saving.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        return;
      }

      try {
        if (id) {
          setFormDataJobOffer({
            ...formDataJobOffer,
            ["id"]: parseInt(id),
          } as JobOffer);

          const result = await updateJobOffer(formDataJobOffer, me.xsrfToken);
          setJobOffer(result);
          setIsEditing(false);
        }
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
    }
  };

  const handleDeleteJobOffer = async () => {
    try {
      await deleteJobOfferById(jobOffer?.id ? jobOffer.id : -1, me.xsrfToken);
      navigate("/ui", { state: { success: true } });
    } catch (error) {
      console.error("Failed to delete job offer:", error);
      navigate("/ui", { state: { success: false } });
    }
  };

  const handleCloseModal = () => {
    setShowModalDeleteConfirmation(false);
  };

  /**
   * Handles the transition of a job offer to the selection phase.
   *
   * This function gathers the list of selected professionals and sends a PATCH request
   * to update the job offer's status to "SELECTION_PHASE". If the request is successful,
   * it reloads the job offer data. In case of an error, it sets an appropriate error message
   * and ensures that the error message is scrolled into view.
   *
   * The function also manages the loading state during the asynchronous operations.
   */
  const handleGoToSelectionPhase = async () => {
    const jobOffer = {
      professionalsId: candidateProfessionalList.map((p: Professional) => p.id),
    };

    if (candidateProfessionalList?.length === 0) {
      setErrorMessage("Select at least one candidate!");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setLoading(true);
      const jobOfferResponse = await goToSelectionPhase(parseInt(id ? id : ""), me.xsrfToken, jobOffer);

      await loadJobOffer(jobOfferResponse);

      setErrorMessage("");
      setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setLoading(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  /**
   * Handles the abortion of a job offer.
   *
   * This function sends a request to abort (cancel) the job offer. If the request
   * is successful, it reloads the job offer data to reflect the changes. In case of an error,
   * it sets an appropriate error message and ensures that the error message is scrolled into view.
   *
   * The function also manages the loading state during the asynchronous operations.
   */
  const handleAbortJobOffer = async () => {
    try {
      setLoading(true);
      const jobOfferResponse = await abortJobOffer(parseInt(id ? id : ""), me.xsrfToken);

      await loadJobOffer(jobOfferResponse);

      setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setLoading(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  const handleSelectCandidateProfessional = async (indexCandidate: number) => {
    const candidate = candidateProfessionalList[indexCandidate];
    try {
      setLoading(true);

      const jobOfferResponse = await goToCandidateProposalPhase(parseInt(id ? id : ""), me.xsrfToken, candidate.id);
      await loadJobOffer(jobOfferResponse);

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

      setLoading(false);
    }
  };

  const handleGoToConsolidated = async () => {
    if (jobOffer?.status !== JobOfferState.CANDIDATE_PROPOSAL || !professional?.id) {
      setErrorMessage("This action is not available in this moment.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setLoading(true);
      const jobOfferResponse = await goToCondolidated(parseInt(id ? id : ""), me.xsrfToken, professional.id);

      await loadJobOffer(jobOfferResponse);

      setErrorMessage("");
      setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setLoading(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  const handleCancelCadidation = async () => {
    if (jobOffer?.status !== JobOfferState.CANDIDATE_PROPOSAL) {
      setErrorMessage("This action is not available in this moment.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setLoading(true);
      const jobOfferResponse = await cancelCandidation(parseInt(id ? id : ""), me.xsrfToken, candidateProfessionalList);

      console.log(jobOfferResponse);

      await loadJobOffer(jobOfferResponse);

      setErrorMessage("");
      setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setLoading(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  const handleDoneJobOffer = async () => {
    if (jobOffer?.status !== JobOfferState.CONSOLIDATED || !professional?.id) {
      setErrorMessage("This action is not available in this moment.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setLoading(true);
      const jobOfferResponse = await doneJobOffer(parseInt(id ? id : ""), me.xsrfToken, professional.id);

      await loadJobOffer(jobOfferResponse);

      setErrorMessage("");
      setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setLoading(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  const handleDeleteCandidateProfessional = (indexToRemove: number) => {
    const updatedList = candidateProfessionalList.filter((professional, index) => index !== indexToRemove);
    setCandidateProfessionalList(updatedList);
  };

  if (loading) {
    return <LoadingSection h={null} />;
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
          {Object.values(JobOfferState).map(
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
          {errorMessage && (
            <Row className="justify-content-center" ref={errorRef}>
              <Col xs={12} md={10} lg={6}>
                <Alert
                  variant="danger"
                  onClose={() => setErrorMessage("")}
                  className="d-flex mt-3 justify-content-center align-items-center"
                  dismissible
                >
                  {errorMessage}
                </Alert>
              </Col>
            </Row>
          )}

          {/* Job Offer Name */}
          <Row className="pt-3 mb-3">
            {!isEditing && (
              <>
                <Col md={9}>
                  <h3 className="font-weight-bold">{jobOffer?.name}</h3>
                </Col>
                {jobOffer?.status !== JobOfferState.ABORT && jobOffer?.status !== JobOfferState.DONE && (
                  <>
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
                  {toTitleCase(jobOffer?.status || JobOfferState.CREATED)}
                </div>
              ) : (
                <Form.Group as={Row} controlId="status" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={3} className="mb-0 fw-bold">
                    Status
                  </Form.Label>
                  <Col xs={12} sm={9}>
                    <Form.Control type="text" name="status" value={toTitleCase(formDataJobOffer?.status || JobOfferState.CREATED)} disabled />
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

                {formDataJobOffer?.requiredSkills.length === 0 ? (
                  <Col xs={12} className="text-center">
                    <p className="text-muted">No required skill added yet</p>
                  </Col>
                ) : (
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
                )}
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
                <Button variant="primary" onClick={handleAddSkill}>
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
                  {jobOffer?.note || "Any note found!"}
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

          {/* Customer Section */}
          <Row className="border-top pt-3 mb-3">
            <Col xs={12}>
              <Form.Group as={Row} controlId="professionalId">
                <Form.Label column xs={6} className="fw-bold">
                  Customer Information
                </Form.Label>

                {/* Buttons Section */}
                <Col xs={6} className="text-end">
                  <Button variant="primary" className="me-2" onClick={() => {}}>
                    Profile
                  </Button>
                </Col>

                <Col xs={12} md={6} className="mb-3">
                  <Form.Label>Surname</Form.Label>
                  <Form.Control type="text" name="professionalSurname" value={customer?.information.contactDTO.surname || ""} disabled />
                </Col>
                <Col xs={12} md={6} className="mb-3">
                  <Form.Label>Name</Form.Label>
                  <Form.Control type="text" name="professionalName" value={customer?.information.contactDTO.name || ""} disabled />
                </Col>
                <Col xs={12} md={6} className="mb-3">
                  <Form.Label>SSN Code</Form.Label>
                  <Form.Control type="text" name="professionalSSnCode" value={customer?.information.contactDTO.ssnCode || ""} disabled />
                </Col>
              </Form.Group>
            </Col>
          </Row>

          {/* Professional Section */}
          {professional && (
            <Row className="border-top pt-3 mb-3">
              <Col xs={12}>
                <Form.Group as={Row} controlId="professionalId">
                  <Form.Label column xs={6} className="fw-bold">
                    Professional Information
                  </Form.Label>

                  {/* Buttons Section */}
                  <Col xs={6} className="text-end">
                    <Button variant="primary" className="me-2" onClick={() => {}}>
                      Profile
                    </Button>
                    {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL && (
                      <>
                        <Button variant="success" className="me-2" onClick={handleGoToConsolidated}>
                          Confirm Candidation
                        </Button>
                        <Button variant="danger" onClick={handleCancelCadidation}>
                          Cancel Candidation
                        </Button>
                      </>
                    )}
                  </Col>

                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>Surname</Form.Label>
                    <Form.Control type="text" name="professionalSurname" value={professional?.information.surname || ""} disabled />
                  </Col>
                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>Name</Form.Label>
                    <Form.Control type="text" name="professionalName" value={professional?.information.name || ""} disabled />
                  </Col>
                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>SSN Code</Form.Label>
                    <Form.Control type="text" name="professionalSSnCode" value={professional?.information.ssnCode || ""} disabled />
                  </Col>
                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>Daily Rate</Form.Label>
                    <Form.Control
                      type="text"
                      name="professionalDailyRate"
                      value={professional?.dailyRate ? `${professional.dailyRate} €` : ""}
                      disabled
                    />
                  </Col>
                </Form.Group>
              </Col>
            </Row>
          )}

          {/* Candidate Professionals */}
          {!professional && !isEditing && (
            <Row className="border-top pt-3 mb-3">
              <Col md={9} className="d-flex align-items-center">
                <strong>Candidate Professionals: </strong>
              </Col>
              {jobOffer?.status !== JobOfferState.ABORT && (
                <>
                  <Col md={2} className="text-end">
                    <Button variant="primary" onClick={handleOpenProfessionalCandidateModal}>
                      Add Candidate
                    </Button>
                  </Col>
                  {jobOffer?.candidateProfessionalIds.length !== candidateProfessionalList.length && !loadingCandidateProfessional && (
                    <Col md={1} className="text-end">
                      {/* Si attiva quando si aggiunge un nuovo professional candidato */}
                      <Button variant="success" onClick={handleGoToSelectionPhase}>
                        Confirm
                      </Button>
                    </Col>
                  )}
                </>
              )}

              {jobOffer?.status === JobOfferState.SELECTION_PHASE && (
                <Col md={12} className="mt-2">
                  <p style={{ color: "gray", fontSize: "0.9rem" }}>Please select one of the candidates to propose them for this job offer.</p>
                </Col>
              )}

              <Col md={12} className="mt-3">
                {loadingCandidateProfessional && <LoadingSection h={100} />}
                {showProfessionalCandidateModal && !loadingCandidateProfessional && (
                  <CandidateProfessionalModal
                    show={showProfessionalCandidateModal}
                    alreadyExistentCandidates={candidateProfessionalList}
                    handleClose={handleCloseProfessionalCandidateModal}
                    onSelectProfessional={handleAddCandidate}
                  />
                )}
                {candidateProfessionalList?.length > 0 && !loadingCandidateProfessional && (
                  <Table striped bordered hover className="align-middle">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th>Surname</th>
                        <th>SSN Code</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {candidateProfessionalList.map((professional, index) => (
                        <tr key={index}>
                          <td>{index + 1}</td>
                          <td>{professional.information.name}</td>
                          <td>{professional.information.surname}</td>
                          <td>{professional.information.ssnCode}</td>
                          <td className="text-center">
                            <ButtonGroup>
                              {jobOffer?.status !== JobOfferState.CREATED && (
                                <Button
                                  variant="success"
                                  className="me-2"
                                  onClick={() => handleSelectCandidateProfessional(index)}
                                  disabled={
                                    jobOffer?.status !== JobOfferState.SELECTION_PHASE ||
                                    jobOffer?.candidateProfessionalIds.length !== candidateProfessionalList.length ||
                                    loadingCandidateProfessional
                                  }
                                >
                                  <FaCheck />
                                </Button>
                              )}
                              <Button
                                variant="danger"
                                onClick={() => handleDeleteCandidateProfessional(index)}
                                disabled={jobOffer?.status === JobOfferState.ABORT || jobOffer?.status === JobOfferState.DONE}
                              >
                                <FaTrash />
                              </Button>
                            </ButtonGroup>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
                {candidateProfessionalList?.length === 0 && !loadingCandidateProfessional && <p>No candidate professionals.</p>}
              </Col>
            </Row>
          )}

          {isEditing && (
            <Row className="mt-5 mb-3">
              <Col className="text-center">
                <Button
                  className="secondaryDangerButton mb-2"
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
                <Button className="primaryButton mb-2" variant="primary" size="lg" onClick={handleSave}>
                  Save
                </Button>
              </Col>
            </Row>
          )}

          {!isEditing && (
            <Row className="mt-5">
              {jobOffer?.status !== JobOfferState.ABORT && jobOffer?.status !== JobOfferState.DONE && (
                <Col className="text-center">
                  <Button className="secondaryDangerButton mb-2" variant="danger" size="lg" onClick={handleAbortJobOffer}>
                    Abort
                  </Button>
                </Col>
              )}
              <Col className="text-center">
                <Button className="secondaryButton mb-2" variant="danger" size="lg" onClick={() => navigate("/ui")}>
                  Go Back
                </Button>
              </Col>
              {jobOffer?.status === JobOfferState.CONSOLIDATED && (
                <Col className="text-center">
                  <Button className="primarySuccessButton mb-2" variant="primary" size="lg" onClick={handleDoneJobOffer}>
                    Done
                  </Button>
                </Col>
              )}
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

const CandidateProfessionalModal: React.FC<{
  show: boolean;
  handleClose: () => void;
  alreadyExistentCandidates: Professional[];
  onSelectProfessional: (selected: Professional) => void;
}> = ({ show, handleClose, alreadyExistentCandidates, onSelectProfessional }) => {
  const [professionals, setProfessionals] = useState<Professional[]>([]);
  const [searchSkill, setSkill] = useState("");
  const [searchLocation, setSearchLocation] = useState("");
  const [searchEmploymentState, setSearchEmploymentState] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const loadProfessionals = async () => {
      try {
        setLoading(true);
        const result: any = await fetchProfessionals(currentPage, 10, searchSkill, searchLocation, searchEmploymentState);

        const existingCandidateIds = new Set(alreadyExistentCandidates.map((p) => p.id));
        result.content = result.content.filter((p: Professional) => !existingCandidateIds.has(p.id)); // filtraggio dei candidati già presenti

        setProfessionals(result.content);
        setTotalPages(result.totalPages);
        setLoading(false);
      } catch (error) {
        setError(true);
        setLoading(false);
      }
    };

    if (show) {
      loadProfessionals();
    }
  }, [show, currentPage, searchSkill, searchLocation, searchEmploymentState]);

  const handleSearchChangeBySkill = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSkill(event.target.value);
  };

  const handleSearchChangeByLocation = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchLocation(event.target.value);
  };

  const handleSearchChangeByEmploymentState = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchEmploymentState(event.target.value);
  };

  const handleProfessionalSelect = (professional: Professional) => {
    onSelectProfessional(professional);
    handleClose();
  };

  const handlePageChange = (page: number) => {
    if (page !== currentPage) {
      setCurrentPage(page);
    }
  };

  const getPaginationItems = () => {
    let pages = [];
    if (currentPage > 0) {
      pages.push(currentPage - 1);
    }
    pages.push(currentPage);
    if (currentPage < totalPages) {
      pages.push(currentPage + 1);
    }
    return pages;
  };

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title className="fw-bold">Select Professional</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form.Group controlId="search">
          <Row>
            <Col xs={12} sm={6}>
              <Form.Control type="text" className="mb-2" placeholder="Search by skill" value={searchSkill} onChange={handleSearchChangeBySkill} />
            </Col>
            <Col xs={12} sm={6}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by location"
                value={searchLocation}
                onChange={handleSearchChangeByLocation}
              />
            </Col>
          </Row>
          <Row>
            <Col xs={12} sm={6}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by employment state"
                value={searchEmploymentState}
                onChange={handleSearchChangeByEmploymentState}
              />
            </Col>
          </Row>
        </Form.Group>
        {loading ? (
          <LoadingSection h={200} />
        ) : error ? (
          <Container className="d-flex justify-content-center align-items-center" style={{ height: "200px" }}>
            <Alert variant="danger" className="text-center w-75">
              <h5>{error}</h5>
            </Alert>
          </Container>
        ) : (
          <>
            <Table hover>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Surname</th>
                  <th>SSN Code</th>
                </tr>
              </thead>
              <tbody>
                {professionals.length === 0 && (
                  <tr>
                    <td colSpan={3}>
                      <div className="d-flex justify-content-center align-items-center" style={{ height: "150px" }}>
                        <span className="text-muted fw-bold">No Customer Found!</span>
                      </div>
                    </td>
                  </tr>
                )}
                {professionals.map((professional) => (
                  <tr
                    key={professional.information.id}
                    onClick={() => handleProfessionalSelect(professional)}
                    style={{
                      cursor: "pointer",
                    }}
                  >
                    <td>{professional.information.name}</td>
                    <td>{professional.information.surname}</td>
                    <td>{professional.information.ssnCode}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
            <Pagination className="justify-content-center">
              {getPaginationItems().map((page) => (
                <Pagination.Item key={page} active={page === currentPage} onClick={() => handlePageChange(page)}>
                  {page}
                </Pagination.Item>
              ))}
            </Pagination>
          </>
        )}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default JobOfferDetail;
