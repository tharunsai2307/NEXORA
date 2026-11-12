@echo off
title Stop NEXORA Services
cd /d "%~dp0"

echo ====================================================================
echo                   Stopping NEXORA Services                         
echo ====================================================================
echo.

:: 1. Terminate processes listening on port 5173 (Frontend Vite)
echo Checking port 5173 (Frontend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173 .*LISTENING" 2^>nul') do (
    echo Terminating PID %%a on port 5173...
    taskkill /F /PID %%a >nul 2>&1
)

:: 2. Terminate processes listening on port 8080 (Spring Boot Backend)
echo Checking port 8080 (Backend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 .*LISTENING" 2^>nul') do (
    echo Terminating PID %%a on port 8080...
    taskkill /F /PID %%a >nul 2>&1
)

:: 3. Terminate processes listening on port 8000 (FastAPI AI Engine)
echo Checking port 8000 (AI Engine)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8000 .*LISTENING" 2^>nul') do (
    echo Terminating PID %%a on port 8000...
    taskkill /F /PID %%a >nul 2>&1
)

:: 4. If docker was used, stop compose containers
where docker >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Checking Docker containers...
    docker compose down >nul 2>&1
)

echo.
echo ====================================================================
echo [DONE] All NEXORA services have been stopped.
echo ====================================================================
echo.
pause

