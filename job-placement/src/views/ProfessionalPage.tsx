import { useEffect, useState } from "react";
import {
  Button,
  Col,
  Modal,
  OverlayTrigger,
  Row,
  Toast,
  ToastContainer,
  Tooltip,
} from "react-bootstrap";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
  BsChevronDown,
  BsChevronUp,
  BsFileEarmark,
  BsFileEarmarkExcel,
  BsFileEarmarkImage,
  BsFileEarmarkPdf,
  BsFileEarmarkPpt,
  BsFileEarmarkText,
  BsFileEarmarkWord,
  BsFileEarmarkZip,
  BsPencilSquare,
  BsTrash,
} from "react-icons/bs";
import { MeInterface } from "../interfaces/MeInterface";
import { employmentStateToText, RoleState } from "../utils/costants";
import {
  deleteProfessional,
  fetchProfessional,
} from "../apis/ProfessionalRequests";
import { ProfessionalWithAssociatedData } from "../interfaces/ProfessionalWithAssociatedData";
import { JobOfferCard } from "./JobOffersPage";
import { DocumentFile } from "../interfaces/DocumentFile";
import { getDocumentById } from "../apis/DocumentRequests";

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

  const [professional, setProfessional] =
    useState<ProfessionalWithAssociatedData | null>(null);

  const [attachments, setAttachments] = useState<DocumentFile[]>([]);

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
        setAttachments([]);
        json.professionalDTO.attachmentsList.forEach((attachment) => {
          getDocumentById(attachment, me)
            .then((json) => {
              setAttachments((attachments) => [...attachments, json]);
            })
            .catch((error) => {
              console.error("Error:", error);
            });
        });
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
          <p
            style={{
              color: "#856404",
              fontSize: "1rem",
              backgroundColor: "#fff3cd",
              padding: "10px",
              borderRadius: "5px",
            }}
          >
            <strong>Warning:</strong> Are you sure you want to{" "}
            <strong>permanently delete</strong> this professional record? This
            action is <strong>irreversible</strong> and will delete all{" "}
            <strong>associated job applications</strong>, including{" "}
            <strong>pending</strong> and <strong>accepted</strong> ones. Linked
            job offers will revert to the <strong>selection phase</strong>.
          </p>

          <p className="text-center fs-3 fw-semibold">
            {`${professional?.professionalDTO.information.name} ${professional?.professionalDTO.information.surname}?`}{" "}
          </p>
        </Modal.Body>
        <Modal.Footer className="justify-content-between">
          <Button
            variant="secondary"
            className="ms-5"
            onClick={() => handleCloseDeleteModal()}
          >
            Close
          </Button>
          <Button
            variant="danger"
            className="me-5"
            onClick={() => {
              deleteProfessional(Number.parseInt(id!!), me)
                .then(() => {
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

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col>
          <h3 className="title">Professional</h3>
        </Col>
        {!loading && me.role === RoleState.OPERATOR && (
          <Col className="d-flex justify-content-end">
            <OverlayTrigger
              overlay={<Tooltip id="editButton">Edit Professional</Tooltip>}
            >
              <Button
                className="d-flex align-items-center primaryButton me-4"
                onClick={() => navigate(`/ui/professionals/${id}/edit`)}
              >
                <BsPencilSquare size={"1em"} className="me-2" />
                Edit
              </Button>
            </OverlayTrigger>
            <OverlayTrigger
              overlay={<Tooltip id="deleteButton">Delete Professional</Tooltip>}
            >
              <Button
                className="d-flex align-items-center primaryDangerButton me-4"
                onClick={() => setShowDeleteModal(true)}
              >
                <BsTrash size={"1em"} className="me-2" />
                Delete
              </Button>
            </OverlayTrigger>
          </Col>
        )}
      </Row>

      {loading && (
        <Row>
          <Col md={12}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!loading && (
        <Row className="w-100 borderedSection heigth-transition">
          <div className="d-flex justify-content-between mb-2 ps-0">
            <h4 className="p-0 m-0 d-flex align-items-center">
              {`${professional?.professionalDTO.information.name} ${professional?.professionalDTO.information.surname}`}
              <span className="fw-light fs-6 ms-2">
                ({professional?.professionalDTO.information.ssnCode})
              </span>
            </h4>

            <span
              className="text-uppercase fs-4 fw-semibold"
              style={{ color: "#162250" }}
            >
              {employmentStateToText(
                professional?.professionalDTO.employmentState || ""
              )}
            </span>
          </div>
          <Row className="d-flex">
            <p className="p-0 m-0 fs-6 fw-light">
              {professional?.professionalDTO.information.comment}
            </p>
          </Row>

          {expandedInfoSection && (
            <>
              <Col xs={12} md={6} lg={8}>
                <Row>
                  <Col xs={12} md={6} lg={6}>
                    <Row className="d-flex flex-column mt-3">
                      <Row className="d-flex">
                        <h6 className="p-0 m-0">Geographical location</h6>
                      </Row>

                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">
                          {professional?.professionalDTO.geographicalLocation}{" "}
                        </p>
                      </Row>
                    </Row>
                  </Col>
                  <Col xs={12} md={6} lg={4}>
                    <Row className="d-flex flex-column mt-3">
                      <Row className="d-flex">
                        <h6 className="p-0 m-0">Daily rate</h6>
                      </Row>

                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">
                          {professional?.professionalDTO.dailyRate} €
                        </p>
                      </Row>
                    </Row>
                  </Col>
                </Row>

                <Col xs={12} md={8} lg={8}>
                  <Row className="d-flex flex-column mt-3">
                    <Row className="d-flex">
                      <h6 className="p-0 m-0">Attachments</h6>
                    </Row>
                    <Row className="d-flex flex-wrap">
                      {attachments &&
                        attachments.length > 0 &&
                        attachments
                          .sort((a, b) => a.name.localeCompare(b.name))
                          .map((attachment, index) => (
                            <Row key={index} className="d-flex mb-1">
                              <a
                                href={`/documentStoreService/v1/API/documents/${attachment.id}/data`}
                                target="_blank"
                                rel="noreferrer"
                                className="text-truncate me-2 p-0 d-flex align-items-center"
                              >
                                {attachment.contentType ===
                                "application/pdf" ? (
                                  <BsFileEarmarkPdf
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType ===
                                    "application/msword" ||
                                  attachment.contentType ===
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ? (
                                  <BsFileEarmarkWord
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType ===
                                    "application/vnd.ms-excel" ||
                                  attachment.contentType ===
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ? (
                                  <BsFileEarmarkExcel
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType ===
                                    "application/vnd.ms-powerpoint" ||
                                  attachment.contentType ===
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" ? (
                                  <BsFileEarmarkPpt
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType ===
                                    "application/zip" ||
                                  attachment.contentType ===
                                    "application/x-rar-compressed" ? (
                                  <BsFileEarmarkZip
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType === "image/jpeg" ||
                                  attachment.contentType === "image/png" ? (
                                  <BsFileEarmarkImage
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : attachment.contentType === "text/plain" ||
                                  attachment.contentType ===
                                    "application/octet-stream" ? (
                                  <BsFileEarmarkText
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                ) : (
                                  <BsFileEarmark
                                    style={{ width: "20px", height: "20px" }}
                                    className="me-1"
                                  />
                                )}
                                {attachment.name}
                              </a>
                            </Row>
                          ))}
                    </Row>
                    {attachments.length === 0 && (
                      <Row className="d-flex">
                        <p className="p-0 m-0 fs-6">No attachment found</p>
                      </Row>
                    )}
                  </Row>
                </Col>
              </Col>

              <Col xs={12} md={6} lg={4}>
                <Row className="d-flex flex-column mt-3">
                  <Row className="d-flex">
                    <h6 className="p-0 m-0">Skills</h6>
                  </Row>
                  <Row className="d-flex flex-wrap ps-2">
                    {professional?.professionalDTO.skills &&
                      professional.professionalDTO.skills.length > 0 &&
                      professional.professionalDTO.skills.map(
                        (skill, index) => (
                          <div
                            key={index}
                            style={{ width: "auto" }}
                            className="text-truncate me-2 tag mb-1"
                          >
                            {skill}
                          </div>
                        )
                      )}
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
      )}

      <Row className="d-flex flex-column mt-5">
        <h4 className="title">Job Offers</h4>
      </Row>

      {loading && (
        <Row>
          <Col md={12}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!loading &&
        professional?.jobofferDTOS &&
        professional?.jobofferDTOS.length === 0 && (
          <Row className="d-flex flex-column text-center mt-5">
            <h4>No job offers found yet!</h4>
          </Row>
        )}

      {!loading &&
        professional?.jobofferDTOS.map((joboffer) => (
          <div
            key={joboffer.id}
            className="job-offer-item mb-4 p-3"
            onClick={() =>
              navigate(`/ui/joboffers/${joboffer.id}`, {
                state: { jobOfferSelected: joboffer },
              })
            }
          >
            <JobOfferCard joboffer={joboffer} />
          </div>
        ))}
    </div>
  );
}

export default ProfessionalPage;
