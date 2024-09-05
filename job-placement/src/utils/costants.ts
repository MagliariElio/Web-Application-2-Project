export enum JobOfferState {
  CREATED = "CREATED",
  SELECTION_PHASE = "SELECTION_PHASE",
  CANDIDATE_PROPOSAL = "CANDIDATE_PROPOSAL",
  CONSOLIDATED = "CONSOLIDATED",
  ABORT = "ABORT",
  DONE = "DONE",
}

export enum RoleState {
  GUEST = "GUEST",
  OPERATOR = "OPERATOR",
  MANAGER = "MANAGER",
}

export enum EmploymentStateEnum {
  EMPLOYED = "EMPLOYED",
  UNEMPLOYED = "UNEMPLOYED",
  AVAILABLE_FOR_WORK = "AVAILABLE_FOR_WORK",
  NOT_AVAILABLE = "NOT_AVAILABLE",
}

export enum EmploymentStateEnumSearchCandidateProfessional {
  UNEMPLOYED = "UNEMPLOYED",
  AVAILABLE_FOR_WORK = "AVAILABLE_FOR_WORK",
}

export const contractTypeList = ["Full Time", "Part Time", "Contract", "Freelance"];

export const workModeList = ["Remote", "Hybrid", "In-Person"];

export const toTitleCase = (str: string) => {
  return str
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};
