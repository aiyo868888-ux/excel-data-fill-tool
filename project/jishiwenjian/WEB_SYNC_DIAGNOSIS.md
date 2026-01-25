# Web同步功能快速诊断指南

## 问题：ERR_CONNECTION_REFUSED

这个错误表示电脑无法连接到手机上的Web服务器。

---

## 快速诊断步骤

### 步骤 1：检查应用是否已安装最新版本

```bash
cd project/jishiwenjian
./gradlew uninstallDebug
./gradlew installDebug
```

### 步骤 2：启动Web同步测试Activity

**方式1：通过ADB启动（推荐）**

```bash
adb shell am start -n com.jishi.clipboard/.ui.WebSyncTestActivity
```

**方式2：在应用中手动启动**

在手机的设置界面或主界面找到"Web同步测试"入口并点击进入。

### 步骤 3：在应用中启动服务器

1. 点击"启动服务器"按钮
2. 查看显示的服务器地址（例如：`192.168.1.100:8443`）
3. 记录6位配对码

### 步骤 4：验证服务器已启动

在手机上查看Logcat日志：

```bash
adb logcat | grep WebServerManager
```

应该看到类似输出：
```
D/WebServerManager: 正在启动服务器, 端口=8443
I/WebServerManager: ✅ 服务器启动成功: https://192.168.1.100:8443
```

如果看到错误，记下错误信息。

### 步骤 5：检查WiFi连接

**在手机上**：
1. 设置 → WiFi → 确认已连接
2. 点击WiFi名称查看IP地址

**在电脑上**：
1. 确保连接到同一WiFi
2. 打开命令行，ping手机IP：
```bash
ping 192.168.1.100
```

如果ping不通，说明网络隔离，需要检查路由器设置。

### 步骤 6：测试服务器状态

在电脑浏览器中访问：
```
http://手机IP:8443/api/status
```

**预期返回**：
```json
{
  "running": true,
  "connections": 0,
  "activeTokens": 0,
  "mode": "HTTP (生产环境请使用HTTPS)"
}
```

### 步骤 7：测试剪贴板列表

访问：
```
http://手机IP:8443/api/clipboard
```

**预期返回**：
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "content": "剪贴板内容",
      "createdAt": 1705766400000,
      "updatedAt": 1705766400000
    }
  ],
  "count": 1,
  "total": 1
}
```

### 步骤 8：导出Markdown

访问：
```
http://手机IP:8443/api/clipboard/export/markdown
```

浏览器应该自动下载文件：`clipboard_export_xxx.md`

---

## 常见问题排查

### 问题1：Activity未找到

**症状**：
```
Error: Activity does not exist
```

**解决**：
1. 确认已重新编译安装应用
2. 检查 `AndroidManifest.xml` 中是否已注册 `WebSyncTestActivity`
3. 清除应用数据后重试：
```bash
adb shell pm clear com.jishi.clipboard
```

### 问题2：服务器启动失败

**症状**：
Logcat显示启动错误

**检查日志**：
```bash
adb logcat | grep -E "WebServerManager|KtorWebServer|Exception"
```

**可能原因**：
- 端口8443被占用
- 缺少网络权限
- 依赖注入失败

**解决方法**：
1. 尝试更改端口（在代码中修改 DEFAULT_PORT）
2. 检查应用权限设置
3. 确认Hilt依赖配置正确

### 问题3：ping不通

**可能原因**：
- 手机和电脑不在同一WiFi
- 路由器启用了AP隔离
- 防火墙阻止连接

**解决方法**：
1. 关闭路由器的AP隔离（访客网络通常有隔离）
2. 临时关闭防火墙测试
3. 使用USB网络共享（高级）

### 问题4：返回空列表

**症状**：
`/api/clipboard` 返回 `"data": []`

**原因**：
数据库中没有剪贴板记录

**解决**：
1. 在应用中复制一些文本
2. 查看历史记录确认数据已保存
3. 重新访问API

---

## 手动测试步骤（完整流程）

### 准备工作

1. 手机和电脑连接同一WiFi
2. 手机开启开发者选项和USB调试
3. 安装最新版应用

### 测试流程

```bash
# 1. 安装应用
./gradlew installDebug

# 2. 启动测试Activity
adb shell am start -n com.jishi.clipboard/.ui.WebSyncTestActivity

# 3. 查看日志（另一个终端）
adb logcat | grep -E "WebServerManager|KtorWebServer"

# 4. 在手机应用中点击"启动服务器"

# 5. 获取服务器地址（在应用界面显示）

# 6. 在电脑上测试（替换为实际IP）
# 测试状态
curl "http://192.168.1.100:8443/api/status"

# 获取剪贴板列表
curl "http://192.168.1.100:8443/api/clipboard"

# 导出Markdown
curl "http://192.168.1.100:8443/api/clipboard/export/markdown" -o test.md

# 7. 查看导出的文件
cat test.md
```

---

## 成功标志

✅ **应用界面**：
- 状态显示"运行中"（绿色）
- 服务器地址显示为 `xxx.xxx.xxx.xxx:8443`
- 配对码显示为6位数字

✅ **Logcat日志**：
```
I/WebServerManager: ✅ 服务器启动成功: https://192.168.1.100:8443
```

✅ **浏览器测试**：
- `/api/status` 返回 `{"running": true}`
- `/api/clipboard` 返回剪贴板数据
- `/api/clipboard/export/markdown` 自动下载MD文件

---

## 下一步

测试成功后，你可以：

1. **创建桌面快捷方式** - 保存常用命令为脚本
2. **设置定时导出** - 使用cron或Task Scheduler定期备份
3. **集成到工作流** - 与其他工具联动

---

## 仍然有问题？

如果按照以上步骤仍然无法连接，请提供以下信息：

1. Logcat完整日志（`adb logcat > log.txt`）
2. 电脑ping手机IP的结果
3. 浏览器开发者控制台的错误信息
4. 手机型号和Android版本
5. 电脑操作系统
