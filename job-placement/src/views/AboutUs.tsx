import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { FaUsers, FaInstagram } from "react-icons/fa";

const AboutUs = () => {
  return (
    <Container className="mb-5 pt-4 pb-5">
      <Row className="text-center mb-5">
        <Col>
          <div className="icon-container mb-3">
            <FaUsers className="icon-about primaryTextColor" />
          </div>
          <h1 className="display-4 fw-bold primaryTextColor">Who We Are</h1>
          <p className="lead text-muted about-text  p-2">
            We are three passionate software development students from the{" "}
            <strong>Politecnico di Torino</strong>. This website was born as
            part of our project for the
            <strong> Web Application 2 course</strong>, where we combined our
            love for coding, creativity, and teamwork.
          </p>
        </Col>
      </Row>

      <Row className="g-4 justify-content-center">
        <Col xs={12} lg={10}>
          <Card className="shadow-lg border-0 text-center">
            <Card.Body>
              <Card.Title className="fw-bold">
                Connect with Us on Instagram
              </Card.Title>
              <Card.Text>
                Follow our journey and stay updated with our latest projects,
                behind-the-scenes moments, and more! Click on our profiles below
                to check out what we're up to.
              </Card.Text>
              <Row className="d-flex justify-content-around mt-4">
                <Col xs={12} lg={4} className="mb-3">
                    <Button
                    href="https://www.instagram.com/elia_ferraro/"
                    target="_blank"
                    className="instagram-button secondaryButton w-75 d-lg-none"
                    >
                    <FaInstagram className="me-2" /> @elia_ferraro
                    </Button>

                    <Button
                    href="https://www.instagram.com/elia_ferraro/"
                    target="_blank"
                    className="instagram-button secondaryButton d-none d-lg-block"
                    >
                    <FaInstagram className="me-2" /> @elia_ferraro
                    </Button>
                </Col>

                <Col xs={12} lg={4} className="mb-3">
                  <Button
                    href="https://www.instagram.com/magliari_elio/"
                    target="_blank"
                    className="instagram-button secondaryButton w-75 d-lg-none"
                  >
                    <FaInstagram className="me-2" /> @magliari_elio
                  </Button>

                  <Button
                    href="https://www.instagram.com/magliari_elio/"
                    target="_blank"
                    className="instagram-button secondaryButton d-none d-lg-block"
                  >
                    <FaInstagram className="me-2" /> @magliari_elio

                  </Button>
                </Col>
                <Col xs={12} lg={4} className="mb-3">
                  <Button
                    href="https://instagram.com/ale.white_"
                    target="_blank"
                    className="instagram-button secondaryButton w-75 d-lg-none"
                  >
                    <FaInstagram className="me-2" /> @ale.white_
                  </Button>

                  <Button
                    href="https://instagram.com/ale.white_"
                    target="_blank"
                    className="instagram-button secondaryButton d-none d-lg-block"
                  >
                    <FaInstagram className="me-2" /> @ale.white_

                  </Button>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default AboutUs;
