# Task Tracker API

Spring Boot REST API for managing projects and tasks with role-based access control. Built for the Asterbit.

## Stack

Java 17, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, H2, MapStruct, Lombok, Swagger, JUnit 5 + Mockito.

## Getting it running

Clone it:

```bash
git clone https://github.com/GiorgiNikola/task-tracker-api.git
cd task-tracker-api
```

You need Java 17+ and Maven, or just use the wrapper that's already in the repo.

```bash
./mvnw spring-boot:run
```

The application runs on `http://localhost:8080`. Swagger at `/swagger-ui/index.html`. There's no separate DB setup step, H2 is in-memory and starts automatically with the app.

Run the tests:

```bash
./mvnw clean test
```

`JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` have defaults baked into `application.properties` so it runs with zero setup. Don't reuse these anywhere real, override them for any actual deployment. There's an admin account seeded on first startup using those values. It checks if the account already exists first, so restarting the app won't create duplicates.

Default admin login, if you want to check admin-only endpoints without going through the promotion flow:
```
email: admin@test.com
password: adminPassword1
```

## Auth

Register, login, get a JWT, send `Authorization: Bearer <token>` on everything else. Stateless, no sessions. Role checks use `@PreAuthorize`. For ownership checks, like whether you actually own a given project or task, there are two custom security beans doing the work: `ProjectSecurity` and `TaskSecurity`.

## Roles

ADMIN gets everything. MANAGER has full control over their own projects and tasks. USER can view and update status on tasks assigned to them, and can't create projects.

Task edits are allowed for the owner or the assignee. Status updates are assignee-only though, even the owner can't touch it. That's straight from the spec.

## Endpoints

```
POST   /auth/register              public
POST   /auth/login                 public
PATCH  /users/{id}/role            admin only

POST   /projects                   manager/admin
GET    /projects/{id}              owner/admin
PUT    /projects/{id}              owner/admin
DELETE /projects/{id}              owner/admin (blocked if it has tasks)

POST   /tasks                      project owner/admin
GET    /tasks/{id}                 owner, assignee, or admin
PUT    /tasks/{id}                 owner, assignee, or admin
DELETE /tasks/{id}                 owner/admin
PATCH  /tasks/{id}/assign          owner/admin
PATCH  /tasks/{id}/status          assignee or admin only
GET    /tasks/project?projectId=   owner/admin, paginated + filterable
GET    /tasks/user                 your own tasks, same filters
```

Full schemas in Swagger, easier to read the actual request/response shapes there than in a table here.

## Postman

Everything's in `/postman`, a collection and an environment file. Import both into Postman, then pick **Local Dev** as your active environment before sending anything, the collection relies on it for `baseUrl` and it won't work with no environment selected.

Run order matters the first time through since later requests pull tokens and IDs from earlier ones. Folders go Auth, Admin, Project, Task in that order, and each one's requests are already arranged top to bottom the way they're meant to run. Easiest way to go through it is Collection Runner, run the whole collection start to finish, everything should come back green.

It's safe to run more than once without restarting the app. Registration requests use a timestamp in the email so nothing collides with what a previous run already created in the H2 database. If you do want a totally clean slate, just restart the app, H2 resets on restart since it's in-memory.

The collection isn't just happy-path requests either, there's a "Forbidden" version of most endpoints checking that the wrong role or wrong user actually gets blocked, and a few requests specifically checking the 403-vs-404 behavior and the JWT staleness thing mentioned below.

## Testing

33 unit tests, service layer, Mockito, full branch coverage. Doesn't cover `@PreAuthorize` though, that's a Spring proxy and only exists in a real Spring context, mocked tests skip it entirely. Authorization is actually tested through the Postman "forbidden" cases instead, against the real running app with real JWTs.

## Known limitations

JWT role staleness: role changes don't apply until re-login, old token keeps the old role until it expires.

403 not 404 on unowned/nonexistent resources, stops ID enumeration.

Can't delete a project that still has tasks, returns 409 instead of cascading or throwing a raw FK error. Delete all tasks in the project first.

Status updates are assignee-only per spec, so an unassigned task can't have its status changed by anyone.

Ownership doesn't track role changes, a demoted manager keeps access to projects they already own. No ownership-transfer endpoint exists.

Password rules are length only, 8 to 20 characters, no complexity requirements.