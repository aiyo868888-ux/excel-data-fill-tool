# QuickDrop - 极简局域网文件传输

手机上传文件到电脑，自动按日期归档。

## 使用方法

1. **启动服务**
   - 双击 `start.bat`（Windows）
   - 或运行 `python server.py`

2. **手机访问**
   - 确保手机和电脑在同一 Wi-Fi
   - 手机浏览器访问显示的地址（如 `http://192.168.1.100:8899`）

3. **上传文件**
   - 点击或拖拽文件到上传区域
   - 文件自动保存到 `uploads/今日日期/` 文件夹
   - 上传完成后可复制文件路径

## 配置

编辑 `config.json`：

```json
{
  "port": 8899,              // 端口号
  "max_file_size": 100,      // 最大文件大小（MB）
  "auto_open_folder": false  // 是否自动打开文件夹
}
```

## 目录结构

```
quick-drop/
├── server.py          # 服务器程序
├── static/
│   └── index.html     # 上传页面
├── uploads/           # 接收的文件
│   └── 2025-01-23/    # 按日期归档
├── config.json        # 配置文件
├── start.bat          # 启动脚本
└── README.md          # 说明文档
```

## 系统要求

- Python 3.7+
- Flask（`pip install flask`）
- 手机和电脑在同一局域网

## 特点

- ✓ 无需安装 APP，手机浏览器直接使用
- ✓ 自动按日期归档文件
- ✓ 支持任意类型文件
- ✓ 局域网传输，无需互联网
- ✓ 上传进度实时显示
- ✓ 一键复制文件路径
