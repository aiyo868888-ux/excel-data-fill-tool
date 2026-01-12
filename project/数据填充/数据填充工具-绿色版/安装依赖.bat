@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ================================================================================
echo   安装Python依赖包
echo ================================================================================
echo.

echo 📦 正在安装依赖包...
echo.

python -m pip install flask openpyxl pandas psutil -i https://pypi.tuna.tsinghua.edu.cn/simple

echo.
if errorlevel 1 (
    echo ❌ 安装失败！请检查网络连接
    echo.
    echo 尝试使用官方源：
    python -m pip install flask openpyxl pandas psutil
) else (
    echo ✅ 所有依赖包安装完成！
)

echo.
pause
