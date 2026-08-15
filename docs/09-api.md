# Rest API

## Unprotected Routes

### Authentication
- POST: /auth/login
  - body: { phoneNumber: String, password: String }

### Manage main hub account
- POST: /accounts
  - create new hub account and new user
  - body: { phoneNumber: String, userName: String, accountName: String, password: String }

## Protected Routes
All protected routes require bearer token in the authorization header
- Authorization: Bearer {token}

### Authentication
- POST: /auth/logout
- POST: /auth/logout-all

### Manage main hub account
- POST: /accounts/create-for-user
  - create new hub account
  - body: { userId: String, accountName: String }
- PATCH: /accounts/{accountId}/disable
- PATCH: /accounts/{accountId}/reactivate
- DELETE: /accounts/{accountId}

### Manage user accounts


### Manage invitations