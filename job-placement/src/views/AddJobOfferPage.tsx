import { useState } from "react";
import { Button, Col, Row } from "react-bootstrap";
import Form from "react-bootstrap/Form";
import { BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import JobOfferRequests from "../apis/JobOfferRequests";
import { contractTypeList, toTitleCase, workModeList } from "../utils/costants";

function AddJobOfferPage({ me }: { me: MeInterface }) {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [duration, setDuration] = useState("");
  const [note, setNote] = useState("");
  const [contractType, setContractType] = useState("");
  const [location, setLocation] = useState("");
  const [workMode, setWorkMode] = useState("");

  const [requiredSkills, setRequiredSkills] = useState<any[]>([]);
  const [singleRequiredSkill, setSingleRequiredSkill] = useState("");

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (name === "") {
      return;
    }

    const jobOffer = {
      name: name,
      description: description,
      contractType: contractType,
      location: location,
      workMode: workMode,
      requiredSkills: requiredSkills,
      duration: duration,
      note: note,
      customerId: 1,
    };

    try {
      await JobOfferRequests.submitJobOffer(jobOffer, me.xsrfToken);
      navigate("/ui", { state: { success: true } });
    } catch (error) {
      // navigate("/ui", { state: { success: false } });
    }
  };

  return (
    <div>
      <Row className="d-flex flex-row p-0 mb-5 align-items-center">
        <Col>
          <h3>Add New Job Offer</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate(-1)}>
            <BsXLg size={"1.5em"} />
          </Button>
        </Col>
      </Row>

      <Form onSubmit={handleSubmit}>
        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-4">
            <Form.Control placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required />
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-4">
            <Form.Control
              as="textarea"
              placeholder="Description"
              rows={5}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Select value={contractType} onChange={(e) => setContractType(e.target.value)} required>
              <option value="">Select Contract Type</option>
              {contractTypeList.map((contract, index) => (
                <option key={index} value={contract}>
                  {toTitleCase(contract)}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Select value={workMode} onChange={(e) => setWorkMode(e.target.value)} required>
              <option value="">Select Work Mode</option>
              {workModeList.map((workMode, index) => (
                <option key={index} value={workMode}>
                  {toTitleCase(workMode)}
                </option>
              ))}
            </Form.Select>
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-4">
            <Form.Control placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} required />
          </Col>
        </Row>
        <Row className="justify-content-center">
          <Col xs={12} md={6} lg={3} className="mb-4">
            <Form.Control
              type="number"
              placeholder="Duration in hours"
              value={duration}
              onChange={(e) => {
                const value = e.target.value;
                if (/^\d*$/.test(value)) {
                  setDuration(value);
                }
              }}
              onKeyPress={(e) => {
                if (!/^\d*$/.test(e.key)) {
                  e.preventDefault();
                }
              }}
              min="0"
            />
          </Col>
        </Row>

        <Row className="justify-content-center">
          <Col xs={12} md={12} lg={6} className="mb-4">
            <Form.Control as="textarea" placeholder="Note" rows={3} value={note} onChange={(e) => setNote(e.target.value)} />
          </Col>
        </Row>

        <Row className="mt-4 justify-content-center">
          <Col xs={12} md={10} lg={6}>
            <Row className="align-items-center">
              <Col>
                <hr />
              </Col>
              <Col xs="auto">
                <h5 className="fw-bold text-primary">Required Skills</h5>
              </Col>
              <Col>
                <hr />
              </Col>
            </Row>

            {requiredSkills.length === 0 && (
              <Row className="justify-content-center mt-3">
                <Col xs={12} className="text-center">
                  <p className="text-muted">No required skills added yet</p>
                </Col>
              </Row>
            )}

            {requiredSkills.length > 0 &&
              requiredSkills.map((requiredSkill, index) => (
                <Row key={index} className="mt-3 d-flex align-items-center justify-content-between">
                  <Col xs={8} md={8} lg={10}>
                    <li>
                      <ul>
                        <p className="text-truncate fw-light mb-0">{requiredSkill}</p>
                      </ul>
                    </li>
                  </Col>
                  <Col xs={4} md={4} lg={2} className="text-end">
                    <Button variant="outline-danger" size="sm" onClick={() => setRequiredSkills(requiredSkills.filter((_, i) => i !== index))}>
                      Remove
                    </Button>
                  </Col>
                </Row>
              ))}

            <Row className="mt-4 justify-content-center">
              <Col xs={12} md={8} lg={6}>
                <Form.Control
                  placeholder="Add a skill"
                  value={singleRequiredSkill}
                  onChange={(e) => {
                    setSingleRequiredSkill(e.target.value);
                  }}
                  className="mb-2"
                />
              </Col>
              <Col xs={12} md={4} lg={3} className="text-end">
                <Button
                  variant="primary"
                  onClick={() => {
                    setRequiredSkills([...requiredSkills, singleRequiredSkill]);
                    setSingleRequiredSkill("");
                  }}
                >
                  Add Skill
                </Button>
              </Col>
            </Row>
          </Col>
        </Row>

        <Row className="mt-5 justify-content-center">
          <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
            <Button type="submit" className="primaryButton">
              Save Job Offer
            </Button>
          </Col>
        </Row>
      </Form>
    </div>
  );
}

export default AddJobOfferPage;
