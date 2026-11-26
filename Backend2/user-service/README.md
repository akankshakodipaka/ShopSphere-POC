# ShopSphere User Service

Accounts, login and tokens. Port **8081**, database **userdb**. This is the only service that creates tokens.

> Written without network access: **not compiled and not run**. If `mvn test` shows an error, send it and it will be fixed.

## Run

1. Create the database: `CREATE DATABASE userdb;`
2. Check `src/main/resources/application.properties` (MySQL user and password, default root/root).
3. `mvn spring-boot:run`
4. The log shows `Created the first super admin: superadmin@shopsphere.com`.

`mvn test` needs no MySQL (it uses in-memory H2 and its own super admin, `superadmin@test.com`).

## API

| Method | Path | Who | Notes |
|---|---|---|---|
| POST | /auth/register | public | `{"name","email","password","phone"}`, always creates a CUSTOMER |
| POST | /auth/login | public | `{"email","password"}` returns `{token, tokenType, expiresInSeconds, user}` |
| GET | /users/me | any logged-in | your own account |
| PUT | /users/me | any logged-in | `{"name","phone"}` |
| PUT | /users/me/password | any logged-in | `{"currentPassword","newPassword"}`, 204 |
| POST | /users/admins | SUPER_ADMIN | same body as register, creates an ADMIN |
| GET | /users?role= | ADMIN, SUPER_ADMIN | all users, or one role |
| GET | /users/{id} | ADMIN, SUPER_ADMIN | |
| PUT | /users/{id}/status | ADMIN, SUPER_ADMIN | `{"active":false}`, rules below |

## Rules

- Passwords are stored as BCrypt hashes. Emails are stored in lower case.
- Role comes from the endpoint, never from the request body.
- Login gives the same error for an unknown email and a wrong password. A blocked account gets 403 only after the password was right.
- Blocking: the SUPER_ADMIN can never be blocked, nobody changes their own status, only the SUPER_ADMIN changes an ADMIN, any admin can change a customer.
- Users are never deleted, only blocked (`active = false`).

## Token contract (used by Product, Cart and Order)

HS256, `sub` = user id, claim `role` = `CUSTOMER` | `ADMIN` | `SUPER_ADMIN`.
`security.jwt.secret` must be **identical** in all four services (at least 32 characters).

## Swagger

`http://localhost:8081/swagger-ui.html`. Click Authorize and paste only the token (no "Bearer " in front).
`springdoc` 2.5.0 is meant for Spring Boot 3.2.x. If `/v3/api-docs` returns 500, match the springdoc version to your Boot version.
Do not enable `springdoc.swagger-ui.persist-authorization` while debugging: a stale stored token makes even public URLs return 401.

## Try it

```
# login as the super admin
curl -X POST localhost:8081/auth/login -H "Content-Type: application/json" \
  -d '{"email":"superadmin@shopsphere.com","password":"Admin@12345"}'

# create an admin (use the token from above)
curl -X POST localhost:8081/users/admins -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Ravi","email":"ravi@shopsphere.com","password":"secret123"}'
```

Then log in as that admin and use the token on the Product service (`POST /categories`).

## Known limits

- A blocked user's existing token keeps working until it expires (tokens are stateless). Blocking stops new logins only.
- No email confirmation, no password reset, no refresh tokens.
- Change the super admin password through `PUT /users/me/password`. Changing it in the properties later does nothing for an existing account.
