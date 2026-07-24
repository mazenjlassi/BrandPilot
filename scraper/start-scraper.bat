@echo off
title MetaTry Scraper + Tunnel

cd /d "%~dp0"

echo Starting MetaTry Scraper...

REM Check if .env exists, if not copy from example
if not exist ".env" (
    if exist ".env.example" (
        copy ".env.example" ".env" >nul
        echo Created .env from .env.example -- edit it with your credentials!
    )
)

REM Start Node scraper in a new window
start "MetaTry-Scraper" cmd /c "node src/server.js"

echo Waiting for scraper to start...
timeout /t 3 /nobreak >nul

REM Check if cloudflared is available
where cloudflared >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo cloudflared not found. Install it from: https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/
    echo Or run manually: cloudflared tunnel --url http://localhost:3001
    pause
    exit /b 1
)

echo.
echo Starting Cloudflare Tunnel...
echo The URL below is your SCRAPER_BASE_URL -- copy it!
echo.
cloudflared tunnel --url http://localhost:3001

pause
