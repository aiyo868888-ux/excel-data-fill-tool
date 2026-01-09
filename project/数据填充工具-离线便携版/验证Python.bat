@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo 验证便携版 Python 安装
echo ========================================
echo.

if not exist "python\python.exe" (
    echo ❌ 错误：找不到便携版 Python
    echo.
    echo 请先运行：下载Python.ps1
    echo 或手动下载并解压 Python Embeddable Package
    echo.
    pause
    exit /b 1
)

echo 测试 1: Python 版本
python\python.exe --version
if errorlevel 1 (
    echo ❌ Python 无法运行
    pause
    exit /b 1
)

echo.
echo 测试 2: Python 可执行文件
dir python\python.exe | findstr "python.exe"
if errorlevel 1 (
    echo ❌ 找不到 python.exe
    pause
    exit /b 1
)

echo.
echo 测试 3: 配置文件
type python\python311._pth | findstr "Lib/site-packages"
if errorlevel 1 (
    echo ⚠️  配置文件未修改
    echo 请确保 python311._pth 包含以下内容：
    echo   Lib/site-packages
    echo   import site
) else (
    echo ✅ 配置文件已正确修改
)

echo.
echo ========================================
echo ✅ 验证完成！
echo ========================================
echo.
echo 下一步：运行 "安装依赖.bat"
echo.
pause
