@echo off
chcp 65001 >nul
title 数据填充工具
cd /d "%~dp0"

echo ================================================================================
echo   数据填充工具 - 绿色版
echo ================================================================================
echo.

echo 🔍 检查Python环境...
echo.

python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到Python环境！
    echo.
    echo 请先安装Python 3.7或更高版本：
    echo 1. 访问 https://www.python.org/downloads/
    echo 2. 下载并安装Python
    echo 3. 安装时勾选 "Add Python to PATH"
    echo.
    pause
    exit /b 1
)

echo ✅ Python环境检测通过
echo.

echo 🔍 检查依赖包...
echo.

python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo ⚠️  缺少依赖包，正在自动安装...
    echo.
    python -m pip install flask openpyxl pandas psutil -i https://pypi.tuna.tsinghua.edu.cn/simple
    echo.
    if errorlevel 1 (
        echo ❌ 依赖安装失败！
        echo 请手动运行: pip install flask openpyxl pandas psutil
        pause
        exit /b 1
    )
    echo ✅ 依赖包安装完成
) else (
    echo ✅ 依赖包检测通过
)

echo.
echo 🚀 启动服务器...
echo.
echo ⏳ 服务器启动中，请稍候...
echo.
echo    访问地址: http://localhost:8888
echo    按 Ctrl+C 停止服务器
echo.
echo ================================================================================
echo.

python web_app.py

if errorlevel 1 (
    echo.
    echo ❌ 服务器启动失败！
    echo.
    pause
)
