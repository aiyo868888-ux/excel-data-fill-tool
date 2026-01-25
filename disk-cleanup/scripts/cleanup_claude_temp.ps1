# Claude Code Temp File Cleanup
param(
    [string]$BasePath = (Get-Location).Path,
    [switch]$DryRun,
    [switch]$Force
)

Write-Host "=== Claude Code Temp File Cleanup ===" -ForegroundColor Cyan
Write-Host "Scanning path: $BasePath" -ForegroundColor Gray
Write-Host ""

# Find all tmpclaude-*-cwd directories
$tempDirs = Get-ChildItem -Path $BasePath -Recurse -Filter "tmpclaude-*-cwd" -Directory -ErrorAction SilentlyContinue

if ($tempDirs.Count -eq 0) {
    Write-Host "No Claude Code temp directories found" -ForegroundColor Green
    return
}

$totalSize = 0
Write-Host "Found $($tempDirs.Count) temp directories" -ForegroundColor Yellow

foreach ($dir in $tempDirs) {
    $size = (Get-ChildItem -Path $dir.FullName -Recurse -ErrorAction SilentlyContinue |
        Measure-Object -Property Length -Sum -ErrorAction SilentlyContinue).Sum
    $sizeMB = [math]::Round($size / 1MB, 2)
    $totalSize += $size

    Write-Host "`nDirectory: $($dir.Name)" -ForegroundColor Cyan
    Write-Host "Path: $($dir.FullName)" -ForegroundColor Gray
    Write-Host "Size: $sizeMB MB" -ForegroundColor Yellow

    if (-not $DryRun) {
        if (-not $Force) {
            $confirm = Read-Host "Confirm deletion? (Y/N/A=All)"
            if ($confirm -ne "Y" -and $confirm -ne "A") {
                Write-Host "Skipped" -ForegroundColor Gray
                continue
            }
        }

        try {
            Remove-Item -Path $dir.FullName -Recurse -Force
            Write-Host "Deleted" -ForegroundColor Green
        } catch {
            Write-Host "Deletion failed: $_" -ForegroundColor Red
        }
    }
}

$totalSizeMB = [math]::Round($totalSize / 1MB, 2)
Write-Host "`nTotal: $totalSizeMB MB" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "`nPreview mode - no files deleted. Use -Force to execute actual cleanup." -ForegroundColor Yellow
}
