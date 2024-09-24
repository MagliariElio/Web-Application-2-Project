import { useNavigate } from "react-router-dom";
import Form from "react-bootstrap/Form";
import { MeInterface } from "../interfaces/MeInterface";
import { Alert, Button, Col, Row } from "react-bootstrap";
import { BsXLg } from "react-icons/bs";
import { useRef, useState } from "react";
import { addUser } from "../apis/KeycloakRequests";

export function AddUserPage({me}: { me: MeInterface }) {
    const navigate = useNavigate();
    const [name, setName] = useState("");
    const [surname, setSurname] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("")
    const [email, setEmail] = useState("");
    const [role, setRole] = useState("GUEST");
    const [errorMessage, setErrorMessage] = useState("");
    const errorRef = useRef<HTMLDivElement | null>(null);

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
    
        if (email.trim() === "") {
           setErrorMessage("The emaile cannot be empty or just spaces.");
          if (errorRef.current) {
            errorRef.current.scrollIntoView({ behavior: "smooth" });
          }
          return;
        }

        // Regex for basic email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        if (!emailRegex.test(email)) {
          setErrorMessage("Please enter a valid email address.");
          if (errorRef.current) {
            errorRef.current.scrollIntoView({ behavior: "smooth" });
          }
          return;
        }
     
        if (role.trim() === "") {
            setErrorMessage("The role cannot be empty or just spaces.");
           if (errorRef.current) {
             errorRef.current.scrollIntoView({ behavior: "smooth" });
           }
           return;
        }   
        
        if (username.trim() === "") {
            setErrorMessage("The username cannot be empty or just spaces.");
           if (errorRef.current) {
             errorRef.current.scrollIntoView({ behavior: "smooth" });
           }
           return;
        }
        
        addUser(name, surname, email, username, role, password)
          .then(() => {
            navigate("/ui/users", { state: { success: true } });
          })
          .catch((error) => {
            navigate("/ui/users", { state: { success: false } });
            console.log("Error during user post: ", error);
            throw new Error("Keycloak : Network response was not ok");
          });
        
    };
    
    return (
        <div className="add-job-offer-container">
            <Row className="d-flex flex-row p-0 mb-5 align-items-center">
              <Col>
                <h3>Add Nnew user</h3>
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
                  <Form.Control placeholder="Username" value={username} required onChange={(e) => setUsername(e.target.value)} />
                </Col>
              </Row>
              
              <Row className="justify-content-center">
                <Col xs={12} md={6} lg={6} className="mb-4">
                  <Form.Control placeholder="Email" value={email} required onChange={(e) => setEmail(e.target.value)} />
                </Col>
              </Row>

              <Row className="justify-content-center">
                <Col xs={12} md={6} lg={6} className="mb-4">
                  <Form.Control type="password" placeholder="Password" value={password} required onChange={(e) => setPassword(e.target.value)} />
                </Col>
              </Row>

              <Row className="justify-content-center">
                <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Select
                  name="role"
                  value={role}
                  onChange={(e) => {
                    setRole(e.target.value)
                  }}
                >
                  <option value="GUEST">GUEST</option>
                  <option value="OPERATOR">OPERATOR</option>
                  <option value="MANAGER">MANAGER</option>
                </Form.Select>
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