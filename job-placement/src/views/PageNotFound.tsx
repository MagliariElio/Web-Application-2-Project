import { Row, Col, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { FaExclamationTriangle } from "react-icons/fa"; 

function PageNotFound() {
  const navigate = useNavigate();

  return (
    <Row className="text-center d-flex flex-column align-items-center justify-content-center page-not-found" style={{ height: "500px" }}>
      <Col md={6} className="p-4">
        <div className="icon-container">
          <FaExclamationTriangle className="icon-404" />
        </div>
        <h1 className="display-4" style={{ fontWeight: "bold", color: "#343a40" }}>
          Oops! Page Not Found
        </h1>
        <p className="lead" style={{ color: "#6c757d", marginBottom: "30px" }}>
          Sorry, the page you’re looking for doesn’t exist. Please check the URL or return to the homepage.
        </p>
        <Button variant="primary" size="lg" className="px-4" onClick={() => navigate("/ui")}>
          Back to Home
        </Button>
      </Col>
    </Row>
  );
}

export default PageNotFound;
