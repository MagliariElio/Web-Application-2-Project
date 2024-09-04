export function checkValidEmail(email: string): boolean {
  const re = /\S+@\S+\.\S+/;
  return re.test(email);
}

export function checkValidTelephone(telephone: string): boolean {
  const re = /^(?:\+?\d{1,2}\s?)?(?:1\s?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}$/;
  return re.test(telephone);
}
export const debounce = (func: Function, delay: number) => {
  let timeoutId: ReturnType<typeof setTimeout>;
  return (...args: any[]) => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      func(...args);
    }, delay);
  };
};