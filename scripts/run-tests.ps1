# PowerShell script to execute all automated test suites across all tiers

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Edge-AI Smart Building: Automated Test Suite Runner     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Ensure JDK 21+ environment
if (-not $env:JAVA_HOME -or ($env:JAVA_HOME -like "*java8*")) {
    if (Test-Path "C:\Program Files\Android\Android Studio\jbr") {
        $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
        $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    }
}

# 1. Run Python AI Engine Tests
Write-Host "`n[1/3] Executing Python AI Service Pytest Suite..." -ForegroundColor Yellow
$env:PYTHONPATH="ai-engine"
python -m pytest ai-engine/tests -v
if ($LASTEXITCODE -ne 0) {
    Write-Host "Python AI tests failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Python AI tests passed successfully." -ForegroundColor Green

# 2. Run Java Spring Boot Backend Tests
Write-Host "`n[2/3] Executing Java Spring Boot JUnit Test Suite..." -ForegroundColor Yellow
if (Test-Path "$env:USERPROFILE\.m2\apache-maven-3.9.6\bin\mvn.cmd") {
    & "$env:USERPROFILE\.m2\apache-maven-3.9.6\bin\mvn.cmd" -f backend/pom.xml test
} elseif (Test-Path "backend/mvnw.cmd") {
    & "backend/mvnw.cmd" -f backend/pom.xml test
} else {
    mvn -f backend/pom.xml test
}
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java backend tests failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Java backend tests passed successfully." -ForegroundColor Green

# 3. Run React Frontend Tests
Write-Host "`n[3/3] Executing React Frontend Vitest Suite..." -ForegroundColor Yellow
Set-Location frontend
npm test
$frontendResult = $LASTEXITCODE
Set-Location ..
if ($frontendResult -ne 0) {
    Write-Host "Frontend Vitest tests failed!" -ForegroundColor Red
    exit $frontendResult
}
Write-Host "Frontend Vitest tests passed successfully." -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  ALL AUTOMATED TEST SUITES EXECUTED AND PASSED (100%)     " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
