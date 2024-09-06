export interface Filters {
  contractType: string;
  location: string;
  workMode: string;
  status: string;
  elementsPerPage: number;
  sortBy: "duration" | "value" | "";
  sortDirection: "asc" | "desc" | "";
}
