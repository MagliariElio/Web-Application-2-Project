import { Container, Row, Card, Col } from 'react-bootstrap';
import {MeInterface} from "../interfaces/MeInterface.ts";
//import {useState} from "react";

interface JPHomePageProps {
    me: MeInterface | null;
    role: string; // Use the type defined earlier for role
}

const JPHomePage: React.FC<JPHomePageProps> = ({ me, role }) => {
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
}

export default JPHomePage;