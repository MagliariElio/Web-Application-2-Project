import { FC, useEffect, useState } from "react";
import "./App.css";
import { MeInterface } from "./interfaces/MeInterface.ts";
import { BrowserRouter as Router, Route, Routes, useNavigate, useLocation, Navigate } from "react-router-dom";
import { Container, Row, Card, Col, Nav, Button } from "react-bootstrap";

import JPAPIAuth from "./apis/JPAuth.ts";
import NavBar from "./views/NavBar.tsx";
import JobOffersPage from "./views/JobOffersPage.tsx";
import {
  BsBriefcaseFill,
  BsBuildingsFill,
  BsCaretLeftFill,
  BsCaretRightFill,
  BsGearFill,
  BsPieChartFill,
  BsPersonFill,
  BsChatSquareDotsFill,
  BsCaretUpFill,
  BsCaretDownFill,
} from "react-icons/bs";
import ProfilePage from "./views/ProfilePage.tsx";
import CustomersPage from "./views/CustomersPage.tsx";
import ProfessionalsPage from "./views/ProfessionalsPage.tsx";
import PageNotFound from "./views/PageNotFound.tsx";
import AddCustomerPage from "./views/AddCustomerPage.tsx";
import AddJobOfferPage from "./views/AddJobOfferPage.tsx";
import JobOfferDetail from "./views/JobOfferDetailPage.tsx";
import AddProfessionalPage from "./views/AddProfessionalPage.tsx";
import EditCustomerPage from "./views/EditCustomerPage.tsx";
import { RoleState } from "./utils/costants.ts";
import EditProfessionalPage from "./views/EditProfessionalPage.tsx";
import { runCustomerTests, runJobOfferTests, runProfessionalTests } from "./testing/TestRunner.ts";
import { FaLock, FaSignInAlt, FaUsers } from "react-icons/fa";
import AboutUs from "./views/AboutUs.tsx";
import AnalyticsPage from "./views/AnalyticsPage.tsx";
import UsersPage from "./views/UsersPage.tsx";
import { AddUserPage } from "./views/Adduser.tsx";
import MessagesPage from "./views/MessagesPage.tsx";
import MessageDetail from "./views/MessageDetail.tsx";
import SettingsPage from "./views/SettingsPage.tsx";
import CustomerPage from "./views/CustomerPage.tsx";
import ProfessionalPage from "./views/ProfessionalPage.tsx";

function App() {
  const [me, setMe] = useState<MeInterface | null>(null);
  const [loading, setLoading] = useState(true);

  const [sidebarOpened, setSidebarOpened] = useState(true);

  // Run dei dati di testing
  const [testsExecuted, setTestsExecuted] = useState(true); // impostare questa a true per non runnare più
  const runTests = async () => {
    if (!testsExecuted && me) {
      try{
        await runCustomerTests(me);
        await runProfessionalTests(me);
        await runJobOfferTests(me);
        setTestsExecuted(true);
      }
      catch(err){
        console.error("Error running tests:", err);
      }
      
    }
  };
  useEffect(() => {
    runTests();
  }, []);

  useEffect(() => {
    const fetchUserRoles = async () => {
      try {
        setLoading(true);

        var meResult = await JPAPIAuth.fetchMe();
        const roleResult = await JPAPIAuth.fetchRole();
        const roles = roleResult?.principal?.claims?.realm_access?.roles || [];

        const roleUser: RoleState = roles.find(
          (role: string) => role === RoleState.GUEST || role === RoleState.OPERATOR || role === RoleState.MANAGER
        );

        if (meResult) {
          meResult.role = roleUser;
          setMe(meResult);
        }
      } catch (err) {
        console.error("Error fetching user roles:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchUserRoles();

    // Call periodically every 15 minutes (900000 ms)
    const interval = setInterval(fetchUserRoles, 900000);

    // Cleanup on unmount to avoid memory leaks
    return () => clearInterval(interval);
  }, []);

  return (
    <Router>
      <Row className="vw-100  d-flex">
        {/* Sidebar */}
        <Col
          xs={sidebarOpened ? 12 : 4}
          md={sidebarOpened ? 4 : 2}
          lg={sidebarOpened ? 2 : 1}
          className="text-white d-none d-md-flex flex-column position-fixed p-0 background-white vh-100"
          style={{
            top: 0,
            left: 0,
          }}
        >
          <Sidebar opened={sidebarOpened} setOpened={setSidebarOpened} me={me} />
        </Col>

        <Col
          xs={sidebarOpened ? 12 : 4}
          md={sidebarOpened ? 4 : 2}
          lg={sidebarOpened ? 2 : 1}
          className="text-white d-none d-md-flex flex-column p-0 background-white vh-100"
          style={{
            top: 0,
            left: 0,
          }}
        ></Col>

        <Col xs={12} className="text-white d-flex d-md-none flex-column p-0 background-white">
          <Topbar opened={sidebarOpened} setOpened={setSidebarOpened} me={me} />
        </Col>

        <Col xs={12} md={sidebarOpened ? 8 : 10} lg={sidebarOpened ? 10 : 11} className="ps-4 pt-2">
          {/* Navbar */}
          <Row className="w-100 text-white">
            <NavBar me={me} />
          </Row>

          {/* Main Content */}
          <Col className="d-flex flex-column py-4 px-3">
            {loading && <LoadingSection h={null} />}

            <Routes>
              <Route path="/ui/aboutus" element={<AboutUs />} />
              {!loading && !me?.principal && <Route path="*" element={<LoginPrompt />} />}

              {!loading && me?.principal && (
                <>
                  <Route
                    path="/ui"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER || me.role === RoleState.GUEST ? (
                          <JobOffersPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/profile"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER || me.role === RoleState.GUEST ? (
                          <ProfilePage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/customers"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <CustomersPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/customers/:id"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <CustomerPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/customers/:id/edit"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR ? (
                          <EditCustomerPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/professionals/:id/edit"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR ? (
                          <EditProfessionalPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/professionals"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <ProfessionalsPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/professionals/:id"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <ProfessionalPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/customers/add"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR ? (
                          <AddCustomerPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/professionals/add"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR ? (
                          <AddProfessionalPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/users/add"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.MANAGER ? (
                          <AddUserPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/joboffers/add"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR ? (
                          <AddJobOfferPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/joboffers/:id"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER || me.role === RoleState.GUEST ? (
                          <JobOfferDetail me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />

                  <Route
                    path="/ui/users"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.MANAGER ? (
                          <UsersPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/messages"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <MessagesPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/messages/:id"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER ? (
                          <MessageDetail me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/joboffers/:id"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER || me.role === RoleState.GUEST ? (
                          <JobOfferDetail me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/analytics"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.MANAGER ? (
                          <AnalyticsPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/users"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.MANAGER ? (
                          <UsersPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />
                  <Route
                    path="/ui/settings"
                    element={
                      me && me.principal !== null ? (
                        me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER || me.role === RoleState.GUEST ? (
                          <SettingsPage me={me} />
                        ) : (
                          <UnauthorizedPage />
                        )
                      ) : (
                        <Navigate to="/not-found" />
                      )
                    }
                  />

                  <Route path="*" element={<PageNotFound />} />
                </>
              )}
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
        {me !== null &&
          me.principal !== null && ( // Only logged user links here
            <>
              <Nav.Link
                className={(opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui" ? " nav-link-active" : "")}
                onClick={() => {
                  if (location.pathname !== "/ui") navigate("/ui");
                }}
              >
                <BsBriefcaseFill className={opened ? "me-2" : ""} />
                {opened && "Job Offers"}
              </Nav.Link>
              {(me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER) && (
                <Nav.Link
                  className={
                    (opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/customers" ? " nav-link-active" : "")
                  }
                  onClick={() => {
                    if (location.pathname !== "/ui/customers") navigate("/ui/customers");
                  }}
                >
                  <BsBuildingsFill className={opened ? "me-2" : ""} />
                  {opened && "Customers"}
                </Nav.Link>
              )}
              {(me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER) && (
                <Nav.Link
                  className={
                    (opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/professionals" ? " nav-link-active" : "")
                  }
                  onClick={() => {
                    if (location.pathname !== "/ui/professionals") navigate("/ui/professionals");
                  }}
                >
                  <BsBriefcaseFill className={opened ? "me-2" : ""} />
                  {opened && "Professionals"}
                </Nav.Link>
              )}
              {(me.role === RoleState.OPERATOR || me.role === RoleState.MANAGER) && (
                <Nav.Link
                  className={
                    (opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/messages" ? " nav-link-active" : "")
                  }
                  onClick={() => {
                    if (location.pathname !== "/ui/messages") navigate("/ui/messages");
                  }}
                >
                  <BsChatSquareDotsFill className={opened ? "me-2" : ""} />
                  {opened && "Messages"}
                </Nav.Link>
              )}
              {me?.role === RoleState.MANAGER && (
                <Nav.Link
                  className={
                    (opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/analytics" ? " nav-link-active" : "")
                  }
                  onClick={() => {
                    if (location.pathname !== "/ui/analytics") navigate("/ui/analytics");
                  }}
                >
                  <BsPieChartFill className={opened ? "me-2" : ""} />
                  {opened && "Analytics"}
                </Nav.Link>
              )}
              {me?.role === RoleState.MANAGER && (
                <Nav.Link
                  className={
                    (opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/users" ? " nav-link-active" : "")
                  }
                  onClick={() => {
                    if (location.pathname !== "/ui/users") navigate("/ui/users");
                  }}
                >
                  <BsPersonFill className={opened ? "me-2" : ""} />
                  {opened && "Users"}
                </Nav.Link>
              )}
            </>
          )}

        <div className="flex-grow-1"></div>

        {me !== null && me.principal !== null && (
          <Nav.Link
            className={
              (opened ? navLinkClassnameOpened + " mt-auto" : navLinkClassnameClosed + " mt-auto") +
              (location.pathname === "/ui/settings" ? " nav-link-active" : "")
            }
            onClick={() => {
              if (location.pathname !== "/ui/settings") navigate("/ui/settings");
            }}
          >
            <BsGearFill className={opened ? "me-2" : ""} />
            {opened && "Settings"}
          </Nav.Link>
        )}

        <Nav.Link
          className={(opened ? navLinkClassnameOpened : navLinkClassnameClosed) + (location.pathname === "/ui/aboutus" ? " nav-link-active" : "")}
          onClick={() => {
            if (location.pathname !== "/ui/aboutus") navigate("/ui/aboutus");
          }}
        >
          <FaUsers className={opened ? "me-2" : ""} />
          {opened && "About Us"}
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

const Topbar: FC<SidebarProps> = ({ opened, setOpened, me }) => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <>
      <Nav className="flex-column vw-100 p-3 topbar">
        <div className="text-white mb-3">
          <div className="w-100 d-flex justify-content-center mb-3">
            <img
              src="https://www.bgscareerventures.com/uploads/source/Logos/JobConnectLogoFINAL-400.png?1620160597743"
              alt="Logo"
              className="handpointeronhover"
              width={"75%"}
              style={{ maxWidth: "200px" }}
              onClick={() => {
                if (location.pathname !== "/ui") navigate("/ui");
              }}
            />
          </div>
          <hr className="border-top border-light" />
        </div>
        {me !== null && me?.principal !== null && ( // Only logged user links here
          <>
            {opened && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui") navigate("/ui");
                }}
              >
                <>
                  <BsBriefcaseFill className="me-2" />
                  Job Offers
                </>
              </Nav.Link>
            )}
            {opened && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui/customers" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui/customers") navigate("/ui/customers");
                }}
              >
                <>
                  <BsBuildingsFill className="me-2" />
                  Customers
                </>
              </Nav.Link>
            )}
            {opened && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui/professionals" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui/professionals") navigate("/ui/professionals");
                }}
              >
                <>
                  <BsBriefcaseFill className="me-2" />
                  Professionals
                </>
              </Nav.Link>
            )}
            {opened && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui/messages" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui/messages") navigate("/ui/messages");
                }}
              >
                <>
                  <BsChatSquareDotsFill className="me-2" />
                  Messages
                </>
              </Nav.Link>
            )}
            {opened && me?.role === RoleState.MANAGER && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui/analytics" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui/analytics") navigate("/ui/analytics");
                }}
              >
                <>
                  <BsPieChartFill className="me-2" />
                  Analytics
                </>
              </Nav.Link>
            )}
            {opened && me?.role === RoleState.MANAGER && (
              <Nav.Link
                className={
                  "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
                  (location.pathname === "/ui/users" ? " nav-link-active" : "")
                }
                onClick={() => {
                  if (location.pathname !== "/ui/users") navigate("/ui/users");
                }}
              >
                <>
                  <BsPersonFill className="me-2" />
                  Users
                </>
              </Nav.Link>
            )}
          </>
        )}

        <div className="flex-grow-1"></div>

        {me?.principal !== null && opened && (
          <Nav.Link
            className={
              "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
              (location.pathname === "/ui/settings" ? " nav-link-active" : "")
            }
            onClick={() => {
              if (location.pathname !== "/ui/settings") navigate("/ui/settings");
            }}
          >
            <>
              <BsGearFill className="me-2" />
              Settings
            </>
          </Nav.Link>
        )}

        {opened && (
          <Nav.Link
            className={
              "text-white nav-link-hover ms-2 d-flex flex-row justify-content-center align-items-center" +
              (location.pathname === "/ui/aboutus" ? " nav-link-active" : "")
            }
            onClick={() => {
              if (location.pathname !== "/ui/aboutus") navigate("/ui/aboutus");
            }}
          >
            <>
              <FaUsers className="me-2" />
              About Us
            </>
          </Nav.Link>
        )}

        <Button className="w-100 align-self-center text-white ms-2 bg-transparent border-0 nav-link-hover" onClick={() => setOpened(!opened)}>
          {opened ? <BsCaretUpFill /> : <BsCaretDownFill />}
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
            <Card.Body className="text-center p-5">
              <div className="icon-container mb-4">
                <FaSignInAlt className="icon-login" />
              </div>
              <Card.Title className="mb-4 fw-bold fs-3" style={{ color: "#343a40" }}>
                Access Required
              </Card.Title>
              <Card.Text className="mb-4 fs-5 text-muted">
                To continue using this application, please <strong>log in</strong> with your credentials. Click the properly button to be redirected
                to the secure login page.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export const UnauthorizedPage = () => {
  return (
    <Container className="mt-5">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <Card className="shadow-lg border-0">
            <Card.Body className="text-center p-5">
              <div className="icon-container mb-4">
                <FaLock className="icon-login" />
              </div>
              <Card.Title className="mb-4 fw-bold fs-3" style={{ color: "#343a40" }}>
                Unauthorized Access
              </Card.Title>
              <Card.Text className="mb-4 fs-5 text-muted">
                You do not have the necessary permissions to view this page. Please ensure you have the correct access rights or{" "}
                <strong>log in</strong> with an authorized account.
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
