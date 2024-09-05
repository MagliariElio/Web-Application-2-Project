import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Container, Modal, Pagination, Row, Table } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsXLg } from "react-icons/bs";
import { useLocation, useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { contractTypeList, toTitleCase, workModeList } from "../utils/costants";
import { Customer } from "../interfaces/Customer";
import { fetchCustomers } from "../apis/CustomerRequests";
import { submitJobOffer } from "../apis/JobOfferRequests";
import { debounce } from "../utils/checkers";

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

  useEffect(() => {
    if (selectedCustomer) {
      setCustomer(selectedCustomer.customer);
    }
  }, []);

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
      await submitJobOffer(jobOffer, me.xsrfToken);
      navigate("/ui", { state: { success: true } });
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
    <div>
      <Row className="d-flex flex-row p-0 mb-5 align-items-center">
        <Col>
          <h3>Add New Job Offer</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate("/ui")}>
            <BsXLg size={"1.5em"} />
          </Button>
        </Col>
      </Row>

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
            <Form.Control placeholder="Job Offer Name" value={name} onChange={(e) => setName(e.target.value)} required />
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
              required
            />
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Select value={contractType} onChange={(e) => setContractType(e.target.value)} required>
              <option value="">Select Contract Type</option>
              {contractTypeList.map((contract, index) => (
                <option key={index} value={contract}>
                  {toTitleCase(contract)}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Select value={workMode} onChange={(e) => setWorkMode(e.target.value)} required>
              <option value="">Select Work Mode</option>
              {workModeList.map((workMode, index) => (
                <option key={index} value={workMode}>
                  {toTitleCase(workMode)}
                </option>
              ))}
            </Form.Select>
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
            </Form.Group>
          </Col>

          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Control
              type="number"
              placeholder="Duration in hours"
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
            <Form.Control as="textarea" placeholder="Note" rows={3} value={note} onChange={(e) => setNote(e.target.value)} />
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
                  <p className="text-muted">No required skill added yet</p>
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
                <Button variant="primary" onClick={handleAddSkill}>
                  Add Skill
                </Button>
              </Col>
            </Row>
          </Col>
        </Row>

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
            <Button type="submit" className="primaryButton">
              Save Job Offer
            </Button>
          </Col>
        </Row>
      </Form>
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

export default AddJobOfferPage;
