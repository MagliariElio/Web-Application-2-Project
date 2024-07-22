import { Container, Row, Card, Col, Navbar, Nav, Button } from 'react-bootstrap';
import {MeInterface} from "../interfaces/MeInterface.ts";
import 'bootstrap/dist/css/bootstrap.min.css';

//import {useState} from "react";

interface JPHomePageProps {
    me: MeInterface | null;
    role: string; // Use the type defined earlier for role
}

/*const JPHomePage: React.FC<JPHomePageProps> = ({ me, role }) => {
    //const [authenticated, setAuthenticated] = useState(false);
    return (
        <Container className="mt-5 pt-5 w-100">
            <Row>
                <Col md={12} className="mb-12">
                    <Card>
                        <Card.Body>
                            <Card.Title>Welcome to Lab 5 of Group 04</Card.Title>
                            {me && me.principal &&
                                <Card.Text>
                                    {`Hi ${me.name},`}
                                    <br />
                                    <br />
                                    Welcome to our application.
                                    <br />
                                    <br />
                                    Your current role is {role}
                                </Card.Text>
                            }
                            {me && me.principal==null && me.loginUrl &&
                                <Card.Text>
                                    {`Hi Guest,`}
                                    <br />
                                    <br />
                                    Welcome to our application.
                                    <br />
                                    <br />
                                    Please login to see your role.
                                </Card.Text>
                            }
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
}*/

const HomePage: React.FC<JPHomePageProps> = () => {
    return (
      <Container fluid className="vh-100 d-flex flex-column p-0">
        <h1>DISPLAY HERE INFORMATION</h1>
      </Container>
    );
  }

export default HomePage;