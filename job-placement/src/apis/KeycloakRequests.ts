import { KeycloakUser } from "../interfaces/KeycloakUser";

async function fetchAccessToken(): Promise<string> {
  const tokenUrl = 'http://localhost:9090/realms/job-connect/protocol/openid-connect/token';
  const params = new URLSearchParams();
  
  // Adding the required body parameters
  params.append('client_id', 'job-client-user-requests');
  params.append('client_secret', '9FKPAzvfnUrEopDYXlnnI7SHUxuzRSru');
  params.append('grant_type', 'client_credentials');

  try {
      // Make the POST request to fetch the access token
      const response = await fetch(tokenUrl, {
          method: 'POST',
          headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: params.toString(),
      });

      if (!response.ok) {
          throw new Error(`Error fetching access token: ${response.statusText}`);
      }

      const data = await response.json();
      return data.access_token;
  } catch (error) {
      console.error('Failed to fetch access token:', error);
      throw error;
  }
}

async function fetchUserRoles(userId: string, accessToken: string): Promise<string> {
    const rolesUrl = `http://localhost:9090/admin/realms/job-connect/users/${userId}/role-mappings/realm`; // Modify as per your Keycloak setup

    try {
        const response = await fetch(rolesUrl, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
            },
        });

        if (!response.ok) {
            throw new Error(`Error fetching roles for user ${userId}: ${response.statusText}`);
        }
        const keywords = ['OPERATOR', 'MANAGER', 'GUEST'];
        const roles : any[] = await response.json();

        const filteredRoles = roles.filter((role) => 
            keywords.some(keyword => role.name.includes(keyword))
        );

        if(filteredRoles.length > 0){
            return filteredRoles[0].name
        }else {
            return "Unknown"
        }
        
        
    } catch (error) {
        console.error(`Failed to fetch roles for user ${userId}:`, error);
        throw error; // Re-throw error to handle it at the caller level
    }
}


// Assuming KeycloakUser interface is already defined as per previous response
export async function fetchUsers(): Promise<KeycloakUser[]> {
  const usersUrl = 'http://localhost:9090/admin/realms/job-connect/users';

  try {
      // First, fetch the access token
      const accessToken = await fetchAccessToken();

      // Make the GET request to fetch users using the access token
      const response = await fetch(usersUrl, {
          method: 'GET',
          headers: {
              'Authorization': `Bearer ${accessToken}`,
          },
      });

      if (!response.ok) {
          throw new Error(`Error fetching users: ${response.statusText}`);
      }

      // Parse the user data and return it
      const users: KeycloakUser[] = await response.json();
      for (const user of users) {
        user.role = await fetchUserRoles(user.id, accessToken);
      }
      return users;
  } catch (error) {
      console.error('Failed to fetch users:', error);
      throw error; // Re-throw the error to inform the caller
  }
}


/*export async function addUser(
    firstName: string,
    lastName: string,
    email: string,
    username: string,
    role: string
  ): Promise<void> {
    const usersUrl = 'http://localhost:9090/admin/realms/job-connect/users';
  
    try {
      // Fetch access token first
      const accessToken = await fetchAccessToken();
  
      // Prepare the new user data
      const newUser = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        username: username,
        enabled: true, // Set to true to activate the user
        realmRoles: [role]
      };
  
      // Step 1: Create the user
      const createUserResponse = await fetch(usersUrl, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newUser),
      });
  
      if (!createUserResponse.ok) {
        throw new Error(`Error creating user: ${createUserResponse.statusText}`);
      }
  
      // Step 2: Fetch the newly created user's ID from Keycloak
      const createdUserResponse = await fetch(usersUrl, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });
  
      if (!createdUserResponse.ok) {
        throw new Error(`Error fetching users after creating: ${createdUserResponse.statusText}`);
      }
  
      const users: KeycloakUser[] = await createdUserResponse.json();
      const createdUser = users.find(user => user.username === username);
  
      if (!createdUser) {
        throw new Error(`Newly created user not found: ${username}`);
      }
  
      console.log(`User ${username} created and assigned role ${role} successfully.`);
    } catch (error) {
      console.error('Failed to add user:', error);
      throw error; // Re-throw error to inform the caller
    }
  }*/

    async function fetchRoleByName(roleName: string, accessToken: string): Promise<any> {
        const roleUrl = `http://localhost:9090/admin/realms/job-connect/roles/${roleName}`;
      
        try {
          const response = await fetch(roleUrl, {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
            },
          });
      
          if (!response.ok) {
            throw new Error(`Error fetching role ${roleName}: ${response.statusText}`);
          }
      
          const role = await response.json();
          return role;
        } catch (error) {
          console.error(`Failed to fetch role: ${roleName}`, error);
          throw error;
        }
      }
      
      // Function to assign a role to the created user
      async function assignRoleToUser(userId: string, roleDetails: any, accessToken: string): Promise<void> {
        const roleMappingUrl = `http://localhost:9090/admin/realms/job-connect/users/${userId}/role-mappings/realm`;
      
        try {
          const assignRoleResponse = await fetch(roleMappingUrl, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify([roleDetails]), // Role details include the role ID
          });
      
          if (!assignRoleResponse.ok) {
            throw new Error(`Error assigning role: ${assignRoleResponse.statusText}`);
          }
      
          console.log(`Role ${roleDetails.name} assigned to user ${userId} successfully.`);
        } catch (error) {
          console.error('Failed to assign role to user:', error);
          throw error;
        }
      }
      
      // Function to create a new user and assign a role
      export async function addUser(
        firstName: string,
        lastName: string,
        email: string,
        username: string,
        role: string,
        password: string
      ): Promise<void> {
        const usersUrl = 'http://localhost:9090/admin/realms/job-connect/users';
      
        try {
          // Fetch access token
          const accessToken = await fetchAccessToken();
      
          // Step 1: Prepare the new user data, including the password
          const newUser = {
            firstName: firstName,
            lastName: lastName,
            email: email,
            username: username,
            enabled: true, // Enable user upon creation
            credentials: [
              {
                type: 'password',
                value: password,
                temporary: false // Set to true if you want the user to reset the password on first login
              }
            ]
          };
      
          // Step 2: Create the user in Keycloak
          const createUserResponse = await fetch(usersUrl, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(newUser),
          });
      
          if (!createUserResponse.ok) {
            throw new Error(`Error creating user: ${createUserResponse.statusText}`);
          }
      
          // Step 3: Fetch the newly created user's ID from Keycloak
          const createdUserResponse = await fetch(usersUrl, {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
            },
          });
      
          if (!createdUserResponse.ok) {
            throw new Error(`Error fetching users after creating: ${createdUserResponse.statusText}`);
          }
      
          const users: KeycloakUser[] = await createdUserResponse.json();
          const createdUser = users.find(user => user.username === username);
      
          if (!createdUser) {
            throw new Error(`Newly created user not found: ${username}`);
          }
      
          // Step 4: Fetch the role details by role name (to get role ID)
          const roleDetails = await fetchRoleByName(role, accessToken);
      
          // Step 5: Assign the role to the newly created user
          await assignRoleToUser(createdUser.id, roleDetails, accessToken);
      
          console.log(`User ${username} created with the role ${role} and password set successfully.`);
        } catch (error) {
          console.error('Failed to add user:', error);
          throw error; // Re-throw error to inform the caller
        }
      }

      export async function deleteUser(userId: string): Promise<void> {
        const deleteUserUrl = `http://localhost:9090/admin/realms/job-connect/users/${userId}`;
      
        try {
          // Fetch access token
          const accessToken = await fetchAccessToken();
      
          // Send DELETE request to Keycloak to delete the user
          const response = await fetch(deleteUserUrl, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
          });
      
          if (!response.ok) {
            throw new Error(`Error deleting user ${userId}: ${response.statusText}`);
          }
      
          console.log(`User with ID ${userId} deleted successfully.`);
        } catch (error) {
          console.error(`Failed to delete user ${userId}:`, error);
          throw error; // Re-throw error to inform the caller
        }
      }