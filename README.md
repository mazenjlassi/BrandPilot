# BrandPilot

Monorepo for BrandPilot — an AI-powered social media content management platform.

## Structure

```
MetaTry/
├── backend/         Spring Boot REST API (Java 17)
├── frontend/        Angular UI (Angular 19)
├── scraper/         Social media scraper (Node.js)
├── e2e/             Cypress end-to-end tests
└── docker-compose.yml
```

## Quick Start

```bash
docker compose up -d
```

## Running locally

See README in each subdirectory:

- [backend/](backend/README.md) — API setup & testing
- [frontend/](frontend/README.md) — UI dev server & testing
- [scraper/](scraper/README.md) — scraper usage & testing
