export interface KeycloakUser {
    id: string;                       // Unique identifier for the user
    username: string;                // The user's username
    firstName: string;               // The user's first name
    lastName: string;                // The user's last name
    email: string;                   // The user's email address
    emailVerified: boolean;          // Whether the user's email is verified
    createdTimestamp: number;        // Timestamp of user creation
    enabled: boolean;                // Whether the user account is enabled
    totp: boolean;                   // Whether TOTP is enabled for the user
    disableableCredentialTypes: string[]; // List of credential types that can be disabled
    requiredActions: string[];       // List of actions required for the user
    notBefore: number;               // The time before which the user cannot be authenticated
    access: UserAccess;              // Access permissions for the user
    role: string;
}