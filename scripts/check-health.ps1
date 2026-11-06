# Health check probe script for local and docker deployments

param (
    [string]$BackendUrl = "http://localhost:8080/api/v1",
    [string]$AiUrl = "http://localhost:8000"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Edge-AI Smart Building: Live Service Health Probe       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Probe AI Service
Write-Host "`n[1] Probing Python AI Service ($AiUrl/health)..." -ForegroundColor Yellow
try {
    $aiResp = Invoke-RestMethod -Uri "$AiUrl/health" -Method Get -TimeoutSec 3
    Write-Host "    Status: $($aiResp.status) | Service: $($aiResp.service)" -ForegroundColor Green
} catch {
    Write-Host "    Failed to connect to AI Service: $_" -ForegroundColor Red
}

# Probe Java Backend
Write-Host "`n[2] Probing Java Backend Core ($BackendUrl/health)..." -ForegroundColor Yellow
try {
    $backendResp = Invoke-RestMethod -Uri "$BackendUrl/health" -Method Get -TimeoutSec 3
    Write-Host "    Status: $($backendResp.status) | Service: $($backendResp.service)" -ForegroundColor Green
} catch {
    Write-Host "    Failed to connect to Backend: $_" -ForegroundColor Red
}

# Probe System Info via Backend Bridge
Write-Host "`n[3] Probing Subsystem Info via Backend Bridge ($BackendUrl/system/info)..." -ForegroundColor Yellow
try {
    $sysResp = Invoke-RestMethod -Uri "$BackendUrl/system/info" -Method Get -TimeoutSec 5
    Write-Host "    Application: $($sysResp.applicationName) v$($sysResp.applicationVersion)" -ForegroundColor Cyan
    Write-Host "    Backend Status: $($sysResp.backendStatus)" -ForegroundColor Cyan
    Write-Host "    Database Status: $($sysResp.databaseConnectivityStatus)" -ForegroundColor Cyan
    Write-Host "    AI Service Status: $($sysResp.aiServiceConnectivityStatus)" -ForegroundColor Cyan
} catch {
    Write-Host "    Failed to query system info: $_" -ForegroundColor Red
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
