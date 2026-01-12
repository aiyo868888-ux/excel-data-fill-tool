# Python便携版下载指南

## 📥 下载步骤

### 方法1：官网下载（推荐）

1. **访问Python官网下载页面**
   ```
   https://www.python.org/downloads/windows/
   ```

2. **找到 "Windows embeddable package (64-bit)"**
   - 向下滚动到页面底部
   - 在 "Files" 区域找到最新版本（推荐 Python 3.11.x）
   - 点击下载 "Windows embeddable package (64-bit)"
   - 文件名类似：`python-3.11.9-embed-amd64.zip`

3. **下载完成后**
   - 将下载的 zip 文件放到项目目录：
     ```
     d:\claude code -11\project\数据填充\
     ```
   - 确保文件名为：`python-3.11.9-embed-amd64.zip`

4. **运行添加脚本**
   - 双击运行：`添加Python到绿色版.bat`
   - 脚本会自动解压Python到绿色版目录

### 方法2：直接下载链接

**Python 3.11.9（推荐）**
```
https://www.python.org/ftp/python/3.11.9/python-3.11.9-embed-amd64.zip
```

**Python 3.12.x（最新）**
```
https://www.python.org/ftp/python/3.12.7/python-3.12.7-embed-amd64.zip
```

### 方法3：PowerShell下载

在项目目录打开PowerShell，运行：

```powershell
# Python 3.11.9
Invoke-WebRequest -Uri "https://www.python.org/ftp/python/3.11.9/python-3.11.9-embed-amd64.zip" -OutFile "python-3.11.9-embed-amd64.zip"

# 或 Python 3.12.7
Invoke-WebRequest -Uri "https://www.python.org/ftp/python/3.12.7/python-3.12.7-embed-amd64.zip" -OutFile "python-3.12.7-embed-amd64.zip"
```

## ✅ 验证下载

下载完成后，确认：
- 文件大小约 25-30 MB
- 文件名格式正确：`python-x.x.x-embed-amd64.zip`
- 文件位于：`d:\claude code -11\project\数据填充\`

## 🚀 下一步

下载完成后，运行：
```
添加Python到绿色版.bat
```

脚本会自动：
1. 解压Python到绿色版
2. 配置Python环境
3. 更新启动脚本
4. 创建完整的绿色版

## 📦 完成后

绿色版将包含：
- ✅ 内置Python环境（无需安装）
- ✅ 自动检测并安装依赖
- ✅ 双击启动即可使用
- ✅ 可复制给任何人使用（无需他们安装Python）

## ⚠️ 注意事项

1. **只下载 embeddable package**
   - ❌ 不要下载 "Windows installer"
   - ❌ 不要下载 "Windows installer (32-bit)"
   - ✅ 只下载 "Windows embeddable package (64-bit)"

2. **版本选择**
   - 推荐：Python 3.11.9（稳定）
   - 可选：Python 3.12.x（最新）

3. **系统要求**
   - 64位 Windows 系统
   - Windows 7 及以上

---

如有问题，请访问：https://www.python.org/downloads/
