export const statesJobOffer = ["CREATED", "SELECTION_PHASE", "CANDIDATE_PROPOSAL", "CONSOLIDATED", "ABORT", "DONE"];

export const contractTypeList = ["Full Time", "Part Time", "Contract", "Freelance"];

export const workModeList = ["Remote", "Hybrid", "In-Person"];

export const toTitleCase = (str: string) => {
  return str
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};
