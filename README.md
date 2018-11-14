# Role-Service
Provides Endpoints for Determining User Roles

## Exposes

        get("/test", ((request, response) -> "Welcome to the Auth Server"));
        get("/roles", App::createRolesResponse, getJsonTransformer());

        // Admin Endpoints
        get("/all_roles", App::getAllRoles, getJsonTransformer());
        post("/users", ((request, response) -> "Welcome!"));
        get("/users", ((request, response) -> "NO"));
        delete("/users", ((request, response) -> "Yes"));

        get("/", App::checkRole, getJsonTransformer());

All Endpoints require an AuthToken

- /
    - GET
    - Requires a query parameter ?role=<ROLE>
    - This will return a CheckResponse, checking to see if the user defined by the AuthToken has the specified role

- /test
    - GET
    - Does not require AuthToken
    - Returns "Hello World"
    
- /roles
    - GET
    - Returns a RolesResponse
    
- /all_roles
    - GET
    - Returns a RolesResponse with no user, but all roles are in the Roles object
    
- /users
    - GET / POST / DELETE
    - POST / DELETE expect a body consisting of, where the roles array is optional for Delete
    ```
    {
        "username" : "<username>",
        "name" : "<full name>",
        "roles" : [ "role1", "role2" ]
    }
    ```
    - Returns a UserResponse
    
    
    
### CheckResponse
```
{
    "role": "<ROLE>",
    "isAuthorized": true,
    "message": "Some message describing the result or error"
}
```

### RolesResponse
```
{
    "user" : {
        "username": "<USERNAME>",
        "name": "<FULL NAME>"
    },
    "roles" : [ "<ROLE1>", "<ROLE2>" ],
    "message" : "Some message describing the result or error"
}
```

### UserResponse
```
{
    "users" : [
        {
            "username" : "USERNAME",
            "name" : "FULL NAME",
            "roles" : ["Role1", "Role2"]
        }, 
        {
            "username" : "USERNAME",
            "name" : "FULL NAME",
            "roles" : ["Role1", "Role2"]
        }
    ],
    "message" : "Some message describing the result or error"
}
```