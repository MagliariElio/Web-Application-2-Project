import { useEffect, useState } from "react";
import { Button, Col, Modal, Row } from "react-bootstrap";
import Form from 'react-bootstrap/Form';
import { BsPencilSquare, BsTrash, BsX, BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { checkValidEmail, checkValidTelephone } from "../utils/checkers";
import { MeInterface } from "../interfaces/MeInterface";
import { createProfessional } from "../apis/ProfessionalRequests";
import { Professional } from "../interfaces/Professional";
import { deleteEmail, fetchAllContactWhatContact } from "../apis/ContactRequests";
import { Email } from "../interfaces/Email";
import { Telephone } from "../interfaces/Telephone";
import { Address } from "../interfaces/Address";

function AddProfessionalPage({ me }: { me: MeInterface }) {

    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [surname, setSurname] = useState('');
    const [ssnCode, setSsnCode] = useState('');
    const [comment, setComment] = useState('');

    const [contactModalOpen, setContactModalOpen] = useState<string | null>(null);

    const [emails, setEmails] = useState<any[]>([]);
    const [singleEmailAddress, setSingleEmailAddress] = useState('');
    const [singleEmailAddressComment, setSingleEmailAddressComment] = useState('');
    const [removedEmails, setRemovedEmails] = useState<number[]>([]);
    const [emailError, setEmailError] = useState(false);

    const [telephones, setTelephones] = useState<any[]>([]);
    const [singleTelephoneNumber, setSingleTelephoneNumber] = useState('');
    const [singleTelephoneNumberComment, setSingleTelephoneNumberComment] = useState('');
    const [telephoneError, setTelephoneError] = useState(false);

    const [addresses, setAddresses] = useState<any[]>([]);
    const [singleAddress, setSingleAddress] = useState({
        address: '',
        city: '',
        region: '',
        state: '',
        comment: ''
    });
    const [addressError, setAddressError] = useState(false);

    const [skills, setSkills] = useState<string[]>([]);
    const [singleSkill, setSingleSkill] = useState('');
    const [geographicalLocation, setGeographicalLocation] = useState('');
    const [dailyRate, setDailyRate] = useState(0);

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        
        if(name === '' || surname === '' || ssnCode === '') {
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
                addresses: addresses
            },
            skills: skills,
            geographicalLocation: geographicalLocation,
            dailyRate: dailyRate,
            }
        

        createProfessional(professional, me)
        .then(res => {
            navigate("/ui/professionals", { state: { success: true } });
        })
        .catch((error) => {
            navigate("/ui/professionals", { state: { success: false } });
            console.log("Error during professional post: ", error);
            throw new Error('POST /API/professionals : Network response was not ok');
        }
        );


    };

  return (
    <div>

      {contactModalOpen != null && <ContactModal open={contactModalOpen} setOpen={setContactModalOpen} />}

        <Row className="d-flex flex-row p-0 mb-5 align-items-center">
            <Col>
                <h3>Add new professional</h3> 
            </Col>
            <Col className="d-flex justify-content-end">
                <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate(-1) } >
                    <BsXLg size={"1.5em"} />
                </Button>
            </Col>
        </Row>

        <Form onSubmit={handleSubmit}>
            <Row>
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
            <Row>
                <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Control
                    placeholder="Ssn code"
                    value={ssnCode}
                    required
                    onChange={(e) => setSsnCode(e.target.value)}
                />
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={12} lg={6} className="mb-4">
                    <Form.Control
                        as="textarea"
                        placeholder="Comments"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        maxLength={255}
                    />
                </Col>
            </Row>

            <Row>
                <Col xs={12} md={12} lg={6} className="mb-4">
                    <Form.Control
                        placeholder="Geographical location"
                        required
                        value={geographicalLocation}
                        onChange={(e) => setGeographicalLocation(e.target.value)}
                    />
                </Col>
            </Row>

            <Row>
                <Col xs={12} md={6} lg={3} className="mb-4">
                    <Form.Label>Daily rate</Form.Label>
                    <Form.Control
                        type="number"
                        step="0.01"
                        placeholder="Daily rate"
                        value={dailyRate}
                        onChange={(e) => setDailyRate(parseFloat(e.target.value))}
                        min={0}
                    />
                </Col>
            </Row>

            <Row className="mt-5">
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
            {
                emails.length === 0 && 
                    <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                                            <p>No emails added yet</p>
                                        </Col>
                                        </Row>
            }
            {
                emails.length > 0 && emails.map((email, index) => {
                    return (
                    <Row key={index} className="mb-1 d-flex align-items-center">
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
                                <Button className="secondaryDangerButton w-100" onClick={() => {
                                    if(email.id !== undefined) {
                                        setRemovedEmails([...removedEmails, email.id]);
                                    }
                                    setEmails(emails.filter((e, i) => i !== index));
                                }}>
                                    Remove
                                </Button>
                            </Col>
                        </Col>                                    
                    </Row>)
                })
            }
            <Row>
                
                <Col xs={12} md={12} lg={6} className="mb-2">
                    <Button className="secondaryButton w-100" onClick={() => {
                        setContactModalOpen('email');
                    }}>
                        Add email
                    </Button>
                </Col>
            </Row>
            {
                emailError && <Row>
                                <Col xs={12} md={12} lg={6} className="mb-4">
                                    <p className="text-danger">Invalid email address</p>
                                </Col>
                            </Row>
            }

            <Row className="mt-5">
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
            {
                telephones.length === 0 && 
                    <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                                            <p>No phone numbers added yet</p>
                                        </Col>
                                        </Row>
            }
            {
                telephones.length > 0 && telephones.map((telephone, index) => {
                    return (
                    <Row key={index} className="mb-1 d-flex align-items-center">
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
                                <Button className="secondaryDangerButton w-100" onClick={() => {
                                    setTelephones(telephones.filter((e, i) => i !== index));
                                }}>
                                    Remove
                                </Button>
                            </Col>
                        </Col>                                    
                    </Row>)
                })
            }
            <Row>
                <Col xs={12} md={12} lg={2} className="mb-2">
                    <Form.Control
                        placeholder="Telephone number"
                        value={singleTelephoneNumber}
                        onChange={(e) => {setSingleTelephoneNumber(e.target.value); setTelephoneError(false);}}
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
                    <Button className="secondaryButton w-100 px-0" onClick={() => {
                        if(checkValidTelephone(singleTelephoneNumber)) {
                            setTelephones([...telephones, {telephone: singleTelephoneNumber, comment: singleTelephoneNumberComment}]);
                            setSingleTelephoneNumber('');
                            setSingleTelephoneNumberComment('');
                            setTelephoneError(false);
                        }
                        else {
                            setTelephoneError(true);
                        }
                    }}>
                        Add number
                    </Button>
                </Col>
            </Row>
            {
                telephoneError && <Row>
                                <Col xs={12} md={12} lg={6} className="mb-4">
                                    <p className="text-danger">Invalid telephone number</p>
                                </Col>
                            </Row>
            }

<Row className="mt-5">
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
          <Row>
            <Col xs={12} md={12} lg={6} className="mb-0">
              <p>No addresses added yet</p>
            </Col>
          </Row>
        )}
        {addresses.length > 0 &&
          addresses.map((address, index) => {
            return (
              <Row key={index} className="mb-1 d-flex align-items-center">
                <Col xs={8} md={6} lg={5}>
                  <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                      <p className="text-truncate">
                        {`${address.address}, ${address.city}, ${address.region}, ${address.state}`}
                      </p>
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
        <Row>
          <Col xs={12} md={6} lg={3} className="mb-2">
            <Form.Control
              placeholder="Address"
              value={singleAddress.address}
              onChange={(e) =>
                setSingleAddress({ ...singleAddress, address: e.target.value })
              }
            />
          </Col>
          <Col xs={12} md={6} lg={3} className="mb-2">
            <Form.Control
              placeholder="City"
              value={singleAddress.city}
              onChange={(e) =>
                setSingleAddress({ ...singleAddress, city: e.target.value })
              }
            />
          </Col>
        </Row>
        <Row>
          <Col xs={12} md={6} lg={3} className="mb-2">
            <Form.Control
              placeholder="Region"
              value={singleAddress.region}
              onChange={(e) =>
                setSingleAddress({ ...singleAddress, region: e.target.value })
              }
            />
          </Col>
          <Col xs={12} md={6} lg={3} className="mb-2">
            <Form.Control
              placeholder="State"
              value={singleAddress.state}
              onChange={(e) =>
                setSingleAddress({ ...singleAddress, state: e.target.value })
              }
            />
          </Col>
        </Row>
        <Row>
          <Col xs={12} md={12} lg={6} className="mb-2">
            <Form.Control
              as="textarea"
              placeholder="Address comment"
              value={singleAddress.comment}
              onChange={(e) =>
                setSingleAddress({ ...singleAddress, comment: e.target.value })
              }
            />
          </Col>
        </Row>
        {addressError && (
          <Row>
            <Col xs={12} md={12} lg={6} className="mb-4">
              <p className="text-danger">
                Address, city, region and state are required
              </p>
            </Col>
          </Row>
        )}
        <Row>
          <Col
            xs={12}
            md={12}
            lg={6}
            className="mb-2 d-flex justify-content-center"
          >
            <Button
              className="secondaryButton"
              onClick={() => {
                if (
                  singleAddress.address === "" ||
                  singleAddress.city === "" ||
                  singleAddress.region === "" ||
                  singleAddress.state === ""
                ) {
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

            <Row className="mt-5">
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

            {
                skills.length === 0 && 
                    <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                                            <p>No skills added yet</p>
                                        </Col>
                                        </Row>
            }
            {
                <Row>
                  <Col xs={12} md={12} lg={6} className="mb-2">
                    <Row className="d-flex flex-wrap ps-2">
                      {skills.length > 0 && skills.map((skill, index) => (
                        <div key={index} style={{width: "auto"}} className="text-truncate me-2 tag mb-1">
                            {skill}

                            <BsX size={20} className="ms-2" style={{cursor: "pointer"}} onClick={() => {
                                setSkills(skills.filter((e, i) => i !== index));
                            }} />
                            
                            </div>
                      ))}
                    </Row>
                  </Col>
                </Row>
            }

            <Row>
                <Col xs={12} md={6} lg={5} className="mb-2">
                    <Form.Control
                        placeholder="New skill"
                        value={singleSkill}
                        onChange={(e) => setSingleSkill(e.target.value)}
                    />
                </Col>
                <Col xs={12} md={6} lg={1} className="mb-2">
                    <Button className="secondaryButton w-100" onClick={() => {
                        setSkills([...skills, singleSkill]);
                        setSingleSkill('');
                    }}>
                        Add skill
                    </Button>
                </Col>
            </Row>

            <Row className="mt-5">
              <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
                <Button type="submit" className="primaryButton">
                  Save professional
                </Button>
              </Col>
            </Row>
            
        </Form>

    </div>
  );
}

export default AddProfessionalPage;


const loadContactContacts = async (whatContact: string): Promise<Email[] | Telephone[] | Address[]> => {
  if (whatContact === 'email') {
    try {
      const allEmails = await fetchAllContactWhatContact("emails");
      console.log("All emails: ", allEmails);
      return allEmails;
    } catch (err) {
      console.log("Error fetching emails: ", err);
      return [] as Email[] | Telephone[] | Address[];
    }
  }

  return [] as Email[] | Telephone[] | Address[];
}

const ContactModal = ({open, setOpen}: {open: string | null, setOpen: any}) => {

  const [contacts, setContacts] = useState<Email[] | Telephone[] | Address[]>([]);

  useEffect(() => {
    const fetchContacts = async () => {
      if (open !== null) {
        const cont = await loadContactContacts(open);
        console.log("Contactssssssssssssssssssssss: ", cont);
        setContacts(cont);
      }
    };

    fetchContacts();
  }, [open]);


  console.log("Contacts: ", contacts);

    return (
        <Modal size="lg" show={open != null} onHide={() => setOpen(null)}>
                <Modal.Header closeButton>
                  <Modal.Title >
                    Add a new {open}
                  </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                  <small>Click on a {open} to select it or use side buttons to edit and delete them from the {open} book</small>

                  {
                    contacts.length === 0 &&
                    <Row className="mt-2">
                      <Col xs={12} md={12} lg={6} className="mb-0">
                        <p>No {open}s added yet</p>
                      </Col>
                    </Row>
                  }

                  {
                    contacts.length > 0 && contacts.map((contact, index) => {
                      return (
                        <Row key={index} className="mb-1 mt-2 d-flex align-items-center">
                          <Col xs={12} md={8} lg={10}>
                            <Row>
                              <Col xs={12} md={12} lg={6} className="mb-0">
                                <p className="text-truncate">{open === 'email' ? (contact as Email).email : (contact as Telephone).telephone}</p>
                              </Col>
                              <Col xs={12} md={12} lg={6} className="mb-0 fs-10 fw-light">
                                <p className="text-truncate">{open === 'email' ? (contact as Email).comment : (contact as Telephone).comment}</p>
                              </Col>
                            </Row>
                          </Col>
                          <Col xs={6} md={2} lg={1}>
                            <Col className="mb-0">
                              <Button className="secondaryButton w-100 d-flex justify-content-center align-items-center" onClick={() => {
                                // setContacts(contacts.filter((e, i) => i !== index));
                              }}>
                                <BsPencilSquare size={20} />
                              </Button>
                            </Col>
                          </Col>
                          <Col xs={6} md={2} lg={1}>
                            <Col className="mb-0">
                              <Button className="secondaryDangerButton w-100 d-flex justify-content-center align-items-center" onClick={() => {
                                // setContacts(contacts.filter((e, i) => i !== index));
                              }}>
                                <BsTrash size={20} />
                              </Button>
                            </Col>
                          </Col>
                        </Row>
                      )
                    })
                  }

                <Form>
                    <Row>
                        <Col xs={12} md={12} lg={6} className="mb-2">
                            <Form.Control
                                placeholder={open === 'email' ? "Email address" : "Telephone number"}
                            />
                        </Col>
                        <Col xs={12} md={12} lg={3} className="mb-2">
                            <Form.Control
                                placeholder={open === 'email' ? "Email address comment" : "Telephone number comment"}
                            />
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
                            <Button className="primaryButton">
                                Save {open}
                            </Button>
                        </Col>
                    </Row>
                </Form>
                </Modal.Body>

        </Modal>
            
    );
}



/*

<Col xs={12} md={12} lg={2} className="mb-2">
                    <Form.Control
                        placeholder="Email address"
                        value={singleEmailAddress}
                        onChange={(e) => {setSingleEmailAddress(e.target.value); setEmailError(false);}}
                    />
                </Col>
                <Col xs={12} md={12} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="Email address comment"
                        value={singleEmailAddressComment}
                        onChange={(e) => setSingleEmailAddressComment(e.target.value)}
                    />
                </Col>







                if(checkValidEmail(singleEmailAddress)) {
                            setEmails([...emails, {email: singleEmailAddress, comment: singleEmailAddressComment}]);
                            setSingleEmailAddress('');
                            setSingleEmailAddressComment('');
                            setEmailError(false);
                        }
                        else {
                            setEmailError(true);
                        }




                */