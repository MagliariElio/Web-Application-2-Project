import { useEffect, useState } from "react";
import "./App.css";
import { MeInterface } from "./interfaces/MeInterface.ts";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import {
  Container,
  Row,
  Card,
  Col,
  Navbar,
  Nav,
  Button,
} from "react-bootstrap";

import JPAPIAuth from "./apis/JPAuth.ts";
import NavBar from "./views/NavBar.tsx";
import HomePage from "./views/HomePage.tsx";

function App() {
  const [me, setMe] = useState<MeInterface | null>(null);
  const [role, setRole] = useState<string>("");

  useEffect(() => {
    JPAPIAuth.fetchMe(setMe).then(async () => {
      if (me?.principal !== null) {
        const res = await fetch("/documentStoreService/v1/API/documents/auth");
        const json = await res.json();

        if (JSON.stringify(json.principal.claims.realm_access.roles[0])) {
          var i = 0;
          while (
            json.principal.claims.realm_access.roles[i] !== "GUEST" &&
            json.principal.claims.realm_access.roles[i] !== "OPERATOR" &&
            json.principal.claims.realm_access.roles[i] !== "MANAGER"
          ) {
            console.log(
              JSON.stringify(json.principal.claims.realm_access.roles[i])
            );
            i = i + 1;
          }
          if (json.principal.claims.realm_access.roles[i]) {
            console.log(
              "New role: " + json.principal.claims.realm_access.roles[i]
            );
            setRole(json.principal.claims.realm_access.roles[i]);
          } else {
            setRole("OPERATOR");
          }
        } else {
          setRole("OPERATOR");
        }
      }
    });
  }, []);

  return (
    <Router>
      <Container fluid className="vh-100 d-flex flex-column p-0">
        {/* Navbar */}
        <NavBar me={me} />

        <Row className="flex-grow-1 no-gutters">
          {/* Sidebar */}
          <Col md={2} className="bg-dark text-white d-flex flex-column p-0 sidebar">
            <Sidebar />
          </Col>

          {/* Main Content */}
          <Col md={10} className="d-flex flex-column p-0">
            <Container className="d-flex flex-grow-1 align-items-center justify-content-center p-0">
              <Routes>
                <Route path="/ui" element={<HomePage me={me} role={role} />} />
              </Routes>
            </Container>
          </Col>

        </Row>
      </Container>
    </Router>
  );
}

function Sidebar() {
  return (
    <Nav className="flex-column w-100 p-3">
      <Nav.Link href="#" className="text-white">
        Home Page
      </Nav.Link>
      <Nav.Link href="#" className="text-white">
        Feature 1
      </Nav.Link>
      <Nav.Link href="#" className="text-white">
        Feature 2
      </Nav.Link>
      <Nav.Link href="#" className="text-white">
        Feature 3
      </Nav.Link>
      <Nav.Link href="#" className="text-white">
        Feature 4
      </Nav.Link>
      <Nav.Link href="#" className="text-white mt-auto">
        Settings
      </Nav.Link>
    </Nav>
  );
}

export default App;
