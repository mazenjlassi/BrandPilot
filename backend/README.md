# BrandPilot — Backend

Spring Boot REST API powering the BrandPilot platform.

## Tech Stack

- Java 17 + Spring Boot 3.x
- Spring Security + JWT auth
- PostgreSQL (production) / H2 (tests)
- Testcontainers (integration tests)
- JaCoCo (code coverage)

## Commands

| Command | Description |
|---|---|
| `mvn test` | Run unit tests (Surefire) |
| `mvn verify` | Run all tests including integration (Failsafe) |
| `mvn spring-boot:run` | Start dev server |

## Coverage

- Unit: ~569 tests
- Integration: ~176 tests
- Overall: ~75%

## API

RESTful API under `/api/v1/` covering:
- Auth (login, register, refresh)
- Posts, Comments, Metrics
- Campaigns & Content Patterns
- Social media publishing (Facebook, Instagram, LinkedIn, X)
- AI content & image generation
- Admin management
- Analytics & Insights
