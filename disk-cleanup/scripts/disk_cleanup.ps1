# Disk Cleanup Master Script
param(
    [switch]$Report,
    [switch]$Analyze,
    [switch]$Cleanup,
    [switch]$IncludeDev,
    [switch]$Force,
    [switch]$DryRun,
    [switch]$DevCaches,
    [string]$Tool
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $Tool) {
    $Tool = "all"
}

function Show-Help {
    Write-Host @"

Windows Disk Cleanup Tool
=========================

Usage:
  .\disk_cleanup.ps1 -Report           # Generate disk usage report
  .\disk_cleanup.ps1 -Analyze          # Analyze large files
  .\disk_cleanup.ps1 -Cleanup          # Clean temp files
  .\disk_cleanup.ps1 -Cleanup -IncludeDev -DryRun  # Preview cleanup with dev caches
  .\disk_cleanup.ps1 -DevCaches        # Clean dev tool caches
  .\disk_cleanup.ps1 -DevCaches -Tool gradle  # Clean specific tool

Examples:
  # 1. Check current status
  .\disk_cleanup.ps1 -Report

  # 2. Analyze large files
  .\disk_cleanup.ps1 -Analyze

  # 3. Preview cleanup (recommended)
  .\disk_cleanup.ps1 -Cleanup -DryRun
  .\disk_cleanup.ps1 -Cleanup -IncludeDev -DryRun

  # 4. Execute cleanup
  .\disk_cleanup.ps1 -Cleanup -Force
  .\disk_cleanup.ps1 -Cleanup -IncludeDev -Force

  # 5. Clean dev caches only
  .\disk_cleanup.ps1 -DevCaches -DryRun
  .\disk_cleanup.ps1 -DevCaches -Tool gradle -Force

"@ -ForegroundColor Cyan
}

# Show help if no parameters
$hasParam = $Report -or $Analyze -or $Cleanup -or $DevCaches
if (-not $hasParam) {
    Show-Help
    exit 0
}

if ($Report) {
    Write-Host "Generating disk usage report..." -ForegroundColor Cyan
    & "$scriptDir\generate_cleanup_report.ps1"
}

if ($Analyze) {
    Write-Host "Analyzing disk space usage..." -ForegroundColor Cyan
    & "$scriptDir\analyze_disk.ps1" -Path "C:\" -TopN 20
}

if ($Cleanup) {
    Write-Host "Cleaning temporary files..." -ForegroundColor Cyan
    $params = @()
    if ($DryRun) { $params += "-DryRun" }
    if ($Force) { $params += "-Force" }
    if ($IncludeDev) { $params += "-IncludeDevCaches" }

    & "$scriptDir\cleanup_temp.ps1" @params
}

if ($DevCaches) {
    Write-Host "Cleaning dev tool caches..." -ForegroundColor Cyan
    $params = @()
    if ($Tool -ne "all") { $params += "-Tool", $Tool }
    if ($DryRun) { $params += "-DryRun" }
    if ($Force) { $params += "-Force" }

    & "$scriptDir\cleanup_dev_caches.ps1" @params
}

Write-Host "`nDone!" -ForegroundColor Green
