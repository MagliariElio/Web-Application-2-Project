import { useState, useRef, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { MeInterface } from "../interfaces/MeInterface";
import { PagedResponse } from "../interfaces/PagedResponse";
import { Message } from "../interfaces/Message";
import { Container, Row, Col, Button, Toast, ToastContainer, Form, Alert, Pagination, ButtonGroup } from "react-bootstrap";
import { BsCalendarFill , BsEnvelopeFill  , BsSearch } from "react-icons/bs";
import { contractTypeList, messageStateTypeList, RoleState, toTitleCase } from "../utils/costants";
import { fetchMessages } from "../apis/MessagesRequests";

const MessagesPage: React.FC<{ me: MeInterface }> = ({ me }) => {
  const navigate = useNavigate();

  const [messages, setMessages] = useState<PagedResponse<Message> | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const errorRef = useRef<HTMLDivElement | null>(null);

  const location = useLocation();
  var { success } = location.state || {};
  const [showAlert, setShowAlert] = useState(false);

  const [pageSize, setPageSize] = useState(10);

  const [filters, setFilters] = useState({
    state: "",
  });

  var presentedMessages = messages?.content || [];

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    console.log(name + " " + value);
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const changePage = (page: number) => {
    if (messages?.totalPages && page >= messages?.totalPages) page = messages?.totalPages - 1;
    if (page < 0) page = 0;

    setLoading(true);
    fetchMessages(page, pageSize, filters.state)
      .then((result) => {
        console.log("Messages fetched: ", result);
        setMessages(result);
        presentedMessages = result.content;
        setLoading(false);
      })
      .catch((error) => {
        setErrorMessage("Error fetching messages, please reload the page");
        setLoading(false);
        console.log(error);
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        throw new Error("GET /API/messages : Network response was not ok");
      });
  };

  useEffect(() => {
    fetchMessages(0, pageSize, filters.state)
      .then((result) => {
        setMessages(result);
        presentedMessages = result.content;
        setLoading(false);
      })
      .catch((error) => {
        setErrorMessage("Error fetching messages, please reload the page: " + error);
        setLoading(false);
        console.log(error);
        if (errorRef.current) {
          errorRef.current.scrollIntoView({ behavior: "smooth" });
        }
        throw new Error("GET /API/messages : Network response was not ok");
      });
  }, []);

  return (
    <Container fluid>
      {showAlert && (
        <ToastContainer position="top-end" className="p-3">
          <Toast bg={success ? "success" : "danger"} show={success != null} onClose={() => (success = null)}>
            <Toast.Header>
              <img src="holder.js/20x20?text=%20" className="rounded me-2" alt="" />
              <strong className="me-auto">JobConnect</strong>
              <small>now</small>
            </Toast.Header>
            <Toast.Body>{success ? "Operation correctly executed!" : "Operation failed!"}</Toast.Body>
          </Toast>
        </ToastContainer>
      )}

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col md={10}>
          <h3 className="title">Messages</h3>
        </Col>
        <Col md={2} className="d-flex justify-content-end">
          <Form.Group controlId="elementsPerPage">
            <Form.Select
              style={{ width: "auto" }}
              name="pageSize"
              value={pageSize}
              onChange={(e) => {
                setPageSize(parseInt(e.target.value));

                fetchMessages(0, parseInt(e.target.value), filters.state)
                  .then((result) => {
                    console.log("Messages fetched: ", result);
                    setMessages(result);
                    presentedMessages = result.content;
                    setLoading(false);
                  })
                  .catch((error) => {
                    setErrorMessage("Error fetching messages, please reload the page");
                    setLoading(false);
                    console.log(error);
                    if (errorRef.current) {
                      errorRef.current.scrollIntoView({ behavior: "smooth" });
                    }
                    throw new Error("GET /API/messages : Network response was not ok");
                  });
              }}
            >
              <option value="10">10 messages</option>
              <option value="20">20 messages</option>
              <option value="50">50 messages</option>
              <option value="100">100 messages</option>
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

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
          <Col md={4}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!loading && messages !== null && (
        <div>
          <Row>
            <Col xs={12} lg={7} xl={8} className="order-2 order-lg-1">
              {messages?.content.length === 0 ? (
                <Row className="w-100">
                  <Col className="w-100 d-flex flex-column justify-content-center align-items-center mt-5">
                    <h5 className="p-3 text-center">No messages found with the selected criteria.</h5>
                    <h5 className="p-3 text-center">Try adjusting the filters, or it could be that no messages have been added yet.</h5>
                  </Col>
                </Row>
              ) : (
                messages?.content
                .sort((a, b) => {
                  const dateA = new Date(a.date[0], a.date[1] - 1, a.date[2], a.date[3], a.date[4], a.date[5]); // Create Date from array
                  const dateB = new Date(b.date[0], b.date[1] - 1, b.date[2], b.date[3], b.date[4], b.date[5]); // Create Date from array
                  return dateB.getTime() - dateA.getTime();
                })
                .map((message) => (
                  <div
                    key={message.id}
                    className="job-offer-item mb-4 p-3"
                    onClick={() =>
                      navigate(`/ui/messages/${message.id}`, {
                        state: { message: message },
                      })
                    }
                  >
                    <Row className="align-items-center">
                      <Col md={8}>
                        <Row className="mb-2">
                          <Col>
                            <p><BsEnvelopeFill /><> {message.sender}</></p>
                          </Col>
                          <Col>
                          <p><BsCalendarFill /><> {formatArrayDate(message.date)}</></p>
                          </Col>
                        </Row>
                        <Row>
                          <Col xs={6}>
                            <p><>{message.subject}</></p>
                          </Col>
                          <Col xs={2}>
                            <span className={`status ${message.actualState.toLowerCase()}`}>{toTitleCase(message.actualState).toLocaleUpperCase()}</span>
                          </Col>
                        </Row>
                      </Col>
                    </Row>
                  </div>
                ))
              )}

              {/* Pagination */}
              {messages?.content.length > 0 && (
                <Row className="mt-auto">
                  <Col className="d-flex justify-content-center mt-4 custom-pagination">
                    <Pagination>
                      <Pagination.First onClick={() => changePage(0)} disabled={messages.currentPage === 0} />
                      <Pagination.Prev onClick={() => changePage(messages.currentPage - 1)} disabled={messages.currentPage === 0} />

                      {Array.from({ length: Math.min(5, messages.totalPages) }, (_, index) => {
                        const startPage = Math.max(Math.min(messages.currentPage - 2, messages.totalPages - 5), 0);
                        const actualPage = startPage + index;

                        return (
                          <Pagination.Item key={actualPage} active={actualPage === messages.currentPage} onClick={() => changePage(actualPage)}>
                            {actualPage + 1}
                          </Pagination.Item>
                        );
                      })}

                      <Pagination.Next
                        onClick={() => changePage(messages.currentPage + 1)}
                        disabled={messages.currentPage + 1 === messages.totalPages}
                      />
                      <Pagination.Last
                        onClick={() => changePage(messages.totalPages - 1)}
                        disabled={messages.currentPage + 1 === messages.totalPages}
                      />
                    </Pagination>
                  </Col>
                </Row>
              )}
            </Col>

            <Col xs={12} lg={5} xl={4} className="order-1 order-lg-2">
              <div className="sidebar-search p-4 w-100">
                <h5>Filter Messages</h5>
                <Form>
                  <Form.Group controlId="state" className="mb-3">
                    <Form.Label>State</Form.Label>
                    <Form.Control as="select" name="state" value={filters.state} onChange={handleFilterChange}>
                      <option value={""}>All</option>
                      {messageStateTypeList.map((contract, index) => (
                        <option key={index} value={contract}>
                          {toTitleCase(contract)}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>

                  <ButtonGroup className="d-flex justify-content-center mt-4">
                    <Col className="text-center">
                      <Button
                        className="primaryButton"
                        variant="primary"
                        onClick={() => {
                          setLoading(true);
                          fetchMessages(0, pageSize, filters.state)
                            .then((result) => {
                              console.log("Messages fetched: ", result);
                              setMessages(result);
                              setLoading(false);
                            })
                            .catch((error) => {
                              setErrorMessage("Error fetching the filtered messages");
                              setLoading(false);
                              console.log(error);
                              throw new Error("GET /API/messages : Network response was not ok");
                            });
                        }}
                      >
                        <BsSearch className="me-1" />
                        Filter
                      </Button>
                    </Col>
                    <Col className="text-center">
                      <Button
                        className="secondaryButton"
                        variant="primary"
                        onClick={() => {
                          setFilters({
                            state: "",
                          })
                          fetchMessages(0, pageSize, "")
                            .then((result) => {
                              console.log("Messages fetched: ", result);
                              setMessages(result);
                              setLoading(false);
                            })
                            .catch((error) => {
                              setErrorMessage("Error fetching the filtered messages");
                              setLoading(false);
                              console.log(error);
                              throw new Error("GET /API/messages : Network response was not ok");
                            });
                        }}
                      >
                        Clear Filters
                      </Button>
                    </Col>
                  </ButtonGroup>
                </Form>
              </div>
            </Col>
          </Row>
        </div>
      )}
    </Container>
  );
};

export function formatArrayDate(dateArray: number[]): string {
  const [year, month, day, hour, minute, second] = dateArray;

  // Create a Date object (month - 1 because JavaScript months are 0-indexed)
  const date = new Date(year, month - 1, day, hour, minute, second);

  // Format the Date object as DD/MM/YYYY HH:MM
  const dayFormatted = String(date.getDate()).padStart(2, "0");
  const monthFormatted = String(date.getMonth() + 1).padStart(2, "0"); // Add 1 because months are 0-indexed
  const yearFormatted = date.getFullYear();
  const hoursFormatted = String(date.getHours()).padStart(2, "0");
  const minutesFormatted = String(date.getMinutes()).padStart(2, "0");

  return `${dayFormatted}/${monthFormatted}/${yearFormatted} ${hoursFormatted}:${minutesFormatted}`;
}

export default MessagesPage;
