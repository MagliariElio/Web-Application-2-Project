import { useState } from "react";
import { Button, Col, Row } from "react-bootstrap";
import Form from 'react-bootstrap/Form';
import { BsXLg } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import { checkValidEmail, checkValidTelephone } from "../utils/checkers";

function AddCustomerPage() {

    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [surname, setSurname] = useState('');
    const [ssnCode, setSsnCode] = useState('');
    const [comment, setComment] = useState('');

    const [emails, setEmails] = useState<any[]>([]);
    const [singleEmailAddress, setSingleEmailAddress] = useState('');
    const [singleEmailAddressComment, setSingleEmailAddressComment] = useState('');
    const [emailError, setEmailError] = useState(false);

    const [telephones, setTelephones] = useState<any[]>([]);
    const [singleTelephoneNumber, setSingleTelephoneNumber] = useState('');
    const [singleTelephoneNumberComment, setSingleTelephoneNumberComment] = useState('');
    const [telephoneError, setTelephoneError] = useState(false);

    const [address, setAddress] = useState('');
    const [city, setCity] = useState('');
    const [region, setRegion] = useState('');
    const [state, setState] = useState('');
    const [addressComment, setAddressComment] = useState('');


    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        
        if(name === '' || surname === '') {
            return;
        }

        const customer = {
            name: name,
            surname: surname,
            ssnCode: ssnCode,
            comment: comment,
            category: "CUSTOMER",
            emails: emails,
            telephones: telephones,
            address: {
                address: address,
                city: city,
                region: region,
                state: state,
                comment: addressComment
            }
        };

        fetch("/crmService/v1/API/customers", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(customer)
        })
        .then(res => {
            if (!res.ok) {
                navigate("/ui/customers", { state: { success: false } });
                throw new Error('/API/customers : Network response was not ok');
                
            }
            else {
                navigate("/ui/customers", { state: { success: true } });
            }
            return res.json();
        })
        .catch((error) => {
            console.log(error);
        }
        );


    };

  return (
    <div>
        <Row className="d-flex flex-row p-0 mb-5 align-items-center">
            <Col>
                <h3>Add new customer</h3> 
            </Col>
            <Col className="d-flex justify-content-end">
                <Button className="d-flex align-items-center secondaryButton" onClick={() => navigate(-1) } >
                    <BsXLg size={"1.5em"} />
                </Button>
            </Col>
        </Row>

        <Form onSubmit={handleSubmit}>
            <Row>
                <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Control
                    placeholder="Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                />
                </Col>
                <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Control
                    placeholder="Surname"
                    value={surname}
                    onChange={(e) => setSurname(e.target.value)}
                    required
                />
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={6} lg={3} className="mb-4">
                <Form.Control
                    placeholder="Ssn code"
                    value={ssnCode}
                    onChange={(e) => setSsnCode(e.target.value)}
                />
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={12} lg={6} className="mb-4">
                    <Form.Control
                        as="textarea"
                        placeholder="Comments"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                    />
                </Col>
            </Row>
            <Row className="mt-5">
                <Col xs={12} md={12} lg={6} className="mb-2">
                    <Row className="align-items-center">
                        <Col>
                        <hr />
                        </Col>
                        <Col xs="auto">
                        <h5 className="fw-normal">Emails</h5>
                        </Col>
                        <Col>
                        <hr />
                        </Col>
                    </Row>
                </Col>
            </Row>
            {
                emails.length === 0 && 
                    <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                                            <p>No emails added yet</p>
                                        </Col>
                                        </Row>
            }
            {
                emails.length > 0 && emails.map((email, index) => {
                    return (
                    <Row className="mb-1 d-flex align-items-center">
                        <Col xs={8} md={6} lg={5}>
                            <Row>
                                <Col xs={12} md={12} lg={6} className="mb-0">
                                    <p className="text-truncate">{email.email}</p>
                                </Col>
                                <Col xs={12} md={12} lg={6} className="mb-0">
                                    <p className="text-truncate">{email.comment}</p>
                                </Col>
                            </Row>
                        </Col>
                        <Col xs={4} md={6} lg={1}>
                            <Col className="mb-0">
                                <Button className="secondaryDangerButton w-100" onClick={() => {
                                    setEmails(emails.filter((e, i) => i !== index));
                                }}>
                                    Remove
                                </Button>
                            </Col>
                        </Col>                                    
                    </Row>)
                })
            }
            <Row>
                <Col xs={12} md={12} lg={2} className="mb-2">
                    <Form.Control
                        placeholder="Email address"
                        value={singleEmailAddress}
                        onChange={(e) => {setSingleEmailAddress(e.target.value); setEmailError(false);}}
                    />
                </Col>
                <Col xs={12} md={12} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="Email address comment"
                        value={singleEmailAddressComment}
                        onChange={(e) => setSingleEmailAddressComment(e.target.value)}
                    />
                </Col>
                <Col xs={12} md={12} lg={1} className="mb-2">
                    <Button className="secondaryButton w-100" onClick={() => {
                        if(checkValidEmail(singleEmailAddress)) {
                            setEmails([...emails, {email: singleEmailAddress, comment: singleEmailAddressComment}]);
                            setSingleEmailAddress('');
                            setSingleEmailAddressComment('');
                            setEmailError(false);
                        }
                        else {
                            setEmailError(true);
                        }
                    }}>
                        Add email
                    </Button>
                </Col>
            </Row>
            {
                emailError && <Row>
                                <Col xs={12} md={12} lg={6} className="mb-4">
                                    <p className="text-danger">Invalid email address</p>
                                </Col>
                            </Row>
            }

            <Row className="mt-5">
                <Col xs={12} md={12} lg={6} className="mb-2">
                    <Row className="align-items-center">
                        <Col>
                        <hr />
                        </Col>
                        <Col xs="auto">
                        <h5 className="fw-normal">Telephones</h5>
                        </Col>
                        <Col>
                        <hr />
                        </Col>
                    </Row>
                </Col>
            </Row>
            {
                telephones.length === 0 && 
                    <Row>
                    <Col xs={12} md={12} lg={6} className="mb-0">
                                            <p>No phone numbers added yet</p>
                                        </Col>
                                        </Row>
            }
            {
                telephones.length > 0 && telephones.map((telephone, index) => {
                    return (
                    <Row key={index} className="mb-1 d-flex align-items-center">
                        <Col xs={8} md={6} lg={5}>
                            <Row>
                                <Col xs={12} md={12} lg={6} className="mb-0">
                                    <p className="text-truncate">{telephone.telephone}</p>
                                </Col>
                                <Col xs={12} md={12} lg={6} className="mb-0">
                                    <p className="text-truncate">{telephone.comment}</p>
                                </Col>
                            </Row>
                        </Col>
                        <Col xs={4} md={6} lg={1}>
                            <Col className="mb-0">
                                <Button className="secondaryDangerButton w-100" onClick={() => {
                                    setTelephones(telephones.filter((e, i) => i !== index));
                                }}>
                                    Remove
                                </Button>
                            </Col>
                        </Col>                                    
                    </Row>)
                })
            }
            <Row>
                <Col xs={12} md={12} lg={2} className="mb-2">
                    <Form.Control
                        placeholder="Telephone number"
                        value={singleTelephoneNumber}
                        onChange={(e) => {setSingleTelephoneNumber(e.target.value); setTelephoneError(false);}}
                    />
                </Col>
                <Col xs={12} md={12} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="Telephone number comment"
                        value={singleTelephoneNumberComment}
                        onChange={(e) => setSingleTelephoneNumberComment(e.target.value)}
                    />
                </Col>
                <Col xs={12} md={12} lg={1} className="mb-2">
                    <Button className="secondaryButton w-100 px-0" onClick={() => {
                        if(checkValidTelephone(singleTelephoneNumber)) {
                            setTelephones([...telephones, {telephone: singleTelephoneNumber, comment: singleTelephoneNumberComment}]);
                            setSingleTelephoneNumber('');
                            setSingleTelephoneNumberComment('');
                            setTelephoneError(false);
                        }
                        else {
                            setTelephoneError(true);
                        }
                    }}>
                        Add number
                    </Button>
                </Col>
            </Row>
            {
                telephoneError && <Row>
                                <Col xs={12} md={12} lg={6} className="mb-4">
                                    <p className="text-danger">Invalid telephone number</p>
                                </Col>
                            </Row>
            }

            <Row className="mt-5">
                <Col xs={12} md={12} lg={6} className="mb-2">
                    <Row className="align-items-center">
                        <Col>
                        <hr />
                        </Col>
                        <Col xs="auto">
                        <h5 className="fw-normal">Address</h5>
                        </Col>
                        <Col>
                        <hr />
                        </Col>
                    </Row>
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={6} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="Address"
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                    />
                </Col>
                <Col xs={12} md={6} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="City"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                    />
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={6} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="Region"
                        value={region}
                        onChange={(e) => setRegion(e.target.value)}
                    />
                </Col>
                <Col xs={12} md={6} lg={3} className="mb-2">
                    <Form.Control
                        placeholder="State"
                        value={state}
                        onChange={(e) => setState(e.target.value)}
                    />
                </Col>
            </Row>
            <Row>
                <Col xs={12} md={12} lg={6} className="mb-2">
                    <Form.Control
                        as="textarea"
                        placeholder="Address comment"
                        value={addressComment}
                        onChange={(e) => setAddressComment(e.target.value)}
                    />
                </Col>
            </Row>

            <Row className="mt-5">
              <Col xs={12} md={12} lg={6} className="d-flex flex-column justify-content-center align-items-center">
                <Button type="submit" className="primaryButton">
                  Save customer
                </Button>
              </Col>
            </Row>
            
        </Form>

    </div>
  );
}

export default AddCustomerPage;