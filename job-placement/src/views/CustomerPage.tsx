import { useEffect, useState } from "react";
import { Button, Col, Modal, Row, Toast, ToastContainer } from "react-bootstrap";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Customer } from "../interfaces/Customer";
import { BsChevronDown, BsChevronUp, BsPencilSquare, BsPlus, BsTrash } from "react-icons/bs";
import { MeInterface } from "../interfaces/MeInterface";
import { deleteCustomer, fetchCustomer } from "../apis/CustomerRequests";

function CustomerPage({ me }: { me: MeInterface }) {
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

  const [customer, setCustomer] = useState<Customer | null>(null);

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

    
      fetchCustomer(Number.parseInt(id))
      .then((json) => {
        setCustomer(json);
        console.log("Customer:  ", json);
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
          <Modal.Title>Delete customer</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <p className="text-center">Are you sure you want to permanently delete customer</p>
            <p className="text-center fs-3 fw-semibold">{`${customer?.information.contactDTO.name} ${customer?.information.contactDTO.surname}?`} </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => handleCloseDeleteModal()}>
            Close
          </Button>
          <Button variant="danger" onClick={
            () => {
              deleteCustomer(Number.parseInt(id!!), me)
                .then((json) => {
                  console.log("Deleted customer: ", json);
                  navigate("/ui/customers", { state: { success: true } });
                })
                .catch((error) => {
                  console.error("Error:", error);
                  navigate("/ui/customers", { state: { success: false } });
                });
            }
          }>
            Delete
          </Button>
        </Modal.Footer>
      </Modal>
      

      <Row className="d-flex flex-row p-0 mb-3 align-items-center">
        <Col>
          <h3>Customer</h3>
        </Col>
        <Col className="d-flex justify-content-end">
          <Button
            className="d-flex align-items-center primaryButton me-4"
            onClick={() => navigate(`/ui/customers/${id}/edit`)}
          >
            <BsPencilSquare size={"1em"} className="me-2" />
            Edit Customer
          </Button>
          <Button
            className="d-flex align-items-center primaryDangerButton me-4"
            onClick={() => setShowDeleteModal(true)}
          >
            <BsTrash size={"1em"} className="me-2" />
            Delete Customer
          </Button>
        </Col>
      </Row>

      <Row className="w-100 borderedSection heigth-transition">
      <Row className="d-flex mb-2">
        <h4 className="p-0 m-0 d-flex align-items-center">
          {`${customer?.information.contactDTO.name} ${customer?.information.contactDTO.surname}`} 
          <span className="fw-light fs-6 ms-2">({customer?.information.contactDTO.ssnCode})</span>
        </h4>
      </Row>
      <Row className="d-flex">
        <p className="p-0 m-0 fs-6 fw-light">{customer?.information.contactDTO.comment}</p>
      </Row>

      {
        expandedInfoSection && (<>
          <Row className="d-flex flex-column mt-3">
            <Row className="d-flex">
              <h6 className="p-0 m-0">Emails</h6>
            </Row>
            {
              customer?.information.emailDTOs.map((email, index) => (
                <Row key={index} className="d-flex">
                  <p className="p-0 m-0 fs-6">{email.email} </p>
                  <small className="m-0 p-0">{email.comment}</small>
                </Row>
              ))
            }
            {
              customer?.information.emailDTOs.length === 0 && (
                <Row className="d-flex">
                  <p className="p-0 m-0 fs-6">No emails found</p>
                </Row>  
              )
            }
          </Row>

          <Row className="d-flex flex-column mt-3">
            <Row className="d-flex">
              <h6 className="p-0 m-0">Phones</h6>
            </Row>
            {
              customer?.information.telephoneDTOs.map((phone, index) => (
                <Row key={index} className="d-flex">
                  <p className="p-0 m-0 fs-6">{phone.telephone} </p>
                  <small className="m-0 p-0">{phone.comment}</small>
                </Row>
              ))
            }
            {
              customer?.information.telephoneDTOs.length === 0 && (
                <Row className="d-flex">
                  <p className="p-0 m-0 fs-6">No phones found</p>
                </Row>  
              )
            }
          </Row>

          <Row className="d-flex flex-column mt-3">
            <Row className="d-flex">
              <h6 className="p-0 m-0">Addresses</h6>
            </Row>
            {
              customer?.information.addressDTOs.map((address, index) => (
                <Row key={index} className="d-flex">
                  <p className="p-0 m-0 fs-6">{address.address} </p>
                  <small className="m-0 p-0">{address.comment}</small>
                </Row>
              ))
            }
            {
              customer?.information.addressDTOs.length === 0 && (
                <Row className="d-flex">
                  <p className="p-0 m-0 fs-6">No addresses found</p>
                </Row>  
              )
            }
          </Row>
          </>
        )
      }


      <Row className="d-flex justify-content-center">
        <Col className="d-flex justify-content-center align-items-center">
          <div className="text-center fs-6 text-muted"
          style={{cursor: "pointer"}}
          onClick={ 
            () => setExpandedInfoSection(!expandedInfoSection)
           }>
            {
              expandedInfoSection ? <>
                <BsChevronUp size={"1em"} />
                <p className="m-0">Hide details</p>
                </> : <>
                <p className="m-0">Show details</p>
                <BsChevronDown size={"1em"} />
                </>
            }
          </div>
        </Col>
      </Row>
    </Row>

    </div>
  );
}

export default CustomerPage;
