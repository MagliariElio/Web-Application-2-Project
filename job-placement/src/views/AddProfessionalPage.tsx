import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Modal, Row } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsPencilSquare, BsTrash, BsX, BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { createProfessional } from "../apis/ProfessionalRequests";
import { deleteContactWhatContact, editContactWhatContact, fetchAllContactWhatContact, postNewWhatContact } from "../apis/ContactRequests";
import { Email } from "../interfaces/Email";
import { Telephone } from "../interfaces/Telephone";
import { Address } from "../interfaces/Address";
import { toTitleCase } from "../utils/costants";
import { checkValidEmail, checkValidTelephone } from "../utils/checkers";
import { LoadingSection } from "../App";
import { DescriptionGenerateAIModal } from "./AddJobOfferPage";
import { generateSkillsAPI } from "../apis/JobOfferRequests";
import { FaMicrochip, FaPlus, FaTrashAlt } from "react-icons/fa";

function AddProfessionalPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [ssnCode, setSsnCode] = useState("");
  const [comment, setComment] = useState("");

  const [contactModalOpen, setContactModalOpen] = useState<string | null>(null);

  const [emails, setEmails] = useState<any[]>([]);

  const [telephones, setTelephones] = useState<any[]>([]);

  const [addresses, setAddresses] = useState<any[]>([]);

  const [skills, setSkills] = useState<string[]>([]);
  const [singleSkill, setSingleSkill] = useState("");
  const [geographicalLocation, setGeographicalLocation] = useState("");
  const [dailyRate, setDailyRate] = useState("");

  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const [showGenerateSkillsModal, setShowGenerateSkillsModal] = useState(false);
  const [generationSkills, setGenerationSkills] = useState(false);

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();

    if (name.trim() === "") {
      setErrorMessage("The name cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (surname.trim() === "") {
      setErrorMessage("The surname cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (ssnCode.trim() === "") {
      setErrorMessage("The SSN Code cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (geographicalLocation.trim() === "") {
      setErrorMessage("The geographical location cannot be empty or just spaces.");
      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (skills.length === 0) {
      setErrorMessage("You must add at least one skill before saving.");

      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    const professional = {
      information: {
        name: name,
        surname: surname,
        ssnCode: ssnCode,
        comment: comment,
        category: "PROFESSIONAL",
        emails: emails,
        telephones: telephones,
        addresses: addresses,
      },
      skills: skills,
      geographicalLocation: geographicalLocation,
      dailyRate: dailyRate,
    };

    createProfessional(professional, me)
      .then(() => {
        navigate("/ui/professionals", { state: { success: true } });
      })
      .catch((error) => {
        navigate("/ui/professionals", { state: { success: false } });
        console.log("Error during professional post: ", error);
        throw new Error("POST /API/professionals : Network response was not ok");
      });
  };

  const generateSkills = async (description: string) => {
    try {
      setGenerationSkills(true);
      setShowGenerateSkillsModal(false);
      const response = await generateSkillsAPI(description, me.xsrfToken);

      const newSkills: string[] = [...skills];
      response.forEach((r: string) => newSkills.push(r));
      setSkills(newSkills);

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

  return (
    <div className="add-job-offer-container">
      <DescriptionGenerateAIModal
        name={"Generate Professional Skills with AI"}
        placeholderValue={"Enter a detailed description of the professional role to generate the skills"}
        suggestion_1={"Be as precise as possible about the role and responsibilities to generate relevant skills."}
        suggestion_2={
          "For example: <i>Generate required skills for a Senior Project Manager with experience in Agile methodologies and team leadership.</i>"
        }
        show={showGenerateSkillsModal}
        handleClose={() => setShowGenerateSkillsModal(false)}
        onSubmit={generateSkills}
      />

      {contactModalOpen != null && (
        <ContactModal
          me={me}
          open={contactModalOpen}
          setOpen={setContactModalOpen}
          contactContainer={
            contactModalOpen === "email" ? emails : contactModalOpen === "telephone" ? telephones : contactModalOpen === "address" ? addresses : []
          }
          setContactContainer={
            contactModalOpen === "email"
              ? setEmails
              : contactModalOpen === "telephone"
              ? setTelephones
              : contactModalOpen === "address"
              ? setAddresses
              : []
          }
        />
      )}

      <Row className="d-flex flex-row p-0 mb-5 align-items-center">
        <Col>
          <h3>Add New Professional</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate(-1)}>
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
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Control placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required />
          </Col>
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Control placeholder="Surname" value={surname} onChange={(e) => setSurname(e.target.value)} required />
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={6} lg={6} className="mb-4">
            <Form.Control placeholder="SSN Code" value={ssnCode} required onChange={(e) => setSsnCode(e.target.value)} />
          </Col>
        </Row>

        <Row className="justify-content-center mb-4">
          <Col xs={12} md={12} lg={3}>
            <Form.Control
              placeholder="Geographical location"
              required
              value={geographicalLocation}
              onChange={(e) => setGeographicalLocation(e.target.value)}
            />
          </Col>

          <Col xs={12} md={12} lg={3}>
            <Form.Control
              type="text"
              placeholder="Daily rate"
              value={dailyRate}
              onChange={(e) => {
                const value = e.target.value;
                if (/^\d*\.?\d{0,2}$/.test(value)) {
                  setDailyRate(value);
                }
              }}
              onBlur={() => {
                if (dailyRate) {
                  const formattedValue = parseFloat(dailyRate).toFixed(2);
                  setDailyRate(formattedValue);
                }
              }}
              min={0}
              required
            />
          </Col>
        </Row>

        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-4">
            <Form.Control
              as="textarea"
              placeholder="Comments"
              rows={4}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              maxLength={255}
            />
          </Col>
        </Row>

        <Row className="mt-3 justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Row className="align-items-center">
              <Col>
                <hr />
              </Col>
              <Col xs="auto">
                <h5 className="fw-normal">Emails</h5>
              </Col>
              <Col>
                <hr />
              </Col>
            </Row>
          </Col>
        </Row>
        {emails.length === 0 && (
          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-0">
              <p>No emails added yet.</p>
            </Col>
          </Row>
        )}
        {emails.length > 0 &&
          emails.map((email, index) => {
            return (
              <Row key={index} className="mb-1 d-flex align-items-center justify-content-center">
                <Col xs={8} md={6} lg={5}>
                  <Row className="justify-content-center">
                    <Col xs={12} md={12} lg={6} className="mb-0">
                      <p className="text-truncate">{email.email}</p>
                    </Col>
                    <Col xs={12} md={12} lg={6} className="mb-0 fs-10 fw-light">
                      <p className="text-truncate">{email.comment}</p>
                    </Col>
                  </Row>
                </Col>
                <Col xs={4} md={6} lg={1}>
                  <Col className="mb-0">
                    <Button
                      className="secondaryDangerButton w-100"
                      onClick={() => {
                        setEmails(emails.filter((_e, i) => i !== index));
                      }}
                    >
                      Remove
                    </Button>
                  </Col>
                </Col>
              </Row>
            );
          })}
        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Button
              className="secondaryButton w-100"
              onClick={() => {
                setContactModalOpen("email");
              }}
            >
              Add email
            </Button>
          </Col>
        </Row>

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Row className="align-items-center">
              <Col>
                <hr />
              </Col>
              <Col xs="auto">
                <h5 className="fw-normal">Telephones</h5>
              </Col>
              <Col>
                <hr />
              </Col>
            </Row>
          </Col>
        </Row>
        {telephones.length === 0 && (
          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-0">
              <p>No phone numbers added yet.</p>
            </Col>
          </Row>
        )}
        {telephones.length > 0 &&
          telephones.map((telephone, index) => {
            return (
              <Row key={index} className="mb-1 d-flex align-items-center justify-content-center">
                <Col xs={8} md={6} lg={5}>
                  <Row className="justify-content-center">
                    <Col xs={12} md={12} lg={6} className="mb-0">
                      <p className="text-truncate">{telephone.telephone}</p>
                    </Col>
                    <Col xs={12} md={12} lg={6} className="mb-0  fs-10 fw-light">
                      <p className="text-truncate">{telephone.comment}</p>
                    </Col>
                  </Row>
                </Col>
                <Col xs={4} md={6} lg={1}>
                  <Col className="mb-0">
                    <Button
                      className="secondaryDangerButton w-100"
                      onClick={() => {
                        setTelephones(telephones.filter((_e, i) => i !== index));
                      }}
                    >
                      Remove
                    </Button>
                  </Col>
                </Col>
              </Row>
            );
          })}

        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Button
              className="secondaryButton w-100"
              onClick={() => {
                setContactModalOpen("telephone");
              }}
            >
              Add telephone
            </Button>
          </Col>
        </Row>

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Row className="align-items-center">
              <Col>
                <hr />
              </Col>
              <Col xs="auto">
                <h5 className="fw-normal">Address</h5>
              </Col>
              <Col>
                <hr />
              </Col>
            </Row>
          </Col>
        </Row>
        {addresses.length === 0 && (
          <Row className="justify-content-center">
            <Col xs={12} md={12} lg={6} className="mb-0">
              <p>No addresses added yet.</p>
            </Col>
          </Row>
        )}
        {addresses.length > 0 &&
          addresses.map((address, index) => {
            return (
              <Row key={index} className="mb-1 d-flex align-items-center justify-content-center">
                <Col xs={8} md={6} lg={5}>
                  <Row className="justify-content-center">
                    <Col xs={12} md={12} lg={6} className="mb-0">
                      <p className="text-truncate">{`${address.address}, ${address.city}, ${address.region}, ${address.state}`}</p>
                    </Col>
                    <Col xs={12} md={12} lg={6} className="mb-0 fs-10 fw-light">
                      <p className="text-truncate">{address.comment}</p>
                    </Col>
                  </Row>
                </Col>
                <Col xs={4} md={6} lg={1}>
                  <Col className="mb-0">
                    <Button
                      className="secondaryDangerButton w-100"
                      onClick={() => {
                        setAddresses(addresses.filter((_e, i) => i !== index));
                      }}
                    >
                      Remove
                    </Button>
                  </Col>
                </Col>
              </Row>
            );
          })}

        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Button
              className="secondaryButton w-100"
              onClick={() => {
                setContactModalOpen("address");
              }}
            >
              Add address
            </Button>
          </Col>
        </Row>

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Row className="align-items-center">
              <Col>
                <hr />
              </Col>
              <Col xs="auto">
                <h5 className="fw-normal">Skills</h5>
              </Col>
              <Col>
                <hr />
              </Col>
            </Row>
          </Col>
        </Row>

        {generationSkills && <LoadingSection h={200} />}
        {!generationSkills && (
          <>
            {skills.length === 0 && (
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6} className="mb-0">
                  <p>No skills added yet.</p>
                </Col>
              </Row>
            )}
            {
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6} className="mb-2">
                  <Row className="d-flex flex-wrap ps-2">
                    {skills.length > 0 &&
                      skills.map((skill, index) => (
                        <div key={index} style={{ width: "auto" }} className="text-truncate me-2 tag mb-1">
                          {skill}

                          <BsX
                            size={20}
                            className="ms-2"
                            style={{ cursor: "pointer" }}
                            onClick={() => {
                              setSkills(skills.filter((_e, i) => i !== index));
                            }}
                          />
                        </div>
                      ))}
                  </Row>
                </Col>
              </Row>
            }
          </>
        )}

        {!generationSkills && (
          <>
            <Row className="justify-content-center">
              <Col xs={12} md={8} lg={5} className="mb-2">
                <Form.Control placeholder="Enter a new skill" value={singleSkill} onChange={(e) => setSingleSkill(e.target.value)} />
              </Col>
            </Row>

            <Row className="justify-content-center mt-4">
              <Col xs={12} md={8} lg={6} className="d-flex justify-content-center">
                <Button
                  className="secondaryButton mb-2 d-flex align-items-center me-2"
                  onClick={() => {
                    if (singleSkill.trim() === "") {
                      setErrorMessage("Please enter a skill before adding.");
                      if (errorRef.current) {
                        errorRef.current.scrollIntoView({ behavior: "smooth" });
                      }
                      return;
                    }

                    setSkills([...skills, singleSkill]);
                    setSingleSkill("");
                  }}
                  disabled={singleSkill.trim() === ""}
                >
                  <FaPlus style={{ marginRight: "5px" }} />
                  Add Skill
                </Button>

                <Button
                  className="secondaryDangerButton mb-2 d-flex align-items-center me-2"
                  onClick={() => setSkills([])}
                  disabled={skills.length === 0}
                >
                  <FaTrashAlt style={{ marginRight: "5px" }} />
                  Clear
                </Button>

                <Button
                  className="secondaryButton mb-2 d-flex align-items-center"
                  onClick={() => setShowGenerateSkillsModal(true)}
                  disabled={skills.length > 100}
                >
                  <FaMicrochip style={{ marginRight: "5px" }} />
                  Generate Skills with AI
                </Button>
              </Col>
            </Row>
          </>
        )}

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
            <Button type="submit" className="primaryButton">
              Save
            </Button>
          </Col>
        </Row>
      </Form>
    </div>
  );
}

export default AddProfessionalPage;

const loadContactContacts = async (whatContact: string): Promise<Email[] | Telephone[] | Address[]> => {
  if (whatContact === "email") {
    try {
      const allEmails = await fetchAllContactWhatContact("email");
      console.log("All emails: ", allEmails);
      return allEmails;
    } catch (err) {
      console.log("Error fetching emails: ", err);
      return [] as Email[] | Telephone[] | Address[];
    }
  }
  if (whatContact === "telephone") {
    try {
      const allTelephones = await fetchAllContactWhatContact("telephone");
      console.log("All telephones: ", allTelephones);
      return allTelephones;
    } catch (err) {
      console.log("Error fetching telephones: ", err);
      return [] as Email[] | Telephone[] | Address[];
    }
  }
  if (whatContact === "address") {
    try {
      const allAddresses = await fetchAllContactWhatContact("address");
      console.log("All addresses: ", allAddresses);
      return allAddresses;
    } catch (err) {
      console.log("Error fetching addresses: ", err);
      return [] as Email[] | Telephone[] | Address[];
    }
  }

  return [] as Email[] | Telephone[] | Address[];
};

export const ContactModal = ({
  me,
  open,
  setOpen,
  contactContainer,
  setContactContainer,
}: {
  me: MeInterface;
  open: string | null;
  setOpen: any;
  contactContainer: Email[] | Telephone[] | Address[];
  setContactContainer: any;
}) => {
  const [contacts, setContacts] = useState<Email[] | Telephone[] | Address[]>([]);

  const [singleContact, setSingleContact] = useState("");
  const [singleContactComment, setSingleContactComment] = useState("");

  const [singleCity, setSingleCity] = useState("");
  const [singleRegion, setSingleRegion] = useState("");
  const [singleState, setSingleState] = useState("");

  const [deleteSelected, setDeleteSelected] = useState<number | null>(null);
  const [editSelected, setEditSelected] = useState<Email | Telephone | Address | null>(null);

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchContacts = async () => {
      if (open !== null) {
        setLoading(true);
        const cont = await loadContactContacts(open);
        setContacts(cont);
        setLoading(false);
        console.log("Fetched whatcontacts: ", cont);
      }
    };

    fetchContacts();
  }, [open]);

  useEffect(() => {
    if (editSelected !== null) {
      if (open === "email") {
        setSingleContact((editSelected as Email).email);
        setSingleContactComment((editSelected as Email).comment);
      } else if (open === "telephone") {
        setSingleContact((editSelected as Telephone).telephone);
        setSingleContactComment((editSelected as Telephone).comment);
      } else {
        setSingleContact((editSelected as Address).address);
        setSingleCity((editSelected as Address).city);
        setSingleRegion((editSelected as Address).region);
        setSingleState((editSelected as Address).state);
        setSingleContactComment((editSelected as Address).comment);
      }
    }
  }, [editSelected?.id]);

  return (
    <Modal size="lg" show={open != null} onHide={() => setOpen(null)}>
      <Modal.Header closeButton>
        <Modal.Title>Add New {toTitleCase(open ? open : "")}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <small>
          Click on a <strong>{open}</strong> to select it or use side buttons to edit and delete them from the <strong>{open} book</strong>.
        </small>

        {loading && <LoadingSection h={300} />}

        {!loading && contacts.length === 0 && (
          <Row className="mt-2">
            <Col xs={12} md={12} lg={6} className="mb-0">
              <p>No {open}s added yet.</p>
            </Col>
          </Row>
        )}

        {!loading &&
          contacts.length > 0 &&
          contacts
            .filter((contact) => !contactContainer.find((c) => c.id === contact.id))
            .filter((contact) => {
              if (editSelected !== null) {
                return true;
              }
              if (open === "email") {
                return (contact as Email).email.includes(singleContact) && (contact as Email).comment.includes(singleContactComment);
              } else if (open === "telephone") {
                return (contact as Telephone).telephone.includes(singleContact) && (contact as Telephone).comment.includes(singleContactComment);
              } else {
                return (contact as Address).address.includes(singleContact) && (contact as Address).comment.includes(singleContactComment);
              }
            })
            .sort((a, b) => {
              if (open === "email") {
                return (a as Email).email.localeCompare((b as Email).email);
              } else if (open === "telephone") {
                return (a as Telephone).telephone.localeCompare((b as Telephone).telephone);
              } else {
                return (a as Address).address.localeCompare((b as Address).address);
              }
            })
            .map((contact, index) => {
              return (
                <>
                  <Row key={index} className="mb-1 mt-2 ms-1 d-flex align-items-center">
                    <Col
                      xs={12}
                      md={8}
                      lg={10}
                      className={
                        "d-flex align-items-center justify-content-between " +
                        (editSelected?.id === contact.id ? " secondaryWarningButton " : "") +
                        (deleteSelected === contact.id ? " secondaryDangerButton " : " secondaryButton ")
                      }
                      onClick={() => {
                        setContactContainer([...contactContainer, contact]);
                        setOpen(null);
                      }}
                    >
                      <Row className="w-100">
                        <Col xs={12} md={12} lg={6} className="my-2  d-flex align-items-center">
                          <p className="text-truncate m-0">
                            {open === "email"
                              ? (contact as Email).email
                              : open === "telephone"
                              ? (contact as Telephone).telephone
                              : `${(contact as Address).address}, ${(contact as Address).city}, ${(contact as Address).region}, ${
                                  (contact as Address).state
                                }`}
                          </p>
                        </Col>
                        <Col xs={12} md={12} lg={6} className="my-2 fs-10 fw-light d-flex align-items-center">
                          <p className="text-truncate m-0">
                            {open === "email"
                              ? (contact as Email).comment
                              : open === "telephone"
                              ? (contact as Telephone).comment
                              : (contact as Address).comment}
                          </p>
                        </Col>
                      </Row>
                    </Col>
                    <Col xs={6} md={2} lg={1}>
                      <Col className="mb-0">
                        <Button
                          className="secondaryButton w-100 d-flex justify-content-center align-items-center "
                          onClick={() => {
                            if (editSelected === null || editSelected?.id !== contact.id) {
                              setEditSelected(contact);
                            } else {
                              setEditSelected(null);
                              setSingleContact("");
                              setSingleContactComment("");
                              setSingleCity("");
                              setSingleRegion("");
                              setSingleState("");
                            }
                          }}
                        >
                          {editSelected?.id === contact.id && <BsXLg size={20} />}

                          {editSelected?.id !== contact.id && <BsPencilSquare size={20} />}
                        </Button>
                      </Col>
                    </Col>
                    <Col xs={6} md={2} lg={1}>
                      <Col className="mb-0">
                        <Button
                          className="secondaryDangerButton w-100 d-flex justify-content-center align-items-center "
                          onClick={() => {
                            if (contact.id !== undefined) {
                              setDeleteSelected(contact.id);
                            }
                          }}
                        >
                          <BsTrash size={20} />
                        </Button>
                      </Col>
                    </Col>
                  </Row>
                  {deleteSelected === contact.id && (
                    <Row className="mt-2 ms-2 d-flex align-items-center">
                      <Col xs={12} md={12} lg={6} className="mb-0">
                        <p className="text-danger my-auto">Are you sure you want to delete this {open}?</p>
                        <p className="text-danger my-auto">It will be also removed from all contacts using it</p>
                      </Col>
                      <Col xs={6} md={2} lg={1}>
                        <Col className="mb-0">
                          <Button
                            className="secondaryDangerButton w-100 d-flex justify-content-center align-items-center "
                            onClick={() => {
                              if (contact.id !== undefined) {
                                deleteContactWhatContact(open!!, contact.id.toString(), me).then(() => {
                                  setContacts((prevContacts) => prevContacts.filter((e) => e.id !== contact.id) as Email[] | Telephone[] | Address[]);

                                  setDeleteSelected(null);
                                });
                              }
                            }}
                          >
                            Yes
                          </Button>
                        </Col>
                      </Col>
                      <Col xs={6} md={2} lg={1}>
                        <Col className="mb-0">
                          <Button
                            className="secondaryButton w-100 d-flex justify-content-center align-items-center "
                            onClick={() => {
                              setDeleteSelected(null);
                            }}
                          >
                            No
                          </Button>
                        </Col>
                      </Col>
                    </Row>
                  )}
                </>
              );
            })}

        <Form className="mt-4">
          <small className="mb-1 ms-1">{editSelected === null ? `Use fields to search or create a new ${open}` : `Edit the ${open} below`}</small>
          <Row className="d-flex align-items-center">
            <Col xs={12} md={12} lg={6} className="">
              <Form.Control
                value={singleContact}
                onChange={(e) => {
                  setSingleContact(e.target.value);
                }}
                placeholder={open === "email" ? "Email address" : open === "telephone" ? "Telephone number" : "Address"}
              />
            </Col>
            {open === "address" && (
              <>
                <Col xs={12} md={12} lg={6} className="">
                  <Form.Control
                    value={singleCity}
                    onChange={(e) => {
                      setSingleCity(e.target.value);
                    }}
                    placeholder="City"
                  />
                </Col>
                <Col xs={12} md={12} lg={6} className="my-2">
                  <Form.Control
                    value={singleRegion}
                    onChange={(e) => {
                      setSingleRegion(e.target.value);
                    }}
                    placeholder="Region"
                  />
                </Col>
                <Col xs={12} md={12} lg={6} className="">
                  <Form.Control
                    value={singleState}
                    onChange={(e) => {
                      setSingleState(e.target.value);
                    }}
                    placeholder="State"
                  />
                </Col>
              </>
            )}
            <Col xs={12} md={12} lg={open === "address" ? 9 : 3} className="">
              <Form.Control
                value={singleContactComment}
                onChange={(e) => {
                  setSingleContactComment(e.target.value);
                }}
                placeholder={open === "email" ? "Email address comment" : open === "telephone" ? "Telephone number comment" : "Address comment"}
              />
            </Col>
            <Col xs={12} md={12} lg={3} className="d-flex flex-column justify-content-center align-items-center">
              <Button
                disabled={
                  (open === "email" &&
                    (singleContact.trim() === "" ||
                      !checkValidEmail(singleContact) ||
                      contactContainer.some((c): c is Email => "email" in c && c.email === singleContact))) ||
                  (open === "telephone" &&
                    (singleContact.trim() === "" ||
                      !checkValidTelephone(singleContact) ||
                      contactContainer.some((c): c is Telephone => "telephone" in c && c.telephone === singleContact))) ||
                  (open === "address" &&
                    (singleContact.trim() === "" ||
                      singleCity.trim() === "" ||
                      singleRegion.trim() === "" ||
                      singleState.trim() === "" ||
                      contactContainer.some(
                        (c): c is Address =>
                          "address" in c &&
                          (c.address === singleContact || c.city === singleCity || c.region === singleRegion || c.state === singleState)
                      )))
                }
                className="primaryButton"
                onClick={() => {
                  if (editSelected === null) {
                    postNewWhatContact(
                      open!!,
                      open === "email"
                        ? {
                            createEmailDTO: {
                              email: singleContact,
                              comment: singleContactComment,
                            },
                          }
                        : open === "telephone"
                        ? {
                            createTelephoneDTO: {
                              telephone: singleContact,
                              comment: singleContactComment,
                            },
                          }
                        : {
                            createAddressDTO: {
                              address: singleContact,
                              city: singleCity,
                              region: singleRegion,
                              state: singleState,
                              comment: singleContactComment,
                            },
                          },
                      me
                    ).then((res) => {
                      setContactContainer([...contactContainer, res]);
                      setOpen(null);
                    });
                  } else {
                    editContactWhatContact(
                      open!!,
                      editSelected.id.toString(),
                      open === "email"
                        ? {
                            createEmailDTO: {
                              email: singleContact,
                              comment: singleContactComment,
                            },
                          }
                        : open === "telephone"
                        ? {
                            createTelephoneDTO: {
                              telephone: singleContact,
                              comment: singleContactComment,
                            },
                          }
                        : {
                            createAddressDTO: {
                              address: singleContact,
                              city: singleCity,
                              region: singleRegion,
                              state: singleState,
                              comment: singleContactComment,
                            },
                          },
                      me
                    ).then((res) => {
                      setContacts(
                        (prevContacts) => prevContacts.map((c) => (c.id === editSelected.id ? res : c)) as Email[] | Telephone[] | Address[]
                      );
                      setSingleContact("");
                      setSingleCity("");
                      setSingleRegion("");
                      setSingleState("");
                      setSingleContactComment("");
                      setEditSelected(null);
                    });
                  }
                }}
              >
                {editSelected === null ? `Create new ${open}` : `Save changes`}
              </Button>
            </Col>
          </Row>
        </Form>
      </Modal.Body>
    </Modal>
  );
};
