import { Row } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";

interface JPProfilePageProps {
    me: MeInterface | null;
    role: string; // Use the type defined earlier for role
}

function ProfilePage(JPHomePageProps: JPProfilePageProps) {
  return (
    <Row className="d-flex flex-column p-0">
      <h3>My account</h3>
    </Row>
  );
}

export default ProfilePage;