import { Navbar, Nav, Button } from "react-bootstrap";
import { Link } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface.ts";
//import JPAPIAuth from "../apis/JPAuth.ts";

function NavBar({ me }: { me: MeInterface | null }) {
  /*const handleLoginClick = () => {
        if(me !== null)
            window.location.href = me?.loginUrl
       // setShowLoginModal(true); // Apre il modale di accesso quando viene cliccato il link di accesso
    };

    const handleCloseLoginModal = () => {
       // setShowLoginModal(false); // Chiude il modale di accesso
    };

    const handleLogoutClick = () =>{
        if(me !== null)
            JPAPIAuth.logout(me.logoutUrl, me.xsrfToken).then(handleCloseLoginModal)
    };*/

  return (
    /*<Navbar bg="dark" variant="dark" fixed="top" style={{ padding: '0.5rem 2rem' }}>
            <Nav className="container-fluid">
                <Nav.Item style={{ padding: '0.5rem 5rem' }}>
                    <Navbar.Brand className="navbar navbar-expand-lg navbar-dark bg-dark">
                        <Link className="navbar-brand" to="/ui" >LOGO</Link>
                    </Navbar.Brand>
                </Nav.Item>
                <Nav.Item className="ml-auto">
                    {
                        me && me.principal &&
                        <>
                            <form method={"post"} action={me.logoutUrl} >
                                <input type={"hidden"} name={"_csrf"} value={me.xsrfToken}/>
                                <button type={"submit"} className="ml-auto">Logout</button>
                            </form>
                        </>
                    }
                    {
                        me && me.principal==null && me.loginUrl &&
                        <button onClick={() => window.location.href=me?.loginUrl} className="ml-auto">Login</button>
                    }
                </Nav.Item>
            </Nav>
        </Navbar>*/

    <Navbar bg="light" className="justify-content-between">
      <Navbar.Brand href="#">LOGO</Navbar.Brand>
      {me?.principal && (
        <Navbar.Text>
          <span className="mr-2">👤</span>{" "}
          {me?.principal?.fullName ?? "Loading..."}
        </Navbar.Text>
      )}
      <Nav.Item className="ml-auto">
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
            onClick={() => (window.location.href = me?.loginUrl)}
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
