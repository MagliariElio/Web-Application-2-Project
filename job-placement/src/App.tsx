import { useEffect, useState } from "react";
import "./App.css";
import { MeInterface } from "./interfaces/MeInterface.ts";
import { BrowserRouter as Router, Route, Routes, useNavigate, useLocation } from "react-router-dom";
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
      <Row className="vw-100 d-flex">
        {/* Sidebar */}
        <Col xs={12} md={4} lg={2} className="text-white d-flex flex-column p-0 background-white">
          <Sidebar />
        </Col>
        <Col xs={12} md={8} lg={10}>
           {/* Navbar */}
           <Row md={2} className="bg-dark text-white d-flex flex-column p-0">
            <NavBar me={me} />
          </Row>

          {/* Main Content */}
          <Col md={10} className="d-flex flex-column p-0">
            <Container className="d-flex flex-grow-1 align-items-center justify-content-center p-0">
              <Routes>
                <Route path="/ui" element={<HomePage me={me} role={role} />} />
              </Routes>
            </Container>
          </Col>
        </Col>
      </Row>
    </Router>
  );
}



function Sidebar() {

  const location = useLocation();
  const navigate = useNavigate();

  return (
    <Nav className="h-100 flex-column w-100 p-3 sidebar">
      <div className="text-white mb-3">
        <div className="w-100 d-flex justify-content-center mb-3">
          <img
            src="https://www.bgscareerventures.com/uploads/source/Logos/JobConnectLogoFINAL-400.png?1620160597743"
            alt="Logo"
            className="handpointeronhover"
            width="75%"
            style={{ maxWidth: "200px" }}
            onClick={ () => { if (location.pathname !== '/ui') navigate('/ui') }}
          />
        </div>
        <hr className="border-top border-light" /> {/* Riga orizzontale */}
      </div>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2">
        Home Page
      </Nav.Link>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2">
        Feature 1
      </Nav.Link>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2">
        Feature 2
      </Nav.Link>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2">
        Feature 3
      </Nav.Link>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2">
        Feature 4
      </Nav.Link>
      <Nav.Link href="#" className="text-white nav-link-hover ms-2 mt-auto">
        Settings
      </Nav.Link>
    </Nav>
  );
}

export default App;
