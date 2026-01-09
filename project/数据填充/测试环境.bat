@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo 测试绿色版 Python 环境
echo ========================================
echo.

if not exist "python\python.exe" (
    echo ❌ 找不到便携版 Python
    pause
    exit /b 1
)

echo 测试 1: Python 版本
python\python.exe --version
echo.

echo 测试 2: 导入 Flask
python\python.exe -c "import flask; print('✅ Flask', flask.__version__)"
echo.

echo 测试 3: 导入 pandas
python\python.exe -c "import pandas; print('✅ pandas', pandas.__version__)"
echo.

echo 测试 4: 导入 openpyxl
python\python.exe -c "import openpyxl; print('✅ openpyxl', openpyxl.__version__)"
echo.

echo 测试 5: 导入 xlrd
python\python.exe -c "import xlrd; print('✅ xlrd', xlrd.__version__)"
echo.

echo ========================================
echo ✅ 所有测试完成！
echo ========================================
echo.
echo 下一步：双击 "启动工具.bat" 启动应用
echo.
pause
