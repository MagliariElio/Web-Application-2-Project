export function checkValidEmail(email: string): boolean {
  const re = /\S+@\S+\.\S+/;
  return re.test(email);
}

export function checkValidTelephone(telephone: string): boolean {
  const re = /^[+\d\s]{10,}$/;
  return re.test(telephone);
}