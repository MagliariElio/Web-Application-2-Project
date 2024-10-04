import {
  Container,
  Row,
  Col,
  Form,
  FormControl,
  FormLabel,
  Button,
  Alert,
  Modal,
  Toast,
  ToastContainer
} from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";
import { toTitleCase } from "../utils/costants";
import { useRef, useState } from "react";
import { changeUserPassword, updateUserName } from "../apis/KeycloakRequests";
import { useLocation } from "react-router-dom";
import { BsPencilSquare } from "react-icons/bs";

const SettingsPage: React.FC<{ me: MeInterface }> = ({ me }) => {
  const [name, setName] = useState<string>(me.name);
  const [surname, setSurname] = useState<string>(me.surname);
  const [editName, setEditName] = useState<boolean>(false);
  const [editSurname, setEditSurname] = useState<boolean>(false);

  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState("");

  const location = useLocation();
  const [success, setSuccess] = useState<boolean>(false);
  const [showAlert, setShowAlert] = useState(false);

  const handleChangePassword = () => {
    setShowPasswordModal(true); // Open the modal when "Change Password" is clicked
  };

  const handleClosePasswordModal = () => {
    setShowPasswordModal(false); // Close the modal
    setNewPassword(""); // Reset the password fields
    setConfirmPassword("");
    setPasswordError(""); // Reset error message
  };

  const handleSavePassword = () => {
    if (newPassword !== confirmPassword) {
      setPasswordError("Oops! The passwords don’t match. Make sure they are the same.");
      return;
    }
    // Logic for saving the new password
    try {
      changeUserPassword(me.principal.attributes.sub, newPassword);
      setSuccess(true);
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    } catch (e) {
      setErrorMessage("Error updating user password");
      setSuccess(false);
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }
    setShowPasswordModal(false); // Close the modal on success
    setNewPassword(""); // Reset fields
    setConfirmPassword("");
    setPasswordError(""); // Reset error message
  };

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const errorRef = useRef<HTMLDivElement | null>(null);

  const handleInputNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setName(e.target.value);
  };

  const handleInputSurnameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSurname(e.target.value);
  };

  const handleSaveNameClicked = () => {
    updateUserName(me.principal.attributes.sub, name, surname)
      .then(() => {
        console.log("User name updated");
        setSuccess(true);
        setShowAlert(true);
        setTimeout(() => {
          setShowAlert(false);
        }, 3000);
      })
      .catch((error) => {
        console.log("Error during user update: ", error);
        setErrorMessage("Error updating user data");
        setSuccess(false);
        setShowAlert(true);
        setTimeout(() => {
          setShowAlert(false);
        }, 3000);
      });
    setEditName(false);
    setEditSurname(false);
  };

  return (
    <Container fluid className="profile-container mt-4">
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => (location.state = null)}>
            <Toast.Header>
              <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}
      {errorMessage && (
        <Row className="justify-content-center" ref={errorRef}>
          <Col xs={12} md={10} lg={6}>
            <Alert variant="danger" onClose={() => setErrorMessage("")} className="d-flex mt-3 justify-content-center align-items-center" dismissible>
              {errorMessage}
            </Alert>
          </Col>
        </Row>
      )}

      <Row className="mb-4">
        <Col>
          <h3 className="title">Settings</h3>
        </Col>
      </Row>
      <Row  className="d-flex justify-content-center">
        <Col xs={12} lg={8} xl={5}>
          <Form>
            {!editName && (
              <Form.Group as={Row} className="mb-3" controlId="inputName">
                <FormLabel column sm={4}>
                  Name:
                </FormLabel>

                <Col sm={8}>
                  <div className="d-flex align-items-center">
                    <FormControl type="text" value={name || ""} disabled />
                    <Button
                      size="sm"
                      className="ms-2 d-flex align-items-center secondaryButton"
                      onClick={() => {
                        setEditName(true);
                      }}
                    >
                      <BsPencilSquare size={"1em"} className="me-2" />
                      Edit
                    </Button>
                  </div>
                </Col>
              </Form.Group>
            )}

            {editName && (
              <Form.Group as={Row} className="mb-3" controlId="inputName">
                <FormLabel column sm={4}>
                  Name:
                </FormLabel>

                <Col sm={8}>
                  <div className="d-flex align-items-center">
                    <FormControl type="text" value={name || ""} onChange={handleInputNameChange} />
                    <Button variant="success" size="sm" className="ms-2" onClick={handleSaveNameClicked}>
                      Save
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      className="ms-2"
                      onClick={() => {
                        setName(me.name);
                        setEditName(false);
                      }}
                    >
                      Cancel
                    </Button>
                  </div>
                </Col>
              </Form.Group>
            )}

            {!editSurname && (
              <Form.Group as={Row} className="mb-3" controlId="inputName">
                <FormLabel column sm={4}>
                  Surname:
                </FormLabel>

                <Col sm={8}>
                  <div className="d-flex align-items-center">
                    <FormControl type="text" value={surname || ""} disabled />
                    <Button
                      size="sm"
                      className="ms-2 d-flex align-items-center secondaryButton"
                      onClick={() => {
                        setEditSurname(true);
                      }}
                    >
                      <BsPencilSquare size={"1em"} className="me-2" />
                      Edit
                    </Button>
                  </div>
                </Col>
              </Form.Group>
            )}

            {editSurname && (
              <Form.Group as={Row} className="mb-3" controlId="inputName">
                <FormLabel column sm={4}>
                  Surname:
                </FormLabel>

                <Col sm={8}>
                  <div className="d-flex align-items-center">
                    <FormControl type="text" value={surname || ""} onChange={handleInputSurnameChange} />
                    <Button variant="success" size="sm" className="ms-2" onClick={handleSaveNameClicked}>
                      Save
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      className="ms-2"
                      onClick={() => {
                        setSurname(me.surname);
                        setEditSurname(false);
                      }}
                    >
                      Cancel
                    </Button>
                  </div>
                </Col>
              </Form.Group>
            )}

            {me?.role && (
              <Form.Group as={Row} className="mb-3" controlId="inputRole">
                <FormLabel column sm={4}>
                  Role:
                </FormLabel>
                <Col sm={8}>
                  <FormControl type="text" value={toTitleCase(me.role)} disabled />
                </Col>
              </Form.Group>
            )}

            <Form.Group as={Row} className="mb-3" controlId="changePasswordButton">
              <Col xs={12} className="d-flex justify-content-center justify-content-md-end">
                <Button onClick={handleChangePassword} className="primaryButton">
                  <BsPencilSquare size={"1em"} className="me-2" />
                  Change Password
                </Button>
              </Col>
            </Form.Group>
          </Form>
        </Col>
      </Row>

      {/* Change Password Modal */}
      <Modal show={showPasswordModal} onHide={handleClosePasswordModal}>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Change Password</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group as={Row} className="mb-3" controlId="newPassword">
              <FormLabel column sm={4}>
                New Password:
              </FormLabel>
              <Col sm={8}>
                <FormControl type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="Enter new password" />
              </Col>
            </Form.Group>

            <Form.Group as={Row} className="mb-3" controlId="confirmPassword">
              <FormLabel column sm={4}>
                Confirm Password:
              </FormLabel>
              <Col sm={8}>
                <FormControl
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                />
              </Col>
            </Form.Group>

            {passwordError && (
              <Row className="mb-3">
                <Col>
                  <p style={{ color: "red" }}>{passwordError}</p>
                </Col>
              </Row>
            )}
          </Form>
        </Modal.Body>
        <Modal.Footer className="justify-content-between">
          <Button variant="primary" className="ms-5" onClick={handleClosePasswordModal}>
            Cancel
          </Button>
          <Button variant="success" className="me-5" onClick={handleSavePassword}>
            Save
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default SettingsPage;
