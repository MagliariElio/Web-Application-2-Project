import { useRef, useState } from "react";
import { Alert, Button, Col, Row } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { checkValidEmail, checkValidTelephone } from "../utils/checkers";
import { MeInterface } from "../interfaces/MeInterface";
import { createCustomer } from "../apis/CustomerRequests";
import { ContactModal } from "./AddProfessionalPage";

function AddCustomerPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [ssnCode, setSsnCode] = useState("");
  const [comment, setComment] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);
  
  const [contactModalOpen, setContactModalOpen] = useState<string | null>(null);

  const [emails, setEmails] = useState<any[]>([]);

  const [telephones, setTelephones] = useState<any[]>([]);

  const [addresses, setAddresses] = useState<any[]>([]);

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

    const customer = {
      name: name,
      surname: surname,
      ssnCode: ssnCode,
      comment: comment,
      category: "CUSTOMER",
      emails: emails,
      telephones: telephones,
      addresses: addresses,
    };

    createCustomer(customer, me)
      .then((res) => {
        navigate("/ui/customers", { state: { success: true } });
      })
      .catch((error) => {
        navigate("/ui/customers", { state: { success: false } });
        console.log("Error during customer post: ", error);
        throw new Error("POST /API/customers : Network response was not ok");
      });
  };

  return (
    <div className="add-job-offer-container">
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
          <h3>Add New Customer</h3>
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
        <Row className="mt-5 justify-content-center">
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
              <p>No emails added yet</p>
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
              <p>No phone numbers added yet</p>
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
              <p>No addresses added yet</p>
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

export default AddCustomerPage;
