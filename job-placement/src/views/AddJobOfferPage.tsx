import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Container, Modal, OverlayTrigger, Pagination, Row, Table, Tooltip } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsXLg } from "react-icons/bs";
import { useLocation, useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { contractTypeList, toTitleCase, workModeList } from "../utils/costants";
import { Customer } from "../interfaces/Customer";
import { fetchCustomers } from "../apis/CustomerRequests";
import { generateJobOffer, submitJobOffer } from "../apis/JobOfferRequests";
import { LoadingSection } from "../App";

function AddJobOfferPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const selectedCustomer = useLocation().state as { customer: Customer } | undefined;

  const errorRef = useRef<HTMLDivElement | null>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [duration, setDuration] = useState("");
  const [note, setNote] = useState("");
  const [contractType, setContractType] = useState("");
  const [location, setLocation] = useState("");
  const [workMode, setWorkMode] = useState("");
  const [customer, setCustomer] = useState<Customer | null>(null);

  const [requiredSkills, setRequiredSkills] = useState<any[]>([]);
  const [singleRequiredSkill, setSingleRequiredSkill] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  // Customer Selection
  const [showSelectCustomerModal, setShowSelectCustomerModal] = useState(false);
  const handleOpenSelectCustomerModal = () => setShowSelectCustomerModal(true);
  const handleCloseSelectCustomerModal = () => setShowSelectCustomerModal(false);
  const handleCustomerSelect = (customer: Customer) => {
    setCustomer(customer);
  };

  const [showGenerateJobOfferModal, setShowGenerateJobOfferModal] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selectedCustomer) {
      setCustomer(selectedCustomer.customer);
    }
  }, []);

  const generateJobOfferFields = async (description: string) => {
    try {
      setLoading(true);
      setShowGenerateJobOfferModal(false);
      const response = await generateJobOffer(description, me.xsrfToken);

      console.log(response);

      setName(response.name);
      setDescription(response.description);
      setDuration(response.duration);
      setNote(response.note);
      setContractType(response.contractType);
      setLocation(response.location);
      setWorkMode(response.workMode);

      const newSkills: string[] = [];
      response.requiredSkills.forEach((r: string) => newSkills.push(r));
      setRequiredSkills(newSkills);

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

  const handleAddSkill = () => {
    if (singleRequiredSkill.trim() === "") {
      setErrorMessage("Please enter a skill before adding.");

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }

      return;
    }
    setRequiredSkills([...requiredSkills, singleRequiredSkill]);
    setSingleRequiredSkill("");
    setErrorMessage("");
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (name.trim() === "") {
      setErrorMessage("Job offer name cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (description.trim() === "") {
      setErrorMessage("Description cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (location.trim() === "") {
      setErrorMessage("Location cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (requiredSkills.length === 0) {
      setErrorMessage("You must add at least one required skill before saving.");

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (customer === null) {
      setErrorMessage("You must add a customer.");

      // Scroll to error message when it appears
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    const jobOffer = {
      name: name,
      description: description,
      contractType: contractType,
      location: location,
      workMode: workMode,
      requiredSkills: requiredSkills,
      duration: duration,
      note: note,
      customerId: customer.id,
    };

    try {
      const response = await submitJobOffer(jobOffer, me.xsrfToken);
      navigate(`/ui/joboffers/${response.id}`);
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

  return (
    <div className="add-job-offer-container">

      <DescriptionGenerateJobOfferModal
        show={showGenerateJobOfferModal}
        handleClose={() => setShowGenerateJobOfferModal(false)}
        onSubmit={generateJobOfferFields}
      />

      <Row className="d-flex flex-row p-0 mb-5 align-items-center">
        <Col sm={7}>
          <h3>Add New Job Offer</h3>
        </Col>
        <Col sm={4} className="d-flex justify-content-end">
          <Button className="d-flex align-items-center secondaryButton" onClick={() => setShowGenerateJobOfferModal(true)}>
            Generate Job Offer
          </Button>
        </Col>
        <Col sm={1} className="d-flex justify-content-end">
          <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate("/ui")}>
            <BsXLg size={"1.5em"} />
          </Button>
        </Col>
      </Row>

      {loading && <LoadingSection h={null} />}

      {!loading && (
        <Form onSubmit={handleSubmit}>
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

          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-4">
              <Form.Control placeholder="Job Offer Name" value={name} onChange={(e) => setName(e.target.value)} maxLength={255} required />
            </Col>
          </Row>
          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-4">
              <Form.Control
                as="textarea"
                placeholder="Description"
                rows={5}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                maxLength={255}
                required
              />
            </Col>
          </Row>
          <Row className="justify-content-center">
            <Col xs={12} md={6} lg={3} className="mb-4">
              <OverlayTrigger overlay={<Tooltip id="contractTypeButton">Select Contract Type</Tooltip>}>
                <Form.Select style={{ cursor: "pointer" }} value={contractType} onChange={(e) => setContractType(e.target.value)} required>
                  <option value="">Select Contract Type</option>
                  {contractTypeList.map((contract, index) => (
                    <option key={index} value={contract}>
                      {toTitleCase(contract)}
                    </option>
                  ))}
                </Form.Select>
              </OverlayTrigger>
            </Col>
            <Col xs={12} md={6} lg={3} className="mb-4">
              <OverlayTrigger overlay={<Tooltip id="workModeButton">Select Work Mode</Tooltip>}>
                <Form.Select style={{ cursor: "pointer" }} value={workMode} onChange={(e) => setWorkMode(e.target.value)} required>
                  <option value="">Select Work Mode</option>
                  {workModeList.map((workMode, index) => (
                    <option key={index} value={workMode}>
                      {toTitleCase(workMode)}
                    </option>
                  ))}
                </Form.Select>
              </OverlayTrigger>
            </Col>
          </Row>
          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-4">
              <Form.Control placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} required />
            </Col>
          </Row>
          <Row className="justify-content-center">
            <Col xs={12} md={6} lg={3} className="mb-4">
              {showSelectCustomerModal && (
                <CustomerSelectModal
                  show={showSelectCustomerModal}
                  handleClose={handleCloseSelectCustomerModal}
                  onSelectCustomer={handleCustomerSelect}
                />
              )}
              <Form.Group controlId="customer">
                <OverlayTrigger overlay={<Tooltip id="customerButton">Select a Customer</Tooltip>}>
                  <Form.Control
                    style={{ cursor: "pointer" }}
                    type="text"
                    placeholder="Select a customer"
                    value={
                      customer
                        ? `${customer?.information.contactDTO.name} ${customer?.information.contactDTO.surname} (${customer?.information.contactDTO.ssnCode})`
                        : "Select a customer"
                    }
                    readOnly
                    onClick={handleOpenSelectCustomerModal}
                  />
                </OverlayTrigger>
              </Form.Group>
            </Col>

            <Col xs={12} md={6} lg={3} className="mb-4">
              <Form.Control
                type="number"
                placeholder="Duration in days"
                value={duration}
                onChange={(e) => {
                  const value = e.target.value;
                  if (/^\d*$/.test(value)) {
                    setDuration(value);
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
          </Row>

          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-4">
              <Form.Control as="textarea" placeholder="Note" rows={3} value={note} onChange={(e) => setNote(e.target.value)} maxLength={255} />
            </Col>
          </Row>

          <Row className="mt-4 justify-content-center">
            <Col xs={12} md={10} lg={6}>
              <Row className="align-items-center">
                <Col>
                  <hr />
                </Col>
                <Col xs="auto">
                  <h5 className="fw-bold text-primary">Required Skills</h5>
                </Col>
                <Col>
                  <hr />
                </Col>
              </Row>

              {requiredSkills.length === 0 && (
                <Row className="justify-content-center mt-3">
                  <Col xs={12} className="text-center">
                    <p className="text-muted">No required skill added yet.</p>
                  </Col>
                </Row>
              )}

              {requiredSkills.length > 0 &&
                requiredSkills.map((requiredSkill, index) => (
                  <Row key={index} className="mt-3 d-flex align-items-center justify-content-between">
                    <Col xs={8} md={8} lg={10}>
                      <li>
                        <ul>
                          <p className="text-truncate fw-light mb-0">{requiredSkill}</p>
                        </ul>
                      </li>
                    </Col>
                    <Col xs={4} md={4} lg={2} className="text-end">
                      <Button variant="outline-danger" size="sm" onClick={() => setRequiredSkills(requiredSkills.filter((_, i) => i !== index))}>
                        Remove
                      </Button>
                    </Col>
                  </Row>
                ))}

              <Row className="mt-4 justify-content-center">
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
                <Col xs={12} md={4} lg={3} className="text-end">
                  <OverlayTrigger overlay={<Tooltip id="addSkillButton">Add Skill</Tooltip>}>
                    <Button variant="primary" onClick={handleAddSkill}>
                      Add Skill
                    </Button>
                  </OverlayTrigger>
                </Col>
              </Row>
            </Col>
          </Row>

          <Row className="mt-5 justify-content-center">
            <Col xs={12} md={12} lg={3} className="d-flex flex-column justify-content-end align-items-center">
              <Button
                className="secondaryDangerButton"
                onClick={() => {
                  setName("");
                  setDescription("");
                  setDuration("");
                  setNote("");
                  setContractType("");
                  setLocation("");
                  setWorkMode("");
                  setCustomer(null);
                  setRequiredSkills([]);
                }}
              >
                Clear
              </Button>
            </Col>
            <Col xs={12} md={12} lg={3} className="d-flex flex-column justify-content-start align-items-center">
              <Button type="submit" className="primaryButton">
                Save
              </Button>
            </Col>
          </Row>
        </Form>
      )}
    </div>
  );
}

const CustomerSelectModal: React.FC<{
  show: boolean;
  handleClose: () => void;
  onSelectCustomer: (selectedCustomer: Customer) => void;
}> = ({ show, handleClose, onSelectCustomer }) => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [searchName, setSearchName] = useState("");
  const [searchSurname, setSearchSurname] = useState("");
  const [searchSsnCode, setSearchSsnCode] = useState("");
  const [searchComment, setSearchComment] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const loadCustomers = async () => {
      try {
        setLoading(true);
        const result: any = await fetchCustomers(currentPage, searchName, searchSurname, searchSsnCode, searchComment);
        setCustomers(result.content);
        setTotalPages(result.totalPages);
        setLoading(false);
      } catch (error) {
        setError(true);
        setLoading(false);
      }
    };

    if (show) {
      loadCustomers();
    }
  }, [show, currentPage, searchName, searchSurname, searchSsnCode, searchComment]);

  const handleSearchChangeByName = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchName(event.target.value);
  };

  const handleSearchChangeBySurname = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchSurname(event.target.value);
  };

  const handleSearchChangeBySsnCode = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchSsnCode(event.target.value);
  };

  const handleSearchChangeByComment = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setSearchComment(event.target.value);
  };

  const handleCustomerSelect = (customer: Customer) => {
    onSelectCustomer(customer);
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
        <Modal.Title className="fw-bold">Select Customer</Modal.Title>
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
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by ssn code"
                value={searchSsnCode}
                onChange={handleSearchChangeBySsnCode}
              />
            </Col>
            <Col xs={12} sm={6}>
              <Form.Control
                type="text"
                className="mb-2"
                placeholder="Search by comment"
                value={searchComment}
                onChange={handleSearchChangeByComment}
              />
            </Col>
          </Row>
        </Form.Group>
        {loading ? (
          <div className="d-flex justify-content-center align-items-center" style={{ height: "200px" }}>
            <div className="spinner-border" role="status">
              <span className="sr-only"></span>
            </div>
          </div>
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
                {customers.length === 0 && (
                  <tr>
                    <td colSpan={3}>
                      <div className="d-flex justify-content-center align-items-center" style={{ height: "150px" }}>
                        <span className="text-muted fw-bold">No Customer Found!</span>
                      </div>
                    </td>
                  </tr>
                )}
                {customers.map((customer) => (
                  <tr
                    key={customer.information.contactDTO.id}
                    onClick={() => handleCustomerSelect(customer)}
                    style={{
                      cursor: "pointer",
                    }}
                  >
                    <td>{customer.information.contactDTO.name}</td>
                    <td>{customer.information.contactDTO.surname}</td>
                    <td>{customer.information.contactDTO.ssnCode}</td>
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

const DescriptionGenerateJobOfferModal: React.FC<{
  show: boolean;
  handleClose: () => void;
  onSubmit: (description: string) => void;
}> = ({ show, handleClose, onSubmit }) => {
  const [description, setDescription] = useState<string>("");

  const handleSubmit = () => {
    if (description.trim() !== "") {
      onSubmit(description);
      setDescription("");
    }
  };

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title className="fw-bold">Enter Job Offer Description</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          <Form.Group controlId="descriptionInput">
            <Form.Control
              as="textarea"
              rows={3}
              placeholder="Enter a brief and detailed description of the job offer"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <Form.Text className="text-muted">Try to be as precise as possible when describing the job offer details.</Form.Text>
            <br />
            <Form.Text className="text-muted">
              For example: <i>Generate a job offer for a Java developer with Spring skills and microservices.</i>
            </Form.Text>
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <Button variant="secondary" className="ms-5" onClick={handleClose}>
          Close
        </Button>
        <Button variant="success" className="me-5" onClick={handleSubmit} disabled={description.length === 0}>
          Generate
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default AddJobOfferPage;
