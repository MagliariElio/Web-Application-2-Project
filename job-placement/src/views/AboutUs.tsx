import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { FaUsers, FaInstagram } from "react-icons/fa";

const AboutUs = () => {
  return (
    <Container className="my-5 py-5">
      <Row className="text-center mb-5">
        <Col>
          <div className="icon-container mb-3">
            <FaUsers className="icon-about" />
          </div>
          <h1 className="display-4 fw-bold text-primary">Who We Are</h1>
          <p className="lead text-muted about-text  p-2">
            We are three passionate software development students from the Politecnico di Torino. This website was born as part of our project for the Web Application 2 course, where we combined our love for coding, creativity, and teamwork.
          </p>
        </Col>
      </Row>

      <Row className="g-4 justify-content-center">
        <Col md={8}>
          <Card className="shadow-lg border-0 text-center">
            <Card.Body>
              <Card.Title className="fw-bold">Connect with Us on Instagram</Card.Title>
              <Card.Text>
                Follow our journey and stay updated with our latest projects, behind-the-scenes moments, and more! Click on our profiles below to check out what we're up to.
              </Card.Text>
              <div className="d-flex justify-content-around mt-4">
                <Button variant="outline-primary" href="https://www.instagram.com/magliari_elio/" target="_blank" className="instagram-button">
                  <FaInstagram className="me-2" /> @magliari_elio
                </Button>
                <Button variant="outline-primary" href="https://www.instagram.com/elia_ferraro/" target="_blank" className="instagram-button">
                  <FaInstagram className="me-2" /> @elia_ferraro
                </Button>
                <Button variant="outline-primary" href="https://instagram.com/" target="_blank" className="instagram-button">
                  <FaInstagram className="me-2" /> @alessandro_bianco
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default AboutUs;
