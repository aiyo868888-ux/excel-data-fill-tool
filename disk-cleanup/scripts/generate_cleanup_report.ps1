# 磁盘清理报告生成脚本
param(
    [string]$OutputPath = "$env:TEMP\disk-cleanup-report.html"
)

$drive = Get-PSDrive C
$usedGB = [math]::Round($drive.Used / 1GB, 2)
$freeGB = [math]::Round($drive.Free / 1GB, 2)
$totalGB = [math]::Round(($drive.Used + $drive.Free) / 1GB, 2)
$usedPercent = [math]::Round(($drive.Used / ($drive.Used + $drive.Free)) * 100, 1)

$html = @"
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>磁盘清理报告</title>
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #333; border-bottom: 2px solid #0078d4; padding-bottom: 10px; }
        .summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }
        .metric { background: #f8f9fa; padding: 15px; border-radius: 6px; text-align: center; border-left: 4px solid #0078d4; }
        .metric-label { color: #666; font-size: 14px; margin-bottom: 5px; }
        .metric-value { color: #333; font-size: 24px; font-weight: bold; }
        .progress-bar { width: 100%; height: 30px; background: #e0e0e0; border-radius: 15px; overflow: hidden; margin: 20px 0; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #0078d4, #00bcf2); display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; }
        .section { margin: 30px 0; }
        .section h2 { color: #0078d4; border-left: 4px solid #0078d4; padding-left: 10px; }
        .recommendation { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 10px 0; border-radius: 4px; }
        .recommendation.warning { background: #f8d7da; border-left-color: #dc3545; }
        .recommendation.success { background: #d4edda; border-left-color: #28a745; }
        .timestamp { color: #999; font-size: 12px; text-align: right; margin-top: 30px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>磁盘清理报告</h1>
        <p>C 盘使用情况分析</p>

        <div class="summary">
            <div class="metric">
                <div class="metric-label">总容量</div>
                <div class="metric-value">$totalGB GB</div>
            </div>
            <div class="metric">
                <div class="metric-label">已使用</div>
                <div class="metric-value">$usedGB GB</div>
            </div>
            <div class="metric">
                <div class="metric-label">可用空间</div>
                <div class="metric-value">$freeGB GB</div>
            </div>
        </div>

        <div class="progress-bar">
            <div class="progress-fill" style="width: $usedPercent%">
                $usedPercent%
            </div>
        </div>

        <div class="section">
            <h2>清理建议</h2>
"@

if ($usedPercent -gt 90) {
    $html += @"
            <div class="recommendation warning">
                <strong>严重警告</strong>: 磁盘使用率超过 90%，强烈建议立即执行以下操作：
                <ul>
                    <li>运行磁盘清理工具 (cleanmgr)</li>
                    <li>清理 Windows 更新缓存</li>
                    <li>卸载不需要的程序</li>
                    <li>使用本工具的开发缓存清理功能</li>
                </ul>
            </div>
"@
} elseif ($usedPercent -gt 75) {
    $html += @"
            <div class="recommendation">
                <strong>建议</strong>: 磁盘使用率超过 75%，建议执行以下操作：
                <ul>
                    <li>清理浏览器缓存</li>
                    <li>清理系统临时文件</li>
                    <li>清理开发工具缓存 (Gradle, npm, pip)</li>
                </ul>
            </div>
"@
} else {
    $html += @"
            <div class="recommendation success">
                <strong>良好</strong>: 磁盘使用率正常。定期清理即可保持系统健康。
            </div>
"@
}

$html += @"
        </div>

        <div class="section">
            <h2>可清理项目估算</h2>
            <table style="width: 100%; border-collapse: collapse;">
                <tr style="background: #f8f9fa;">
                    <th style="padding: 10px; text-align: left; border: 1px solid #dee2e6;">项目</th>
                    <th style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">预估大小</th>
                    <th style="padding: 10px; text-align: left; border: 1px solid #dee2e6;">风险</th>
                </tr>
                <tr>
                    <td style="padding: 10px; border: 1px solid #dee2e6;">Windows 临时文件</td>
                    <td style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">500 MB - 2 GB</td>
                    <td style="padding: 10px; border: 1px solid #dee2e6; color: #28a745;">安全</td>
                </tr>
                <tr>
                    <td style="padding: 10px; border: 1px solid #dee2e6;">浏览器缓存</td>
                    <td style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">200 MB - 1 GB</td>
                    <td style="padding: 10px; border: 1px solid #dee2e6; color: #28a745;">安全</td>
                </tr>
                <tr>
                    <td style="padding: 10px; border: 1px solid #dee2e6;">Gradle 缓存</td>
                    <td style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">1 GB - 5 GB</td>
                    <td style="padding: 10px; border: 1px solid #dee2e6; color: #ffc107;">中（需重新下载依赖）</td>
                </tr>
                <tr>
                    <td style="padding: 10px; border: 1px solid #dee2e6;">npm 缓存</td>
                    <td style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">500 MB - 2 GB</td>
                    <td style="padding: 10px; border: 1px solid #dee2e6; color: #ffc107;">中（需重新下载依赖）</td>
                </tr>
                <tr>
                    <td style="padding: 10px; border: 1px solid #dee2e6;">Claude Code 临时文件</td>
                    <td style="padding: 10px; text-align: right; border: 1px solid #dee2e6;">100 MB - 500 MB</td>
                    <td style="padding: 10px; border: 1px solid #dee2e6; color: #28a745;">安全</td>
                </tr>
            </table>
        </div>

        <div class="timestamp">
            报告生成时间: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
        </div>
    </div>
</body>
</html>
"@

$html | Out-File -FilePath $OutputPath -Encoding UTF8

Write-Host "报告已生成: $OutputPath" -ForegroundColor Green
Invoke-Item $OutputPath
