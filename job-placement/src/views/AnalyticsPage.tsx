import { Col, Row, Form } from "react-bootstrap";
import { MeInterface } from "../interfaces/MeInterface";
import { useEffect, useState } from "react";
import { fetchJobOffersAnalytics, fetchJobOffersPerMonthAnalytics, fetchMessagesAnalytics, fetchMessagesPerMonthAnalytics, fetchProfessionalsAnalytics } from "../apis/AnalyticsRequests";
import { MessageAnalytics } from "../interfaces/MessageAnalytics";
import { useNavigate } from "react-router-dom";
import { PieChart } from '@mui/x-charts/PieChart';
import { JobOfferAnalytics } from "../interfaces/JobOfferAnalytics";
import Box from '@mui/material/Box';
import { BarChart } from "@mui/x-charts";
import { MessagesPerMonth } from "../interfaces/MessagesPerMonth";
import { ProfessionalAnalytics } from "../interfaces/ProfessionalAnalytics";
import { RoleState } from "../utils/costants";

const AnalyticsPage = ({ me }: { me: MeInterface }) => {
    const navigate = useNavigate();
    const [dataToDisplay, setDataToDisplay] = useState("messages")
    const [yearToDisplay, setYearToDisplay] = useState(2024)
    const [yearToDisplayJobOffers, setYearToDisplayJobOffers] = useState(2024)
    const [error, setError] = useState(false);
    
    const [loading, setLoading] = useState(true)
    const [messagesAnalytics, setMessagesAnalytics] = useState<MessageAnalytics>({
      totalMessages: 0,
      receivedMessages: 0,
      readMessages: 0,
      discardedMessages: 0,
      processingMessages: 0,
      doneMessages: 0,
      failedMessages: 0
    });
    
    const [completedMessagesPerMonth, setCompletedMessagesPerMonth] = useState<MessagesPerMonth>({
      january: 0,
      february: 0,
      march: 0,
      april: 0,
      may: 0,
      june: 0,
      july: 0,
      august: 0,
      september: 0,
      october: 0,
      november: 0,
      december: 0
    })

    const [jobOfferAnalytics, setJobOfferAnalytics] = useState<JobOfferAnalytics>({
      totalJobOffers: 0,
      createdJobOffers: 0,
      selectionPhaseJobOffers: 0,
      candidateProposalJobOffers: 0,
      consolidatedJobOffers: 0,
      doneJobOffers: 0,
      abortJobOffers: 0,
      fullTimeCouner: 0,
      partTimeCounter: 0,
      contractCounter:0,
      freelanceCounter: 0,
      remoteCounter: 0,
      hybridCounter: 0,
      inPersonCounter: 0,
      AvarageJobOfferCompletionTime: 0
    });

    const [completedJobOffersPerMonth, setCompletedJobOffersPerMonth] = useState<MessagesPerMonth>({
      january: 0,
      february: 0,
      march: 0,
      april: 0,
      may: 0,
      june: 0,
      july: 0,
      august: 0,
      september: 0,
      october: 0,
      november: 0,
      december: 0
    })

    const [professionalAnalytics, setProfessionalAnalytics] = useState<ProfessionalAnalytics>({
      employedProfessional: 0,
      unemployedProfessional: 0,
      availableForWorkProfessional: 0,
      notAvailableProfessional: 0
    });
      

    useEffect(() => {
      const fetchAllData = async () => {
        try {
          setLoading(true); // Start loading
          
          // Perform all API calls concurrently
          const [
            messagesAnalyticsData,
            jobOfferAnalyticsData,
            messagesPerMonthData,
            jobOffersPerMonthData, 
            professionalAnalyticsData
          ] = await Promise.all([
            fetchMessagesAnalytics(me),
            fetchJobOffersAnalytics(me),
            fetchMessagesPerMonthAnalytics(yearToDisplay, me),
            fetchJobOffersPerMonthAnalytics(yearToDisplayJobOffers, me),
            fetchProfessionalsAnalytics(me)
          ]);
    
          // Set the responses to state
          setMessagesAnalytics(messagesAnalyticsData);
          setJobOfferAnalytics(jobOfferAnalyticsData);
          setCompletedMessagesPerMonth(messagesPerMonthData);
          setCompletedJobOffersPerMonth(jobOffersPerMonthData);
          setProfessionalAnalytics(professionalAnalyticsData);
    
          console.log("Completed messages per month fetched: ", messagesPerMonthData);
          console.log("Completed job offers per month fetched: ", jobOffersPerMonthData);
          console.log("JobOffers created: " + jobOfferAnalytics.createdJobOffers)
        } catch (error) {
          console.error("Error:", error);
          setLoading(false); // Stop loading if error
          setError(true); // Set error state
          navigate("/not-found"); // Redirect in case of error
          throw new Error("Network response was not ok");
        } finally {
          setLoading(false); // Stop loading after data is fetched
        }
      };
    
      if(me.role !== RoleState.GUEST){
        fetchAllData(); // Invoke the async function
      }
      
    }, []); 

    function formatDuration(minutes: number): string {
      const days = Math.floor(minutes / (24 * 60));  // 1 day = 24 * 60 minutes
      const hours = Math.floor((minutes % (24 * 60)) / 60);  // remainder from days to hours
      const mins = minutes % 60;  // remainder from hours to minutes
  
      let result = '';
  
      if (days > 0) {
          result += `${days} day${days > 1 ? 's' : ''} `;
      }
  
      if (hours > 0) {
          result += `${hours} hour${hours > 1 ? 's' : ''} `;
      }
  
      result += `${mins} minute${mins !== 1 ? 's' : ''}`;
  
      return result.trim();  // Remove trailing spaces if any
    }

    return(
    <>
    {me.role === RoleState.GUEST && <div className="w-100">
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
            <h5>Only manager and operators can access this content</h5>
          </Col>
        </Row>
      </div>}
    {(me.role === RoleState.MANAGER || me.role === RoleState.OPERATOR) && <div className="w-100">
      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col md={9}>
          <h3 className="title">Analytics</h3>
        </Col> 
        <Col md={1} className="d-flex justify-content-end">
          <Form.Group controlId="dataToDisplay">
            <Form.Select
              style={{ width: "auto" }}
              name="dataset"
              value={dataToDisplay}
              onChange={(e) => {
                setDataToDisplay(e.target.value)
              }}
            >
              <option value="messages">Messages</option>
              <option value="job offers">Job offers</option>
              <option value="professionals">Professionals</option>
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      {error && (
        <Row className="w-100">
          <Col className="w-100 d-flex justify-content-center align-items-center mt-5 text-danger">
            <h5>An error occurred. Please, reload the page!</h5>
          </Col>
        </Row>
      )}

      {loading && (
        <Row>
          <Col md={12}>
            <div className="loading-card"></div>
            <div className="loading-card"></div>
          </Col>
        </Row>
      )}

      {!loading && (
        <>
        {dataToDisplay === "messages" && 
        <>
        <Row>
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <h5>Number of messages in the different states</h5>
              <PieChart
                series={[
                  {
                    data: [
                      { id: 0, value: messagesAnalytics.receivedMessages, label: 'Received' },
                      { id: 1, value: messagesAnalytics.readMessages, label: 'Read' },
                      { id: 2, value: messagesAnalytics.processingMessages, label: 'Processing' },
                      { id: 3, value: messagesAnalytics.doneMessages, label: 'Done' },
                      { id: 4, value: messagesAnalytics.failedMessages, label: 'Failed' },
                      { id: 5, value: messagesAnalytics.discardedMessages, label: 'Discarded' }
                    ],
                  },
                ]}
                width={400}
                height={200}
              />
            </Box>            
          </Col> 
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <Row className="d-flex flex-row p-0 mb-3 align-items-center">
                <Col md={9}>
                  <h5>Completed messages per month</h5>
                </Col> 
                <Col md={1} className="d-flex justify-content-end">
                  <Form.Group controlId="yearToDisplay">
                    <Form.Select
                      style={{ width: "auto" }}
                      name="yearToDisplay"
                      value={yearToDisplay}
                      onChange={(e) => {
                        setYearToDisplay(parseInt(e.target.value))

                        fetchMessagesPerMonthAnalytics(parseInt(e.target.value), me)
                        .then((result) => {
                          console.log("Completed messages per month fetched: " + result)
                          setCompletedMessagesPerMonth(result)
                        })
                        .catch((error) => {
                          setError(true);
                          console.log(error);
                          throw new Error(
                            "Network response was not ok"
                          );
                        })
                      }}
                    >
                      <option value="2024">2024</option>
                      <option value="2025">2025</option>
                      <option value="2026">2026</option>
                    </Form.Select>
                  </Form.Group>
                </Col>
              </Row>
              <BarChart
                xAxis={[{ scaleType: 'band', data: [
                  'Jan', 'Feb', 'Mar', 'Apr',
                   'May', 'June', 'July', 'Aug',
                    'Sept', 'Oct', 'Nov', 'Dec'] 
                  }]}
                series={[{ data: [
                  completedMessagesPerMonth.january, 
                  completedMessagesPerMonth.february, 
                  completedMessagesPerMonth.march, 
                  completedMessagesPerMonth.april, 
                  completedMessagesPerMonth.may, 
                  completedMessagesPerMonth.june, 
                  completedMessagesPerMonth.july, 
                  completedMessagesPerMonth.august, 
                  completedMessagesPerMonth.september, 
                  completedMessagesPerMonth.october, 
                  completedMessagesPerMonth.november, 
                  completedMessagesPerMonth.december
                ] },]}
                width={500}
                height={200}
              />
            </Box>            
          </Col>        
        </Row>
        </>
        }
        {dataToDisplay === "job offers" && 
        <>
        <Row>
          <h5>Total number of job offers: {jobOfferAnalytics.totalJobOffers}</h5>
        </Row>
        <Row>
          <h5>Avarage completition time for completed job offers: {formatDuration(jobOfferAnalytics.AvarageJobOfferCompletionTime)}</h5>
        </Row>
        <Row>
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <h5>Number of job offers in the different states</h5>
              <PieChart
                series={[
                  {
                    data: [
                      { id: 0, value: jobOfferAnalytics.createdJobOffers, label: 'Created' },
                      { id: 1, value: jobOfferAnalytics.selectionPhaseJobOffers, label: 'Selection Phase' },
                      { id: 2, value: jobOfferAnalytics.candidateProposalJobOffers, label: 'Candidate Proposal' },
                      { id: 3, value: jobOfferAnalytics.consolidatedJobOffers, label: 'Consolidated' },
                      { id: 4, value: jobOfferAnalytics.doneJobOffers, label: 'Done' },
                      { id: 5, value: jobOfferAnalytics.abortJobOffers, label: 'Abort' }
                    ],
                  },
                ]}
                width={500}
                height={200}
              />
            </Box>            
          </Col> 
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <Row className="d-flex flex-row p-0 mb-3 align-items-center">
                <Col md={9}>
                  <h5>Completed job offers per month</h5>
                </Col> 
                <Col md={1} className="d-flex justify-content-end">
                  <Form.Group controlId="yearToDisplayJobOffers">
                    <Form.Select
                      style={{ width: "auto" }}
                      name="yearToDisplayJobOffers"
                      value={yearToDisplayJobOffers}
                      onChange={(e) => {
                        setYearToDisplayJobOffers(parseInt(e.target.value))

                        fetchJobOffersPerMonthAnalytics(parseInt(e.target.value), me)
                        .then((result) => {
                          console.log("Completed job offers per month fetched: " + result)
                          setCompletedJobOffersPerMonth(result)
                        })
                        .catch((error) => {
                          setError(true);
                          console.log(error);
                          throw new Error(
                            "Network response was not ok"
                          );
                        })
                      }}
                    >
                      <option value="2024">2024</option>
                      <option value="2025">2025</option>
                      <option value="2026">2026</option>
                    </Form.Select>
                  </Form.Group>
                </Col>
              </Row>
              <BarChart
                xAxis={[{ scaleType: 'band', data: [
                  'Jan', 'Feb', 'Mar', 'Apr',
                   'May', 'June', 'July', 'Aug',
                    'Sept', 'Oct', 'Nov', 'Dec'] 
                  }]}
                series={[{ data: [
                  completedJobOffersPerMonth.january, 
                  completedJobOffersPerMonth.february, 
                  completedJobOffersPerMonth.march, 
                  completedJobOffersPerMonth.april, 
                  completedJobOffersPerMonth.may, 
                  completedJobOffersPerMonth.june, 
                  completedJobOffersPerMonth.july, 
                  completedJobOffersPerMonth.august, 
                  completedJobOffersPerMonth.september, 
                  completedJobOffersPerMonth.october, 
                  completedJobOffersPerMonth.november, 
                  completedJobOffersPerMonth.december
                ] },]}
                width={500}
                height={200}
              />
            </Box>            
          </Col> 
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <h5>Number of job offers with differnt contract types</h5>
              <PieChart
                series={[
                  {
                    data: [
                      { id: 0, value: jobOfferAnalytics.fullTimeCouner, label: 'Full time' },
                      { id: 1, value: jobOfferAnalytics.partTimeCounter, label: 'Partr time' },
                      { id: 2, value: jobOfferAnalytics.contractCounter, label: 'Contract' },
                      { id: 3, value: jobOfferAnalytics.freelanceCounter, label: 'Freelance' }
                    ],
                  },
                ]}
                width={500}
                height={200}
              />
            </Box>            
          </Col>
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <h5>Number of job offers with differet working mode</h5>
              <PieChart
                series={[
                  {
                    data: [
                      { id: 0, value: jobOfferAnalytics.remoteCounter, label: 'Remote' },
                      { id: 1, value: jobOfferAnalytics.hybridCounter, label: 'Hybrid' },
                      { id: 2, value: jobOfferAnalytics.inPersonCounter, label: 'In-Person' },
                    ],
                  },
                ]}
                width={500}
                height={200}
              />
            </Box>            
          </Col>       
        </Row>
        </>
        }
        {dataToDisplay === "professionals" && 
        <>
        <Row>
          <Col className="w-100 h-100 justify-content-center align-items-center mt-5">
            <Box flexGrow={1}>
              <h5>Number of professionals in the different states</h5>
              <PieChart
                series={[
                  {
                    data: [
                      { id: 0, value: professionalAnalytics.employedProfessional, label: 'Employed' },
                      { id: 1, value: professionalAnalytics.unemployedProfessional, label: 'Unemployed' },
                      { id: 2, value: professionalAnalytics.availableForWorkProfessional, label: 'Available for work' },
                      { id: 3, value: professionalAnalytics.notAvailableProfessional, label: 'Unavailable for work' },
                    ],
                  },
                ]}
                width={500}
                height={200}
              />
            </Box>            
          </Col>       
        </Row>
        </>
        }
        </>
      )}
    </div>}
    </>
    )
}

export default AnalyticsPage;