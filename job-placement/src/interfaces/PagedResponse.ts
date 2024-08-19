export interface PagedResponse<T>{
    content: T[],
    totalPages: number,
    totalElements: number,
    last: boolean,
    first: boolean,
    empty: boolean,
    size: number,
    number: number
}