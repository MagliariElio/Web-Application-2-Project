import { useLocation, useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { Alert, Button, Col, Container, Row, Toast, ToastContainer, Modal, Form } from "react-bootstrap";
import { useEffect, useRef, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { RoleState } from "../utils/costants";
import { deleteUser, fetchUsers, updateUserRole } from "../apis/KeycloakRequests";  // <-- Added updateUserRole API
import { KeycloakUser } from "../interfaces/KeycloakUser";
import { FaEnvelope, FaTrashAlt, FaUserTie } from "react-icons/fa";

const UsersPage: React.FC<{ me: MeInterface }> = ({ me }) => {
  const navigate = useNavigate();
  const [showAlert, setShowAlert] = useState(false);
  const [loading, setLoading] = useState(true);
  const location = useLocation();
  var { success } = location.state || {};
  const [users, setUsers] = useState<KeycloakUser[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const errorRef = useRef<HTMLDivElement | null>(null);

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [userIdToDelete, setUserIdToDelete] = useState<string | null>(null);
  
  // Role modification state
  const [showRoleModal, setShowRoleModal] = useState(false); // For role modal visibility
  const [userIdToModifyRole, setUserIdToModifyRole] = useState<string | null>(null); // User for role modification
  const [newRole, setNewRole] = useState<string>(""); // Store selected role

  const roles = [RoleState.MANAGER, RoleState.OPERATOR, RoleState.GUEST]; // Available roles

  const handleDelete = async () => {
    if (userIdToDelete) {
      try {
        await deleteUser(userIdToDelete);
        setUsers(users.filter((user) => user.id !== userIdToDelete)); // Update user list after deletion
        setShowModal(false); // Close modal
      } catch (error) {
        console.error("Error deleting user:", error);
        setErrorMessage("Failed to delete user. Please try again.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }
    }
  };

  const openDeleteModal = (userId: string) => {
    setUserIdToDelete(userId);
    setShowModal(true);
  };

  const openRoleModal = (userId: string, currentRole: string) => {
    setUserIdToModifyRole(userId);
    setNewRole(currentRole);
    setShowRoleModal(true); // Open role modal
  };

  const handleRoleChange = async () => {
    if (userIdToModifyRole && newRole) {
      try {
        await updateUserRole(userIdToModifyRole, newRole); // Call API to update role
        setUsers(users.map((user) => user.id === userIdToModifyRole ? { ...user, role: newRole } : user)); // Update role in state
        setShowRoleModal(false);
      } catch (error) {
        console.error("Error updating user role:", error);
        setErrorMessage("Failed to update user role. Please try again.");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const fetchedUsers = await fetchUsers();
        setUsers(fetchedUsers);
        console.log("Fetched users:", fetchedUsers);
        setLoading(false);
      } catch (error) {
        console.error("Error occurred while fetching users:", error);
        setErrorMessage("Failed to fetch users, please reload the page");
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        setLoading(false);
      }
    };
    setLoading(true);
    fetchData(); // Call the async function
  }, []);

  return (
    <Container fluid>
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => (success = null)}>
            <Toast.Header>
              <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col xs={6}>
          <h3 className="title">Users</h3>
        </Col>

        {me.role === RoleState.MANAGER && (
          <Col xs={6} className="d-flex justify-content-end">
            <Button className="d-flex align-items-center primaryButton" onClick={() => navigate("/ui/users/add")}>
              <BsPlus size={"1.5em"} className="me-1" />
              Add User
            </Button>
          </Col>
        )}
      </Row>

      {errorMessage && (
        <Row className="justify-content-center" ref={errorRef}>
          <Col xs={12} md={10} lg={6}>
            <Alert variant="danger" onClose={() => setErrorMessage("")} className="d-flex mt-3 justify-content-center align-items-center" dismissible>
              {errorMessage}
            </Alert>
          </Col>
        </Row>
      )}

      {!loading && users !== null && (
        <Row>
          <Col xs={12} md={10} lg={7} xl={5}>
            {users?.length === 0 ? (
              <Row className="w-100">
                <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                  <h5 className="p-5">No users found.</h5>
                </Col>
              </Row>
            ) : (
              users?.map((user) => (
                <div key={user.id} className="user-item mb-4 p-3">
                  <Row className="align-items-center">
                    <Col xs={12}>
                      <Row className="mb-2">
                        <Col xs={8}>
                          <strong>{user.firstName} {user.lastName}</strong>
                        </Col>
                        <Col xs={12} className="order-2 order-md-1">
                          <strong>{user.email}</strong>
                        </Col>
                        <Col xs={4} className="order-1 order-md-2 text-end text-md-start">
                          <strong>{user.role}</strong>
                        </Col>
                      </Row>
                    </Col>

                    {me.role === RoleState.MANAGER && me.principal.claims.sub !== user.id && (
                      <Col xs={12} className="d-flex justify-content-end">
                        <Button variant="warning" className="me-2" onClick={() => openRoleModal(user.id, user.role)}>
                          Change Role
                        </Button>
                        <Button variant="danger" onClick={() => openDeleteModal(user.id)}>
                          Delete
                        </Button>
                      </Col>
                    )}
                  </Row>
                </div>
              ))
            )}
          </Col>
        </Row>
      )}

      {/* Confirmation Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p style={{ color: "#856404", fontSize: "1rem", backgroundColor: "#fff3cd", padding: "10px", borderRadius: "5px" }}>
            Are you sure you want to delete this user?
          </p>
          <p className="text-center fs-3 fw-semibold">
            {users.find((u) => u.id === userIdToDelete)?.firstName} {users.find((u) => u.id === userIdToDelete)?.lastName}
          </p>
        </Modal.Body>
        <Modal.Footer className="justify-content-between">
          <Button variant="secondary" className="ms-5" onClick={() => setShowModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" className="me-5" onClick={handleDelete}>
            Confirm Delete
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Role Change Modal */}
      <Modal show={showRoleModal} onHide={() => setShowRoleModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Change User Role</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group>
            <Form.Label>Select a new role:</Form.Label>
            <Form.Select value={newRole} onChange={(e) => setNewRole(e.target.value)}>
              {roles.map((role) => (
                <option key={role} value={role}>{role}</option>
              ))}
            </Form.Select>
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowRoleModal(false)}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleRoleChange}>
            Save Changes
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default UsersPage;
