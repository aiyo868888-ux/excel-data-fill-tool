@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ========================================
echo    Excel 数据填充工具 v2.0
echo    完全离线绿色版
echo ========================================
echo.

REM 检查便携版 Python
if not exist "python\python.exe" (
    echo ❌ 错误：找不到便携版 Python
    echo.
    echo 请确保以下文件存在：
    echo   - python\python.exe
    echo   - python\python311.dll
    echo   - python\Lib\
    echo.
    pause
    exit /b 1
)

REM 创建运行时目录
if not exist "uploads" mkdir uploads
if not exist "temp" mkdir temp
if not exist "sessions" mkdir sessions

REM 检查依赖
echo 检查依赖包...
python\python.exe -c "import flask, pandas, openpyxl, xlrd" 2>nul
if errorlevel 1 (
    echo ❌ 错误：缺少必要的依赖包
    echo.
    echo 请确保已运行：安装依赖.bat
    pause
    exit /b 1
)
echo ✅ 依赖包检查完成
echo.

REM 启动应用
echo ✅ 启动 Web 应用...
echo.
echo 浏览器将自动打开 http://localhost:8888
echo.
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

REM 启动应用（Python 代码会自动打开浏览器）
python\python.exe web_app.py

if errorlevel 1 (
    echo.
    echo ❌ 启动失败
    echo.
    echo 可能的原因：
    echo   1. 端口 8888 被占用
    echo   2. 文件损坏
    echo.
    pause
)
