@echo off
chcp 65001 >nul
echo ================================================================================
echo   数据填充工具 - EXE 打包脚本（文件夹模式）
echo ================================================================================
echo.

cd /d "%~dp0"

echo [1/6] 检查 Python 环境...
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

echo [2/6] 检查 PyInstaller...
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

echo [3/6] 检查必要文件...
if not exist "数据填充工具_exe.py" (
    echo ❌ 错误：找不到 数据填充工具_exe.py
    pause
    exit /b 1
)
if not exist "数据填充工具_folder.spec" (
    echo ❌ 错误：找不到 数据填充工具_folder.spec
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

echo [4/6] 清理旧版本...
if exist build rmdir /s /q build 2>nul
if exist dist\数据填充工具 rmdir /s /q dist\数据填充工具 2>nul
echo ✅ 清理完成
echo.

echo [5/6] 开始打包（文件夹模式）...
echo 这可能需要几分钟，请耐心等待...
echo.
pyinstaller --clean 数据填充工具_folder.spec

if errorlevel 1 (
    echo.
    echo ❌ 打包失败！
    pause
    exit /b 1
)

echo.
echo [6/6] 创建启动脚本...
(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%%~dp0"
echo.
echo echo ================================================================================
echo echo    数据填充工具
echo echo ================================================================================
echo echo.
echo echo 正在启动...
echo echo.
echo start "" 数据填充工具.exe
) > dist\数据填充工具\启动工具.bat

echo ✅ 启动脚本创建完成
echo.

echo ================================================================================
echo ✅ 打包完成！
echo ================================================================================
echo.
echo 📁 输出位置: dist\数据填充工具\
echo.
echo 💡 使用说明：
echo    1. 整个文件夹可复制分发
echo    2. 双击 "数据填充工具.exe" 启动
echo    3. 或双击 "启动工具.bat" 启动
echo    4. 浏览器将自动打开 http://localhost:8888
echo.
echo 📦 分发说明：
echo    - 压缩整个 "数据填充工具" 文件夹
echo    - 解压后即可在任何 Windows 电脑上运行
echo    - 无需安装 Python 或任何依赖
echo.
echo ================================================================================
pause
