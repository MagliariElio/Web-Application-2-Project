import { RoleState } from "../utils/costants";

export interface MeInterface{
    name: string,
    surname: string,
    loginUrl: string,
    logoutUrl: string,
    principal: any|null,
    xsrfToken: string,
    role: RoleState
}