@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo 安装 Python 依赖包
echo ========================================
echo.

REM 检查便携版 Python
if not exist "python\python.exe" (
    echo ❌ 错误：找不到便携版 Python
    echo.
    echo 请先运行：下载Python.ps1
    echo.
    pause
    exit /b 1
)

echo 步骤 1：使用系统 Python 创建虚拟环境
echo.

REM 检查系统 Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：系统未安装 Python
    echo.
    echo 请先安装 Python 3.7 或更高版本
    echo 下载：https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo ✅ 系统 Python 已安装
echo.

REM 创建虚拟环境
if exist "venv_temp" (
    echo 删除旧的虚拟环境...
    rmdir /s /q venv_temp
)

echo 创建虚拟环境...
python -m venv venv_temp
if errorlevel 1 (
    echo ❌ 创建虚拟环境失败
    pause
    exit /b 1
)

echo ✅ 虚拟环境创建成功
echo.

echo 步骤 2：安装依赖包到虚拟环境
echo.

call venv_temp\Scripts\activate.bat

echo 安装 Flask...
pip install flask>=2.0.0

echo.
echo 安装 pandas...
pip install pandas>=1.3.0

echo.
echo 安装 openpyxl...
pip install openpyxl>=3.0.9

echo.
echo 安装 xlrd...
pip install xlrd>=2.0.1

echo.
echo 安装 msoffcrypto-tools（可选）...
pip install msoffcrypto-tools || echo ⚠️  msoffcrypto-tools 安装失败，将跳过

echo.
echo 步骤 3：复制依赖包到便携版 Python
echo.

if not exist "python\Lib\site-packages" mkdir python\Lib\site-packages

echo 复制依赖包...
xcopy venv_temp\Lib\site-packages\* "python\Lib\site-packages\" /E /I /Y

if errorlevel 1 (
    echo ❌ 复制失败
    pause
    exit /b 1
)

echo ✅ 依赖包复制成功
echo.

REM 清理临时虚拟环境
echo 清理临时文件...
rmdir /s /q venv_temp

echo ========================================
echo ✅ 安装完成！
echo ========================================
echo.
echo 下一步：运行 "验证依赖.bat" 检查安装结果
echo.
pause
