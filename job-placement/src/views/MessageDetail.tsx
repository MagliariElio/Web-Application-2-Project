import { useLocation } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { useEffect, useRef, useState } from "react";
import { Message } from "../interfaces/Message";
import { fetchMessagesHistory, updateMessage } from "../apis/MessagesRequests";
import { Container, Row, Col, Form, Alert, Button, Modal } from "react-bootstrap";
import { formatArrayDate } from "./MessagesPage";
import { MessageHistory } from "../interfaces/MessageHistory";

const  MessageDetail: React.FC<{ me: MeInterface }> = ({ me }) => {
  const location = useLocation();
  const [messageSelected, setMessageSelected] = useState<Message>(location.state?.message)
  const [priority, setPriority] = useState<string>(messageSelected.priority)
  const [history, setHistory] = useState<MessageHistory[]>([])
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const [showModal, setShowModal] = useState(false);
  const [stateToAssign, setStateToAssign] = useState<string | null>(null);

  useEffect(() => {
    const fetchAndUpdateMessage = async () => {
        try {
          if (messageSelected.actualState === "RECEIVED") {
            // Await the updateMessage function here
            const newMessage = await updateMessage(me, messageSelected.id, "READ", undefined);
            setMessageSelected(newMessage);
          }
          
          // Fetch the message history
          const result = await fetchMessagesHistory(messageSelected.id);
          setHistory(result);
          setLoading(false);
        } catch (error) {
          setErrorMessage("Error fetching messages, please reload the page: " + error);
          setLoading(false);
          console.log(error);
          if (errorRef.current) {
              errorRef.current.scrollIntoView({ behavior: "smooth" });
          }
        }
      };
  
      // Call the async function inside useEffect
      fetchAndUpdateMessage()
  }, [])

  const handlePriorityChange = async () => {
    setLoading(true)
    try {
        // Await the updateMessage call
        const newMessage = await updateMessage(me, messageSelected.id, undefined, priority);
        
        // Update the state with the new message
        setMessageSelected(newMessage);
        setLoading(false)
      } catch (error) {
          setErrorMessage("Error updating messages, please reload the page: " + error);
          setLoading(false);
          console.log(error);
          if (errorRef.current) {
              errorRef.current.scrollIntoView({ behavior: "smooth" });
          }
      }
  }

  const handleChangeState = async () => {
    setLoading(true)
    try {
      // Await the updateMessage call
      const newMessage = await updateMessage(me, messageSelected.id, stateToAssign || undefined, priority);
      
      // Update the state with the new message
      setMessageSelected(newMessage);
      setLoading(false)
      setShowModal(false)
    } catch (error) {
        setErrorMessage("Error updating messages, please reload the page: " + error);
        setLoading(false);
        console.log(error);
        if (errorRef.current) {
            errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        setShowModal(false)
    }
  };
  return(
    <>
      <Container fluid>
        {errorMessage && (
          <Row className="justify-content-center" ref={errorRef}>
            <Col xs={12} md={10} lg={6}>
              <Alert variant="danger" onClose={() => setErrorMessage("")} className="d-flex mt-3 justify-content-center align-items-center" dismissible>
                  {errorMessage}
              </Alert>
            </Col>
          </Row>
        )}

        {loading && (
          <Row>
            <Col md={8}>
              <div className="loading-card"></div>
              <div className="loading-card"></div>
              <div className="loading-card"></div>
              <div className="loading-card"></div>
              <div className="loading-card"></div>
              <div className="loading-card"></div>
            </Col>
          </Row>
        )}
        {!loading &&
        <>
        <Row className="d-flex flex-row p-0 mb-3 align-items-center">
            <Col>
              <h3 className="title">Message</h3>
            </Col>
            <Col>
                {messageSelected.actualState === "READ" && <Button variant="warning" style={{ marginRight: '10px' }} onClick={() => {setStateToAssign("PROCESSING"); setShowModal(true)}}>Process</Button>}
                {(messageSelected.actualState === "READ" || messageSelected.actualState === "PROCESSING") && <Button variant="success" style={{ marginRight: '10px' }} onClick={() => {setStateToAssign("DONE"); setShowModal(true)}}>Done</Button>}
                {(messageSelected.actualState === "READ" || messageSelected.actualState === "PROCESSING") && <Button variant="danger" style={{ marginRight: '10px' }}  onClick={() => {setStateToAssign("FAILED"); setShowModal(true)}}>Fail</Button>}
                {messageSelected.actualState === "READ" && <Button variant="danger" style={{ marginRight: '10px' }} onClick={() => {setStateToAssign("DISCARDED"); setShowModal(true)}}>Discard</Button>}
            </Col>
        </Row>
        <div className="border rounded p-3 shadow-sm mt-4">
          <Row>
              <Col>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <h5 style={{ marginBottom: 0 }}>Sender: </h5>
                  <p style={{ marginLeft: '10px', marginBottom: 0 }}>{messageSelected.sender}</p>
                </div>
              </Col>
              <Col>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <h5 style={{ marginBottom: 0 }}>Date: </h5>
                  <p style={{ marginLeft: '10px', marginBottom: 0 }}>{formatArrayDate(messageSelected.date)}</p>
                </div>
              </Col>
          </Row>
          <Row style={{ marginTop: '30px' }}>
              <Col>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <h5 style={{ marginBottom: 0 }}>Subject: </h5>
                  <p style={{ marginLeft: '10px', marginBottom: 0 }}>{messageSelected.subject}</p>
                </div>
              </Col>
              {(messageSelected.actualState !== "DONE" && messageSelected.actualState !== "DONE" && messageSelected.actualState !== "FAILED" && messageSelected.priority !== "DISCARDED") &&
                <Col>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <h5 style={{ marginBottom: 0 }}>Priority: </h5>
                  <Form.Group controlId="priority">
                    <Form.Select
                      style={{ width: "auto" }}
                      name="priority"
                      value={priority}
                      onChange={(e) => {
                        setPriority(e.target.value)
                        handlePriorityChange()
                      }}
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM_LOW">Medium-low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="MEDIUM_HIGH">Medium-high</option>
                      <option value="HIGH">High</option>
                    </Form.Select>
                  </Form.Group>
                </div>
              </Col>}
          </Row>
        </div>
        <div className="border rounded p-3 shadow-sm mt-4" style={{minHeight: "250px"}}>
            <Row>
                <p>{messageSelected.body}</p>
            </Row>
        </div>

        <Row className="d-flex flex-row p-0 mb-3 align-items-center">
            <Col>
              <h3 className="title">Message History</h3>
            </Col>
        </Row>

        <div className="border rounded p-3 shadow-sm mt-4" style={{minHeight: "250px"}}>
            <Row>
                <Col>
                {history.length === 0 ? (
                  <Row className="w-100">
                    <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                      <h5 className="p-5">
                        No job offers found with the selected criteria. Try adjusting the filters, or it could be that no messages have been received yet.
                      </h5>
                    </Col>
                  </Row>
                ) : (
                    history.map((his) => (
                        <div key={his.id} className="user-item mb-4 p-3">
                            <Row className="align-item-center">
                                <Col md={2}>
                                  <div style={{ display: 'flex', alignItems: 'center' }}>
                                    <h5 style={{ marginBottom: 0 }}>Date: </h5>
                                    <p style={{ marginLeft: '10px', marginBottom: 0 }}>{formatArrayDate(his.date)}</p>
                                  </div>
                                </Col>
                                <Col md={2}>
                                  <div style={{ display: 'flex', alignItems: 'center' }}>
                                    <h5 style={{ marginBottom: 0 }}>State: </h5>
                                    <p style={{ marginLeft: '10px', marginBottom: 0 }}>{his.state}</p>
                                  </div>
                                </Col>
                            </Row>
                        </div>
                    ))
                )}
                </Col>
            </Row>
        </div>        
        
        </>}

        {/* Confirmation Modal */}
        <Modal show={showModal} onHide={() => setShowModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm the change of state</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to confirm the action? It can't be reversed
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={handleChangeState}>
                        Confirm
                    </Button>
                    <Button variant="secondary" onClick={() => setShowModal(false)}>
                        Cancel
                    </Button>
                </Modal.Footer>
            </Modal>
      </Container>
    </>
  );
}

export default MessageDetail