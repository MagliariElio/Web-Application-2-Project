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
  ToastContainer,
} from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";
import { toTitleCase } from "../utils/costants";
import { useState } from "react";
import { changeUserPassword, updateUserName } from "../apis/KeycloakRequests";
import { BsPencilSquare } from "react-icons/bs";

const SettingsPage: React.FC<{ me: MeInterface, setMe: React.Dispatch<React.SetStateAction<MeInterface | null>> }> = ({ me, setMe }) => {
  const [name, setName] = useState<string>(me.name);
  const [surname, setSurname] = useState<string>(me.surname);
  const [editName, setEditName] = useState<boolean>(false);
  const [editSurname, setEditSurname] = useState<boolean>(false);

  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const [nameError, setNameError] = useState<string | null>(null);
  const [surnameError, setSurnameError] = useState<string | null>(null);

  const [success, setSuccess] = useState<boolean>(false);
  const [showAlert, setShowAlert] = useState(false);

  const handleChangePassword = () => {
    setShowPasswordModal(true); // Open the modal when "Change Password" is clicked
  };

  const handleClosePasswordModal = () => {
    setShowPasswordModal(false); // Close the modal
    setNewPassword(""); // Reset the password fields
    setConfirmPassword("");
    setPasswordError(null); // Reset error message
  };

  const handleSavePassword = () => {
    // Reset error state before validation
    setPasswordError(null);

    if (!newPassword.trim() || !confirmPassword.trim()) {
      setPasswordError("Both fields must be filled.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError("Passwords do not match.");
      return;
    }

    // Save password logic here
    try {
      changeUserPassword(me.principal.attributes.sub, newPassword);
      setSuccess(true);
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
      handleClosePasswordModal(); // Close modal on success
    } catch (e) {
      setPasswordError("Error updating password.");
    }
  };

  const handleSaveNameClicked = () => {
    setNameError(null);
    setSurnameError(null);

    if (!name.trim()) {
      setNameError("Name cannot be empty.");
      return;
    }

    if (!surname.trim()) {
      setSurnameError("Surname cannot be empty.");
      return;
    }

    updateUserName(me.principal.attributes.sub, name.trim(), surname.trim())
      .then(() => {
        setSuccess(true);
        setShowAlert(true);
        setTimeout(() => {
          setShowAlert(false);
        }, 3000);
        setEditName(false);
        setEditSurname(false);
      })
      .catch(() => {
        setNameError("Error updating user data.");
      });
  };

  return (
    <Container fluid className="profile-container mt-4">
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null}>
            <Toast.Header>
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation successfully executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="mb-4">
        <Col>
          <h3 className="title">Settings</h3>
        </Col>
      </Row>

      <Row className="d-flex justify-content-center">
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
                    <FormControl
                      type="text"
                      value={name || ""}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="Enter name"
                    />
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
                  {nameError && <p style={{ color: "red" }}>{nameError}</p>}
                </Col>
              </Form.Group>
            )}

            {!editSurname && (
              <Form.Group as={Row} className="mb-3" controlId="inputSurname">
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
              <Form.Group as={Row} className="mb-3" controlId="inputSurname">
                <FormLabel column sm={4}>
                  Surname:
                </FormLabel>

                <Col sm={8}>
                  <div className="d-flex align-items-center">
                    <FormControl
                      type="text"
                      value={surname || ""}
                      onChange={(e) => setSurname(e.target.value)}
                      placeholder="Enter surname"
                    />
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
                  {surnameError && <p style={{ color: "red" }}>{surnameError}</p>}
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
                <FormControl
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                />
                {passwordError && newPassword.trim() === "" && <p style={{ color: "red" }}>{passwordError}</p>}
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
                {passwordError && confirmPassword.trim() === "" && <p style={{ color: "red" }}>{passwordError}</p>}
              </Col>
            </Form.Group>

            {passwordError && newPassword !== confirmPassword && (
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