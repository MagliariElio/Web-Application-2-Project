import { Navbar, Nav, Button } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface.ts";
import { BsPersonCircle } from "react-icons/bs";

function NavBar({ me }: { me: MeInterface | null }) {
  return (
    <Navbar className="justify-content-between align-items-center navbarStyle">
      {me?.principal && (
        <Navbar.Text className="ms-4 me-4 px-1 py-0 d-flex align-items-center justify-content-center">
          <span className="fs-3">
            <BsPersonCircle />
          </span>{" "}
          <span className="fs-5 ms-2 fw-semibold">{me?.principal?.fullName ?? "Loading..."}</span>
        </Navbar.Text>
      )}
      <div className="flex-grow-1"></div>

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
          <Button variant="warning" onClick={() => (window.location.href = me?.loginUrl)} className="ml-auto">
            Login
          </Button>
        )}
      </Nav.Item>
    </Navbar>
  );
}

export default NavBar;
