@echo off
chcp 65001 >nul
echo ================================================================================
echo   数据填充工具 - EXE 打包脚本（单文件模式）
echo ================================================================================
echo.

cd /d "%~dp0"

echo [1/5] 检查 Python 环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到 Python 环境！
    echo.
    echo 请先安装 Python 3.7 或更高版本
    pause
    exit /b 1
)
echo ✅ Python 环境检测通过
echo.

echo [2/5] 检查 PyInstaller...
python -c "import PyInstaller" 2>nul
if errorlevel 1 (
    echo ⚠️  PyInstaller 未安装，正在安装...
    pip install pyinstaller
    if errorlevel 1 (
        echo ❌ PyInstaller 安装失败！
        pause
        exit /b 1
    )
    echo ✅ PyInstaller 安装完成
) else (
    echo ✅ PyInstaller 已安装
)
echo.

echo [3/5] 检查必要文件...
if not exist "数据填充工具_exe.py" (
    echo ❌ 错误：找不到 数据填充工具_exe.py
    pause
    exit /b 1
)
if not exist "数据填充工具.spec" (
    echo ❌ 错误：找不到 数据填充工具.spec
    pause
    exit /b 1
)
if not exist "数据填充工具.py" (
    echo ❌ 错误：找不到 数据填充工具.py
    pause
    exit /b 1
)
if not exist "web_app.py" (
    echo ❌ 错误：找不到 web_app.py
    pause
    exit /b 1
)
echo ✅ 所有必要文件检测通过
echo.

echo [4/5] 清理旧版本...
if exist build rmdir /s /q build 2>nul
if exist dist rmdir /s /q dist 2>nul
echo ✅ 清理完成
echo.

echo [5/5] 开始打包（单文件模式）...
echo 这可能需要几分钟，请耐心等待...
echo.
pyinstaller --clean 数据填充工具.spec

if errorlevel 1 (
    echo.
    echo ❌ 打包失败！
    pause
    exit /b 1
)

echo.
echo ================================================================================
echo ✅ 打包完成！
echo ================================================================================
echo.
echo 📁 输出位置: dist\数据填充工具.exe
echo.
echo 💡 使用说明：
echo    1. 双击 exe 文件即可启动
echo    2. 浏览器将自动打开 http://localhost:8888
echo    3. 按 Ctrl+C 停止服务
echo.
echo 📦 分发说明：
echo    - 单个 exe 文件可直接复制分发
echo    - 无需安装 Python 或任何依赖
echo    - 双击即可在任何 Windows 电脑上运行
echo.
echo ================================================================================
pause
