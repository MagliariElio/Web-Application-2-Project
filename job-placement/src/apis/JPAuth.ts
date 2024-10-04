import { MeInterface } from "../interfaces/MeInterface.ts";
import { RoleState } from "../utils/costants.ts";

const fetchMe = async () => {
  try {
    const res = await fetch("http://localhost:8080/me");
    const me = (await res.json()) as MeInterface;
    return me;
  } catch (err) {
    return null;
  }
};

const fetchRole = async () => {
  try {
    const res = await fetch("/documentStoreService/v1/API/documents/auth/public");
    const result = await res.json();
    return result;
  } catch (err) {
    return null;
  }
};

const logout = async (logoutUrl: string, token: string | null) => {
  let head;
  if (token == null) head = { "Content-Type": "application/json" };
  else
    head = {
      "Content-Type": "application/json",
      _csrf: token,
    };
  await fetch(logoutUrl, {
    method: "POST",
    headers: head,
  });
};

const JPAPIAuth = { fetchMe, logout, fetchRole };
export default JPAPIAuth;
