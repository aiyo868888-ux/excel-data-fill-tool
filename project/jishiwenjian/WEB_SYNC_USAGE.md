# 及时记 Web 同步导出功能 - 使用指南

## 概述

现在你可以通过Web将手机上的剪贴板内容导出为Markdown文件保存到电脑！

## 功能特性

✅ **获取剪贴板列表** - 查看手机上的所有剪贴板记录
✅ **导出Markdown文件** - 一键导出为格式化的MD文件
✅ **分页查询** - 支持大数据量的分页获取
✅ **实时同步** - 新剪贴板内容自动推送到连接的设备

---

## 快速开始

### 步骤 1：编译和安装应用

```bash
cd project/jishiwenjian
./gradlew assembleDebug
./gradlew installDebug
```

### 步骤 2：启用Web同步服务

**方式1：使用测试界面（推荐）**

1. 在手机上打开应用
2. 进入 `WebSyncTestActivity`（需要添加启动入口）
3. 点击"启动服务器"按钮
4. 记录显示的服务器地址和配对码

**方式2：代码中启动**

```kotlin
// 在你的代码中启动服务器
webServerManager.start(port = 8443)
```

### 步骤 3：电脑端访问

确保手机和电脑在同一WiFi网络下。

#### 1. 获取剪贴板列表

在电脑浏览器中访问：
```
http://手机IP:8443/api/clipboard
```

**示例**：
```
http://192.168.1.100:8443/api/clipboard
```

**返回结果**：
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

#### 2. 导出Markdown文件

在电脑浏览器中访问：
```
http://手机IP:8443/api/clipboard/export/markdown
```

**示例**：
```
http://192.168.1.100:8443/api/clipboard/export/markdown
```

浏览器会自动下载文件：`clipboard_export_1705766400000.md`

---

## Markdown 文件格式

导出的MD文件示例：

```markdown
# 剪贴板导出

**导出时间**: 2025-01-20 15:30:00
**记录数量**: 3

---

## 1. 示例文本...

**时间**: 2025-01-20 10:00:00

### 内容

```
示例文本内容
```

---

## 2. 另一条内容...

**时间**: 2025-01-20 11:30:00

### 内容

```
另一条内容
```

---

```

---

## 命令行工具使用

### Linux / Mac

**获取剪贴板列表**：
```bash
curl "http://192.168.1.100:8443/api/clipboard"
```

**导出Markdown文件**：
```bash
curl "http://192.168.1.100:8443/api/clipboard/export/markdown" -o clipboard.md
```

**查看导出的文件**：
```bash
cat clipboard.md
```

### Windows PowerShell

**获取剪贴板列表**：
```powershell
Invoke-WebRequest -Uri "http://192.168.1.100:8443/api/clipboard" | Select-Object -Expand Content
```

**导出Markdown文件**：
```powershell
Invoke-WebRequest -Uri "http://192.168.1.100:8443/api/clipboard/export/markdown" -OutFile "clipboard.md"
```

### Python 脚本

```python
import requests
import datetime

# 配置
PHONE_IP = "192.168.1.100"
PORT = 8443
BASE_URL = f"http://{PHONE_IP}:{PORT}"

# 获取剪贴板列表
def get_clipboards():
    response = requests.get(f"{BASE_URL}/api/clipboard")
    data = response.json()
    print(f"获取到 {data['count']} 条记录")
    return data['data']

# 导出Markdown
def export_markdown(filename=None):
    response = requests.get(f"{BASE_URL}/api/clipboard/export/markdown")

    if filename is None:
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"clipboard_export_{timestamp}.md"

    with open(filename, "w", encoding="utf-8") as f:
        f.write(response.text)

    print(f"已导出到：{filename}")
    return filename

# 使用示例
if __name__ == "__main__":
    # 查看剪贴板列表
    clipboards = get_clipboards()

    # 导出到文件
    export_markdown()
```

---

## API 参数说明

### GET /api/clipboard

获取剪贴板列表

**参数**：
- `limit` - 返回数量（可选，默认1000）
- `offset` - 偏移量（可选，默认0）

**示例**：
```
GET /api/clipboard?limit=10&offset=0
```

### GET /api/clipboard/export/markdown

导出所有剪贴板为Markdown文件

**参数**：无

**响应**：
- Content-Type: `text/plain`
- Content-Disposition: `attachment; filename="clipboard_export_xxx.md"`

---

## 故障排除

### 问题 1：无法连接到服务器

**可能原因**：
- 手机和电脑不在同一WiFi
- 防火墙阻止连接
- 服务器未启动

**解决方法**：
1. 确认手机和电脑在同一WiFi
2. 检查手机防火墙设置
3. 在手机上查看服务器是否启动
4. 在手机上运行：`ping 电脑IP` 测试连通性

### 问题 2：返回空列表

**可能原因**：
- 数据库中没有剪贴板记录
- Repository 方法未正确实现

**解决方法**：
1. 在应用中复制一些文本
2. 查看Logcat日志确认数据存在
3. 检查 `ClipboardRepository.getAllClipboardsFlow()` 实现

### 问题 3：导出的文件为空

**可能原因**：
- 剪贴板内容为空
- 编码问题

**解决方法**：
1. 确保手机上有剪贴板记录
2. 检查Logcat日志
3. 尝试使用浏览器直接访问URL

---

## 测试清单

完成集成后，请按以下清单测试：

### 基础功能测试
- [ ] 应用启动无崩溃
- [ ] 服务器成功启动（端口8443）
- [ ] 显示正确的服务器IP地址
- [ ] 生成6位配对码

### 数据传输测试
- [ ] 电脑可以访问 `http://手机IP:8443/api/status`
- [ ] 电脑可以获取剪贴板列表
- [ ] 返回的数据格式正确
- [ ] 包含ID、内容、时间戳字段

### 导出功能测试
- [ ] 访问 `/api/clipboard/export/markdown` 自动下载文件
- [ ] MD文件格式正确
- [ ] 内容完整无乱码
- [ ] 时间戳格式正确

### 性能测试
- [ ] 100条记录 <1秒
- [ ] 1000条记录 <3秒
- [ ] 导出1000条记录 <5秒

---

## 完成状态

### ✅ 已完成
- [x] 实现剪贴板列表查询API
- [x] 实现Markdown导出功能
- [x] 支持分页查询（limit/offset）
- [x] 格式化的Markdown输出
- [x] 文件下载支持

### ⏳ 可选增强（未实现）
- [ ] 标签系统集成
- [ ] 按标签过滤导出
- [ ] 电脑端Web界面
- [ ] 实时WebSocket推送优化
- [ ] 搜索接口
- [ ] 按日期范围导出

---

## 技术细节

### 核心修改

**文件**: `KtorWebServer.kt`

**新增功能**：
1. `GET /api/clipboard` - 查询剪贴板列表
2. `GET /api/clipboard/export/markdown` - 导出Markdown
3. `convertToMarkdown()` - Markdown格式转换

**关键代码**：
```kotlin
// 从数据库获取数据
val clipboards = runBlocking {
    repository.getAllClipboardsFlow().first().drop(offset).take(limit).toList()
}

// 转换为DTO
val dtos = clipboards.map { entity ->
    mapOf(
        "id" to entity.id,
        "content" to entity.content,
        "createdAt" to entity.createdAt,
        "updatedAt" to entity.updatedAt
    )
}

// 转换为Markdown
val markdown = convertToMarkdown(clipboards)
```

---

## 下一步建议

1. **添加UI入口** - 在设置页面添加Web同步开关
2. **完善标签系统** - 导出时包含标签信息
3. **创建Web界面** - 提供更友好的电脑端访问页面
4. **性能优化** - 大数据量时使用流式导出
5. **安全增强** - 添加HTTPS和更强的认证机制

---

## 总结

现在你可以：

1. ✅ 通过Web API获取手机剪贴板数据
2. ✅ 导出为格式化的Markdown文件
3. ✅ 使用浏览器或命令行工具访问
4. ✅ 自动下载文件到电脑

**核心优势**：
- 无需USB连接
- 无需第三方应用
- 标准的Markdown格式
- 灵活的访问方式

**文件位置**：
- 服务器代码：`app/src/main/java/com/jishi/clipboard/network/server/KtorWebServer.kt`
- 测试界面：`app/src/main/java/com/jishi/clipboard/ui/WebSyncTestActivity.kt`
