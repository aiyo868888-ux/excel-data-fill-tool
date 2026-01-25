# 磁盘清理常见问题

## 清理后空间未立即释放？

**原因**: Windows 文件系统延迟释放、回收站未清空、系统还原点占用。

**解决**:
```powershell
# 1. 清空回收站
Clear-RecycleBin -Force

# 2. 重启计算机（最重要）
Restart-Computer

# 3. 如果仍占用，运行磁盘检查（需重启）
chkdsk C: /F /R
```

## 清理 Gradle 缓存后项目构建失败？

**原因**: Gradle 需要重新下载依赖。

**解决**:
```powershell
# 清理项目后重新同步
cd project\jishiwenjian
.\gradlew --refresh-dependencies
```

**影响**: 仅首次构建慢，后续正常。

## 清理 npm 缓存后 npm install 失败？

**原因**: npm 缓存损坏（清理前已损坏）。

**解决**:
```powershell
# 验证缓存
npm cache verify

# 强制重新安装
rm -r node_modules
npm install
```

## Claude Code 临时文件占用多少空间？

**典型**: 每个会话 50-200 MB，长期不清理可达数 GB。

**建议**: 每周清理一次，或使用 `.gitignore` 规则自动忽略。

## 何时清理开发缓存？

**推荐时机**:
- 项目开发完成，准备归档
- 切换到其他项目长期开发
- 磁盘空间严重不足（>90%）
- 清理 IDE/工具后统一清理依赖

**不推荐时机**:
- 正在积极开发的项目
- 网络不稳定或流量受限
- 需要频繁离线构建

## 系统临时文件可以手动删除吗？

**不推荐直接删除**，原因：
- 部分文件正在被使用
- 删除后可能导致应用异常
- 难以区分哪些可删除

**推荐使用脚本**，自动处理安全性和权限问题。

## 磁盘使用率持续增长？

**常见原因**:
1. 系统还原点过多
2. Windows Update 旧文件残留
3. 虚拟内存/休眠文件过大
4. 日志文件未清理

**诊断**:
```powershell
# 查看还原点占用
vssadmin list shadowstorage

# 查看休眠文件大小
dir C:\hiberfil.sys -Force

# 查看页面文件大小
dir C:\pagefile.sys -Force
```

## 高级清理选项

### 减少系统还原点占用
```powershell
# 限制还原点最大空间为 10GB
vssadmin resize shadowstorage /For=C: /On=C: /MaxSize=10GB
```

### 禁用休眠（如果不需要）
```powershell
# 释放数 GB 空间
powercfg /hibernate off
```

### 清理 Windows 更新旧文件
```powershell
# 以管理员身份运行
dism /online /cleanup-image /superseded
dism /online /cleanup-image /startcomponentcleanup
```

### 清理 Windows Store 缓存
```powershell
wsreset.exe
```

## 自动化定期清理

**创建任务计划**:
```powershell
# 创建每月 1 号凌晨 2 点执行清理的任务
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File 'D:\disk-cleanup\scripts\cleanup_temp.ps1' -Force"
$trigger = New-ScheduledTaskTrigger -Monthly -DaysOfMonth 1 -At 2am
Register-ScheduledTask -TaskName "MonthlyDiskCleanup" -Action $action -Trigger $trigger -Description "自动磁盘清理"
```

## 监控磁盘使用率

**邮件警报脚本**（当使用率 >80%）:
```powershell
$drive = Get-PSDrive C
$usedPercent = ($drive.Used / ($drive.Used + $drive.Free)) * 100

if ($usedPercent -gt 80) {
    $body = "C 盘使用率: $($usedPercent.ToString('0.00'))%`n可用空间: $([math]::Round($drive.Free/1GB,2)) GB"
    Send-MailMessage -To "admin@example.com" -Subject "磁盘空间警告" -Body $body -SmtpServer "smtp.example.com"
}
```

## 恢复误删文件

**检查回收站**:
```powershell
Get-ChildItem C:\`$Recycle.Bin\ -Recurse -Force
```

**使用专业工具**（如已删除且回收站已清空）:
- Recuva（免费）
- EaseUS Data Recovery
- Disk Drill

**建议**: 重要文件清理前务必备份。
