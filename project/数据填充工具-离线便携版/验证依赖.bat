@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo 验证便携版 Python 依赖
echo ========================================
echo.

if not exist "python\python.exe" (
    echo ❌ 错误：找不到便携版 Python
    pause
    exit /b 1
)

echo 测试依赖包...
echo.

echo [1/5] Flask
python\python.exe -c "import flask; print('✅ Flask', flask.__version__)" 2>nul
if errorlevel 1 (
    echo ❌ Flask 未安装
    set ERROR=1
) else (
    echo ✅ Flask 已安装
)

echo.
echo [2/5] pandas
python\python.exe -c "import pandas; print('✅ pandas', pandas.__version__)" 2>nul
if errorlevel 1 (
    echo ❌ pandas 未安装
    set ERROR=1
) else (
    echo ✅ pandas 已安装
)

echo.
echo [3/5] openpyxl
python\python.exe -c "import openpyxl; print('✅ openpyxl', openpyxl.__version__)" 2>nul
if errorlevel 1 (
    echo ❌ openpyxl 未安装
    set ERROR=1
) else (
    echo ✅ openpyxl 已安装
)

echo.
echo [4/5] xlrd
python\python.exe -c "import xlrd; print('✅ xlrd', xlrd.__version__)" 2>nul
if errorlevel 1 (
    echo ❌ xlrd 未安装
    set ERROR=1
) else (
    echo ✅ xlrd 已安装
)

echo.
echo [5/5] msoffcrypto（可选）
python\python.exe -c "import msoffcrypto; print('✅ msoffcrypto')" 2>nul
if errorlevel 1 (
    echo ⚠️  msoffcrypto 未安装（可选）
) else (
    echo ✅ msoffcrypto 已安装
)

echo.
echo ========================================
if defined ERROR (
    echo ❌ 部分依赖未安装
    echo.
    echo 请运行：安装依赖.bat
) else (
    echo ✅ 所有依赖已安装！
    echo.
    echo 下一步：运行 "复制应用文件.bat"
)
echo ========================================
echo.
pause
