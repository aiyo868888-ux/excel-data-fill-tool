# 磁盘空间分析脚本
param(
    [string]$Path = "C:\",
    [int]$TopN = 20,
    [switch]$ByFolder
)

$ErrorActionPreference = "SilentlyContinue"

Write-Host "正在扫描 $Path 的大文件..." -ForegroundColor Cyan

if ($ByFolder) {
    # 按文件夹统计
    $folders = Get-ChildItem -Path $Path -Recurse -Directory -ErrorAction SilentlyContinue |
        ForEach-Object {
            $size = (Get-ChildItem -Path $_.FullName -Recurse -File -ErrorAction SilentlyContinue |
                Measure-Object -Property Length -Sum).Sum
            [PSCustomObject]@{
                Path = $_.FullName
                SizeMB = [math]::Round($size / 1MB, 2)
            }
        } | Sort-Object SizeMB -Descending | Select-Object -First $TopN

    $folders | Format-Table -AutoSize
} else {
    # 按文件统计
    $files = Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Length -gt 100MB } |
        Sort-Object Length -Descending |
        Select-Object -First $TopN |
        ForEach-Object {
            [PSCustomObject]@{
                Path = $_.FullName
                SizeGB = [math]::Round($_.Length / 1GB, 2)
                SizeMB = [math]::Round($_.Length / 1MB, 2)
                Modified = $_.LastWriteTime
            }
        }

    $files | Format-Table -AutoSize

    # 汇总信息
    $totalSize = ($files | Measure-Object -Property SizeGB -Sum).Sum
    Write-Host "`n前 $TopN 个文件总计: $([math]::Round($totalSize, 2)) GB" -ForegroundColor Yellow
}
