import { Navbar, Nav, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface.ts";
import { BsPersonCircle } from "react-icons/bs";

function NavBar({ me }: { me: MeInterface | null }) {
  const navigate = useNavigate();

  return (
    <Navbar className="justify-content-between align-items-center navbarStyle">
      {me?.principal && (
        <Navbar.Text
          className="ms-4 me-4 px-1 py-0 handpointeronhover floatingOnHover smallBorders d-flex align-items-center justify-content-center" 
          onClick={() => navigate("/ui/profile")}
        >
          <span className="fs-3"><BsPersonCircle /></span>{" "}
          <span className="fs-5 ms-2 fw-semibold">{me?.principal?.fullName ?? "Loading..."}</span>
        </Navbar.Text>
      )}
      {
        !me?.principal && (
          <Navbar.Text>
            <span className="ms-4 fs-3"><BsPersonCircle /></span>{" "}
            <span className="fs-5 ms-2 fw-semibold">Guest</span>
          </Navbar.Text>
        )
      }
      <Nav.Item className="ml-auto me-4">
        {me && me.principal && (
          <>
            <form method={"post"} action={me.logoutUrl}>
              <input type={"hidden"} name={"_csrf"} value={me.xsrfToken} />
              <Button type={"submit"} variant="warning" className="ml-auto">
                Logout
              </Button>
            </form>
          </>
        )}
        {me && me.principal == null && me.loginUrl && (
          <Button
            variant="warning"
            onClick={() => window.location.href = me?.loginUrl}
            className="ml-auto"
          >
            Login
          </Button>
        )}
      </Nav.Item>
    </Navbar>
  );
}

export default NavBar;
