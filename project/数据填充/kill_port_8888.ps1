# PowerShell脚本：清理占用8888端口的所有进程
Get-NetTCPConnection -LocalPort 8888 -ErrorAction SilentlyContinue | ForEach-Object {
    $pid = $_.OwningProcess
    if ($pid) {
        Write-Host "终止进程 PID: $pid"
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
    }
}
Write-Host "清理完成！"
