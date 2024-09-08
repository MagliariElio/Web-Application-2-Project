import React, { useEffect, useRef, useState } from "react";
import { Container, Row, Col, Button, Alert, Form, Modal, Table, Pagination, ButtonGroup, Badge, Tooltip, OverlayTrigger } from "react-bootstrap";
import { JobOffer } from "../interfaces/JobOffer";
import { useParams, useNavigate } from "react-router-dom";
import {
  FaCheck,
  FaCheckCircle,
  FaCircle,
  FaClock,
  FaMapMarkerAlt,
  FaMoneyBillWave,
  FaPen,
  FaThumbsUp,
  FaTimes,
  FaTimesCircle,
  FaTrash,
  FaUndoAlt,
  FaUser,
  FaUsers,
  FaUserTie,
} from "react-icons/fa";
import {
  contractTypeList,
  EmploymentStateEnum,
  EmploymentStateEnumSearchCandidateProfessional,
  JobOfferState,
  toTitleCase,
  workModeList,
} from "../utils/costants";
import { MeInterface } from "../interfaces/MeInterface";
import { fetchCustomer } from "../apis/CustomerRequests";
import { Customer } from "../interfaces/Customer";
import { fetchProfessional, fetchProfessionals } from "../apis/ProfessionalRequests";
import { Professional } from "../interfaces/Professional";
import {
  abortJobOffer,
  cancelApplication,
  deleteJobOfferById,
  doneJobOffer,
  fetchJobOfferById,
  goToCandidateProposalPhase,
  goToCondolidated,
  goToSelectionPhase,
  updateJobOffer,
} from "../apis/JobOfferRequests";
import { LoadingSection } from "../App";
import { AiOutlineInfoCircle } from "react-icons/ai";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";

const JobOfferDetail = ({ me }: { me: MeInterface }) => {
  const { id } = useParams<{ id: string }>();
  const [jobOffer, setJobOffer] = useState<JobOffer | null>(null);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [professional, setProfessional] = useState<Professional | null>(null);
  const [candidateProfessionalList, setCandidateProfessionalList] = useState<Professional[]>([]);
  const [filteredCandidateProfessionalList, setFilteredCandidateProfessionalList] = useState<Professional[]>([]);
  const [loadingCandidateProfessional, setLoadingCandidateProfessional] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const abortState = "ABORT";

  //const location = useLocation();
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
  const [searchSkill, setSearchSkill] = useState("");
  const [searchLocation, setSearchLocation] = useState("");
  const [searchName, setSearchName] = useState("");
  const [searchSurname, setSearchSurname] = useState("");
  const [searchSsnCode, setSearchSsnCode] = useState("");
  const [filterByRefused, setFilterByRefused] = useState(false);
  const [filterByRevoked, setFilterByRevoked] = useState(false);
  const resetFilteredCandidateProfessionalList = (candidates: Professional[]) => {
    if (filterByRefused) {
      candidates = candidates.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id));
    } else if (filterByRevoked) {
      candidates = candidates.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
    }

    setFilteredCandidateProfessionalList(candidates);
    setSearchSkill("");
    setSearchLocation("");
    setSearchName("");
    setSearchSurname("");
    setSearchSsnCode("");
    setFilterByRefused(false);
    setFilterByRevoked(false);
  };
  const handleAddCandidate = (professional: Professional) => {
    var list: Professional[] = candidateProfessionalList;
    list.push(professional);
    setCandidateProfessionalList(list);
    resetFilteredCandidateProfessionalList(list);
  };
  var isModifyCandidatesList =
    candidateProfessionalList.filter((p: Professional) => !jobOffer?.candidateProfessionalIds.includes(p.id)).length !== 0 ||
    candidateProfessionalList.length !== jobOffer?.candidateProfessionalIds.length;

  // Boolean Confirm Action through a Modal
  const [showModalConfirmAbort, setShowModalConfirmAbort] = useState(false);
  const [showModalConfirmDone, setShowModalConfirmDone] = useState(false);
  const [showModalConfirmCandidate, setShowModalConfirmCandidate] = useState({ b: false, i: -1 });
  const [showModalConfirmApplication, setShowModalConfirmApplication] = useState(false);
  const [showModalCancelApplication, setShowModalCancelApplication] = useState(false);
  const [showModalRevokeApplication, setShowModalRevokeApplication] = useState(false);

  const errorRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();

  const loadCandidateProfessionals = async (candidateList: number[]) => {
    try {
      setLoadingCandidateProfessional(true);
      setCandidateProfessionalList([]);
      resetFilteredCandidateProfessionalList([]);

      const resultList: ProfessionalWithAssociatedData[] = await Promise.all(
        candidateList.map(async (id) => {
          const result = await fetchProfessional(id);
          return result;
        })
      );

      setCandidateProfessionalList(resultList.map((p) => p.professionalDTO));
      resetFilteredCandidateProfessionalList(resultList.map((p) => p.professionalDTO));
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
      resetFilteredCandidateProfessionalList([]);
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
        setProfessional(resultProfessional.professionalDTO);
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
    setShowModalRevokeApplication(false);

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
      setShowModalConfirmAbort(false);
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

  /**
   * Handles the selection of a candidate for a job offer.
   * Fetches the job offer details after proposing the candidate and updates the state.
   * Displays an error message if the operation fails.
   *
   * @param {number} indexCandidate - The index of the selected candidate in the candidateProfessionalList.
   */
  const handleSelectCandidateProfessional = async (indexCandidate: number) => {
    const candidate = candidateProfessionalList[indexCandidate];
    try {
      setShowModalConfirmCandidate({ b: false, i: -1 });
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

  /**
   * Moves the job offer to the "consolidated" state if the current state allows it.
   * Displays an error message if the operation fails or if the state is not appropriate.
   */
  const handleGoToConsolidated = async () => {
    setShowModalConfirmApplication(false);
    
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

  /**
   * Cancels the candidation process for the job offer.
   * Only available if the job offer is currently in the "candidate proposal" state.
   * Displays an error message if the operation fails or if the state is not appropriate.
   */
  const handleCancelApplication = async () => {
    setShowModalCancelApplication(false);

    if (jobOffer?.status !== JobOfferState.CANDIDATE_PROPOSAL) {
      setErrorMessage("This action is not available in this moment.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setLoading(true);
      const jobOfferResponse = await cancelApplication(parseInt(id ? id : ""), me.xsrfToken, candidateProfessionalList);

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
   * Completes the job offer process if the current state allows it.
   * Displays an error message if the operation fails or if the state is not appropriate.
   */
  const handleDoneJobOffer = async () => {
    if (jobOffer?.status !== JobOfferState.CONSOLIDATED || !professional?.id) {
      setErrorMessage("This action is not available in this moment.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    try {
      setShowModalConfirmDone(false);
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

  /**
   * Removes a candidate from the list of professional candidates.
   *
   * @param {number} indexToRemove - The index of the candidate to be removed from the candidateProfessionalList.
   */
  const handleDeleteCandidateProfessional = (indexToRemove: number) => {
    const updatedList = candidateProfessionalList.filter((_, index) => index !== indexToRemove);
    setCandidateProfessionalList(updatedList);
    if (searchName !== "") {
      const list = updatedList.filter((c) => c.information.name.toLowerCase().includes(searchName.toLowerCase()));
      setFilteredCandidateProfessionalList(list);
    } else if (searchSurname !== "") {
      const list = updatedList.filter((c) => c.information.surname.toLowerCase().includes(searchSurname.toLowerCase()));
      setFilteredCandidateProfessionalList(list);
    } else if (searchSkill !== "") {
      const list = updatedList.filter((c) => c.skills.some((skill) => skill.toLowerCase().includes(searchSkill.toLowerCase())));
      setFilteredCandidateProfessionalList(list);
    } else if (searchLocation !== "") {
      const list = updatedList.filter((c) => c.geographicalLocation.toLowerCase().includes(searchLocation.toLowerCase()));
      setFilteredCandidateProfessionalList(list);
    } else if (searchSsnCode !== "") {
      const list = updatedList.filter((c) => c.information.ssnCode.toLowerCase().includes(searchSsnCode.toLowerCase()));
      setFilteredCandidateProfessionalList(list);
    }

    if (filterByRefused === true) {
      const list = updatedList.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id));
      setFilteredCandidateProfessionalList(list);
    } else if (filterByRevoked === true) {
      const list = updatedList.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
      setFilteredCandidateProfessionalList(list);
    }
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
      <ModalConfirmation
        show={showModalConfirmAbort}
        handleCancel={() => setShowModalConfirmAbort(false)}
        handleConfirm={handleAbortJobOffer}
        title="Confirm Job Offer Termination"
        body="<strong>Important:</strong> You are about to terminate this job offer. This action is <strong>permanent</strong> and cannot be undone. Please <strong>confirm</strong> if you wish to proceed with the termination."
        actionType={false}
      />
      <ModalConfirmation
        show={showModalConfirmDone}
        handleCancel={() => setShowModalConfirmDone(false)}
        handleConfirm={handleDoneJobOffer}
        title="Confirm Job Offer Completion"
        body="<strong>Warning:</strong> You are about to mark this job offer as done. This action is <strong>permanent</strong> and cannot be undone. Please <strong>confirm</strong> if you wish to proceed."
        actionType={true}
      />

      <ModalConfirmation
        show={showModalConfirmCandidate.b}
        handleCancel={() => setShowModalConfirmCandidate({ b: false, i: -1 })}
        handleConfirm={() => handleSelectCandidateProfessional(showModalConfirmCandidate.i)}
        title="Confirm Candidate"
        body="<strong>Warning:</strong> You are about to accept this professional candidate for the job offer. They will be responsible for completing it. This action can be undone later through <strong>cancellation</strong>. Please <strong>confirm</strong> if you wish to proceed."
        actionType={true}
      />

      <ModalConfirmation
        show={showModalConfirmApplication}
        handleCancel={() => setShowModalConfirmApplication(false)}
        handleConfirm={handleGoToConsolidated}
        title="Confirm Application"
        body="<strong>Notice:</strong> By confirming, this professional candidate will be assigned to work on the job offer. This operation can only be undone through <strong>revocation</strong>. Please <strong>confirm</strong> if you wish to proceed."
        actionType={true}
      />

      <ModalConfirmation
        show={showModalCancelApplication}
        handleCancel={() => setShowModalCancelApplication(false)}
        handleConfirm={handleCancelApplication}
        title="Cancel Application"
        body="<strong>Notice:</strong> By proceeding, you will cancel this professional candidate's application, returning them to the <strong>selection phase</strong>. Please <strong>confirm</strong> if you wish to continue with this action."
        actionType={false}
      />

      <ModalConfirmation
        show={showModalRevokeApplication}
        handleCancel={() => setShowModalRevokeApplication(false)}
        handleConfirm={handleGoToSelectionPhase}
        title="Revoke Application"
        body="<strong>Warning:</strong> By proceeding, you will revoke the professional candidate's acceptance for this job offer, returning them to the <strong>selection phase</strong>. Please <strong>confirm</strong> if you wish to continue with this action."
        actionType={false}
      />

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

      <div className="border rounded p-3 shadow-sm mt-4">
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
                      <OverlayTrigger overlay={<Tooltip id="deleteButton">Delete</Tooltip>}>
                        <Button variant="danger" onClick={() => setShowModalDeleteConfirmation(true)}>
                          <FaTrash /> Delete
                        </Button>
                      </OverlayTrigger>
                    </Col>
                    <Col md={1} className="d-flex justify-content-end">
                      <OverlayTrigger overlay={<Tooltip id="editButton">Edit</Tooltip>}>
                        <Button variant="primary" onClick={() => setIsEditing(true)}>
                          <FaPen /> Edit
                        </Button>
                      </OverlayTrigger>
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
                    maxLength={255}
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
                      maxLength={255}
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
                    <Form.Select
                      name="contractType"
                      style={{ cursor: "pointer" }}
                      value={formDataJobOffer?.contractType || ""}
                      onChange={handleInputSelectChange}
                      required
                    >
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
                      maxLength={255}
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
                  {jobOffer?.duration} days
                </div>
              ) : (
                <Form.Group as={Row} controlId="duration" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={4} className="mb-0 fw-bold">
                    Duration (days)
                  </Form.Label>
                  <Col xs={12} sm={8}>
                    <Form.Control
                      type="number"
                      name="duration"
                      placeholder="Duration in days"
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
                    <Form.Select
                      name="workMode"
                      style={{ cursor: "pointer" }}
                      value={formDataJobOffer?.workMode || ""}
                      onChange={handleInputSelectChange}
                      required
                    >
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
                            <OverlayTrigger overlay={<Tooltip id="deleteRequiredSkillButton">Delete</Tooltip>}>
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
                            </OverlayTrigger>
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
                <OverlayTrigger overlay={<Tooltip id="addSkillButton">Add Skill</Tooltip>}>
                  <Button variant="primary" onClick={handleAddSkill}>
                    Add Skill
                  </Button>
                </OverlayTrigger>
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
                      maxLength={255}
                    />
                  </Col>
                </Form.Group>
              )}
            </Col>
          </Row>

          {/* Customer Section */}
          <Row className="border-top pt-3 mb-3">
            <Col xs={12}>
              <Form.Group as={Row} controlId="customerId">
                <Form.Label column xs={6} className="fw-bold">
                  Customer Information
                </Form.Label>

                {/* Buttons Section */}
                <Col xs={6} className="text-end">
                  <OverlayTrigger overlay={<Tooltip id="profileCustomerButton">Profile</Tooltip>}>
                    <Button variant="primary" className="me-2 text-center" onClick={() => navigate(`/ui/customers/${customer?.id}`)}>
                      <FaUser className="me-1" />
                      Profile
                    </Button>
                  </OverlayTrigger>
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
                    <OverlayTrigger overlay={<Tooltip id="profileApplicationButton">Profile</Tooltip>}>
                      <Button variant="primary" className="me-2 text-center" onClick={() => navigate(`/ui/professionals/${professional.id}`)}>
                        <FaUser className="me-1" />
                        Profile
                      </Button>
                    </OverlayTrigger>
                    {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL && !isEditing && (
                      <>
                        <OverlayTrigger overlay={<Tooltip id="confirmApplicationButton">Confirm Application</Tooltip>}>
                          <Button variant="success" className="me-2" onClick={() => setShowModalConfirmApplication(true)}>
                            <FaCheck className="me-1" />
                            Confirm
                          </Button>
                        </OverlayTrigger>
                        <OverlayTrigger overlay={<Tooltip id="cancelApplicationButton">Cancel Application</Tooltip>}>
                          <Button variant="danger" onClick={() => setShowModalCancelApplication(true)}>
                            <FaTrash className="me-1" />
                            Cancel
                          </Button>
                        </OverlayTrigger>
                      </>
                    )}
                    {jobOffer?.status === JobOfferState.CONSOLIDATED && !isEditing && (
                      <OverlayTrigger overlay={<Tooltip id="revokeApplicationButton">Revoke Acceptance</Tooltip>}>
                        <Button variant="danger" onClick={() => setShowModalRevokeApplication(true)}>
                          <FaTrash className="me-1" />
                          Revoke
                        </Button>
                      </OverlayTrigger>
                    )}
                  </Col>

                  {jobOffer?.status === JobOfferState.CONSOLIDATED && (
                    <Col xs={12} className="mt-2">
                      <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                        <AiOutlineInfoCircle className="me-1" />
                        To return to the
                        <strong style={{ marginLeft: "0.25rem" }}>candidate selection phase</strong>, please click the
                        <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>Cancel</strong> button.
                      </p>
                    </Col>
                  )}

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

          {/* Professional Candidates */}
          {!isEditing && (
            <Row className="border-top pt-3 mb-3">
              <Col md={9} className="d-flex align-items-center">
                <strong>Professional Candidates </strong>
              </Col>
              {(jobOffer?.status === JobOfferState.CREATED || jobOffer?.status === JobOfferState.SELECTION_PHASE) && (
                <>
                  <Col md={2} className="text-end">
                    <Button variant="primary" onClick={handleOpenProfessionalCandidateModal}>
                      Add Candidate
                    </Button>
                  </Col>
                  {isModifyCandidatesList && !loadingCandidateProfessional && (
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
                  <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                    <AiOutlineInfoCircle className="me-1" /> Please select one of the candidates to propose them for this job offer.
                  </p>
                </Col>
              )}

              <Col md={12} className="mt-2">
                <div className="d-flex justify-content-between align-items-center px-4">
                  <div className="d-flex align-items-center">
                    <FaUsers className="me-2 text-primary" />
                    <span>Potential candidates:</span>
                    <strong className="ms-2">{filteredCandidateProfessionalList.length}</strong>
                  </div>
                  <div className="d-flex align-items-center">
                    <FaTimes className="me-2 text-danger" />
                    <span>Candidates who have rejected position:</span>
                    <strong className="ms-2">
                      {filteredCandidateProfessionalList.filter((candidate) => jobOffer?.candidatesProfessionalRefused.includes(candidate.id)).length}
                    </strong>
                  </div>
                  <div className="d-flex align-items-center">
                    <FaUndoAlt className="me-2 text-danger" />
                    <span>Candidates who revoked acceptance:</span>
                    <strong className="ms-2">
                      {filteredCandidateProfessionalList.filter((candidate) => jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)).length}
                    </strong>
                  </div>
                </div>
              </Col>

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
                  <>
                    <SearchCandidate
                      resetList={() => resetFilteredCandidateProfessionalList(candidateProfessionalList)}
                      searchSkill={searchSkill}
                      searchLocation={searchLocation}
                      handleSearchChangeBySkill={(e: string) => {
                        setSearchSkill(e);
                        var list = candidateProfessionalList.filter((c) => c.skills.some((skill) => skill.toLowerCase().includes(e.toLowerCase())));
                        list = filterByRefused ? list.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id)) : list;
                        list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                        setFilteredCandidateProfessionalList(list);
                      }}
                      handleSearchChangeByLocation={(e: string) => {
                        setSearchLocation(e);
                        var list = candidateProfessionalList.filter((c) => c.geographicalLocation.toLowerCase().includes(e.toLowerCase()));
                        list = filterByRefused ? list.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id)) : list;
                        list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                        setFilteredCandidateProfessionalList(list);
                      }}
                      searchName={searchName}
                      handleSearchChangeByName={(e: string) => {
                        setSearchName(e);
                        var list = candidateProfessionalList.filter((c) => c.information.name.toLowerCase().includes(e.toLowerCase()));
                        list = filterByRefused ? list.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id)) : list;
                        list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                        setFilteredCandidateProfessionalList(list);
                      }}
                      searchSurname={searchSurname}
                      handleSearchChangeBySurname={(e: string) => {
                        setSearchSurname(e);
                        var list = candidateProfessionalList.filter((c) => c.information.surname.toLowerCase().includes(e.toLowerCase()));
                        list = filterByRefused ? list.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id)) : list;
                        list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                        setFilteredCandidateProfessionalList(list);
                      }}
                      searchSsnCode={searchSsnCode}
                      handleSearchChangeBySsnCode={(e: string) => {
                        setSearchSsnCode(e);
                        var list = candidateProfessionalList.filter((c) => c.information.ssnCode.toLowerCase().includes(e.toLowerCase()));
                        list = filterByRefused ? list.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id)) : list;
                        list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                        setFilteredCandidateProfessionalList(list);
                      }}
                      filterByRefused={filterByRefused}
                      handleFilterByRefused={(e: boolean) => {
                        setFilterByRefused(e);
                        const list = candidateProfessionalList.filter((c) => jobOffer?.candidatesProfessionalRefused.includes(c.id));
                        setFilteredCandidateProfessionalList(list);
                      }}
                      filterByRevoked={filterByRevoked}
                      handleFilterByRevoked={(e: boolean) => {
                        setFilterByRevoked(e);
                        const list = candidateProfessionalList.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
                        setFilteredCandidateProfessionalList(list);
                      }}
                    />

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
                        {filteredCandidateProfessionalList.length === 0 && (
                          <tr key={0}>
                            <td>-1</td>
                            <td>No professional candidates found with this filter.</td>
                          </tr>
                        )}
                        {filteredCandidateProfessionalList.map((candidate, index) => {
                          const hasRefused = jobOffer?.candidatesProfessionalRefused.includes(candidate.id);
                          const hasAccepted = professional?.id === candidate.id && jobOffer?.status === JobOfferState.CONSOLIDATED;
                          const isPendingApproval = professional?.id === candidate.id && jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL;
                          const isRevoked = jobOffer?.candidatesProfessionalRevoked.includes(candidate.id);

                          return (
                            <tr key={index}>
                              <td>{index + 1}</td>
                              <td>{candidate.information.name}</td>
                              <td>{candidate.information.surname}</td>
                              <td>{candidate.information.ssnCode}</td>
                              <td className="d-flex align-items-center justify-content-center text-center">
                                {hasRefused && (
                                  <Badge bg="danger" className="p-2 d-flex align-items-center justify-content-center">
                                    <FaTimes className="me-1" />
                                    Rejected
                                  </Badge>
                                )}

                                {isRevoked && (
                                  <Badge bg="secondary" className="p-2 d-flex align-items-center justify-content-center">
                                    <FaUndoAlt className="me-1" />
                                    Revoked
                                  </Badge>
                                )}

                                {hasAccepted && (
                                  <Badge bg="success" className="p-2 d-flex align-items-center justify-content-center">
                                    <FaThumbsUp className="me-1" />
                                    Accepted
                                  </Badge>
                                )}

                                {isPendingApproval && (
                                  <Badge bg="warning" className="p-2 d-flex align-items-center justify-content-center">
                                    <FaClock className="me-1" />
                                    Pending Approval
                                  </Badge>
                                )}

                                {!hasRefused && !hasAccepted && !isPendingApproval && !isRevoked && (
                                  <ButtonGroup>
                                    {jobOffer?.status !== JobOfferState.CREATED && (
                                      <OverlayTrigger overlay={<Tooltip id="confirmCandidateButton">Confirm Candidate</Tooltip>}>
                                        <Button
                                          variant="success"
                                          className="me-2"
                                          onClick={() => setShowModalConfirmCandidate({ b: true, i: index })}
                                          disabled={
                                            jobOffer?.status !== JobOfferState.SELECTION_PHASE ||
                                            isModifyCandidatesList ||
                                            loadingCandidateProfessional
                                          }
                                        >
                                          <FaCheck />
                                        </Button>
                                      </OverlayTrigger>
                                    )}

                                    <OverlayTrigger overlay={<Tooltip id="deleteCandidateButton">Delete</Tooltip>}>
                                      <Button
                                        variant="danger"
                                        className="me-2"
                                        onClick={() => handleDeleteCandidateProfessional(index)}
                                        disabled={
                                          jobOffer?.status === JobOfferState.ABORT ||
                                          jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL ||
                                          jobOffer?.status === JobOfferState.CONSOLIDATED ||
                                          jobOffer?.status === JobOfferState.DONE
                                        }
                                      >
                                        <FaTrash />
                                      </Button>
                                    </OverlayTrigger>

                                    <OverlayTrigger overlay={<Tooltip id="profileCandidateButton">Profile</Tooltip>}>
                                      <Button variant="warning" onClick={() => navigate(`/ui/professionals/${candidate?.id}`)}>
                                        <FaUser />
                                      </Button>
                                    </OverlayTrigger>
                                  </ButtonGroup>
                                )}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </Table>
                  </>
                )}

                {candidateProfessionalList?.length === 0 && !loadingCandidateProfessional && <p>No professional candidates.</p>}
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
                  <Button className="secondaryDangerButton mb-2" variant="danger" size="lg" onClick={() => setShowModalConfirmAbort(true)}>
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
                  <Button className="primarySuccessButton mb-2" variant="primary" size="lg" onClick={() => setShowModalConfirmDone(true)}>
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
  const [searchEmploymentState, setSearchEmploymentState] = useState<EmploymentStateEnum>(EmploymentStateEnum.AVAILABLE_FOR_WORK);
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

  const handleSearchChangeByEmploymentState = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setSearchEmploymentState(event.target.value as EmploymentStateEnum);
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
              <Form.Select name="employmentState" className="mb-2" value={searchEmploymentState || ""} onChange={handleSearchChangeByEmploymentState}>
                {Object.values(EmploymentStateEnumSearchCandidateProfessional).map((state, index) => (
                  <option key={index} value={state}>
                    {toTitleCase(state)}
                  </option>
                ))}
              </Form.Select>
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
                        <span className="text-muted fw-bold">No Professional Found!</span>
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

const ModalConfirmation: React.FC<{
  show: boolean;
  handleCancel: () => void;
  handleConfirm: () => Promise<void>;
  title: string;
  body: string;
  actionType: boolean;
}> = ({ show, handleCancel, handleConfirm, title, body, actionType }) => {
  return (
    <Modal show={show} onHide={handleCancel}>
      <Modal.Header closeButton>
        <Modal.Title className="fw-bold">{title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p
          style={{ color: "#856404", fontSize: "1rem", backgroundColor: "#fff3cd", padding: "10px", borderRadius: "5px" }}
          dangerouslySetInnerHTML={{ __html: body }}
        />
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <Button variant="secondary" onClick={handleCancel}>
          Cancel
        </Button>
        <Button variant={actionType ? "success" : "danger"} onClick={handleConfirm}>
          Confirm
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

const SearchCandidate: React.FC<{
  resetList: () => void;
  searchSkill: string;
  handleSearchChangeBySkill: (e: string) => void;
  searchLocation: string;
  handleSearchChangeByLocation: (e: string) => void;
  searchName: string;
  handleSearchChangeByName: (e: string) => void;
  searchSurname: string;
  handleSearchChangeBySurname: (e: string) => void;
  searchSsnCode: string;
  handleSearchChangeBySsnCode: (e: string) => void;
  filterByRefused: boolean;
  handleFilterByRefused: (e: boolean) => void;
  filterByRevoked: boolean;
  handleFilterByRevoked: (e: boolean) => void;
}> = ({
  resetList,
  searchSkill,
  handleSearchChangeBySkill,
  searchLocation,
  handleSearchChangeByLocation,
  searchName,
  handleSearchChangeByName,
  searchSurname,
  handleSearchChangeBySurname,
  searchSsnCode,
  handleSearchChangeBySsnCode,
  filterByRefused,
  handleFilterByRefused,
  filterByRevoked,
  handleFilterByRevoked,
}) => {
  const [type, setType] = useState<"" | "skill" | "location" | "name" | "surname" | "ssnCode">("");
  const [filter, setFilter] = useState<"" | "filterByRejection" | "filterByRevokation">("");

  return (
    <Form className="mb-2">
      <Form.Group controlId="search">
        <Row>
          <Col xs={12} sm={3}>
            <Form.Select
              name="selectType"
              style={{ cursor: "pointer" }}
              value={type}
              onChange={(e) => {
                setType(e.target.value as "skill" | "location" | "name" | "surname" | "ssnCode" | "");
                resetList();
              }}
            >
              <option value={""}>Search by</option>
              <option value={"name"}>Name</option>
              <option value={"surname"}>Surname</option>
              <option value={"skill"}>Skill</option>
              <option value={"location"}>Location</option>
              <option value={"ssnCode"}>SSN Code</option>
            </Form.Select>
          </Col>

          {type === "name" && (
            <Col xs={12} sm={7}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by name"
                value={searchName}
                onChange={(event) => handleSearchChangeByName(event.target.value)}
              />
            </Col>
          )}

          {type === "surname" && (
            <Col xs={12} sm={7}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by surname"
                value={searchSurname}
                onChange={(event) => handleSearchChangeBySurname(event.target.value)}
              />
            </Col>
          )}

          {type === "skill" && (
            <Col xs={12} sm={7}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by skill"
                value={searchSkill}
                onChange={(event) => handleSearchChangeBySkill(event.target.value)}
              />
            </Col>
          )}
          {type === "location" && (
            <Col xs={12} sm={7}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by location"
                value={searchLocation}
                onChange={(event) => handleSearchChangeByLocation(event.target.value)}
              />
            </Col>
          )}
          {type === "ssnCode" && (
            <Col xs={12} sm={7}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by ssn code"
                value={searchSsnCode}
                onChange={(event) => handleSearchChangeBySsnCode(event.target.value)}
              />
            </Col>
          )}
          <Col xs={12} sm={2} className="text-end">
            <Form.Select
              name="filterType"
              style={{ cursor: "pointer" }}
              value={filter}
              onChange={(e) => {
                setFilter(e.target.value as "filterByRejection" | "");
                if (e.target.value === "filterByRejection") {
                  handleFilterByRefused(true);
                } else if (e.target.value === "filterByRevokation") {
                  handleFilterByRevoked(true);
                } else {
                  resetList();
                }
              }}
            >
              <option value={""}>Filter By</option>
              <option value={"filterByRejection"}>Filter By Rejection</option>
              <option value={"filterByRevokation"}>Filter By Revokation</option>
            </Form.Select>
          </Col>
        </Row>
      </Form.Group>
    </Form>
  );
};

export default JobOfferDetail;
