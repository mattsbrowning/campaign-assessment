# Campaign Assessment API

A Spring Boot REST API that accepts a campaign configuration and returns a basic feasibility assessment.

---

## What it does

`POST /assessments` accepts a campaign configuration — channel type, budget, target country, and an optional flight duration — and returns an assessment including estimated reach, a recommended sample size, and any warnings (e.g. low budget for the chosen channel, very short flight duration).

All created assessments are held in memory and can be retrieved individually or listed:

| Method | Path                  | Description                  |
|--------|-----------------------|------------------------------|
| POST   | `/assessments`        | Create a new assessment      |
| GET    | `/assessments/{id}`   | Retrieve an assessment by ID |
| GET    | `/assessments`        | List all assessments         |

---

## Running it

**Prerequisites:** JDK 21+ and a working internet connection for the first Gradle run.

```bash
./gradlew bootRun
```

The API starts on **http://localhost:8080**.

The interactive Swagger UI — which documents all endpoints, shows request/response schemas, and lets you make live requests — is available at:

**http://localhost:8080/swagger-ui.html**

Click **Authorize** and enter `EXAMPLE-KEY` as the API key before making requests.

---

## Running via Docker

```bash
docker build -t campaign-assessment .
docker run -p 8080:8080 campaign-assessment
```

---

## Running the tests

```bash
./gradlew test
```

Checkstyle (static analysis) runs automatically as part of the build:

```bash
./gradlew check
```

---

## Design decisions

**REST at level 3 (HATEOAS).** Most APIs described as RESTful stop at level 2 — resources and HTTP verbs. This API implements level 3: every response includes hypermedia links that tell the client what it can do next (`self`, `assessments`). Spring HATEOAS handles this via a dedicated `AssessmentModelAssembler` component, keeping the controller free of link-building logic.

**Jakarta Bean Validation throughout.** All input constraints (`@NotNull`, `@Positive`, `@Pattern`) are declared on the `CampaignRequest` record and enforced by Spring's `@Valid` annotation. Validation failures produce a 400 response with field-level messages automatically — no manual checking in the controller.

**Explicit authentication before rate limiting.** The interceptor chain is ordered deliberately: `ApiKeyInterceptor` runs first and short-circuits unauthenticated requests before they can consume rate-limit tokens. Both values (`api.key`, `api.rate-limit.requests-per-minute`) are externalised to `application.properties`; in production they would be injected via environment variables or a secrets manager.

**Illustrative feasibility logic.** The reach and sample-size calculations are intentionally simplified — the brief specified that accuracy was not the goal. The structure is the point: a dedicated `FeasibilityService` handles all calculation, the repository handles storage, and the controller handles HTTP. Replacing the placeholder logic with real media-planning data would not require touching any other layer.

**In-memory storage behind an interface.** `AssessmentRepository` is an interface; `InMemoryAssessmentRepository` is the current implementation. Swapping in a JPA-backed implementation for persistence would be a one-class change.

**Static analysis.** Checkstyle enforces a pragmatic subset of the Google Java Style Guide on every build. Test method names are exempt from the camelCase rule via a `suppressions.xml` — JUnit 5 convention favours underscores for readability.

---

## What I would do differently with more time

- **Persistence.** Replace the in-memory store with a proper database (likely PostgreSQL via Spring Data JPA). The repository interface is already in place, so this is additive rather than structural.

- **Richer validation error responses.** Spring's default 400 body is functional but not particularly friendly. A `@ControllerAdvice` with a structured error response (field name + message per violation) would be a straightforward improvement.

- **More realistic feasibility logic.** The current estimates are placeholders. With more time I would source representative reach-per-spend figures from publicly available media planning data and model the country factor from population data rather than a hardcoded map.

- **Authentication.** The current API key approach is appropriate for a demo. A production service would use OAuth 2.0 with short-lived tokens, and keys would be stored hashed rather than compared in plaintext.

- **Observability.** Spring Boot Actuator, structured JSON logging, and a `management.tracing` configuration would be the minimum additions before running this anywhere production-like.

- **Contract testing.** Pact or a similar consumer-driven contract testing library would be worthwhile once the API had real consumers.

---

## How I used AI tools

I used Claude Code as a development accelerator throughout this project. The API design, the choice to implement HATEOAS at level 3, the decision to separate authentication from rate limiting, and the overall package structure were my own. Claude Code was used to generate Java boilerplate I would otherwise have had to look up (Spring interceptor wiring, Bucket4j configuration, the Checkstyle XML format) and to handle the mechanical parts of the Git workflow. This let me concentrate on the decisions rather than the syntax.
