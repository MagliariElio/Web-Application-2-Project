### Teacher: Malnati Giovanni
### Grade: 30 cum laude
### Politecnico di Torino

---

# KEYCLOAK credentials

- Username: `admin`
- Password: `password`
- Client name: `lab5-g04-client`
- Client secret: `5EN4Ql688ygcMWtJNU1tPUHthGuofGcQ`

# Users

## User 1

- Username: `johndoe`
- Email: `johndoe@email.com`
- First name: `John`
- Last name:  `Doe`
- Password: `johndoePassword`
- Role: `guest`

## User 2

- Username: `alexsmith`
- Email: `alexsmith@email.com`
- First name: `Alex`
- Last name:  `Smith`
- Password: `alexsmithPassword`
- Role: `manager`

## User 3

- Username: `emilyjones`
- Email: `emilyjones@email.com`
- First name: `Emily`
- Last name:  `Jones`
- Password: `emilyjonesPassword`
- Role: `operator`

# Usage

The infrastructure of this lab consists of an API gateway, an Identity and Access Management (IAM) system, and several resource servers, each corresponding to an independently implemented module. The API gateway, referred to as `client-oauth`, handles authentication, routing requests to the appropriate microservices based on rules defined in `application.yml`, and managing authentication tokens. This ensures that only authenticated users can access the requested microservices. Keycloak is used as the IAM system for authentication, authorization (managed through roles), and centralized user management.

## Example Microservice Queries:
- `http://localhost:8080/documentStoreService/v1/API/documents/auth`
- `http://localhost:8080/crmService/v1/API/messages/auth`
- `http://localhost:8080/comunicationManagerService/v1/API/emails/auth`

For this example, an `auth` endpoint has been added to **test** and visually display the authenticated user's information in the application.

All requests go through port 8080, specifying the service in the URL. The API gateway, based on the rules set in `application.yml`, will redirect to the correct route according to the specified service and the defined internal port. According to the rules, the service and `v1` will be removed from the request. Importantly, the microservices cannot be accessed directly via the URLs specified in `application.yml`, but only through the gateway if the requests do not need authentication, ensuring that unauthenticated requests are blocked.

## Example of This Behavior:
- `http://localhost:8080/API/documents/auth` = protected route requiring authentication.
- `http://localhost:8081/API/documents/auth/public` = unprotected route (can also be accessed through port 8080).

The API gateway manages several responsibilities. It handles user authentication by interacting with the IAM system (Keycloak) to verify tokens. By forwarding requests to the appropriate microservice based on the URL pattern defined in application.yml, it ensures that the requests reach the correct destination. Additionally, the gateway manages authentication tokens to ensure that only authenticated requests reach the microservices by validating tokens.

# Installation

For installation, it's necessary to open each resource server and the client-oauth (API gateway) in separate instances of IntelliJ and run them individually. For the client-oauth, you might need to run the compose file first to build the containers for Keycloak if they are not already present. For the other resource servers, everything will start automatically.

## Run
For startup, first launch the `client-oauth` containers (`postgres-1` and `keycloak-1`), wait about 1 minute, and then start the `client-oauth` instance from IntelliJ, along with the other instances of the various microservices.

For the frontend, navigate to the `job-placement/src` directory, run `npm install`, and then `npm run dev` to start the frontend client.
