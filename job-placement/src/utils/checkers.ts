export function checkValidEmail(email: string): boolean {
  const re = /\S+@\S+\.\S+/;
  return re.test(email);
}

export function checkValidTelephone(telephone: string): boolean {
  const re = /^(?:\+?\d{1,2}\s?)?(?:1\s?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}$/;
  return re.test(telephone);
}

export function convertLocalDateTimeToDate(localDateTimeArray: number[] | undefined): Date {
  if (localDateTimeArray === undefined) {
    return new Date();
  }
  const [year, month, day, hour, minute, second, nanosecond] = localDateTimeArray;
  return new Date(year, month - 1, day, hour, minute, second, Math.floor(nanosecond / 1e6));
}
