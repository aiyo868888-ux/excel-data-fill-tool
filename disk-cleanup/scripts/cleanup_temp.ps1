# 临时文件清理脚本（安全模式）
param(
    [switch]$DryRun,
    [switch]$Force,
    [switch]$IncludeDevCaches
)

$ErrorActionPreference = "SilentlyContinue"

# 安全清理路径列表
$tempPaths = @(
    "$env:TEMP",
    "$env:WINDIR\Temp",
    "$env:USERPROFILE\AppData\Local\Temp",
    "$env:USERPROFILE\AppData\Local\Google\Chrome\User Data\Default\Cache",
    "$env:USERPROFILE\AppData\Local\Microsoft\Windows\INetCache"
)

# 开发缓存路径
$devCachePaths = @(
    "$env:USERPROFILE\.gradle\caches",
    "$env:USERPROFILE\.npm",
    "$env:USERPROFILE\.pip\cache",
    "$env:USERPROFILE\.m2\repository",
    "$env:LOCALAPPDATA\Pip"
)

# Claude Code 临时文件
$claudeTempPaths = @(
    "$env:USERPROFILE\AppData\Local\Temp\tmpclaude-*-cwd"
)

function Get-FolderSize {
    param([string]$Path)
    if (Test-Path $Path) {
        (Get-ChildItem -Path $Path -Recurse -ErrorAction SilentlyContinue |
            Measure-Object -Property Length -Sum -ErrorAction SilentlyContinue).Sum
    } else {
        0
    }
}

function Remove-SafeTemp {
    param([string]$Path, [string]$Description)

    if (-not (Test-Path $Path)) {
        return
    }

    $size = Get-FolderSize -Path $Path
    $sizeMB = [math]::Round($size / 1MB, 2)

    if ($sizeMB -lt 1) {
        return
    }

    Write-Host "`n发现: $Description" -ForegroundColor Cyan
    Write-Host "路径: $Path" -ForegroundColor Gray
    Write-Host "大小: $sizeMB MB" -ForegroundColor Yellow

    if ($DryRun) {
        Write-Host "[预览] 将删除此目录内容" -ForegroundColor DarkYellow
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
        Remove-Item -Path "$Path\*" -Recurse -Force -ErrorAction Stop
        Write-Host "已清理: $sizeMB MB" -ForegroundColor Green
    } catch {
        Write-Host "清理失败: $_" -ForegroundColor Red
    }
}

# 开始清理
Write-Host "=== Windows 临时文件清理 ===" -ForegroundColor Cyan
Write-Host "模式: $(if ($DryRun) { '预览模式' } else { '实际清理' })" -ForegroundColor Yellow
Write-Host ""

# 清理系统临时文件
foreach ($path in $tempPaths) {
    $desc = switch -Wildcard ($path) {
        "*Temp*" { "系统临时文件" }
        "*Chrome*" { "Chrome 缓存" }
        "*INetCache*" { "IE 缓存" }
        default { "临时文件" }
    }
    Remove-SafeTemp -Path $path -Description $desc
}

# 清理开发缓存（可选）
if ($IncludeDevCaches) {
    Write-Host "`n=== 开发工具缓存 ===" -ForegroundColor Cyan

    foreach ($path in $devCachePaths) {
        $desc = switch -Wildcard ($path) {
            "*gradle*" { "Gradle 缓存" }
            "*npm*" { "npm 缓存" }
            "*pip*" { "pip 缓存" }
            "*m2*" { "Maven 仓库" }
            default { "开发缓存" }
        }
        Remove-SafeTemp -Path $path -Description $desc
    }
}

# 清理 Claude Code 临时文件
Write-Host "`n=== Claude Code 临时文件 ===" -ForegroundColor Cyan
$claudeTemps = Get-ChildItem -Path "$env:TEMP" -Filter "tmpclaude-*-cwd" -Directory -ErrorAction SilentlyContinue
foreach ($tempDir in $claudeTemps) {
    Remove-SafeTemp -Path $tempDir.FullName -Description "Claude Code 临时目录"
}

Write-Host "`n=== 清理完成 ===" -ForegroundColor Green
if ($DryRun) {
    Write-Host "预览模式，未实际删除文件。使用 -Force 参数执行实际清理。" -ForegroundColor Yellow
}
