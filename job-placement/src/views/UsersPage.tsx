import { useLocation, useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { Alert, Button, Col, Container, Row, Toast, ToastContainer, Modal } from "react-bootstrap";
import { useEffect, useRef, useState } from "react";
import { BsPlus } from "react-icons/bs";
import { RoleState } from "../utils/costants";
import { deleteUser, fetchUsers } from "../apis/KeycloakRequests";
import { KeycloakUser } from "../interfaces/KeycloakUser";

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
        <Col md={8}>
          <h3 className="title">Users</h3>
        </Col>

        {me.role === RoleState.MANAGER && (
          <Col md={2} className="d-flex justify-content-end">
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

      {loading && (
        <Row>
          <Col md={8}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
          <Col md={4}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!loading && users !== null && (
        <>
          <Row>
            <Col md={8}>
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
                      <Col md={8}>
                        <Row className="mb-2">
                          <Col>
                            <strong>
                              {user.firstName} {user.lastName}
                            </strong>
                          </Col>
                        </Row>
                        <Row>
                          <Col xs={6}>
                            <strong>{user.email}</strong>
                          </Col>
                          <Col xs={2}>
                            <strong>{user.role}</strong>
                          </Col>
                        </Row>
                      </Col>
                      {me.role === RoleState.MANAGER && (
                        <Col md={2} className="d-flex justify-content-end">
                          <Button
                            variant="danger"
                            onClick={() => openDeleteModal(user.id)} // Open confirmation modal
                          >
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
        </>
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
    </Container>
  );
};

export default UsersPage;
