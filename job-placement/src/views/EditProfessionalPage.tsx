import { useEffect, useRef, useState } from "react";
import { Alert, Button, Col, Row } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsX, BsXLg } from "react-icons/bs";
import { useNavigate, useParams } from "react-router-dom";
import { checkValidEmail, checkValidTelephone } from "../utils/checkers";
import { MeInterface } from "../interfaces/MeInterface";
import { fetchProfessional, updateProfessional } from "../apis/ProfessionalRequests";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";
import { LoadingSection } from "../App";

function EditProfessionalPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const { id } = useParams();
  const [contactID, setContactID] = useState<number>(0);
  const [employmentState, setEmploymentState] = useState("");

  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [ssnCode, setSsnCode] = useState("");
  const [comment, setComment] = useState("");

  const [emails, setEmails] = useState<any[]>([]);
  const [singleEmailAddress, setSingleEmailAddress] = useState("");
  const [singleEmailAddressComment, setSingleEmailAddressComment] = useState("");
  const [emailError, setEmailError] = useState(false);

  const [telephones, setTelephones] = useState<any[]>([]);
  const [singleTelephoneNumber, setSingleTelephoneNumber] = useState("");
  const [singleTelephoneNumberComment, setSingleTelephoneNumberComment] = useState("");
  const [telephoneError, setTelephoneError] = useState(false);

  const [addresses, setAddresses] = useState<any[]>([]);
  const [singleAddress, setSingleAddress] = useState({
    address: "",
    city: "",
    region: "",
    state: "",
    comment: "",
  });
  const [addressError, setAddressError] = useState(false);

  const [skills, setSkills] = useState<string[]>([]);
  const [singleSkill, setSingleSkill] = useState("");
  const [geographicalLocation, setGeographicalLocation] = useState("");
  const [dailyRate, setDailyRate] = useState("");

  useEffect(() => {
    if (id === undefined || id === null || id === "" || Number.parseInt(id) < 1) {
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
        setEmails([]); //TODO: set emails
        setTelephones([]); //TODO: set telephones
        setAddresses([]); //TODO: set addresses
      })
      .catch((error) => {
        navigate("/not-found");
        throw new Error(`GET /API/professional/${id} : Network response was not ok`);
      });

    setLoading(false);
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

    if (addresses.length === 0) {
      setErrorMessage("You must add at least one address before saving.");

      if (errorRef.current) {
        errorRef.current.scrollIntoView({ behavior: "smooth" });
      }
      return;
    }

    if (emails.length === 0 && telephones.length === 0) {
      setErrorMessage("You must add at least one email or phone number before saving.");

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
        //emails: emails,
        //telephones: telephones,
        //addresses: addresses
      },
      skills: skills,
      employmentState: employmentState,
      geographicalLocation: geographicalLocation,
      dailyRate: dailyRate,
    };

    updateProfessional(professional, me)
      .then((res) => {
        navigate("/ui/professionals", { state: { success: true } });
      })
      .catch((error) => {
        navigate("/ui/professionals", { state: { success: false } });
        console.log("Error during professional post: ", error);
        throw new Error("POST /API/professionals : Network response was not ok");
      });
  };

  return (
    <div className="add-job-offer-container">
      {!loading && (
        <div>
          <Row className="d-flex flex-row p-0 mb-5 align-items-center">
            <Col>
              <h3>Edit professional</h3>
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
                      <Row>
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
                            setEmails(emails.filter((e, i) => i !== index));
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
              <Col xs={12} md={12} lg={2} className="mb-2">
                <Form.Control
                  placeholder="Email address"
                  value={singleEmailAddress}
                  onChange={(e) => {
                    setSingleEmailAddress(e.target.value);
                    setEmailError(false);
                  }}
                />
              </Col>
              <Col xs={12} md={12} lg={3} className="mb-2">
                <Form.Control
                  placeholder="Email address comment"
                  value={singleEmailAddressComment}
                  onChange={(e) => setSingleEmailAddressComment(e.target.value)}
                />
              </Col>
              <Col xs={12} md={12} lg={1} className="mb-2">
                <Button
                  className="secondaryButton w-100"
                  onClick={() => {
                    if (checkValidEmail(singleEmailAddress)) {
                      setEmails([
                        ...emails,
                        {
                          email: singleEmailAddress,
                          comment: singleEmailAddressComment,
                        },
                      ]);
                      setSingleEmailAddress("");
                      setSingleEmailAddressComment("");
                      setEmailError(false);
                    } else {
                      setEmailError(true);
                    }
                  }}
                >
                  Add email
                </Button>
              </Col>
            </Row>
            {emailError && (
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6}>
                  <p className="text-danger">Invalid email address</p>
                </Col>
              </Row>
            )}

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
                  <Row key={index} className="mb-1 d-flex align-items-center justify-content-center">
                    <Col xs={8} md={6} lg={5}>
                      <Row>
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
                            setTelephones(telephones.filter((e, i) => i !== index));
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
              <Col xs={12} md={12} lg={2} className="mb-2">
                <Form.Control
                  placeholder="Telephone number"
                  value={singleTelephoneNumber}
                  onChange={(e) => {
                    setSingleTelephoneNumber(e.target.value);
                    setTelephoneError(false);
                  }}
                />
              </Col>
              <Col xs={12} md={12} lg={3} className="mb-2">
                <Form.Control
                  placeholder="Telephone number comment"
                  value={singleTelephoneNumberComment}
                  onChange={(e) => setSingleTelephoneNumberComment(e.target.value)}
                />
              </Col>
              <Col xs={12} md={12} lg={1} className="mb-2">
                <Button
                  className="secondaryButton w-100 px-0"
                  onClick={() => {
                    if (checkValidTelephone(singleTelephoneNumber)) {
                      setTelephones([
                        ...telephones,
                        {
                          telephone: singleTelephoneNumber,
                          comment: singleTelephoneNumberComment,
                        },
                      ]);
                      setSingleTelephoneNumber("");
                      setSingleTelephoneNumberComment("");
                      setTelephoneError(false);
                    } else {
                      setTelephoneError(true);
                    }
                  }}
                >
                  Add number
                </Button>
              </Col>
            </Row>
            {telephoneError && (
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6}>
                  <p className="text-danger">Invalid telephone number</p>
                </Col>
              </Row>
            )}

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
                  <Row key={index} className="mb-1 d-flex align-items-center justify-content-center">
                    <Col xs={8} md={6} lg={5}>
                      <Row>
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
                            setAddresses(addresses.filter((e, i) => i !== index));
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
              <Col xs={12} md={6} lg={3} className="mb-2">
                <Form.Control
                  placeholder="Address"
                  value={singleAddress.address}
                  onChange={(e) =>
                    setSingleAddress({
                      ...singleAddress,
                      address: e.target.value,
                    })
                  }
                />
              </Col>
              <Col xs={12} md={6} lg={3} className="mb-2">
                <Form.Control
                  placeholder="City"
                  value={singleAddress.city}
                  onChange={(e) => setSingleAddress({ ...singleAddress, city: e.target.value })}
                />
              </Col>
            </Row>
            <Row className="justify-content-center">
              <Col xs={12} md={6} lg={3} className="mb-2">
                <Form.Control
                  placeholder="Region"
                  value={singleAddress.region}
                  onChange={(e) =>
                    setSingleAddress({
                      ...singleAddress,
                      region: e.target.value,
                    })
                  }
                />
              </Col>
              <Col xs={12} md={6} lg={3} className="mb-2">
                <Form.Control
                  placeholder="State"
                  value={singleAddress.state}
                  onChange={(e) =>
                    setSingleAddress({
                      ...singleAddress,
                      state: e.target.value,
                    })
                  }
                />
              </Col>
            </Row>
            <Row className="justify-content-center">
              <Col xs={12} md={12} lg={6} className="mb-2">
                <Form.Control
                  as="textarea"
                  placeholder="Address comment"
                  rows={3}
                  value={singleAddress.comment}
                  onChange={(e) =>
                    setSingleAddress({
                      ...singleAddress,
                      comment: e.target.value,
                    })
                  }
                />
              </Col>
            </Row>
            {addressError && (
              <Row className="justify-content-center">
                <Col xs={12} md={12} lg={6} className="mb-2">
                  <p className="text-danger">Address, city, region and state are required</p>
                </Col>
              </Row>
            )}
            <Row className="justify-content-center">
              <Col xs={12} md={12} lg={6} className="mb-2 d-flex justify-content-center">
                <Button
                  className="secondaryButton"
                  onClick={() => {
                    if (singleAddress.address === "" || singleAddress.city === "" || singleAddress.region === "" || singleAddress.state === "") {
                      setAddressError(true);
                      return;
                    }
                    setAddresses([...addresses, singleAddress]);
                    setSingleAddress({
                      address: "",
                      city: "",
                      region: "",
                      state: "",
                      comment: "",
                    });
                    setAddressError(false);
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
                              setSkills(skills.filter((e, i) => i !== index));
                            }}
                          />
                        </div>
                      ))}
                  </Row>
                </Col>
              </Row>
            }

            <Row className="justify-content-center">
              <Col xs={12} md={6} lg={5} className="mb-2">
                <Form.Control placeholder="New skill" value={singleSkill} onChange={(e) => setSingleSkill(e.target.value)} />
              </Col>
              <Col xs={12} md={6} lg={1} className="mb-2">
                <Button
                  className="secondaryButton w-100"
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
                >
                  Add skill
                </Button>
              </Col>
            </Row>

            <Row className="mt-5 justify-content-center">
              <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
                <Button type="submit" className="primaryButton">
                  Save
                </Button>
              </Col>
            </Row>
          </Form>
        </div>
      )}
      {loading && <LoadingSection h={null} />}
    </div>
  );
}

export default EditProfessionalPage;
