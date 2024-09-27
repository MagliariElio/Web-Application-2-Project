export interface DocumentFile{
    id: number,
    name: string,
    size: number,
    contentType: string,
    creationTimestamp: string,
    modifiedTimestamp: string,
    document: {
        id: number,
        content: any
    }
}

