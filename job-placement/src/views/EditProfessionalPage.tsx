import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Row } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsX, BsXLg } from "react-icons/bs";
import { useNavigate, useParams } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import {
  fetchProfessional,
  updateProfessional,
} from "../apis/ProfessionalRequests";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";
import { ContactModal } from "./AddProfessionalPage";
import {
  fetchContactAddresses,
  fetchContactEmails,
  fetchContactTelephones,
} from "../apis/ContactRequests";
import { generateSkillsAPI } from "../apis/JobOfferRequests";
import { DescriptionGenerateAIModal } from "./AddJobOfferPage";
import { FaMicrochip, FaPlus, FaTrashAlt } from "react-icons/fa";
import { LoadingSection } from "../App";
import type { DocumentFile } from "../interfaces/DocumentFile";
import { getDocumentById } from "../apis/DocumentRequests";

function EditProfessionalPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const [contactModalOpen, setContactModalOpen] = useState<string | null>(null);

  const { id } = useParams();
  const [contactID, setContactID] = useState<number>(0);
  const [employmentState, setEmploymentState] = useState("");

  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [ssnCode, setSsnCode] = useState("");
  const [comment, setComment] = useState("");

  const [emails, setEmails] = useState<any[]>([]);

  const [telephones, setTelephones] = useState<any[]>([]);

  const [addresses, setAddresses] = useState<any[]>([]);

  const [skills, setSkills] = useState<string[]>([]);
  const [singleSkill, setSingleSkill] = useState("");
  const [geographicalLocation, setGeographicalLocation] = useState("");
  const [dailyRate, setDailyRate] = useState("");

  const [showGenerateSkillsModal, setShowGenerateSkillsModal] = useState(false);
  const [generationSkills, setGenerationSkills] = useState(false);

  const [attachments, setAttachments] = useState<(File | DocumentFile)[]>([]);
  const [singleAttachment, setSingleAttachment] = useState<File | null>(null);

  const [removedAttachments, setRemovedAttachments] = useState<number[]>([]);
  const [addedAttachments, setAddedAttachments] = useState<File[]>([]);

  useEffect(() => {
    if (
      id === undefined ||
      id === null ||
      id === "" ||
      Number.parseInt(id) < 1
    ) {
      navigate("/not-found");
      return;
    }

    setLoading(true);

    fetchProfessional(Number.parseInt(id))
      .then((json: ProfessionalWithAssociatedData) => {
        setContactID(json.professionalDTO.information.id);
        setEmploymentState(json.professionalDTO.employmentState);
        setName(json.professionalDTO.information.name);
        setSurname(json.professionalDTO.information.surname);
        setSsnCode(json.professionalDTO.information.ssnCode);
        setComment(json.professionalDTO.information.comment);
        setSkills(json.professionalDTO.skills);
        setGeographicalLocation(json.professionalDTO.geographicalLocation);
        setDailyRate(json.professionalDTO.dailyRate);

        fetchContactEmails(json.professionalDTO.information.id).then(
          (emails) => {
            setEmails(emails);
          }
        );

        fetchContactTelephones(json.professionalDTO.information.id).then(
          (telephones) => {
            setTelephones(telephones);
          }
        );

        fetchContactAddresses(json.professionalDTO.information.id).then(
          (addresses) => {
            setAddresses(addresses);
          }
        );

        setAttachments([]);

        json.professionalDTO.attachmentsList.forEach((attachment: number) => {
          getDocumentById(attachment, me)
            .then((response: DocumentFile) => {
              setAttachments((prevAttachments) => [
                ...prevAttachments,
                response,
              ]);
            })
            .catch((error) => {
              console.error("Error fetching attachment:", error);
            });
        });

        setLoading(false);
      })
      .catch(() => {
        navigate("/not-found");
        throw new Error(
          `GET /API/professional/${id} : Network response was not ok`
        );
      });
  }, [id]);

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
      setErrorMessage(
        "The geographical location cannot be empty or just spaces."
      );
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
      id: Number.parseInt(id!!),
      information: {
        id: Number.parseInt(contactID.toString()),
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
      employmentState: employmentState,
      geographicalLocation: geographicalLocation,
      dailyRate: dailyRate,
      attachmentsList: null,
    };

    updateProfessional(professional, addedAttachments, removedAttachments, me)
      .then(() => {
        navigate("/ui/professionals", { state: { success: true } });
      })
      .catch((error) => {
        navigate("/ui/professionals", { state: { success: false } });
        console.log("Error during professional post: ", error);
        throw new Error(
          "POST /API/professionals : Network response was not ok"
        );
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
        placeholderValue={
          "Enter a detailed description of the professional role to generate the skills"
        }
        suggestion_1={
          "Be as precise as possible about the role and responsibilities to generate relevant skills."
        }
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
            contactModalOpen === "email"
              ? emails
              : contactModalOpen === "telephone"
              ? telephones
              : contactModalOpen === "address"
              ? addresses
              : []
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

      {!loading && (
        <div>
          <Row className="d-flex flex-row p-0 mb-5 align-items-center">
            <Col>
              <h3>Edit professional</h3>
            </Col>
            <Col className="d-flex justify-content-end">
              <Button
                className="d-flex align-items-center secondaryButton"
                onClick={() => navigate(-1)}
              >
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
                <Form.Control
                  placeholder="Name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </Col>
              <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Control
                  placeholder="Surname"
                  value={surname}
                  onChange={(e) => setSurname(e.target.value)}
                  required
                />
              </Col>
            </Row>
            <Row className="justify-content-center">
              <Col xs={12} md={6} lg={6} className="mb-4">
                <Form.Control
                  placeholder="SSN Code"
                  value={ssnCode}
                  required
                  onChange={(e) => setSsnCode(e.target.value)}
                />
              </Col>
            </Row>
            <Row className="justify-content-center mb-4">
              <Col xs={12} md={12} lg={3} className="mb-4 md-lg-0">
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
                  <Row
                    key={index}
                    className="mb-1 d-flex align-items-center justify-content-center"
                  >
                    <Col xs={8} md={6} lg={5}>
                      <Row className="justify-content-center">
                        <Col xs={12} md={12} lg={6} className="mb-0">
                          <p className="text-truncate">{email.email}</p>
                        </Col>
                        <Col
                          xs={12}
                          md={12}
                          lg={6}
                          className="mb-0 fs-10 fw-light"
                        >
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

            <Row className="mt-3 justify-content-center">
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
                <Col xs={12} md={12} lg={6}>
                  <p>No phone numbers added yet.</p>
                </Col>
              </Row>
            )}
            {telephones.length > 0 &&
              telephones.map((telephone, index) => {
                return (
                  <Row
                    key={index}
                    className="mb-1 d-flex align-items-center justify-content-center"
                  >
                    <Col xs={8} md={6} lg={5}>
                      <Row className="justify-content-center">
                        <Col xs={12} md={12} lg={6} className="mb-0">
                          <p className="text-truncate">{telephone.telephone}</p>
                        </Col>
                        <Col
                          xs={12}
                          md={12}
                          lg={6}
                          className="mb-0  fs-10 fw-light"
                        >
                          <p className="text-truncate">{telephone.comment}</p>
                        </Col>
                      </Row>
                    </Col>
                    <Col xs={4} md={6} lg={1}>
                      <Col className="mb-0">
                        <Button
                          className="secondaryDangerButton w-100"
                          onClick={() => {
                            setTelephones(
                              telephones.filter((_e, i) => i !== index)
                            );
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

            <Row className="mt-3 justify-content-center">
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
                  <Row
                    key={index}
                    className="mb-1 d-flex align-items-center justify-content-center"
                  >
                    <Col xs={8} md={6} lg={5}>
                      <Row className="justify-content-center">
                        <Col xs={12} md={12} lg={6} className="mb-0">
                          <p className="text-truncate">{`${address.address}, ${address.city}, ${address.region}, ${address.state}`}</p>
                        </Col>
                        <Col
                          xs={12}
                          md={12}
                          lg={6}
                          className="mb-0 fs-10 fw-light"
                        >
                          <p className="text-truncate">{address.comment}</p>
                        </Col>
                      </Row>
                    </Col>
                    <Col xs={4} md={6} lg={1}>
                      <Col className="mb-0">
                        <Button
                          className="secondaryDangerButton w-100"
                          onClick={() => {
                            setAddresses(
                              addresses.filter((_e, i) => i !== index)
                            );
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

            <Row className="mt-3 justify-content-center">
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
                            <div
                              key={index}
                              style={{ width: "auto" }}
                              className="text-truncate me-2 tag mb-1"
                            >
                              {skill}

                              <BsX
                                size={20}
                                className="ms-2"
                                style={{ cursor: "pointer" }}
                                onClick={() => {
                                  setSkills(
                                    skills.filter((_e, i) => i !== index)
                                  );
                                }}
                              />
                            </div>
                          ))}
                      </Row>
                    </Col>
                  </Row>
                }

                {!generationSkills && (
                  <>
                    <Row className="justify-content-center mt-4">
                      <Col xs={12} md={10} lg={6} className="mb-2">
                        <Form.Control
                          placeholder="Enter a new skill"
                          value={singleSkill}
                          onChange={(e) => setSingleSkill(e.target.value)}
                        />
                      </Col>
                    </Row>

                    <Row className="justify-content-center mt-2">
                      <Col xs={6} md={5} lg={3}>
                        <Button
                          className="secondaryButton mb-2 d-flex align-items-center justify-content-center me-2 w-100"
                          onClick={() => {
                            if (singleSkill.trim() === "") {
                              setErrorMessage(
                                "Please enter a skill before adding."
                              );
                              if (errorRef.current) {
                                errorRef.current.scrollIntoView({
                                  behavior: "smooth",
                                });
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
                      </Col>

                      <Col xs={6} md={5} lg={3}>
                        <Button
                          className="secondaryDangerButton mb-2 d-flex align-items-center justify-content-center me-2 w-100"
                          onClick={() => setSkills([])}
                          disabled={skills.length === 0}
                        >
                          <FaTrashAlt style={{ marginRight: "5px" }} />
                          Clear
                        </Button>
                      </Col>
                    </Row>
                    <Row className="justify-content-center">
                      <Col xs={12} md={10} lg={6}>
                        <Button
                          className="secondaryButton mb-2 d-flex align-items-center justify-content-center w-100"
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
              </>
            )}

            <Row className="mt-5 justify-content-center">
              <Col xs={12} md={12} lg={6} className="mb-2">
                <Row className="align-items-center">
                  <Col>
                    <hr />
                  </Col>
                  <Col xs="auto">
                    <h5 className="fw-normal">Attachments</h5>
                  </Col>
                  <Col>
                    <hr />
                  </Col>
                </Row>
              </Col>
            </Row>
            {attachments.length === 0 && (
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6} className="mb-0">
                  <p>No uploaded file yet.</p>
                </Col>
              </Row>
            )}
            {attachments.length > 0 &&
              attachments.map((attachment, index) => {
                return (
                  <Row
                    key={index}
                    className="mb-1 d-flex align-items-center justify-content-center"
                  >
                    <Col xs={8} md={6} lg={5}>
                      <Row className="justify-content-center">
                        <Col xs={12} md={12} lg={6} className="mb-0">
                          <p className="text-truncate">{attachment.name}</p>
                        </Col>
                        <Col
                          xs={12}
                          md={12}
                          lg={6}
                          className="mb-0 fs-10 fw-light"
                        >
                          <p className="text-truncate">{`${attachment.size} B`}</p>
                        </Col>
                      </Row>
                    </Col>
                    <Col xs={4} md={6} lg={1}>
                      <Col className="mb-0">
                        <Button
                          className="secondaryDangerButton w-100"
                          onClick={() => {
                            if ("id" in attachment) {
                              setRemovedAttachments([
                                ...removedAttachments,
                                (attachment as DocumentFile).id,
                              ]);
                            }
                            setAttachments(
                              attachments.filter((_e, i) => i !== index)
                            );
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
              <Col xs={12} md={8} lg={4} className="mb-2">
                <Form.Group controlId="formFile" className="mb-3">
                  <Form.Control
                    value={singleAttachment == null ? "" : undefined}
                    type="file"
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      if (e.target.files && e.target.files.length > 0) {
                        setSingleAttachment(e.target.files[0]);
                      }
                    }}
                  />
                </Form.Group>
              </Col>
              <Col xs={12} md={4} lg={2} className="mb-2">
                <Button
                  className="secondaryButton w-100"
                  onClick={() => {
                    if (singleAttachment) {
                      setAttachments([...attachments, singleAttachment]);
                      setAddedAttachments([
                        ...addedAttachments,
                        singleAttachment,
                      ]);
                      setSingleAttachment(null);
                    }
                  }}
                  disabled={singleAttachment === null}
                >
                  Upload
                </Button>
              </Col>
            </Row>

            <Row className="mt-5 justify-content-center">
              <Col
                xs={12}
                md={12}
                lg={6}
                className="d-flex flex-column justify-content-center align-items-center"
              >
                <Button type="submit" className="primaryButton">
                  Save
                </Button>
              </Col>
            </Row>
          </Form>
        </div>
      )}
      {loading && (
        <>
          <Row className="d-flex flex-row p-0 mb-5 align-items-center">
            <Col>
              <h3>Edit professional</h3>
            </Col>
            <Col className="d-flex justify-content-end">
              <Button
                className="d-flex align-items-center secondaryButton"
                onClick={() => navigate(-1)}
              >
                <BsXLg size={"1.5em"} />
              </Button>
            </Col>
          </Row>

          <Row className="justify-content-center">
            <Col xs={12} md={6} lg={6} className="mb-4">
              <div className="loading-detail-page"></div>
            </Col>
          </Row>
        </>
      )}
    </div>
  );
}

export default EditProfessionalPage;
