import { FormControl, FormLabel, Row } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";
import { toTitleCase } from "../utils/costants";

interface JPProfilePageProps {
  me: MeInterface | null;
  role: string; // Use the type defined earlier for role
}

function ProfilePage(JPHomePageProps: JPProfilePageProps) {
  return (
    <>
      <Row className="d-flex flex-column p-0 mb-3">
        <h3 className="title">My account</h3>
      </Row>
      <Row>
        <FormLabel htmlFor="inputPassword5">Name</FormLabel>
        <FormControl className="ms-2 w-25" type="text" id="inputName" aria-describedby="nameField" value={JPHomePageProps.me?.name} disabled />
      </Row>
      <Row className="mt-2">
        <FormLabel htmlFor="inputPassword5">Surname</FormLabel>
        <FormControl
          className="ms-2 w-25"
          type="text"
          id="inputSurname"
          aria-describedby="surnameField"
          value={JPHomePageProps.me?.surname}
          disabled
        />
      </Row>
      {JPHomePageProps.me?.role && (
        <Row className="mt-2">
          <FormLabel htmlFor="inputPassword5">Role</FormLabel>
          <FormControl
            className="ms-2 w-25"
            type="text"
            id="inputRole"
            aria-describedby="roleField"
            value={toTitleCase(JPHomePageProps.me?.role)}
            disabled
          />
        </Row>
      )}
    </>
  );
}

export default ProfilePage;
