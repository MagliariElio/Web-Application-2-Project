import { FC, useEffect, useState } from "react";
import "./App.css";
import { MeInterface } from "./interfaces/MeInterface.ts";
import { BrowserRouter as Router, Route, Routes, useNavigate, useLocation, Navigate } from "react-router-dom";
import { Container, Row, Card, Col, Nav, Button } from "react-bootstrap";

import JPAPIAuth from "./apis/JPAuth.ts";
import NavBar from "./views/NavBar.tsx";
import HomePage from "./views/HomePage.tsx";
import {
  BsBriefcaseFill,
  BsBuildingsFill,
  BsCaretLeftFill,
  BsCaretRightFill,
  BsFillHouseDoorFill,
  BsGearFill,
  BsPersonWorkspace,
} from "react-icons/bs";
import ProfilePage from "./views/ProfilePage.tsx";
import CustomersPage from "./views/CustomersPage.tsx";
import ProfessionalsPage from "./views/ProfessionalsPage.tsx";
import JPPageNotFound from "./views/PageNotFound.tsx";
import AddCustomerPage from "./views/AddCustomerPage.tsx";
import AddJobOfferPage from "./views/AddJobOfferPage.tsx";
import JobOfferDetail from "./views/JobOfferDetailPage.tsx";
import CustomerPage from "./views/CustomerPage.tsx";
import AddProfessionalPage from "./views/AddProfessionalPage.tsx";

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
            console.log(JSON.stringify(json.principal.claims.realm_access.roles[i]));
            i = i + 1;
          }
          if (json.principal.claims.realm_access.roles[i]) {
            console.log("New role: " + json.principal.claims.realm_access.roles[i]);
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
        <Col
          xs={sidebarOpened ? 12 : 4}
          md={sidebarOpened ? 4 : 2}
          lg={sidebarOpened ? 2 : 1}
          className="text-white d-flex flex-column p-0 background-white vh-100"
          style={{ position: "fixed", top: 0, left: 0 }}
        >
          <Sidebar opened={sidebarOpened} setOpened={setSidebarOpened} me={me} />
        </Col>
        <Col
          xs={sidebarOpened ? 12 : 4}
          md={sidebarOpened ? 4 : 2}
          lg={sidebarOpened ? 2 : 1}
          className="text-white d-flex flex-column p-0 background-white vh-100"
        ></Col>

        <Col xs={sidebarOpened ? 12 : 8} md={sidebarOpened ? 8 : 10} lg={sidebarOpened ? 10 : 11} className="ps-4 pt-2">
          {/* Navbar */}
          <Row className="w-100 text-white">
            <NavBar me={me} />
          </Row>

          {/* Main Content */}
          <Col className="d-flex flex-column py-4 px-3">
            {me?.principal ? (
              <Routes>
                <Route path="/ui" element={<HomePage />} />
                <Route path="/ui/profile" element={<ProfilePage me={me} role={role} />} />
                <Route path="/ui/customers" element={me && me.principal !== null ? <CustomersPage /> : <Navigate to="/not-found" />} />
                <Route path="/ui/customers/:id" element={me && me.principal !== null ? <CustomerPage /> : <Navigate to="/not-found" />} />
                <Route path="/ui/professionals" element={me && me.principal !== null ? <ProfessionalsPage /> : <Navigate to="/not-found" />} />
                <Route path="/ui/customers/add" element={me && me.principal !== null ? <AddCustomerPage me={me} /> : <Navigate to="/not-found" />} />
                <Route
                  path="/ui/professionals/add"
                  element={me && me.principal !== null ? <AddProfessionalPage me={me} /> : <Navigate to="/not-found" />}
                />
                <Route path="/ui/joboffers/add" element={me && me.principal !== null ? <AddJobOfferPage me={me} /> : <Navigate to="/not-found" />} />
                <Route path="/ui/joboffers/:id" element={me && me.principal !== null ? <JobOfferDetail me={me} /> : <Navigate to="/not-found" />} />

                <Route path="*" element={<JPPageNotFound />} />
              </Routes>
            ) : (
              <LoginPrompt />
            )}
          </Col>
        </Col>
      </Row>
    </Router>
  );
}

interface SidebarProps {
  opened: boolean;
  setOpened: (opened: boolean) => void;
  me: MeInterface | null;
}

const Sidebar: FC<SidebarProps> = ({ opened, setOpened, me }) => {
  const location = useLocation();
  const navigate = useNavigate();

  const navLinkClassnameOpened = "text-white nav-link-hover ms-2 d-flex flex-row align-items-center";
  const navLinkClassnameClosed = "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center";

  return (
    <>
      <Nav className="vh-100 flex-column w-100 p-3 sidebar">
        <div className="text-white mb-3">
          <div className="w-100 d-flex justify-content-center mb-3">
            <img
              src="https://www.bgscareerventures.com/uploads/source/Logos/JobConnectLogoFINAL-400.png?1620160597743"
              alt="Logo"
              className="handpointeronhover"
              width={opened ? "75%" : "100%"}
              style={{ maxWidth: "200px" }}
              onClick={() => {
                if (location.pathname !== "/ui") navigate("/ui");
              }}
            />
          </div>
          <hr className="border-top border-light" />
        </div>
        <Nav.Link
          href="#"
          className={opened ? navLinkClassnameOpened : navLinkClassnameClosed}
          onClick={() => {
            if (location.pathname !== "/ui") navigate("/ui");
          }}
        >
          <BsBriefcaseFill className={opened ? "me-2" : ""} />
          {opened && "Job Offers"}
        </Nav.Link>
        {me &&
          me.principal !== null && ( // Only logged user links here
            <>
              <Nav.Link
                href="#"
                className={opened ? navLinkClassnameOpened : navLinkClassnameClosed}
                onClick={() => {
                  if (location.pathname !== "/ui/customers") navigate("/ui/customers");
                }}
              >
                <BsBuildingsFill className={opened ? "me-2" : ""} />
                {opened && "Customers"}
              </Nav.Link>
              <Nav.Link
                href="#"
                className={opened ? navLinkClassnameOpened : navLinkClassnameClosed}
                onClick={() => {
                  if (location.pathname !== "/ui/professionals") navigate("/ui/professionals");
                }}
              >
                <BsBriefcaseFill className={opened ? "me-2" : ""} />
                {opened && "Professionals"}
              </Nav.Link>
            </>
          )}

        <Nav.Link href="#" className={opened ? navLinkClassnameOpened + " mt-auto" : navLinkClassnameClosed + " mt-auto"}>
          <BsGearFill className={opened ? "me-2" : ""} />
          {opened && "Settings"}
        </Nav.Link>
        <Button
          className={
            opened
              ? "w-25 align-self-end text-white ms-2 bg-transparent border-0 nav-link-hover"
              : "w-100 align-self-center text-white ms-2 bg-transparent border-0 nav-link-hover"
          }
          onClick={() => setOpened(!opened)}
        >
          {opened ? <BsCaretLeftFill /> : <BsCaretRightFill />}
        </Button>
      </Nav>
    </>
  );
};

const LoginPrompt = () => {
  return (
    <Container className="mt-5">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <Card className="shadow-lg border-0">
            <Card.Body className="text-center">
              <Card.Title className="mb-4 fw-bold fs-3">Access Required</Card.Title>
              <Card.Text className="mb-4 fs-5 text-muted">
                To continue using this application, please log in with your credentials. Click the properly button to be redirected to the secure
                login page.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export const LoadingSection: FC<{ h: number | null }> = ({ h }) => {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ height: h ? `${h}px` : "100vh" }}>
      <div className="spinner-border" role="status">
        <span className="sr-only"></span>
      </div>
    </div>
  );
};

export default App;
