export enum JobOfferState {
  CREATED = "CREATED",
  SELECTION_PHASE = "SELECTION_PHASE",
  CANDIDATE_PROPOSAL = "CANDIDATE_PROPOSAL",
  CONSOLIDATED = "CONSOLIDATED",
  ABORT = "ABORT",
  DONE = "DONE",
};

export enum RoleState {
  GUEST = "GUEST",
  OPERATOR = "OPERATOR",
  MANAGER = "MANAGER",
};

export const contractTypeList = ["Full Time", "Part Time", "Contract", "Freelance"];

export const workModeList = ["Remote", "Hybrid", "In-Person"];

export const toTitleCase = (str: string) => {
  return str
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

export const employmentStateToText = (state: string) => {
  switch (state) {
    case "EMPLOYED":
      return "Employed";
    case "UNEMPLOYED":
      return "Unemployed";
    case "AVAILABLE_FOR_WORK":
      return "Available for work";
    case "NOT_AVAILABLE":
      return "Not available";
    default:
      return state;
  }
}