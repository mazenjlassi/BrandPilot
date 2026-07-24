FROM node:20-slim

RUN apt-get update && apt-get install -y \
    chromium \
    libatk-bridge2.0-0 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libcairo2 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY package.json .
RUN npm install

RUN npx playwright install chromium

COPY src ./src
COPY .env ./

EXPOSE 3001

ENV HEADLESS=true
ENV PORT=3001

CMD ["node", "src/server.js"]