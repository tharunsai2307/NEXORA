@echo off
setlocal enabledelayedexpansion
title NEXORA Launcher

cd /d "%~dp0"

echo ====================================================================
echo          NEXORA - Smart Building Digital Twin Platform              
echo                    Intelligence Beyond Infrastructure               
echo ====================================================================
echo.

:: 1. Automatic JDK 21+ Detection for Java Spring Boot
if not defined JAVA_HOME (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    ) else (
        for /d %%D in ("C:\Program Files\Java\jdk*") do (
            if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        )
        for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
            if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
        )
    )
)

if defined JAVA_HOME (
    echo [OK] Using JAVA_HOME: !JAVA_HOME!
) else (
    echo [INFO] JAVA_HOME not explicitly detected. Using system default java.
)

:: 2. Thread-safety configuration for Python 3.14 on Windows
set "OPENBLAS_NUM_THREADS=1"
set "LOKY_MAX_CPU_COUNT=1"

:: 3. Check if Docker is available
set DOCKER_AVAILABLE=0
where docker >nul 2>&1
if %ERRORLEVEL% equ 0 (
    docker info >nul 2>&1
    if %ERRORLEVEL% equ 0 set DOCKER_AVAILABLE=1
)

echo.
echo Select launch mode:
echo   [1] Start Standalone Local Services (FastAPI + Spring Boot + Vite) [DEFAULT]
if %DOCKER_AVAILABLE% equ 1 (
    echo   [2] Start Production Docker Containers (Full 4-tier stack)
) else (
    echo   [2] Start Production Docker Containers (Requires Docker Desktop)
)
echo   [3] Open Web Browser Only (http://localhost:5173)
echo   [4] Stop All Running NEXORA Services
echo.

choice /C 1234 /T 6 /D 1 /M "Enter choice [Auto-starting option 1 in 6 seconds]:"
set USER_CHOICE=%ERRORLEVEL%

if %USER_CHOICE% equ 4 goto :stop_services
if %USER_CHOICE% equ 3 goto :open_browser_only
if %USER_CHOICE% equ 2 goto :start_docker
if %USER_CHOICE% equ 1 goto :start_standalone

:start_standalone
echo.
echo ====================================================================
echo Starting NEXORA standalone microservices...
echo ====================================================================
echo.

:: 1. Launch AI Engine (FastAPI on Port 8000)
echo [1/3] Starting Python Edge AI Engine (Port 8000)...
start "NEXORA - AI Engine (Port 8000)" cmd /k "title NEXORA AI Engine [8000] && cd /d "%~dp0ai-engine" && set OPENBLAS_NUM_THREADS=1 && set LOKY_MAX_CPU_COUNT=1 && python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload"

:: 2. Launch Backend (Spring Boot on Port 8080)
echo [2/3] Starting Java Spring Boot Backend (Port 8080)...
start "NEXORA - Backend (Port 8080)" cmd /k "title NEXORA Backend [8080] && cd /d "%~dp0backend" && if defined JAVA_HOME (set "JAVA_HOME=!JAVA_HOME!") && call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=standalone"

:: 3. Launch Frontend (React + Vite on Port 5173)
echo [3/3] Starting React Vite Frontend (Port 5173)...
start "NEXORA - Frontend (Port 5173)" cmd /k "title NEXORA Frontend [5173] && cd /d "%~dp0frontend" && npm run dev"

echo.
echo ====================================================================
echo All 3 services have been launched in separate terminal windows:
echo   * AI Engine:    http://localhost:8000 (Swagger: http://localhost:8000/docs)
echo   * Java Backend: http://localhost:8080 (Health:  http://localhost:8080/api/v1/health)
echo   * Web Frontend: http://localhost:5173
echo ====================================================================
echo.
echo Waiting 5 seconds for local servers to bind ports, then opening browser...
timeout /t 5 /nobreak >nul
start http://localhost:5173

echo.
echo ====================================================================
echo Demo Login Roles (Click quick-fill on login page or use credentials):
echo   * System Administrator: admin@nexora.com   / NexoraAdmin2026!
echo   * Facilities Manager:   manager@nexora.com / NexoraAdmin2026!
echo   * Chief Safety Officer: safety@nexora.com  / NexoraAdmin2026!
echo ====================================================================
echo.
echo Note: To stop all services later, run stop.bat or select option 4.
echo Press any key to close this launcher window (services keep running).
pause >nul
exit /b 0

:start_docker
if %DOCKER_AVAILABLE% equ 0 (
    echo [ERROR] Docker daemon is not active. Falling back to Standalone Local Services...
    goto :start_standalone
)
echo.
echo Starting NEXORA via Docker Compose...
docker compose up -d
echo Opening http://localhost in browser...
timeout /t 5 /nobreak >nul
start http://localhost
echo Docker containers are active. Press any key to exit launcher.
pause >nul
exit /b 0

:open_browser_only
echo.
echo Opening http://localhost:5173 in default browser...
start http://localhost:5173
exit /b 0

:stop_services
echo.
echo Stopping NEXORA services...
call "%~dp0stop.bat"
exit /b 0
