import React, { useEffect, useRef, useState } from "react";
import { Container, Row, Col, Button, Alert, Form, Modal, Table, Pagination, ButtonGroup, Badge, Tooltip, OverlayTrigger } from "react-bootstrap";
import { JobOffer } from "../interfaces/JobOffer";
import { useParams, useNavigate } from "react-router-dom";
import {
  FaBan,
  FaBuilding,
  FaCalendarAlt,
  FaCheck,
  FaCheckCircle,
  FaCircle,
  FaClock,
  FaEnvelope,
  FaExchangeAlt,
  FaInfoCircle,
  FaLaptopHouse,
  FaMapMarkerAlt,
  FaMicrochip,
  FaMoneyBillWave,
  FaPencilAlt,
  FaReply,
  FaThumbsUp,
  FaTimes,
  FaTimesCircle,
  FaTrash,
  FaUndoAlt,
  FaUser,
  FaUsers,
  FaUserSlash,
  FaUserTie,
} from "react-icons/fa";
import {
  contractTypeList,
  EmploymentStateEnum,
  EmploymentStateEnumSearchCandidateProfessional,
  JobOfferState,
  RoleState,
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
  deleteJobOfferById,
  doneJobOffer,
  fetchJobOfferById,
  generateSkillsAPI,
  goToCandidateProposalPhase,
  goToCondolidated,
  goToSelectionPhase,
  updateJobOffer,
} from "../apis/JobOfferRequests";
import { LoadingSection } from "../App";
import { AiOutlineInfoCircle } from "react-icons/ai";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";
import { BsPencilSquare, BsTrash } from "react-icons/bs";
import { convertLocalDateTimeToDate } from "../utils/checkers";
import { FaListCheck } from "react-icons/fa6";
import { DescriptionGenerateAIModal } from "./AddJobOfferPage";

const JobOfferDetail = ({ me }: { me: MeInterface }) => {
  const { id } = useParams<{ id: string }>();
  const [jobOffer, setJobOffer] = useState<JobOffer | null>(null);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [professional, setProfessional] = useState<Professional | null>(null);

  const [candidateProfessionalList, setCandidateProfessionalList] = useState<Professional[]>([]);
  const [candidateProposalProfessionalList, setCandidateProposalProfessionalList] = useState<Professional[]>([]);

  const [filteredCandidateProfessionalList, setFilteredCandidateProfessionalList] = useState<Professional[]>([]);
  const [filteredCandidateProposalProfessionalList, setFilteredCandidateProposalProfessionalList] = useState<Professional[]>([]);
  const [loadingCandidateProfessional, setLoadingCandidateProfessional] = useState(true);
  const [loadingCandidateProposalProfessional, setLoadingCandidateProposalProfessional] = useState(false);

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
  const [filterByRejected, setFilterByRejected] = useState(false);
  const [filterByRevoked, setFilterByRevoked] = useState(false);
  const [filterByCancelled, setFilterByCancelled] = useState(false);
  const [filterByNotSelected, setFilterByNotSelected] = useState(false);
  const [filterByPendingApproval, setFilterByPendingApproval] = useState(false);
  const [searchProposalSkill, setSearchProposalSkill] = useState("");
  const [searchProposalLocation, setSearchProposalLocation] = useState("");
  const [searchProposalName, setSearchProposalName] = useState("");
  const [searchProposalSurname, setSearchProposalSurname] = useState("");
  const [searchProposalSsnCode, setSearchProposalSsnCode] = useState("");
  const resetFilteredCandidateProfessionalList = (candidates: Professional[]) => {
    if (filterByRejected) {
      candidates = candidates.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id));
    } else if (filterByRevoked) {
      candidates = candidates.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
    } else if (filterByCancelled) {
      candidates = candidates.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id));
    } else if (filterByNotSelected) {
      candidates = candidates.filter(
        (candidate) =>
          !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
          professional?.id !== candidate.id &&
          !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
      );
    } else if (filterByPendingApproval) {
      candidates = candidates.filter(
        (candidate) =>
          candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) && jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
      );
    }
    setFilteredCandidateProfessionalList(candidates);
    setSearchSkill("");
    setSearchLocation("");
    setSearchName("");
    setSearchSurname("");
    setSearchSsnCode("");
  };
  const resetFilteredCandidateProposalProfessionalList = (candidates: Professional[]) => {
    setFilteredCandidateProposalProfessionalList(candidates);
    setSearchProposalSkill("");
    setSearchProposalLocation("");
    setSearchProposalName("");
    setSearchProposalSurname("");
    setSearchProposalSsnCode("");
  };
  const handleAddCandidate = (professionals: Professional[]) => {
    var list: Professional[] = [...candidateProfessionalList];
    professionals.forEach((p) => list.push(p));
    setCandidateProfessionalList(list);
    resetFilteredCandidateProfessionalList(list);
  };
  var isModifyCandidatesList =
    candidateProfessionalList.filter((p: Professional) => !jobOffer?.candidateProfessionalIds.includes(p.id)).length !== 0 ||
    candidateProfessionalList.length !== jobOffer?.candidateProfessionalIds.length;

  // Add Professional Candidate in the Selection Phase
  const handleAddCandidateProposal = (professional: Professional) => {
    const list = [...candidateProposalProfessionalList];
    const isAlreadyInList = list.some((p: Professional) => p.id === professional.id);

    if (!isAlreadyInList) {
      list.push(professional);
    }

    setCandidateProposalProfessionalList(list);
    resetFilteredCandidateProposalProfessionalList(list);
  };
  const handleRemoveCandidateProposal = (idProfessional: number) => {
    var list = [...candidateProposalProfessionalList];

    list = list.filter((c: Professional) => c.id !== idProfessional);
    setCandidateProposalProfessionalList(list);
    resetFilteredCandidateProposalProfessionalList(list);
  };

  // Boolean Confirm Action through a Modal
  const [showModalConfirmAbort, setShowModalConfirmAbort] = useState(false);
  const [showModalConfirmDone, setShowModalConfirmDone] = useState(false);
  const [showModalConfirmCandidates, setShowModalConfirmCandidates] = useState(false);
  const [showModalConfirmApplication, setShowModalConfirmApplication] = useState(false);
  const [showModalCancellAllApplications, setShowModalCancellAllApplications] = useState(false);
  const [showModalCancelApplication, setShowModalCancelApplication] = useState<{ b: boolean; c: Professional | null }>({ b: false, c: null });
  const [showModalRevokeApplication, setShowModalRevokeApplication] = useState(false);

  const [showGenerateSkillsModal, setShowGenerateSkillsModal] = useState(false);
  const [generationSkills, setGenerationSkills] = useState(false);

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

  const loadCandidateProposalProfessionals = async (candidateList: number[]) => {
    try {
      setLoadingCandidateProposalProfessional(true);
      setCandidateProposalProfessionalList([]);
      resetFilteredCandidateProposalProfessionalList([]);

      const resultList: ProfessionalWithAssociatedData[] = await Promise.all(
        candidateList.map(async (id) => {
          const result = await fetchProfessional(id);
          return result;
        })
      );

      setCandidateProposalProfessionalList(resultList.map((p) => p.professionalDTO));
      resetFilteredCandidateProposalProfessionalList(resultList.map((p) => p.professionalDTO));
      setLoadingCandidateProposalProfessional(false);
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

      setLoadingCandidateProposalProfessional(false);
      setCandidateProposalProfessionalList([]);
      resetFilteredCandidateProposalProfessionalList([]);
    }
  };

  const loadJobOffer = async (jobOfferSelected: JobOffer | null) => {
    try {
      var result = null;
      setLoading(true);

      if (jobOfferSelected) {
        result = jobOfferSelected;
      } else if (id) {
        result = await fetchJobOfferById(parseFloat(id));
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

      loadCandidateProfessionals(result?.candidateProfessionalIds); // carica i candidati nella fase di selezione
      loadCandidateProposalProfessionals(result?.candidatesProposalProfessional); // carica i candidati nella fase di candidate proposal

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

      setError(true);
    }

    setLoading(false);
  };

  useEffect(() => {
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

    const jobOfferProfessionals = {
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
      //setLoading(true);
      setLoadingCandidateProfessional(true);
      const jobOfferResponse: JobOffer = await goToSelectionPhase(parseInt(id ? id : ""), me.xsrfToken, jobOfferProfessionals);

      var jobOfferResult = jobOffer;
      if (jobOfferResult) {
        setProfessional(null);
        setJobOffer(jobOfferResponse);
        setFormDataJobOffer(jobOfferResponse);
      }

      //await loadJobOffer(jobOfferResponse);

      //setCandidateProfessionalList(jobOfferResponse..map((p) => p.professionalDTO));
      //resetFilteredCandidateProfessionalList(jobOfferResponse.map((p) => p.professionalDTO));

      setErrorMessage("");
      setLoadingCandidateProfessional(false);
      //setLoading(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      //setLoading(false);
      setLoadingCandidateProfessional(false);

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
   */
  const handleSelectCandidateProfessional = async () => {
    try {
      setLoadingCandidateProposalProfessional(true);

      // eventuale eliminazione dei professional da rimuovere
      var list = [...candidateProposalProfessionalList];

      if (showModalCancelApplication.b) {
        const cancelCandidateId = showModalCancelApplication?.c ? showModalCancelApplication?.c.id : -1;
        list = list.filter((c: Professional) => c.id !== cancelCandidateId);
        setShowModalCancelApplication({ b: false, c: null });
      }

      if (showModalCancellAllApplications) {
        list = [];
        setShowModalCancellAllApplications(false);
      }

      const candidates: number[] = list.map((p: Professional) => p.id);

      const jobOfferResponse = await goToCandidateProposalPhase(parseInt(id ? id : ""), me.xsrfToken, candidates);
      //await loadJobOffer(jobOfferResponse); // TODO: importare solo le cose che servono così da evitare un caricamento dell'intera pagina

      setProfessional(null);
      setJobOffer(jobOfferResponse);
      setFormDataJobOffer(jobOfferResponse);

      // aggiorna la lista dei candidati
      setLoadingCandidateProfessional(true);
      var list = [...candidateProfessionalList];
      list = list.filter((c: Professional) => jobOfferResponse.candidateProfessionalIds.includes(c.id));
      setCandidateProfessionalList(list);
      setFilteredCandidateProfessionalList(list);
      setLoadingCandidateProfessional(false);

      // aggiorna la lista dei proposed candidates
      var list = [...candidateProposalProfessionalList];
      list = list.filter((c: Professional) => jobOfferResponse.candidatesProposalProfessional.includes(c.id));
      setCandidateProposalProfessionalList(list);
      setFilteredCandidateProposalProfessionalList(list);
      setLoadingCandidateProposalProfessional(false);
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

      setLoadingCandidateProposalProfessional(false);
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
   * @param {number} idToRemove - The id of the candidate to be removed from the candidateProfessionalList.
   */
  const handleDeleteCandidateProfessional = (idToRemove: number) => {
    var list = [...candidateProfessionalList];
    const updatedList = list.filter((c: Professional) => c.id !== idToRemove);
    setCandidateProfessionalList(updatedList);
    setFilteredCandidateProfessionalList(updatedList);

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

    if (filterByRejected === true) {
      const list = updatedList.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id));
      setFilteredCandidateProfessionalList(list);
    } else if (filterByRevoked === true) {
      const list = updatedList.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
      setFilteredCandidateProfessionalList(list);
    }
  };

  const generateSkills = async (description: string) => {
    try {
      setGenerationSkills(true);
      setShowGenerateSkillsModal(false);
      const response = await generateSkillsAPI(description, me.xsrfToken);

      const newSkills: string[] = jobOffer?.requiredSkills ? [...jobOffer?.requiredSkills] : [];
      response.forEach((r: string) => newSkills.push(r));
      setFormDataJobOffer(
        (prevData) =>
          ({
            ...prevData,
            requiredSkills: [...newSkills],
          } as JobOffer)
      );

      setGenerationSkills(false);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("An unexpected error occurred");
      }

      setGenerationSkills(false);

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  if (error) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ height: "100vh" }}>
        <Alert variant="danger" className="text-center w-50">
          {errorMessage ? <h5>{errorMessage}</h5> : <h5>An error occurred. Please, reload the page!</h5>}
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="mt-2">
      <ConfirmDeleteModal
        name={jobOffer?.name ? jobOffer?.name : ""}
        show={showModalDeleteConfirmation}
        handleClose={handleCloseModal}
        handleConfirm={handleDeleteJobOffer}
      />
      <ModalConfirmation
        show={showModalConfirmAbort}
        handleCancel={() => setShowModalConfirmAbort(false)}
        handleConfirm={handleAbortJobOffer}
        title="Confirm Job Offer Termination"
        body="<strong>Important:</strong> You are about to terminate this job offer. This action is <strong>permanent</strong> and cannot be undone. Please <strong>confirm</strong> if you wish to proceed with the termination."
        name={null}
        actionType={false}
      />
      <ModalConfirmation
        show={showModalConfirmDone}
        handleCancel={() => setShowModalConfirmDone(false)}
        handleConfirm={handleDoneJobOffer}
        title="Confirm Job Offer Completion"
        body="<strong>Warning:</strong> You are about to mark this job offer as done. This action is <strong>permanent</strong> and cannot be undone. Please <strong>confirm</strong> if you wish to proceed."
        name={null}
        actionType={true}
      />

      <ModalConfirmation
        show={showModalConfirmCandidates}
        handleCancel={() => setShowModalConfirmCandidates(false)}
        handleConfirm={async () => {
          setShowModalConfirmCandidates(false);
          handleSelectCandidateProfessional();
        }}
        title="Confirm Candidates"
        body="<strong>Warning:</strong> You are about to accept those professional candidates for the job offer. They will be responsible for completing it. This action can be undone later through <strong>cancellation</strong>. Please <strong>confirm</strong> if you wish to proceed."
        name={null}
        actionType={true}
      />

      <ModalConfirmation
        show={showModalConfirmApplication}
        handleCancel={() => {
          setProfessional(null);
          setShowModalConfirmApplication(false);
        }}
        handleConfirm={handleGoToConsolidated}
        title="Confirm Application"
        body="<strong>Notice:</strong> By confirming, this professional candidate will be assigned to work on the job offer. This operation can only be undone through <strong>revocation</strong>. Additionally, all pending applications for other job offers will be <strong>cancelled</strong>. Please <strong>confirm</strong> if you wish to proceed."
        name={`${professional?.information.name} ${professional?.information.surname}`}
        actionType={true}
      />

      <ModalConfirmation
        show={showModalCancelApplication.b}
        handleCancel={() => setShowModalCancelApplication({ b: false, c: null })}
        handleConfirm={handleSelectCandidateProfessional}
        title="Cancel Application"
        body="<strong>Notice:</strong> By proceeding, you will cancel this professional candidate's application. The job offer will return to the <strong>selection phase</strong> only if no other candidates are currently proposed. Please <strong>confirm</strong> if you wish to continue with this action."
        name={`${showModalCancelApplication.c?.information.name} ${showModalCancelApplication.c?.information.surname}`}
        actionType={false}
      />

      <ModalConfirmation
        show={showModalCancellAllApplications}
        handleCancel={() => setShowModalCancellAllApplications(false)}
        handleConfirm={async () => {
          setCandidateProposalProfessionalList([]);
          await handleSelectCandidateProfessional();
        }}
        title="Cancel Application"
        body="<strong>Notice:</strong> By proceeding, you will cancel all professional candidates applications. The job offer will return to the <strong>selection phase</strong>. Please <strong>confirm</strong> if you wish to continue with this action."
        name={null}
        actionType={false}
      />

      <ModalConfirmation
        show={showModalRevokeApplication}
        handleCancel={() => setShowModalRevokeApplication(false)}
        handleConfirm={async () => {
          setCandidateProposalProfessionalList([]);
          handleGoToSelectionPhase();
        }}
        title="Revoke Application"
        body="<strong>Warning:</strong> By proceeding, you will revoke the professional candidate's acceptance for this job offer, returning them to the <strong>selection phase</strong>. Please <strong>confirm</strong> if you wish to continue with this action."
        name={null}
        actionType={false}
      />

      <DescriptionGenerateAIModal
        name={"Generate Skills with the AI"}
        placeholderValue={"Enter a detailed job offer description to generate the required skills"}
        suggestion_1={"Be specific in the job description to get accurate skills."}
        suggestion_2={"For example: <i>Generate required skills for a Senior Python Developer with experience in AI and Machine Learning.</i>"}
        show={showGenerateSkillsModal}
        handleClose={() => setShowGenerateSkillsModal(false)}
        onSubmit={generateSkills}
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
        {loading ? (
          <div className="loading-detail-page"></div>
        ) : (
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
                  {jobOffer?.status !== JobOfferState.ABORT && jobOffer?.status !== JobOfferState.DONE && me.role === RoleState.OPERATOR && (
                    <>
                      <Col className="d-flex justify-content-end">
                        <OverlayTrigger overlay={<Tooltip id="editButton">Edit</Tooltip>}>
                          <Button variant="primary" className="primaryButton me-3" onClick={() => setIsEditing(true)}>
                            <BsPencilSquare /> Edit
                          </Button>
                        </OverlayTrigger>
                        <OverlayTrigger overlay={<Tooltip id="deleteButton">Delete</Tooltip>}>
                          <Button variant="danger" className="primaryDangerButton" onClick={() => setShowModalDeleteConfirmation(true)}>
                            <BsTrash /> Delete
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
                    <FaUserTie className="me-1" /> <strong>Contract Type: </strong>
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
                    <FaMapMarkerAlt className="me-1" /> <strong>Location: </strong>
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
                    <FaClock className="me-1" /> <strong>Duration: </strong>
                    {jobOffer?.duration} {jobOffer?.duration === 1 ? "day" : "days"}
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
              <Col md={3}>
                {!isEditing ? (
                  <div>
                    <FaMoneyBillWave className="me-1" /> <strong>Value: </strong>
                    {jobOffer?.value} €
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
              <Col md={3} className="d-flex align-items-center">
                <FaClock className="me-2" /> <strong className="me-1">Creation Time: </strong>
                {convertLocalDateTimeToDate(jobOffer?.creationTime).toLocaleDateString()}{" "}
                {convertLocalDateTimeToDate(jobOffer?.creationTime).toLocaleTimeString()}
              </Col>
            </Row>

            {/* Work Mode and Status */}
            <Row className="border-top pt-3 mb-3">
              <Col md={6}>
                {!isEditing ? (
                  <div className="d-flex align-items-center">
                    {jobOffer?.workMode === "Remote" && <FaLaptopHouse className="me-1" />}
                    {jobOffer?.workMode === "Hybrid" && <FaBuilding className="me-1" />}
                    {jobOffer?.workMode === "In-Person" && <FaExchangeAlt className="me-1" />}
                    <strong className="me-1">Work Mode:</strong>
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
              <Col md={3}>
                {!isEditing ? (
                  <div className="d-flex align-items-center">
                    {jobOffer?.status === JobOfferState.CREATED && <FaPencilAlt className="me-2" />}
                    {jobOffer?.status === JobOfferState.SELECTION_PHASE && <FaListCheck className="me-2" />}
                    {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL && <FaEnvelope className="me-2" />}
                    {jobOffer?.status === JobOfferState.CONSOLIDATED && <FaCheck className="me-2" />}
                    {jobOffer?.status === JobOfferState.DONE && <FaCheckCircle className="me-2" />}
                    {jobOffer?.status === JobOfferState.ABORT && <FaTimesCircle className="me-2" />}
                    <strong className="me-1">Status: </strong>
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
              {jobOffer?.endTime && !isEditing && (
                <Col md={3} className="d-flex align-items-center">
                  <FaCalendarAlt className="me-2" /> <strong className="me-1">End Time: </strong>
                  {convertLocalDateTimeToDate(jobOffer?.endTime).toLocaleDateString()}{" "}
                  {convertLocalDateTimeToDate(jobOffer?.endTime).toLocaleTimeString()}
                </Col>
              )}
            </Row>

            {/* Required Skills */}
            <Row className="border-top pt-4">
              <Col>
                <Form.Group as={Row} controlId="requiredSkills" className="d-flex align-items-center">
                  <Form.Label column xs={12} sm={2} className="mb-0 fw-bold">
                    <Row className="ms-3">Required Skills</Row>
                    {isEditing && (
                      <Row className="mt-3">
                        <Button
                          className="mt-2 d-flex align-items-center ms-3 secondaryButton"
                          style={{ maxWidth: "150px" }}
                          onClick={() => setShowGenerateSkillsModal(true)}
                        >
                          <FaMicrochip style={{ marginRight: "5px" }} />
                          Generate Skills with AI
                        </Button>
                      </Row>
                    )}
                  </Form.Label>

                  {generationSkills && <LoadingSection h={200} />}

                  {!generationSkills && (
                    <>
                      {formDataJobOffer?.requiredSkills.length === 0 ? (
                        <Col xs={12} className="text-center">
                          <p className="text-muted">No required skill added yet</p>
                        </Col>
                      ) : (
                        <Col xs={12} sm={10}>
                          <div style={{ maxHeight: "500px", overflowY: "auto" }}>
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
                          </div>
                        </Col>
                      )}
                    </>
                  )}
                </Form.Group>
              </Col>
            </Row>

            {/* Add Skill */}
            {isEditing && !generationSkills && (
              <Row className="mb-3 justify-content-end mt-2">
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
                    <Form.Control type="text" name="customerSurname" value={customer?.information.contactDTO.surname || ""} disabled />
                  </Col>
                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>Name</Form.Label>
                    <Form.Control type="text" name="customerName" value={customer?.information.contactDTO.name || ""} disabled />
                  </Col>
                  <Col xs={12} md={6} className="mb-3">
                    <Form.Label>SSN Code</Form.Label>
                    <Form.Control type="text" name="customerSSnCode" value={customer?.information.contactDTO.ssnCode || ""} disabled />
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
                      {jobOffer?.status === JobOfferState.CONSOLIDATED && me.role === RoleState.OPERATOR && !isEditing && (
                        <OverlayTrigger overlay={<Tooltip id="revokeApplicationButton">Revoke Acceptance</Tooltip>}>
                          <Button variant="danger" onClick={() => setShowModalRevokeApplication(true)}>
                            <FaTrash className="me-1" />
                            Revoke
                          </Button>
                        </OverlayTrigger>
                      )}
                    </Col>

                    {jobOffer?.status === JobOfferState.CONSOLIDATED && me.role === RoleState.OPERATOR && (
                      <Col xs={12} className="mt-2">
                        <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                          <AiOutlineInfoCircle className="me-1" />
                          To return to the
                          <strong style={{ marginLeft: "0.25rem" }}>candidate selection phase</strong>, please click the
                          <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>Revoke</strong> button.
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
                  <strong className="me-2">Professional Candidates</strong>

                  <OverlayTrigger
                    placement="right"
                    overlay={
                      <Tooltip id="info-tooltip">
                        This section is dedicated to gathering all the professional candidates (representing potential candidates) for this job offer.
                        Here, you can confirm one or more candidates to propose them for this job offer. If you no longer want a candidate to appear
                        in this list, you can remove them using the designated button.
                      </Tooltip>
                    }
                  >
                    <div>
                      <FaInfoCircle className="text-muted" style={{ cursor: "pointer" }} />
                    </div>
                  </OverlayTrigger>
                </Col>

                {(jobOffer?.status === JobOfferState.CREATED || jobOffer?.status === JobOfferState.SELECTION_PHASE) &&
                  me.role === RoleState.OPERATOR && (
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

                {jobOffer?.status === JobOfferState.SELECTION_PHASE && me.role === RoleState.OPERATOR && (
                  <Col md={12} className="mt-2">
                    <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                      <AiOutlineInfoCircle className="me-1" /> Please select{" "}
                      <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>one or more candidates</strong> to propose them for this job
                      offer.
                    </p>
                  </Col>
                )}

                <Col md={12} className="mt-4">
                  <div className="d-flex justify-content-between align-items-center px-4">
                    <Col className="d-flex align-items-center">
                      <FaUsers className="me-2 text-primary" />
                      <span>Potential candidates:</span>
                      <strong className="ms-2">{filteredCandidateProfessionalList.length}</strong>
                    </Col>
                    <Col className="d-flex align-items-center">
                      <FaBan className="me-2 text-danger" />
                      <span>Candidates whose position was cancelled:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProfessionalList.filter(
                            (candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) && professional != null
                          ).length
                        }
                      </strong>
                    </Col>
                    <Col className="d-flex align-items-center">
                      <FaUndoAlt className="me-2 text-secondary" />
                      <span>Candidates who revoked acceptance:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProfessionalList.filter((candidate) => jobOffer?.candidatesProfessionalRevoked.includes(candidate.id))
                            .length
                        }
                      </strong>
                    </Col>
                  </div>
                </Col>

                <Col md={12} className="mt-2">
                  <div className="d-flex justify-content-between align-items-center px-4">
                    <Col className="d-flex align-items-center">
                      <FaClock className="me-2 text-warning" />
                      <span>Candidates pending approval:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProfessionalList.filter(
                            (candidate) =>
                              candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                              jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                          ).length
                        }
                      </strong>
                    </Col>
                    <Col className="d-flex align-items-center">
                      <FaTimes className="me-2 text-danger" />
                      <span>Candidates who have rejected position:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProfessionalList.filter((candidate) => jobOffer?.candidatesProfessionalRejected.includes(candidate.id))
                            .length
                        }
                      </strong>
                    </Col>
                    <Col className="d-flex align-items-center">
                      <FaUserSlash className="me-2" style={{ color: "#FF8C00" }} />
                      <span>Candidates not selected:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProfessionalList.filter(
                            (candidate) =>
                              !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                              professional?.id !== candidate.id &&
                              !jobOffer?.candidatesProfessionalRejected.includes(candidate.id) &&
                              !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                          ).length
                        }
                      </strong>
                    </Col>
                  </div>
                </Col>

                <Col md={12} className="mt-3">
                  {loadingCandidateProfessional && <LoadingSection h={100} />}
                  {showProfessionalCandidateModal && !loadingCandidateProfessional && (
                    <CandidateProfessionalModal
                      show={showProfessionalCandidateModal}
                      alreadyExistentCandidates={candidateProfessionalList}
                      handleClose={handleCloseProfessionalCandidateModal}
                      onSelectProfessionals={handleAddCandidate}
                    />
                  )}
                  {candidateProfessionalList?.length > 0 && !loadingCandidateProfessional && (
                    <>
                      <SearchCandidate
                        resetList={() => resetFilteredCandidateProfessionalList(candidateProfessionalList)}
                        resetFilters={() => {
                          setFilterByRejected(false);
                          setFilterByRevoked(false);
                          setFilterByCancelled(false);
                          setFilterByNotSelected(false);
                          setFilterByPendingApproval(false);
                          setFilteredCandidateProfessionalList(candidateProfessionalList);
                        }}
                        searchSkill={searchSkill}
                        handleSearchChangeBySkill={(e: string) => {
                          setSearchSkill(e);
                          var list = candidateProfessionalList.filter((c) => c.skills.some((skill) => skill.toLowerCase().includes(e.toLowerCase())));
                          list = filterByRejected ? list.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id)) : list;
                          list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                          list = filterByCancelled
                            ? list.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id))
                            : list;
                          list = filterByNotSelected
                            ? list.filter(
                                (candidate) =>
                                  !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                                  professional?.id !== candidate.id &&
                                  !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                              )
                            : list;
                          list = filterByPendingApproval
                            ? list.filter(
                                (candidate) =>
                                  candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                                  jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                              )
                            : list;
                          setFilteredCandidateProfessionalList(list);
                        }}
                        searchLocation={searchLocation}
                        handleSearchChangeByLocation={(e: string) => {
                          setSearchLocation(e);
                          var list = candidateProfessionalList.filter((c) => c.geographicalLocation.toLowerCase().includes(e.toLowerCase()));
                          list = filterByRejected ? list.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id)) : list;
                          list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                          list = filterByCancelled
                            ? list.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id))
                            : list;
                          list = filterByNotSelected
                            ? list.filter(
                                (candidate) =>
                                  !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                                  professional?.id !== candidate.id &&
                                  !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                              )
                            : list;
                          list = filterByPendingApproval
                            ? list.filter(
                                (candidate) =>
                                  candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                                  jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                              )
                            : list;
                          setFilteredCandidateProfessionalList(list);
                        }}
                        searchName={searchName}
                        handleSearchChangeByName={(e: string) => {
                          setSearchName(e);
                          var list = candidateProfessionalList.filter((c) => c.information.name.toLowerCase().includes(e.toLowerCase()));
                          list = filterByRejected ? list.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id)) : list;
                          list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                          list = filterByCancelled
                            ? list.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id))
                            : list;
                          list = filterByNotSelected
                            ? list.filter(
                                (candidate) =>
                                  !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                                  professional?.id !== candidate.id &&
                                  !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                              )
                            : list;
                          list = filterByPendingApproval
                            ? list.filter(
                                (candidate) =>
                                  candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                                  jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                              )
                            : list;
                          setFilteredCandidateProfessionalList(list);
                        }}
                        searchSurname={searchSurname}
                        handleSearchChangeBySurname={(e: string) => {
                          setSearchSurname(e);
                          var list = candidateProfessionalList.filter((c) => c.information.surname.toLowerCase().includes(e.toLowerCase()));
                          list = filterByRejected ? list.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id)) : list;
                          list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                          list = filterByCancelled
                            ? list.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id))
                            : list;
                          list = filterByNotSelected
                            ? list.filter(
                                (candidate) =>
                                  !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                                  professional?.id !== candidate.id &&
                                  !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                              )
                            : list;
                          list = filterByPendingApproval
                            ? list.filter(
                                (candidate) =>
                                  candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                                  jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                              )
                            : list;
                          setFilteredCandidateProfessionalList(list);
                        }}
                        searchSsnCode={searchSsnCode}
                        handleSearchChangeBySsnCode={(e: string) => {
                          setSearchSsnCode(e);
                          var list = candidateProfessionalList.filter((c) => c.information.ssnCode.toLowerCase().includes(e.toLowerCase()));
                          list = filterByRejected ? list.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id)) : list;
                          list = filterByRevoked ? list.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id)) : list;
                          list = filterByCancelled
                            ? list.filter((candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id))
                            : list;
                          list = filterByNotSelected
                            ? list.filter(
                                (candidate) =>
                                  !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                                  professional?.id !== candidate.id &&
                                  !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                              )
                            : list;
                          list = filterByPendingApproval
                            ? list.filter(
                                (candidate) =>
                                  candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                                  jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                              )
                            : list;
                          setFilteredCandidateProfessionalList(list);
                        }}
                        filterByRejected={filterByRejected}
                        handleFilterByRejected={(e: boolean) => {
                          setFilterByRejected(e);
                          const list = candidateProfessionalList.filter((c) => jobOffer?.candidatesProfessionalRejected.includes(c.id));
                          setFilteredCandidateProfessionalList(list);
                        }}
                        filterByRevoked={filterByRevoked}
                        handleFilterByRevoked={(e: boolean) => {
                          setFilterByRevoked(e);
                          const list = candidateProfessionalList.filter((c) => jobOffer?.candidatesProfessionalRevoked.includes(c.id));
                          setFilteredCandidateProfessionalList(list);
                        }}
                        filterByCancelled={filterByCancelled}
                        handleFilterByCancelled={(e: boolean) => {
                          setFilterByCancelled(e);
                          const list = candidateProfessionalList.filter((candidate) =>
                            candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id)
                          );
                          setFilteredCandidateProfessionalList(list);
                        }}
                        filterByNotSelected={filterByNotSelected}
                        handleFilterByNotSelected={(e: boolean) => {
                          setFilterByNotSelected(e);

                          const list = candidateProfessionalList.filter(
                            (candidate) =>
                              !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                              professional?.id !== candidate.id &&
                              !jobOffer?.candidatesProfessionalRevoked.includes(candidate.id)
                          );

                          setFilteredCandidateProfessionalList(list);
                        }}
                        filterByPendingApproval={filterByPendingApproval}
                        handleFilterByPendingApproval={(e: boolean) => {
                          setFilterByPendingApproval(e);

                          const list = candidateProfessionalList.filter(
                            (candidate) =>
                              candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                              jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL
                          );

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
                            <th>
                              <OverlayTrigger
                                placement="top"
                                overlay={
                                  <Tooltip id="legendToolTip">
                                    <div className="p-2">
                                      <p>
                                        <FaTimes className="me-1 text-danger" /> Rejected: The application was rejected.
                                      </p>
                                      <p>
                                        <FaUndoAlt className="me-1 text-secondary" /> Revoked: The application was revoked.
                                      </p>
                                      <p>
                                        <FaThumbsUp className="me-1 text-success" /> Accepted: The application was accepted.
                                      </p>
                                      <p>
                                        <FaClock className="me-1 text-warning" /> Pending Approval: Awaiting approval.
                                      </p>
                                      <p>
                                        <FaBan className="me-1 text-danger" /> Cancelled: The application was cancelled.
                                      </p>
                                      <p>
                                        <FaUserSlash className="me-1" style={{ color: "#FF8C00" }} /> Not Selected: The candidate was not selected.
                                      </p>
                                    </div>
                                  </Tooltip>
                                }
                              >
                                <div>
                                  Status <FaInfoCircle className="text-muted" style={{ cursor: "pointer" }} />
                                </div>
                              </OverlayTrigger>
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {filteredCandidateProfessionalList.length === 0 && (
                            <tr key={0}>
                              <td>-1</td>
                              <td>No professional candidates found with this filter.</td>
                              <td></td>
                              <td></td>
                              <td></td>
                              <td></td>
                            </tr>
                          )}
                          {filteredCandidateProfessionalList.map((candidate, index) => {
                            const hasRejected = jobOffer?.candidatesProfessionalRejected.includes(candidate.id);
                            const hasAccepted =
                              professional?.id === candidate.id &&
                              (jobOffer?.status === JobOfferState.CONSOLIDATED ||
                                jobOffer?.status === JobOfferState.DONE ||
                                jobOffer?.status === JobOfferState.ABORT);
                            const isPendingApproval =
                              candidateProposalProfessionalList?.some((c: Professional) => c.id === candidate.id) &&
                              jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL;
                            const isRevoked = jobOffer?.candidatesProfessionalRevoked.includes(candidate.id);
                            const isCancelled =
                              candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                              (jobOffer?.status === JobOfferState.CONSOLIDATED ||
                                jobOffer?.status === JobOfferState.DONE ||
                                jobOffer?.status === JobOfferState.ABORT);
                            const isNotSelected =
                              !candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) &&
                              !isRevoked &&
                              !hasAccepted &&
                              !hasRejected &&
                              jobOffer?.status !== JobOfferState.CREATED &&
                              jobOffer?.status !== JobOfferState.SELECTION_PHASE;
                            const isCandidateProposal = candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id);

                            return (
                              <tr key={index}>
                                <td>{index + 1}</td>
                                <td>{candidate.information.name}</td>
                                <td>{candidate.information.surname}</td>
                                <td>{candidate.information.ssnCode}</td>
                                <td className="d-flex align-items-center justify-content-center text-center">
                                  <ButtonGroup>
                                    {!hasRejected &&
                                      !hasAccepted &&
                                      !isPendingApproval &&
                                      !isRevoked &&
                                      me.role === RoleState.OPERATOR &&
                                      jobOffer?.status !== JobOfferState.CONSOLIDATED && (
                                        <>
                                          {jobOffer?.status === JobOfferState.SELECTION_PHASE && isCandidateProposal && (
                                            <OverlayTrigger overlay={<Tooltip id="cancelCandidateButton">Cancel</Tooltip>}>
                                              <Button
                                                variant="secondary"
                                                className="me-2"
                                                onClick={() => handleRemoveCandidateProposal(candidate.id)}
                                                disabled={
                                                  jobOffer?.status !== JobOfferState.SELECTION_PHASE ||
                                                  isModifyCandidatesList ||
                                                  loadingCandidateProfessional
                                                }
                                              >
                                                <FaReply />
                                              </Button>
                                            </OverlayTrigger>
                                          )}

                                          {jobOffer?.status === JobOfferState.SELECTION_PHASE && !isCandidateProposal && (
                                            <OverlayTrigger overlay={<Tooltip id="confirmCandidateButton">Confirm Candidate</Tooltip>}>
                                              <Button
                                                variant="success"
                                                className="me-2"
                                                onClick={() => handleAddCandidateProposal(candidate)}
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

                                          {jobOffer?.status === JobOfferState.SELECTION_PHASE && (
                                            <OverlayTrigger overlay={<Tooltip id="deleteCandidateButton">Delete</Tooltip>}>
                                              <Button
                                                variant="danger"
                                                className="me-2"
                                                onClick={() => handleDeleteCandidateProfessional(candidate.id)}
                                              >
                                                <FaTrash />
                                              </Button>
                                            </OverlayTrigger>
                                          )}
                                        </>
                                      )}

                                    <OverlayTrigger overlay={<Tooltip id="profileCandidateButton">Profile</Tooltip>}>
                                      <Button variant="warning" onClick={() => navigate(`/ui/professionals/${candidate?.id}`)}>
                                        <FaUser />
                                      </Button>
                                    </OverlayTrigger>
                                  </ButtonGroup>
                                </td>
                                <td>
                                  {hasRejected && (
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

                                  {isCancelled && (
                                    <Badge bg="danger" className="p-2 d-flex align-items-center justify-content-center me-2">
                                      <FaBan className="me-1" />
                                      Cancelled
                                    </Badge>
                                  )}

                                  {isNotSelected && (
                                    <Badge
                                      bg="orange"
                                      className="p-2 d-flex align-items-center justify-content-center me-2"
                                      style={{ backgroundColor: "#FF8C00", color: "#fff" }}
                                    >
                                      <FaUserSlash className="me-1" />
                                      Not Selected
                                    </Badge>
                                  )}
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </Table>
                    </>
                  )}

                  {candidateProfessionalList?.length === 0 && !loadingCandidateProfessional && <p className="mt-3">No professional candidates.</p>}
                </Col>
              </Row>
            )}

            {/* Proposed Candidates */}
            {!isEditing && !(candidateProposalProfessionalList.length === 0 && jobOffer?.status === JobOfferState.CONSOLIDATED) && (
              <Row className="border-top pt-3 mb-3">
                <Col md={9} className="d-flex align-items-center">
                  <strong className="me-2">Proposed Candidates</strong>

                  <OverlayTrigger
                    placement="right"
                    overlay={
                      <Tooltip id="info-tooltip">
                        This section is dedicated to the professional candidates who have been offered a position for this job offer. Please select
                        only one candidate who has accepted the offer, or remove a candidate using the designated button if they are no longer of
                        interest.
                      </Tooltip>
                    }
                  >
                    <div>
                      <FaInfoCircle className="text-muted" style={{ cursor: "pointer" }} />
                    </div>
                  </OverlayTrigger>
                </Col>

                {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL &&
                  me.role === RoleState.OPERATOR &&
                  candidateProposalProfessionalList.length > 0 &&
                  !loadingCandidateProposalProfessional && (
                    <Col md={3} className="text-end">
                      <Button variant="danger" onClick={() => setShowModalCancellAllApplications(true)}>
                        Cancell All
                      </Button>
                    </Col>
                  )}

                {jobOffer?.status === JobOfferState.SELECTION_PHASE &&
                  me.role === RoleState.OPERATOR &&
                  candidateProposalProfessionalList.length > 0 &&
                  !loadingCandidateProposalProfessional && (
                    <Col md={3} className="text-end">
                      <OverlayTrigger overlay={<Tooltip id="confirmRequestButton">Confirm Request</Tooltip>}>
                        <Button variant="success" onClick={() => setShowModalConfirmCandidates(true)}>
                          Confirm
                        </Button>
                      </OverlayTrigger>
                    </Col>
                  )}

                {jobOffer?.status === JobOfferState.SELECTION_PHASE &&
                  candidateProposalProfessionalList.length > 0 &&
                  !loadingCandidateProposalProfessional && (
                    <Col xs={12} className="mt-2">
                      <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                        <AiOutlineInfoCircle className="me-1" />
                        Please select the <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>Confirm</strong> button to move to the
                        <strong style={{ marginLeft: "0.25rem" }}>candidate proposal phase</strong>.
                      </p>
                    </Col>
                  )}

                {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL && (
                  <Col xs={12} className="mt-2">
                    <p style={{ color: "gray", fontSize: "0.9rem", display: "flex", alignItems: "center" }}>
                      <AiOutlineInfoCircle className="me-1" />
                      Please select <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>only one</strong> candidate to accept the job
                      offer. To return to the
                      <strong style={{ marginLeft: "0.25rem" }}>candidate selection phase</strong>, please click the
                      <strong style={{ marginLeft: "0.25rem", marginRight: "0.25rem" }}>Cancel All</strong> button.
                    </p>
                  </Col>
                )}

                <Col md={12} className="mt-4">
                  <div className="d-flex justify-content-between align-items-center px-4">
                    <Col className="d-flex align-items-center">
                      <FaUsers className="me-2 text-primary" />
                      <span>Potential candidates:</span>
                      <strong className="ms-2">{filteredCandidateProposalProfessionalList.length}</strong>
                    </Col>

                    <Col className="d-flex align-items-center">
                      <FaBan className="me-2 text-danger" />
                      <span>Candidates whose position was cancelled:</span>
                      <strong className="ms-2">
                        {
                          filteredCandidateProposalProfessionalList.filter(
                            (candidate) => candidateProposalProfessionalList.some((c: Professional) => c.id === candidate.id) && professional != null
                          ).length
                        }
                      </strong>
                    </Col>
                    <Col></Col>
                  </div>
                </Col>

                <Col md={12} className="mt-3">
                  {loadingCandidateProposalProfessional && <LoadingSection h={100} />}
                  {candidateProposalProfessionalList?.length > 0 && !loadingCandidateProposalProfessional && (
                    <>
                      <SearchCandidate
                        resetList={() => resetFilteredCandidateProposalProfessionalList(candidateProposalProfessionalList)}
                        resetFilters={() => {}}
                        searchSkill={searchProposalSkill}
                        handleSearchChangeBySkill={(e: string) => {
                          setSearchProposalSkill(e);
                          var list = candidateProposalProfessionalList.filter((c) =>
                            c.skills.some((skill) => skill.toLowerCase().includes(e.toLowerCase()))
                          );
                          setFilteredCandidateProposalProfessionalList(list);
                        }}
                        searchLocation={searchProposalLocation}
                        handleSearchChangeByLocation={(e: string) => {
                          setSearchProposalLocation(e);
                          var list = candidateProposalProfessionalList.filter((c) => c.geographicalLocation.toLowerCase().includes(e.toLowerCase()));
                          setFilteredCandidateProposalProfessionalList(list);
                        }}
                        searchName={searchProposalName}
                        handleSearchChangeByName={(e: string) => {
                          setSearchProposalName(e);
                          var list = candidateProposalProfessionalList.filter((c) => c.information.name.toLowerCase().includes(e.toLowerCase()));
                          setFilteredCandidateProposalProfessionalList(list);
                        }}
                        searchSurname={searchProposalSurname}
                        handleSearchChangeBySurname={(e: string) => {
                          setSearchProposalSurname(e);
                          var list = candidateProposalProfessionalList.filter((c) => c.information.surname.toLowerCase().includes(e.toLowerCase()));
                          setFilteredCandidateProposalProfessionalList(list);
                        }}
                        searchSsnCode={searchProposalSsnCode}
                        handleSearchChangeBySsnCode={(e: string) => {
                          setSearchProposalSsnCode(e);
                          var list = candidateProposalProfessionalList.filter((c) => c.information.ssnCode.toLowerCase().includes(e.toLowerCase()));
                          setFilteredCandidateProposalProfessionalList(list);
                        }}
                        filterByRejected={null}
                        handleFilterByRejected={() => {}}
                        filterByRevoked={null}
                        handleFilterByRevoked={() => {}}
                        filterByCancelled={null}
                        handleFilterByCancelled={() => {}}
                        filterByNotSelected={null}
                        handleFilterByNotSelected={() => {}}
                        filterByPendingApproval={null}
                        handleFilterByPendingApproval={() => {}}
                      />

                      <Table striped bordered hover className="align-middle">
                        <thead>
                          <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>Surname</th>
                            <th>SSN Code</th>
                            <th>Actions</th>
                            <OverlayTrigger
                              placement="top"
                              overlay={
                                <Tooltip id="legendToolTip">
                                  <div className="p-2">
                                    <p>
                                      <FaTimes className="me-1 text-danger" /> Rejected: The application was rejected.
                                    </p>
                                    <p>
                                      <FaUndoAlt className="me-1 text-secondary" /> Revoked: The application was revoked.
                                    </p>
                                    <p>
                                      <FaThumbsUp className="me-1 text-success" /> Accepted: The application was accepted.
                                    </p>
                                    <p>
                                      <FaClock className="me-1 text-warning" /> Pending Approval: Awaiting approval.
                                    </p>
                                    <p>
                                      <FaBan className="me-1 text-danger" /> Cancelled: The application was cancelled.
                                    </p>
                                    <p>
                                      <FaUserSlash className="me-1" style={{ color: "#FF8C00" }} /> Not Selected: The candidate was not selected.
                                    </p>
                                  </div>
                                </Tooltip>
                              }
                            >
                              <th>
                                Status <FaInfoCircle className="text-muted" style={{ cursor: "pointer" }} />
                              </th>
                            </OverlayTrigger>
                          </tr>
                        </thead>
                        <tbody>
                          {filteredCandidateProposalProfessionalList.length === 0 && (
                            <tr key={0}>
                              <td>-1</td>
                              <td>No professional candidates found with this filter.</td>
                              <td></td>
                              <td></td>
                              <td></td>
                              <td></td>
                            </tr>
                          )}
                          {filteredCandidateProposalProfessionalList.map((candidate, index) => {
                            return (
                              <tr key={index}>
                                <td>{index + 1}</td>
                                <td>{candidate.information.name}</td>
                                <td>{candidate.information.surname}</td>
                                <td>{candidate.information.ssnCode}</td>
                                <td className="d-flex align-items-center justify-content-center text-center">
                                  <ButtonGroup>
                                    {jobOffer?.status === JobOfferState.CANDIDATE_PROPOSAL && me.role === RoleState.OPERATOR && (
                                      <>
                                        <OverlayTrigger overlay={<Tooltip id="confirmApplicationButton">Confirm Proposal</Tooltip>}>
                                          <Button
                                            variant="success"
                                            className="me-2"
                                            onClick={() => {
                                              setProfessional(candidate);
                                              setShowModalConfirmApplication(true);
                                            }}
                                          >
                                            <FaCheck />
                                          </Button>
                                        </OverlayTrigger>

                                        {me.role === RoleState.OPERATOR && (
                                          <OverlayTrigger overlay={<Tooltip id="deleteCandidateProposalButton">Delete Proposal</Tooltip>}>
                                            <Button
                                              variant="danger"
                                              className="me-2"
                                              onClick={() => setShowModalCancelApplication({ b: true, c: candidate })}
                                            >
                                              <FaTrash />
                                            </Button>
                                          </OverlayTrigger>
                                        )}
                                      </>
                                    )}

                                    <OverlayTrigger overlay={<Tooltip id="profileCandidateButton">Profile</Tooltip>}>
                                      <Button variant="warning" onClick={() => navigate(`/ui/professionals/${candidate?.id}`)}>
                                        <FaUser />
                                      </Button>
                                    </OverlayTrigger>
                                  </ButtonGroup>
                                </td>
                                <td>
                                  {(jobOffer?.status === JobOfferState.CONSOLIDATED ||
                                    jobOffer?.status === JobOfferState.DONE ||
                                    jobOffer?.status === JobOfferState.ABORT) && (
                                    <Badge bg="danger" className="p-2 d-flex align-items-center justify-content-center">
                                      <FaBan className="me-1" />
                                      Cancelled
                                    </Badge>
                                  )}
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </Table>
                    </>
                  )}

                  {candidateProposalProfessionalList?.length === 0 && !loadingCandidateProposalProfessional && (
                    <p className="mt-3">No professional candidates.</p>
                  )}
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
                {jobOffer?.status !== JobOfferState.ABORT && jobOffer?.status !== JobOfferState.DONE && me.role === RoleState.OPERATOR && (
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
                {jobOffer?.status === JobOfferState.CONSOLIDATED && me.role === RoleState.OPERATOR && (
                  <Col className="text-center">
                    <Button className="primarySuccessButton mb-2" variant="primary" size="lg" onClick={() => setShowModalConfirmDone(true)}>
                      Done
                    </Button>
                  </Col>
                )}
              </Row>
            )}
          </Form>
        )}
      </div>
    </Container>
  );
};

const ConfirmDeleteModal: React.FC<{
  name: string;
  show: boolean;
  handleClose: () => void;
  handleConfirm: () => void;
}> = ({ name, show, handleClose, handleConfirm }) => {
  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>Confirm Delete</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p style={{ color: "#856404", fontSize: "1rem", backgroundColor: "#fff3cd", padding: "10px", borderRadius: "5px" }}>
          <strong>Warning:</strong> Are you sure you want to delete this job offer? This action is
          <strong> irreversible</strong>.
        </p>
        <p className="text-center fs-3 fw-semibold">{name}?</p>
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <Button variant="secondary" className="ms-5" onClick={handleClose}>
          Cancel
        </Button>
        <Button variant="danger" className="me-5" onClick={handleConfirm}>
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
  onSelectProfessionals: (selected: Professional[]) => void;
}> = ({ show, handleClose, alreadyExistentCandidates, onSelectProfessionals }) => {
  const [professionals, setProfessionals] = useState<Professional[]>([]);
  const [selectedProfessionals, setSelectedProfessionals] = useState<Professional[]>([]);
  const [searchName, setSearchName] = useState("");
  const [searchSurname, setSearchSurname] = useState("");
  const [searchSkill, setSearchSkill] = useState("");
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
        const result: any = await fetchProfessionals(currentPage, 10, searchSkill, searchLocation, searchEmploymentState, searchName, searchSurname);

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
  }, [show, currentPage, searchName, searchSurname, searchSkill, searchLocation, searchEmploymentState]);

  const handleSearchChangeByName = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchName(event.target.value);
  };

  const handleSearchChangeBySurname = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchSurname(event.target.value);
  };

  const handleSearchChangeBySkill = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchSkill(event.target.value);
  };

  const handleSearchChangeByLocation = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchLocation(event.target.value);
  };

  const handleSearchChangeByEmploymentState = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setSearchEmploymentState(event.target.value as EmploymentStateEnum);
  };

  const handleProfessionalSelect = (professionals: Professional[]) => {
    onSelectProfessionals(professionals);
    handleClose();
  };

  const handleProfessionalClick = (professional: Professional) => {
    setSelectedProfessionals((prevSelected) => {
      if (prevSelected.some((p) => p.id === professional.id)) {
        return prevSelected.filter((p) => p.id !== professional.id);
      } else {
        return [...prevSelected, professional];
      }
    });
  };

  const changePage = (pageNumber: number) => {
    if (pageNumber >= totalPages) pageNumber = totalPages - 1;
    if (pageNumber < 0) pageNumber = 0;
    setCurrentPage(pageNumber);
  };

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title className="fw-bold">Select Professionals</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form.Group controlId="search">
          <Row>
            <Col xs={12} sm={6}>
              <Form.Control type="text" className="mb-2" placeholder="Search by name" value={searchName} onChange={handleSearchChangeByName} />
            </Col>
            <Col xs={12} sm={6}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by surname"
                value={searchSurname}
                onChange={handleSearchChangeBySurname}
              />
            </Col>
          </Row>
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
                  <th></th>
                  <th>Name</th>
                  <th>Surname</th>
                  <th>SSN Code</th>
                </tr>
              </thead>
              <tbody>
                {professionals.length === 0 && (
                  <tr>
                    <td colSpan={4}>
                      <div className="d-flex justify-content-center align-items-center" style={{ height: "150px" }}>
                        <span className="text-muted fw-bold">No Professional Found!</span>
                      </div>
                    </td>
                  </tr>
                )}
                {professionals.map((professional) => (
                  <tr
                    key={professional.information.id}
                    style={{
                      cursor: "pointer",
                    }}
                  >
                    <td>
                      <Form.Check
                        type="checkbox"
                        checked={selectedProfessionals.some((p) => p.id === professional.id)}
                        onChange={() => handleProfessionalClick(professional)}
                      />
                    </td>
                    <td onClick={() => handleProfessionalClick(professional)}>{professional.information.name}</td>
                    <td onClick={() => handleProfessionalClick(professional)}>{professional.information.surname}</td>
                    <td onClick={() => handleProfessionalClick(professional)}>{professional.information.ssnCode}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
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
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <Button variant="secondary" className="ms-5" onClick={handleClose}>
          Close
        </Button>
        <Button variant="success" className="me-5" onClick={() => handleProfessionalSelect(selectedProfessionals)}>
          Select
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
  name: string | null;
  actionType: boolean;
}> = ({ show, handleCancel, handleConfirm, title, body, name, actionType }) => {
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

        <p className="text-center fs-3 fw-semibold">{name}</p>
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <Button variant="secondary" className="ms-5" onClick={handleCancel}>
          Cancel
        </Button>
        <Button variant={actionType ? "success" : "danger"} className="me-5" onClick={handleConfirm}>
          Confirm
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

const SearchCandidate: React.FC<{
  resetList: () => void;
  resetFilters: () => void;
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
  filterByRejected: boolean | null;
  handleFilterByRejected: (e: boolean) => void;
  filterByRevoked: boolean | null;
  handleFilterByRevoked: (e: boolean) => void;
  filterByCancelled: boolean | null;
  handleFilterByCancelled: (e: boolean) => void;
  filterByNotSelected: boolean | null;
  handleFilterByNotSelected: (e: boolean) => void;
  filterByPendingApproval: boolean | null;
  handleFilterByPendingApproval: (e: boolean) => void;
}> = ({
  resetList,
  resetFilters,
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
  filterByRejected,
  handleFilterByRejected,
  filterByRevoked,
  handleFilterByRevoked,
  filterByCancelled,
  handleFilterByCancelled,
  filterByNotSelected,
  handleFilterByNotSelected,
  filterByPendingApproval,
  handleFilterByPendingApproval,
}) => {
  const [type, setType] = useState<"" | "skill" | "location" | "name" | "surname" | "ssnCode">("");
  const [filter, setFilter] = useState<
    "" | "filterByRejection" | "filterByRevokation" | "filterByCancelled" | "filterByNotSelected" | "filterByPendingApproval"
  >("");

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
          {filterByRejected != null &&
            filterByRevoked != null &&
            filterByCancelled != null &&
            filterByNotSelected != null &&
            filterByPendingApproval != null && (
              <Col xs={12} sm={2} className="text-end">
                <Form.Select
                  name="filterType"
                  style={{ cursor: "pointer" }}
                  value={filter}
                  onChange={(e) => {
                    setFilter(
                      e.target.value as
                        | "filterByRejection"
                        | "filterByRevokation"
                        | "filterByCancelled"
                        | "filterByNotSelected"
                        | "filterByPendingApproval"
                        | ""
                    );
                    if (e.target.value === "filterByRejection") {
                      handleFilterByRejected(true);
                    } else if (e.target.value === "filterByRevokation") {
                      handleFilterByRevoked(true);
                    } else if (e.target.value === "filterByCancelled") {
                      handleFilterByCancelled(true);
                    } else if (e.target.value === "filterByNotSelected") {
                      handleFilterByNotSelected(true);
                    } else if (e.target.value === "filterByPendingApproval") {
                      handleFilterByPendingApproval(true);
                    } else {
                      resetFilters();
                    }
                  }}
                >
                  <option value={""}>Filter By</option>
                  <option value={"filterByRejection"}>Filter by Rejection</option>
                  <option value={"filterByRevokation"}>Filter by Revocation</option>
                  <option value={"filterByCancelled"}>Filter by Cancellation</option>
                  <option value={"filterByNotSelected"}>Filter by Not Selected</option>
                  <option value={"filterByPendingApproval"}>Filter by Pending Approval</option>
                </Form.Select>
              </Col>
            )}
        </Row>
      </Form.Group>
    </Form>
  );
};

export default JobOfferDetail;
