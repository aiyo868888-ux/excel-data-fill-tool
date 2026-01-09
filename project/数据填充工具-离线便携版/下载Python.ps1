# 自动下载便携版 Python
# 用于创建完全离线的便携版应用

Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "   便携版 Python 自动下载脚本"        -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

# 获取最新版本号
Write-Host "正在获取最新 Python 版本..." -ForegroundColor Yellow
try {
    $url = "https://www.python.org/downloads/windows/"
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing
    $content = $response.Content

    # 查找 embeddable package 下载链接
    if ($content -match 'python-3\.11\.\d+-embed-amd64\.zip') {
        $version = $matches[0] -replace 'python-', '' -replace '-embed-amd64.zip', ''
        Write-Host "✅ 找到最新版本: Python $version" -ForegroundColor Green
    } else {
        Write-Host "⚠️  无法自动检测版本，使用默认版本 3.11.9" -ForegroundColor Yellow
        $version = "3.11.9"
    }
} catch {
    Write-Host "⚠️  网络请求失败，使用默认版本 3.11.9" -ForegroundColor Yellow
    $version = "3.11.9"
}

# 下载 URL
$downloadUrl = "https://www.python.org/ftp/python/$version/python-$version-embed-amd64.zip"
$outputFile = "python-$version-embed-amd64.zip"

Write-Host ""
Write-Host "下载地址: $downloadUrl" -ForegroundColor Cyan
Write-Host "保存位置: $outputFile" -ForegroundColor Cyan
Write-Host ""

# 检查是否已下载
if (Test-Path $outputFile) {
    Write-Host "⚠️  文件已存在: $outputFile" -ForegroundColor Yellow
    $choice = Read-Host "是否重新下载？(Y/N)"
    if ($choice -ne "Y" -and $choice -ne "y") {
        Write-Host "跳过下载，使用现有文件" -ForegroundColor Green
    } else {
        Remove-Item $outputFile -Force
        goto Download
    }
} else {
    Download:
    Write-Host "正在下载..." -ForegroundColor Yellow

    try {
        # 使用 WebClient 下载（显示进度）
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadFileAsync($downloadUrl, $outputFile)

        # 显示下载进度
        while ($webClient.IsBusy) {
            Start-Sleep -Milliseconds 100
            Write-Host "." -NoNewline
        }
        Write-Host ""

        Write-Host "✅ 下载完成！" -ForegroundColor Green
    } catch {
        Write-Host "❌ 下载失败: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "请手动下载:" -ForegroundColor Yellow
        Write-Host "1. 访问: https://www.python.org/downloads/windows/"
        Write-Host "2. 下载: Windows embeddable package (64-bit)"
        Write-Host "3. 解压到当前目录的 python\ 文件夹"
        pause
        exit 1
    }
}

# 解压文件
Write-Host ""
Write-Host "正在解压文件..." -ForegroundColor Yellow

if (Test-Path "python") {
    Write-Host "⚠️  python 文件夹已存在" -ForegroundColor Yellow
    $choice = Read-Host "是否删除并重新解压？(Y/N)"
    if ($choice -eq "Y" -or $choice -eq "y") {
        Remove-Item "python" -Recurse -Force
    } else {
        Write-Host "跳过解压" -ForegroundColor Green
        goto ModifyPTH
    }
}

# 使用 PowerShell 5.0+ 的 Expand-Archive
try {
    Expand-Archive -Path $outputFile -DestinationPath "python" -Force
    Write-Host "✅ 解压完成！" -ForegroundColor Green
} catch {
    Write-Host "⚠️  PowerShell 解压失败，尝试使用 7-Zip..." -ForegroundColor Yellow

    # 查找 7-Zip
    $7zipPaths = @(
        "${env:ProgramFiles}\7-Zip\7z.exe",
        "${env:ProgramFiles(x86)}\7-Zip\7z.exe"
    )

    $7zip = $null
    foreach ($path in $7zipPaths) {
        if (Test-Path $path) {
            $7zip = $path
            break
        }
    }

    if ($7zip) {
        & $7zip x "-o$PSScriptRoot\python" $outputFile -y
        Write-Host "✅ 解压完成！" -ForegroundColor Green
    } else {
        Write-Host "❌ 找不到解压工具" -ForegroundColor Red
        Write-Host "请手动解压 $outputFile 到 python\ 文件夹"
        pause
        exit 1
    }
}

ModifyPTH:
# 修改 python311._pth 文件
Write-Host ""
Write-Host "正在配置 Python..." -ForegroundColor Yellow

$pthFile = "python\python311._pth"

if (Test-Path $pthFile) {
    # 读取文件
    $content = Get-Content $pthFile

    # 检查是否已经修改过
    if ($content -match "Lib/site-packages") {
        Write-Host "✅ 配置文件已修改，跳过" -ForegroundColor Green
    } else {
        # 备份原文件
        Copy-Item $pthFile "$pthFile.bak" -Force

        # 修改内容
        $newContent = @"
python311.zip
.
Lib/site-packages

# 启用 site-packages
import site
"@

        Set-Content -Path $pthFile -Value $newContent -Encoding UTF8
        Write-Host "✅ 配置完成！" -ForegroundColor Green
    }
} else {
    Write-Host "❌ 找不到 $pthFile" -ForegroundColor Red
    pause
    exit 1
}

# 验证安装
Write-Host ""
Write-Host "正在验证安装..." -ForegroundColor Yellow

if (Test-Path "python\python.exe") {
    $versionOutput = & "python\python.exe" --version 2>&1
    Write-Host "✅ Python 版本: $versionOutput" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================"  -ForegroundColor Cyan
    Write-Host "   安装成功！"                       -ForegroundColor Green
    Write-Host "========================================"  -ForegroundColor Cyan
    Write-Host ""
    Write-Host "下一步：" -ForegroundColor Yellow
    Write-Host "1. 运行: 安装依赖.bat" -ForegroundColor White
    Write-Host "2. 运行: 复制应用文件.bat" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "❌ 验证失败：找不到 python\python.exe" -ForegroundColor Red
    pause
    exit 1
}

pause
