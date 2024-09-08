import { Container, Row, Col, Form, FormControl, FormLabel } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";
import { toTitleCase } from "../utils/costants";

const ProfilePage: React.FC<{ me: MeInterface }> = ({ me }) => {
  return (
    <Container fluid className="profile-container mt-4">
      <Row className="mb-4">
        <Col>
          <h3 className="title">My Account</h3>
        </Col>
      </Row>
      <Row>
        <Col md={6}>
          <Form>
            <Form.Group as={Row} className="mb-3" controlId="inputName">
              <FormLabel column sm={4}>
                Name:
              </FormLabel>
              <Col sm={8}>
                <FormControl type="text" value={me?.name || ""} disabled />
              </Col>
            </Form.Group>

            <Form.Group as={Row} className="mb-3" controlId="inputSurname">
              <FormLabel column sm={4}>
                Surname:
              </FormLabel>
              <Col sm={8}>
                <FormControl type="text" value={me?.surname || ""} disabled />
              </Col>
            </Form.Group>

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
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default ProfilePage;
