# BrandPilot — Scraper

Node.js social media scraper that collects posts from competitor brands across platforms.

## Tech Stack

- Node.js
- Puppeteer (headless browser)
- Jest (unit tests)

## Supported Platforms

- **Facebook** — competitor page posts
- **Instagram** — posts from profiles (with login/CAPTCHA support)
- **LinkedIn** — company page posts
- **X (Twitter)** — user tweets

## Commands

| Command | Description |
|---|---|
| `npm start` | Run the scraper |
| `npm test` | Run Jest tests |
| `npx jest --coverage` | Tests with coverage report |

## Coverage

- 71 Jest tests across 6 spec files
- Config/Models: 100%
- Parsers: 68%
- Scrapers: 0% (require live browser + real targets)

## Data Flow

Scraper → JSON output files → Backend API ingestion → Displayed in Frontend UI
