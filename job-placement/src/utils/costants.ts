export enum JobOfferState {
  CREATED = "CREATED",
  SELECTION_PHASE = "SELECTION_PHASE",
  CANDIDATE_PROPOSAL = "CANDIDATE_PROPOSAL",
  CONSOLIDATED = "CONSOLIDATED",
  ABORT = "ABORT",
  DONE = "DONE",
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
