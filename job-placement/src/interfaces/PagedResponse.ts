export interface PagedResponse<T>{
    content: T[],
    currentPage: number,
    elementPerPage: number,
    totalElements: number,
    totalPages: number
}