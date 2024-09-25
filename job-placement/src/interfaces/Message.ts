export interface Message {
    id: number;
    date: number[];
    subject: string;
    body: string;
    actualState: string;
    priority: string;
    channel: string;
    sender: string;
  } 