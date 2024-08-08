import {MeInterface} from "../interfaces/MeInterface.ts";

const fetchMe = async (setMe: (arg0: MeInterface | null) => void) => {
    try {
        const res = await fetch("http://localhost:8080/me")
        const me = await res.json() as MeInterface
        setMe(me)
    } catch (err) {
        setMe(null)
    }
}

const logout = async (logoutUrl: string, token: string|null) => {
    let head
    if(token == null)
        head = {'Content-Type': 'application/json'}
    else
        head = {
            'Content-Type': 'application/json',
            '_csrf': token
        }
    await fetch(logoutUrl, {
        method: 'POST',
        headers: head
    })
}

const JPAPIAuth = {fetchMe, logout}
export default JPAPIAuth