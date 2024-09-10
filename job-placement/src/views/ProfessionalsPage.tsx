import { useEffect, useState } from "react";
import {
  Button,
  ButtonGroup,
  Col,
  Form,
  Pagination,
  Row,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { BsPlus, BsSearch } from "react-icons/bs";
import { PagedResponse } from "../interfaces/PagedResponse";
import { useLocation, useNavigate } from "react-router-dom";
import { Professional } from "../interfaces/Professional";
import { fetchProfessionals } from "../apis/ProfessionalRequests";
function ProfessionalsPage() {
  const navigate = useNavigate();

  const [professionals, setProfessionals] =
    useState<PagedResponse<Professional> | null>(null);
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);

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

    fetchProfessionals(0)
      .then((result) => {
        console.log("professionals fetched: ", result);
        setProfessionals(result);
        setLoading(false);
      })
      .catch((error) => {
        setError(true);
        console.log(error);
        setLoading(false);
        throw new Error("GET /API/professionals : Network response was not ok");
      });
  }, []);

  const [filters, setFilters] = useState({
    skill: "",
    geographicalLocation: "",
    employmentState: "",
  });

  const [activeFilters, setActiveFilters] = useState({
    skill: "",
    geographicalLocation: "",
    employmentState: "",
  });

  const handleFilterChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setFilters((prevFilters) => ({
      ...prevFilters,
      [name]: value,
    }));
  };

  const [sortCriteria, setSortCriteria] = useState("asc_name");

  var presentedProfessionals = professionals?.content || [];

  const [pageSize, setPageSize] = useState(10);

  const changePage = (page: number) => {
    if (professionals?.totalPages && page >= professionals?.totalPages)
      page = professionals?.totalPages - 1;
    if (page < 0) page = 0;

    setLoading(true);
    fetchProfessionals(
      page,
      pageSize,
      filters.skill,
      filters.geographicalLocation,
      filters.employmentState
    )
      .then((result) => {
        console.log("Professionals fetched: ", result);
        setProfessionals(result);
        presentedProfessionals = result.content;
        setLoading(false);
      })
      .catch((error) => {
        setError(true);
        setLoading(false);
        console.log(error);
        throw new Error("GET /API/professionals : Network response was not ok");
      });
  };

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

      <Row className="d-flex flex-row p-0 mb-1 align-items-center">
        <Col md={8}>
          <h3 className="title">Professionals</h3>
        </Col>
        <Col md={2} className="d-flex justify-content-end">
          <Form.Group controlId="elementsPerPage">
            <Form.Select
              style={{ width: "auto" }}
              name="pageSize"
              value={pageSize}
              onChange={(e) => {
                setPageSize(parseInt(e.target.value));

                fetchProfessionals(
                  0,
                  parseInt(e.target.value),
                  filters.skill,
                  filters.geographicalLocation,
                  filters.employmentState
                )
                  .then((result) => {
                    console.log("Professionals fetched: ", result);
                    setProfessionals(result);
                    presentedProfessionals = result.content;
                    setLoading(false);
                  })
                  .catch((error) => {
                    setError(true);
                    setLoading(false);
                    console.log(error);
                    throw new Error(
                      "GET /API/professionals : Network response was not ok"
                    );
                  });
              }}
            >
              <option value="10">10 professionals</option>
              <option value="20">20 professionals</option>
              <option value="50">50 professionals</option>
              <option value="100">100 professionals</option>
            </Form.Select>
          </Form.Group>
        </Col>
        <Col md={2} className="d-flex justify-content-end">
          <Button
            className="d-flex align-items-center primaryButton me-4"
            onClick={() => navigate("/ui/professionals/add")}
          >
            <BsPlus size={"1.5em"} className="me-1" />
            Add Professional
          </Button>
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

      {!error && !loading && professionals !== null && (
        <>
          <Row className="w-100 d-flex justify-content-center">
            <Col xs={12} lg={4} className="order-1 order-lg-2 mt-3">
              <div className="sidebar-search p-4">
                <h5>Filter Professionals</h5>
                <Form>
                  <Form.Group controlId="skill" className="mb-3">
                    <Form.Label>Skill</Form.Label>
                    <Form.Control
                      type="text"
                      name="skill"
                      value={filters.skill}
                      onChange={handleFilterChange}
                    />
                  </Form.Group>

                  <Form.Group controlId="geographicalLocation" className="mb-3">
                    <Form.Label>Geographical location</Form.Label>
                    <Form.Control
                      type="text"
                      name="geographicalLocation"
                      value={filters.geographicalLocation}
                      onChange={handleFilterChange}
                    />
                  </Form.Group>

                  <Form.Group controlId="employmentState" className="mb-3">
                    <Form.Label>Employment state</Form.Label>

                    <Form.Select
                      name="employmentState"
                      value={filters.employmentState}
                      onChange={handleFilterChange}
                    >
                      <option value="">All</option>
                      <option value="EMPLOYED">Employed</option>
                      <option value="UNEMPLOYED">Unemployed</option>
                      <option value="AVAILABLE_FOR_WORK">
                        Available for work
                      </option>
                      <option value="NOT_AVAILABLE">Not available</option>
                    </Form.Select>
                  </Form.Group>

                  <ButtonGroup className="d-flex justify-content-center mt-4">
                    <Col className="text-center">
                      <Button
                        className="primaryButton mb-2"
                        variant="primary"
                        onClick={() => {
                          setLoading(true);
                          fetchProfessionals(
                            0,
                            pageSize,
                            filters.skill,
                            filters.geographicalLocation,
                            filters.employmentState
                          )
                            .then((result) => {
                              console.log("Professionals fetched: ", result);
                              setProfessionals(result);
                              setActiveFilters(filters);
                              setLoading(false);
                            })
                            .catch((error) => {
                              setError(true);
                              setActiveFilters({
                                skill: "",
                                geographicalLocation: "",
                                employmentState: "",
                              });
                              setLoading(false);
                              console.log(error);
                              throw new Error(
                                "GET /API/professionals : Network response was not ok"
                              );
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
                            skill: "",
                            geographicalLocation: "",
                            employmentState: "",
                          });
                          fetchProfessionals(0, pageSize)
                            .then((result) => {
                              console.log("Professionals fetched: ", result);
                              setProfessionals(result);
                              presentedProfessionals = result.content;
                              setActiveFilters({
                                skill: "",
                                geographicalLocation: "",
                                employmentState: "",
                              });
                              setLoading(false);
                            })
                            .catch((error) => {
                              setError(true);
                              setLoading(false);
                              console.log(error);
                              throw new Error(
                                "GET /API/professionals : Network response was not ok"
                              );
                            });
                        }}
                      >
                        Clear Filters
                      </Button>
                    </Col>
                  </ButtonGroup>
                </Form>

                <h5 className="mt-3">Sort Professionals</h5>
                <Form>
                  <Form.Group controlId="sort" className="mb-3">
                    <Form.Select
                      name="sort"
                      value={sortCriteria}
                      onChange={(e) => setSortCriteria(e.target.value)}
                    >
                      <option value="asc_name">
                        Alphabetically ascending name
                      </option>
                      <option value="asc_surname">
                        Alphabetically ascending surname
                      </option>
                      <option value="asc_num_skills">
                        Ascending number of skills
                      </option>
                      <option value="asc_geographicalLocation">
                        Alphabetically ascending location
                      </option>
                      <option value="desc_name">
                        Alphabetically descending name
                      </option>
                      <option value="desc_surname">
                        Alphabetically descending surname
                      </option>
                      <option value="desc_num_skills">
                        Descending number of skills
                      </option>
                      <option value="desc_geographicalLocation">
                        Alphabetically descending location
                      </option>
                      <option value="employed_before">Occupied before</option>
                      <option value="unemployed_before">
                        Non-occupied before
                      </option>
                    </Form.Select>
                  </Form.Group>
                </Form>
              </div>
            </Col>

            <Col xs={12} lg={8} className="order-2 order-lg-1">
              <Row className="d-flex justify-content-center">
                <Col className="d-flex-column justify-content-center align-items-center mt-3">
                  {presentedProfessionals
                    .sort((a, b) => {
                      if (sortCriteria === "asc_name") {
                        return a.information.name.localeCompare(
                          b.information.name
                        );
                      } else if (sortCriteria === "asc_surname") {
                        return a.information.surname.localeCompare(
                          b.information.surname
                        );
                      } else if (sortCriteria === "asc_num_skills") {
                        return a.skills.length - b.skills.length;
                      } else if (sortCriteria === "asc_geographicalLocation") {
                        return a.geographicalLocation.localeCompare(
                          b.geographicalLocation
                        );
                      } else if (sortCriteria === "desc_name") {
                        return b.information.name.localeCompare(
                          a.information.name
                        );
                      } else if (sortCriteria === "desc_surname") {
                        return b.information.surname.localeCompare(
                          a.information.surname
                        );
                      } else if (sortCriteria === "desc_num_skills") {
                        return b.skills.length - a.skills.length;
                      } else if (sortCriteria === "desc_geographicalLocation") {
                        return b.geographicalLocation.localeCompare(
                          a.geographicalLocation
                        );
                      } else if (sortCriteria === "employed_before") {
                        if (
                          a.employmentState === "EMPLOYED" &&
                          b.employmentState !== "EMPLOYED"
                        ) {
                          return -1;
                        } else if (
                          a.employmentState !== "EMPLOYED" &&
                          b.employmentState === "EMPLOYED"
                        ) {
                          return 1;
                        }
                      } else if (sortCriteria === "unemployed_before") {
                        if (
                          a.employmentState === "UNEMPLOYED" &&
                          b.employmentState !== "UNEMPLOYED"
                        ) {
                          return -1;
                        } else if (
                          a.employmentState !== "UNEMPLOYED" &&
                          b.employmentState === "UNEMPLOYED"
                        ) {
                          return 1;
                        }
                      }
                      return 0;
                    })
                    .map((professional, index) => {
                      const selectedSkill = professional.skills.reduce(
                        (bestMatch, skill) => {
                          if (activeFilters.skill === "") {
                            return professional.skills[0];
                          }
                          const skillLower = skill.toLowerCase();
                          const filterSkillLower =
                            activeFilters.skill.toLowerCase();
                          const matchLength = skillLower.includes(
                            filterSkillLower
                          )
                            ? filterSkillLower.length
                            : 0;
                          const bestMatchLower = bestMatch.toLowerCase();
                          const bestMatchLength = bestMatchLower.includes(
                            filterSkillLower
                          )
                            ? filterSkillLower.length
                            : 0;
                          return matchLength > bestMatchLength
                            ? skill
                            : bestMatch;
                        },
                        professional.skills[0]
                      );
                      return (
                        <Row
                          key={index}
                          className="w-100 border border-dark rounded-3 p-3 mb-2 ms-1 d-flex align-items-center secondaryButton"
                          onClick={() =>
                            navigate(`/ui/professionals/${professional.id}`)
                          }
                        >
                          <Col xs={12} md={6} lg={3}>
                            <h5 className="mb-0 text-center-sm">{`${professional.information.name} ${professional.information.surname}`}</h5>
                          </Col>
                          <Col xs={12} md={6} lg={3}>
                            <p className="mb-0 fw-light  text-center-sm text-right-md">
                              {`${selectedSkill} + `}
                              <strong className="fw-semibold">
                                {professional.skills.length - 1}
                              </strong>
                              {` skills`}
                            </p>
                          </Col>
                          <Col xs={12} md={6} lg={3}>
                            <p className="mb-0 fw-light  text-center-sm">
                              {professional.geographicalLocation}
                            </p>
                          </Col>
                          <Col
                            xs={12}
                            md={6}
                            lg={3}
                            className="d-flex justify-content-end  text-center-sm"
                          >
                            <p className="mb-0">
                              <span className="fw-semibold fs-5">
                                {professional.employmentState === "EMPLOYED"
                                  ? "Employed"
                                  : professional.employmentState ===
                                    "UNEMPLOYED"
                                  ? "Unemployed"
                                  : professional.employmentState ===
                                    "AVAILABLE_FOR_WORK"
                                  ? "Available for work"
                                  : professional.employmentState ===
                                    "NOT_AVAILABLE"
                                  ? "Not available"
                                  : ""}
                              </span>
                            </p>
                          </Col>
                        </Row>
                      );
                    })}

                  {presentedProfessionals.length === 0 && (
                    <Row className="w-100">
                      <Col className="w-100 d-flex justify-content-center align-items-center mt-5">
                        <h5 className="p-5">
                          No professionals found with the selected criteria. Try
                          adjusting the filters, or it could be that no
                          professionals have been added yet.
                        </h5>
                      </Col>
                    </Row>
                  )}
                </Col>
              </Row>

              {/* Pagination */}
              <Row className="mt-auto">
                <Col className="d-flex justify-content-center mt-4 custom-pagination">
                  <Pagination>
                    <Pagination.First
                      onClick={() => changePage(0)}
                      disabled={professionals.currentPage === 0}
                    />
                    <Pagination.Prev
                      onClick={() => changePage(professionals.currentPage - 1)}
                      disabled={professionals.currentPage === 0}
                    />

                    {Array.from(
                      { length: Math.min(5, professionals.totalPages) },
                      (_, index) => {
                        const startPage = Math.max(
                          Math.min(
                            professionals.currentPage - 2,
                            professionals.totalPages - 5
                          ),
                          0
                        );
                        const actualPage = startPage + index;

                        return (
                          <Pagination.Item
                            key={actualPage}
                            active={actualPage === professionals.currentPage}
                            onClick={() => changePage(actualPage)}
                          >
                            {actualPage + 1}
                          </Pagination.Item>
                        );
                      }
                    )}

                    <Pagination.Next
                      onClick={() => changePage(professionals.currentPage + 1)}
                      disabled={
                        professionals.currentPage + 1 ===
                        professionals.totalPages
                      }
                    />
                    <Pagination.Last
                      onClick={() => changePage(professionals.totalPages - 1)}
                      disabled={
                        professionals.currentPage + 1 ===
                        professionals.totalPages
                      }
                    />
                  </Pagination>
                </Col>
              </Row>
            </Col>
          </Row>
        </>
      )}
    </div>
  );
}

export default ProfessionalsPage;
