import { useEffect, useState } from "react";
import {
  Button,
  Col,
  Modal,
  Row,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Customer } from "../interfaces/Customer";
import {
  BsChevronDown,
  BsChevronUp,
  BsPencilSquare,
  BsPlus,
  BsTrash,
} from "react-icons/bs";
import { MeInterface } from "../interfaces/MeInterface";
import { deleteCustomer, fetchCustomer } from "../apis/CustomerRequests";
import { employmentStateToText, toTitleCase } from "../utils/costants";
import { Professional } from "../interfaces/Professional";
import { deleteProfessional, fetchProfessional } from "../apis/ProfessionalRequests";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";

function ProfessionalPage({ me }: { me: MeInterface }) {
  // Estrai l'ID dall'URL
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  useEffect(() => {
    if (success != null) {
      setShowAlert(true);
      setTimeout(() => {
        setShowAlert(false);
      }, 3000);
    }
  }, [success]);

  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const handleCloseDeleteModal = () => setShowDeleteModal(false);

  const [professional, setProfessional] = useState<ProfessionalWithAssociatedData | null>(null);

  const [expandedInfoSection, setExpandedInfoSection] = useState(false);

  useEffect(() => {
    if (
      id === undefined ||
      id === null ||
      id === "" ||
      Number.parseInt(id) < 1
    ) {
      navigate("/not-found");
      return;
    }

    setLoading(true);
    fetchProfessional(Number.parseInt(id))
      .then((json) => {
        setProfessional(json);
        console.log("Professional:  ", json);
        setLoading(false);
      })
      .catch((error) => {
        navigate("/not-found");
        setLoading(false);
        console.error("Error:", error);
      });
  }, [id]);


  return (
    <div className="w-100">
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast
            bg={success ? "success" : "danger"}
            show={success != null}
            onClose={() => (location.state = null)}
          >
            <Toast.Header>
              <img
                src="holder.js/20x20?text=%20"
                className="rounded me-2"
                alt=""
              />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>
              {success ? "Operation correctly executed!" : "Operation failed!"}
            </Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Modal show={showDeleteModal} onHide={() => handleCloseDeleteModal()}>
        <Modal.Header closeButton>
          <Modal.Title>Delete professional</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p className="text-center">
            Are you sure you want to permanently delete professional
          </p>
          <p className="text-center fs-3 fw-semibold">
            {`${professional?.professionalDTO.information.name} ${professional?.professionalDTO.information.surname}?`}{" "}
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => handleCloseDeleteModal()}>
            Close
          </Button>
          <Button
            variant="danger"
            onClick={() => {
              deleteProfessional(Number.parseInt(id!!), me)
                .then((json) => {
                  console.log("Deleted professional: ", json);
                  navigate("/ui/professionals", { state: { success: true } });
                })
                .catch((error) => {
                  console.error("Error:", error);
                  navigate("/ui/professionals", { state: { success: false } });
                });
            }}
          >
            Delete
          </Button>
        </Modal.Footer>
      </Modal>

      {loading && (
        <Row className="d-flex justify-content-center align-items-center w-100 p-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </Row>
      )}
      {!loading && (
        <>
          <Row className="d-flex flex-row p-0 mb-3 align-items-center">
            <Col>
              <h3>Professional</h3>
            </Col>
            <Col className="d-flex justify-content-end">
              <Button
                className="d-flex align-items-center primaryButton me-4"
                onClick={() => navigate(`/ui/professionals/${id}/edit`)}
              >
                <BsPencilSquare size={"1em"} className="me-2" />
                Edit Professional
              </Button>
              <Button
                className="d-flex align-items-center primaryDangerButton me-4"
                onClick={() => setShowDeleteModal(true)}
              >
                <BsTrash size={"1em"} className="me-2" />
                Delete Professional
              </Button>
            </Col>
          </Row>

          <Row className="w-100 borderedSection heigth-transition">
            <div className="d-flex justify-content-between mb-2 ps-0">
              <h4 className="p-0 m-0 d-flex align-items-center">
                {`${professional?.professionalDTO.information.name} ${professional?.professionalDTO.information.surname}`}
                <span className="fw-light fs-6 ms-2">
                  ({professional?.professionalDTO.information.ssnCode})
                </span>
              </h4>

              <span className="text-uppercase fs-4 fw-semibold" style={{color: "#162250"}}>{employmentStateToText(professional?.professionalDTO.employmentState || "")}</span>
            </div>
            <Row className="d-flex">
              <p className="p-0 m-0 fs-6 fw-light">
                {professional?.professionalDTO.information.comment}
              </p>
            </Row>

            {expandedInfoSection && (
              <>

                <Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Geographical location</h6>
                    </Row>
                    
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">{professional?.professionalDTO.geographicalLocation} </p>
                      </Row>
            
                  </Row>
                </Col>

                <Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Daily rate</h6>
                    </Row>
                    
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">{professional?.professionalDTO.dailyRate} </p>
                      </Row>
                
                    
                  </Row>
                </Col>

                <Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Skills</h6>
                    </Row>
                    <Row className="d-flex flex-wrap ps-2">
                      {professional?.professionalDTO.skills && professional.professionalDTO.skills.length > 0 && professional.professionalDTO.skills.map((skill, index) => (
                        <div key={index} style={{width: "auto"}} className="text-truncate me-2 tag mb-1">
                            {skill}                            
                            </div>
                      ))}
                    </Row>
                    {professional?.professionalDTO.skills.length === 0 && (
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">No skill found</p>
                      </Row>
                    )}
                  </Row>
                </Col>
                
              </>
            )}

            <Row className="d-flex justify-content-center">
              <Col className="d-flex justify-content-center align-items-center">
                <div
                  className="text-center fs-6 text-muted"
                  style={{ cursor: "pointer" }}
                  onClick={() => setExpandedInfoSection(!expandedInfoSection)}
                >
                  {expandedInfoSection ? (
                    <>
                      <BsChevronUp size={"1em"} />
                      <p className="m-0">Hide details</p>
                    </>
                  ) : (
                    <>
                      <p className="m-0">Show details</p>
                      <BsChevronDown size={"1em"} />
                    </>
                  )}
                </div>
              </Col>
            </Row>
          </Row>

          {professional?.jobofferDTOS && professional?.jobofferDTOS.length > 0 && (
            <Row className="d-flex flex-column mt-5">
              <h4>Job Offers</h4>
            </Row>
          )}

          {professional?.jobofferDTOS && professional?.jobofferDTOS.length === 0 && (
            <Row className="d-flex flex-column text-center mt-5">
              <h4>No job offers found yet!</h4>
            </Row>
          )}

          {professional?.jobofferDTOS.map((joboffer) => (
            <div
              key={joboffer.id}
              className="job-offer-item mb-4 p-3"
              onClick={() =>
                navigate(`/ui/joboffers/${joboffer.id}`, {
                  state: { jobOfferSelected: joboffer },
                })
              }
            >
              <Row className="align-items-center">
                <Col xs={12} className="mb-2">
                  <h5 className="job-title">{joboffer.name}</h5>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-1">
                    <strong>Contract Type:</strong> {joboffer.contractType}
                  </p>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-1">
                    <strong>Location:</strong> {joboffer.location}
                  </p>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-1">
                    <strong>Work Mode:</strong> {joboffer.workMode}
                  </p>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-1">
                    <strong>Duration:</strong> {joboffer.duration} hours
                  </p>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-1">
                    <strong>Value:</strong> ${joboffer.value}
                  </p>
                </Col>
                <Col md={6} xs={12}>
                  <p className="mb-0">
                    <strong>Status:</strong>{" "}
                    <span className={`status ${joboffer.status.toLowerCase()}`}>
                      {toTitleCase(joboffer.status).toLocaleUpperCase()}
                    </span>
                  </p>
                </Col>
              </Row>
            </div>
          ))}
        </>
      )}
    </div>
  );
}

export default ProfessionalPage;


/*


<Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Emails</h6>
                    </Row>
                    {professional?..map((email, index) => (
                      <Row key={index} className="d-flex">
                        <p className="p-0 m-0 fs-6">{email.email} </p>
                        <small className="m-0 p-0">{email.comment}</small>
                      </Row>
                    ))}
                    {professional?..length === 0 && (
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">No emails found</p>
                      </Row>
                    )}
                  </Row>
                </Col>

                <Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Phones</h6>
                    </Row>
                    {customer?.information.telephoneDTOs.map((phone, index) => (
                      <Row key={index} className="d-flex">
                        <p className="p-0 m-0 fs-6">{phone.telephone} </p>
                        <small className="m-0 p-0">{phone.comment}</small>
                      </Row>
                    ))}
                    {customer?.information.telephoneDTOs.length === 0 && (
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">No phones found</p>
                      </Row>
                    )}
                  </Row>
                </Col>

                <Col xs={12} md={6} lg={4}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Addresses</h6>
                    </Row>
                    {customer?.information.addressDTOs.map((address, index) => (
                      <Row key={index} className="d-flex">
                        <p className="p-0 m-0 fs-6">
                          {" "}
                          {`${address.address}, ${address.city}, ${address.region}, ${address.state}`}{" "}
                        </p>
                        <small className="m-0 p-0">{address.comment}</small>
                      </Row>
                    ))}
                    {customer?.information.addressDTOs.length === 0 && (
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">No addresses found</p>
                      </Row>
                    )}
                  </Row>
                </Col>



                */