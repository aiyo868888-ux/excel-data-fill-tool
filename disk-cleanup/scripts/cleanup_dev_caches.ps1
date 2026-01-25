# 开发工具缓存清理脚本
param(
    [switch]$DryRun,
    [switch]$Force,
    [string]$Tool = "all"  # all, gradle, npm, pip, maven
)

$ErrorActionPreference = "SilentlyContinue"

$cachePaths = @{
    "gradle" = @(
        "$env:USERPROFILE\.gradle\caches",
        "$env:USERPROFILE\.gradle\wrapper"
    )
    "npm" = @(
        "$env:USERPROFILE\.npm",
        "$env:LOCALAPPDATA\npm-cache"
    )
    "pip" = @(
        "$env:USERPROFILE\.pip\cache",
        "$env:LOCALAPPDATA\pip\cache"
    )
    "maven" = @(
        "$env:USERPROFILE\.m2\repository"
    )
    "yarn" = @(
        "$env:LOCALAPPDATA\Yarn\cache"
    )
    "cargo" = @(
        "$env:USERPROFILE\.cargo\registry"
    )
}

function Get-FolderSize {
    param([string]$Path)
    if (Test-Path $Path) {
        (Get-ChildItem -Path $Path -Recurse -ErrorAction SilentlyContinue |
            Measure-Object -Property Length -Sum -ErrorAction SilentlyContinue).Sum
    } else {
        0
    }
}

function Remove-CacheFolder {
    param(
        [string]$Path,
        [string]$ToolName
    )

    if (-not (Test-Path $Path)) {
        return
    }

    $size = Get-FolderSize -Path $Path
    $sizeMB = [math]::Round($size / 1MB, 2)

    if ($sizeMB -lt 1) {
        return
    }

    Write-Host "`n$ToolName 缓存" -ForegroundColor Cyan
    Write-Host "路径: $Path" -ForegroundColor Gray
    Write-Host "大小: $sizeMB MB" -ForegroundColor Yellow

    if ($DryRun) {
        Write-Host "[预览] 将删除" -ForegroundColor DarkYellow
        return
    }

    if (-not $Force) {
        $confirm = Read-Host "确认删除? (Y/N/A全部)"
        if ($confirm -ne "Y" -and $confirm -ne "A") {
            Write-Host "跳过" -ForegroundColor Gray
            return
        }
    }

    try {
        Remove-Item -Path $Path -Recurse -Force -ErrorAction Stop
        Write-Host "已清理: $sizeMB MB" -ForegroundColor Green
    } catch {
        Write-Host "清理失败: $_" -ForegroundColor Red
    }
}

Write-Host "=== 开发工具缓存清理 ===" -ForegroundColor Cyan
Write-Host "模式: $(if ($DryRun) { '预览模式' } else { '实际清理' })" -ForegroundColor Yellow
Write-Host ""

$toolsToClean = if ($Tool -eq "all") {
    $cachePaths.Keys
} else {
    @($Tool)
}

foreach ($tool in $toolsToClean) {
    if ($cachePaths.ContainsKey($tool)) {
        Write-Host "`n--- 清理 $tool 缓存 ---" -ForegroundColor Cyan
        foreach ($path in $cachePaths[$tool]) {
            Remove-CacheFolder -Path $path -ToolName $tool.ToUpper()
        }
    }
}

Write-Host "`n=== 清理完成 ===" -ForegroundColor Green
if ($DryRun) {
    Write-Host "预览模式，未实际删除文件。使用 -Force 参数执行实际清理。" -ForegroundColor Yellow
}
