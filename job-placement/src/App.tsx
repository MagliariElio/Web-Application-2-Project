import { FC, useEffect, useState } from "react";
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
import { BsCaretLeftFill, BsCaretRightFill, BsFillHouseDoorFill, BsGearFill } from "react-icons/bs";
import ProfilePage from "./views/ProfilePage.tsx";

function App() {
  const [me, setMe] = useState<MeInterface | null>(null);
  const [role, setRole] = useState<string>("");

  
  const [sidebarOpened, setSidebarOpened] = useState(true);

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
      <Row className="vw-100  d-flex">
        {/* Sidebar */}
        <Col xs={sidebarOpened ? 12 : 4} md={sidebarOpened ? 4 : 2} lg={sidebarOpened ? 2 : 1} className="text-white d-flex flex-column p-0 background-white">
          <Sidebar opened={sidebarOpened} setOpened={setSidebarOpened} />
        </Col>
        <Col xs={sidebarOpened ? 12 : 8} md={sidebarOpened ? 8 : 10} lg={sidebarOpened ? 10 : 11} className="ps-4 pt-2">
           {/* Navbar */}
           <Row className="w-100 text-white">
            <NavBar me={me} />
          </Row>

          {/* Main Content */}
          <Col className="d-flex flex-column py-4 px-3">
              <Routes>
                <Route path="/ui" element={<HomePage me={me} role={role} />} />
                <Route path="/ui/profile" element={<ProfilePage me={me} role={role} />} />
              </Routes>
          </Col>
        </Col>
      </Row>
    </Router>
  );
}

interface SidebarProps {
  opened: boolean;
  setOpened: (opened: boolean) => void;
}

const Sidebar: FC<SidebarProps> = ({ opened, setOpened }) => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <>
      {opened ? (
        <Nav className="vh-100 flex-column w-100 p-3 sidebar">
          <div className="text-white mb-3">
            <div className="w-100 d-flex justify-content-center mb-3">
              <img
                src="https://www.bgscareerventures.com/uploads/source/Logos/JobConnectLogoFINAL-400.png?1620160597743"
                alt="Logo"
                className="handpointeronhover"
                width="75%"
                style={{ maxWidth: "200px" }}
                onClick={() => {
                  if (location.pathname !== '/ui') navigate('/ui');
                }}
              />
            </div>
            <hr className="border-top border-light" />
          </div>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2 d-flex flex-row align-items-center">
            <BsFillHouseDoorFill className="me-2" />
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
          <Nav.Link href="#" className="text-white nav-link-hover ms-2 mt-auto d-flex flex-row align-items-center">
            <BsGearFill className="me-2" />
            Settings
          </Nav.Link>
          <Button
            className="w-25 align-self-end text-white ms-2 bg-transparent border-0 nav-link-hover"
            onClick={() => setOpened(false)}
          >
            <BsCaretLeftFill />
          </Button>
        </Nav>
      ) : (
        <Nav className="vh-100 flex-column w-100 p-3 sidebar">
          <Nav.Link href="#" className="text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center">
            <BsFillHouseDoorFill />
          </Nav.Link>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2">
            {/* Insert here just the icon */}
          </Nav.Link>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2">
            {/* Insert here just the icon */}
          </Nav.Link>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2">
            {/* Insert here just the icon */}
          </Nav.Link>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2">
            {/* Insert here just the icon */}
          </Nav.Link>
          <Nav.Link href="#" className="text-white nav-link-hover ms-2 mt-auto d-flex flex-row justify-content-center align-items-center">
            <BsGearFill />
          </Nav.Link>
          <Button
            className="w-100 align-self-center text-white ms-2 bg-transparent border-0 nav-link-hover"
            onClick={() => setOpened(true)}
          >
            <BsCaretRightFill />
          </Button>
        </Nav>
      )}
    </>
  );
}
export default App;
