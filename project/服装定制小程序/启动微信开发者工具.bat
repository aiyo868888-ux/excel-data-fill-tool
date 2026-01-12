@echo off
chcp 65001 >nul
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              打开服装定制小程序项目                        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM 检查微信开发者工具是否安装
set CLI_PATH="C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat"

if exist %CLI_PATH% (
    echo ✅ 找到微信开发者工具
    echo.
    echo 正在打开项目...
    call %CLI_PATH% -o "d:\claude code -11\project\服装定制小程序"
) else (
    echo ❌ 未找到微信开发者工具命令行工具
    echo.
    echo 请手动操作：
    echo 1. 打开微信开发者工具
    echo 2. 导入项目目录：d:\claude code -11\project\服装定制小程序
    echo 3. 选择测试号或你的AppID
    echo 4. 点击导入
)

echo.
pause
