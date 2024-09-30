import { KeycloakUser } from "../interfaces/KeycloakUser";

async function fetchAccessToken(): Promise<string> {
  const tokenUrl = 'http://localhost:9090/realms/job-connect/protocol/openid-connect/token';
  const params = new URLSearchParams();
  
  // Adding the required body parameters
  params.append('client_id', 'job-client-user-requests');
  params.append('client_secret', '8yGDKkGgT1Ck5lXQUE8CEm0ZPOv3jmMd');
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


      export async function updateUserName(userId: string, newFirstName: string, newLastName: string): Promise<void> {
        const updateUserUrl = `http://localhost:9090/admin/realms/job-connect/users/${userId}`;
      
        try {
          // Fetch access token
          const accessToken = await fetchAccessToken();
      
          // Step 1: Get the existing user data
          const getUserResponse = await fetch(updateUserUrl, {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
          });
      
          if (!getUserResponse.ok) {
            throw new Error(`Error fetching user data: ${getUserResponse.statusText}`);
          }
      
          // Step 2: Get the current user data and modify the name
          const existingUser = await getUserResponse.json();
          const updatedUser = {
            ...existingUser,
            firstName: newFirstName,
            lastName: newLastName,
          };
      
          // Step 3: Send the updated user data to Keycloak
          const updateUserResponse = await fetch(updateUserUrl, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${accessToken}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(updatedUser),
          });
      
          if (!updateUserResponse.ok) {
            throw new Error(`Error updating user name: ${updateUserResponse.statusText}`);
          }
      
          console.log(`User with ID ${userId} name updated to ${newFirstName} ${newLastName} successfully.`);
        } catch (error) {
          console.error(`Failed to update user name for user ${userId}:`, error);
          throw error;
        }
      }
      
      export async function changeUserPassword(userId: string, newPassword: string): Promise<void> {
        const updatePasswordUrl = `http://localhost:9090/admin/realms/job-connect/users/${userId}/reset-password`;
    
        try {
            // Fetch access token
            const accessToken = await fetchAccessToken();
    
            // Step 1: Prepare the payload for the password change
            const passwordPayload = {
                type: 'password',
                value: newPassword,
                temporary: false, // Set to true if you want the password to be temporary
            };
    
            // Step 2: Send the request to update the password
            const updatePasswordResponse = await fetch(updatePasswordUrl, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(passwordPayload),
            });
    
            if (!updatePasswordResponse.ok) {
                throw new Error(`Error changing user password: ${updatePasswordResponse.statusText}`);
            }
    
            console.log(`User with ID ${userId} password changed successfully.`);
        } catch (error) {
            console.error(`Failed to change password for user ${userId}:`, error);
            throw error;
        }
    }
    