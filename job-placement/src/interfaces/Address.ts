export interface Address{
    id: number,
    state: string,
    region: string,
    city: string,
    address: string,
    comment: string
}

export interface CreateAddress{
    state: string,
    region: string,
    city: string,
    address: string,
    comment: string
}