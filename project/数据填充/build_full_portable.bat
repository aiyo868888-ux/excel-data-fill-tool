@echo off
chcp 65001 >nul
echo ================================================================================
echo   数据填充工具 - 完整绿色版打包脚本（包含Python环境）
echo ================================================================================
echo.

cd /d "%~dp0"

set PORTABLE_DIR="数据填充工具-完整绿色版"
set PYTHON_VERSION=3.11.9
set PYTHON_URL=https://www.python.org/ftp/python/3.11.9/python-3.11.9-embed-amd64.zip
set PYTHON_ZIP=python-%PYTHON_VERSION%-embed-amd64.zip

echo 📦 开始打包完整绿色版...
echo.

REM 清理旧版本
if exist %PORTABLE_DIR% (
    echo 🗑️  清理旧版本...
    rmdir /s /q %PORTABLE_DIR%
)

REM 创建目录结构
echo 📁 创建目录结构...
mkdir %PORTABLE_DIR%
mkdir %PORTABLE_DIR%\templates
mkdir %PORTABLE_DIR%\static
mkdir %PORTABLE_DIR%\static\css
mkdir %PORTABLE_DIR%\static\js
mkdir %PORTABLE_DIR%\uploads
mkdir %PORTABLE_DIR%\temp
mkdir %PORTABLE_DIR%\sessions
mkdir %PORTABLE_DIR%\docs
mkdir %PORTABLE_DIR%\Scripts

echo.
echo 🔍 检查便携版Python...
echo.

REM 检查是否已下载Python
if not exist "%PYTHON_ZIP%" (
    echo ⬇️  需要下载Python便携版...
    echo.
    echo 正在从 Python官网 下载便携版Python...
    echo 下载地址: %PYTHON_URL%
    echo.
    echo 请使用以下方法之一获取Python便携版：
    echo.
    echo 方法1（推荐）- 手动下载：
    echo   1. 访问：https://www.python.org/downloads/windows/
    echo   2. 下载 "Windows embeddable package (64-bit)"
    echo   3. 将下载的 zip 文件放到当前目录
    echo   文件名应为：%PYTHON_ZIP%
    echo.
    echo 方法2 - 使用PowerShell下载（可能较慢）：
    echo   运行命令：powershell -Command "Invoke-WebRequest -Uri '%PYTHON_URL%' -OutFile '%PYTHON_ZIP%'"
    echo.
    pause
    exit /b 1
)

REM 解压Python
echo 📦 解压Python到绿色版目录...
powershell -Command "Expand-Archive -Path '%PYTHON_ZIP%' -DestinationPath '%PORTABLE_DIR%\Python' -Force"

if errorlevel 1 (
    echo ❌ Python解压失败！
    pause
    exit /b 1
)

echo ✅ Python解压完成
echo.

REM 修改Python配置（启用pip）
echo ⚙️  配置Python环境...
(
echo # 修改path配置以支持site-packages
echo import site
echo import os
echo import sys
echo.
echo # 获取Python目录
echo python_dir = os.path.dirname(sys.executable)
echo lib_dir = os.path.join(python_dir, 'Lib', 'site-packages')
echo.
echo # 添加到path
echo if lib_dir not in sys.path:
echo     sys.path.insert(0, lib_dir)
) > %PORTABLE_DIR%\Python\python311._pth

echo ✅ Python配置完成
echo.

REM 复制核心文件
echo 📄 复制核心文件...
copy %SOURCE_DIR%\数据填充工具.py %PORTABLE_DIR%\ >nul
copy %SOURCE_DIR%\web_app.py %PORTABLE_DIR%\ >nul
copy %SOURCE_DIR%\suppliers_config.json %PORTABLE_DIR%\ >nul

REM 复制模板文件
copy %SOURCE_DIR%\templates\index.html %PORTABLE_DIR%\templates\ >nul
copy %SOURCE_DIR%\templates\config.html %PORTABLE_DIR%\templates\ >nul

REM 复制工具脚本
copy %SOURCE_DIR%\clean_port.py %PORTABLE_DIR%\ >nul

REM 创建get-pip.py
echo 📝 下载pip安装脚本...
powershell -Command "Invoke-WebRequest -Uri 'https://bootstrap.pypa.io/get-pip.py' -OutFile '%PORTABLE_DIR%\get-pip.py'"

if errorlevel 1 (
    echo ⚠️  pip下载失败，将在首次运行时尝试安装
)

REM 创建启动脚本
echo 📝 创建启动脚本...

(
echo @echo off
echo chcp 65001 ^>nul
echo title 数据填充工具
echo cd /d "%%~dp0"
echo.
echo set PYTHON_DIR=%%cd%%\Python
echo set PYTHON_EXE=%%PYTHON_DIR%%\python.exe
echo.
echo echo ================================================================================
echo echo   数据填充工具 - 完整绿色版
echo echo ================================================================================
echo echo.
echo.
echo echo 🔍 检查Python环境...
echo if not exist "%%PYTHON_EXE%%" (
echo     echo ❌ 错误：Python环境不存在！
echo     echo 请重新下载完整绿色版
echo     pause
echo     exit /b 1
echo ^)
echo echo ✅ Python环境检测通过
echo.
echo.
echo echo 🔍 检查依赖包...
echo "%%PYTHON_EXE%%" -c "import flask" ^>nul 2^>^&1
echo if errorlevel 1 (
echo     echo ⚠️  缺少依赖包，正在自动安装...
echo     echo.
echo     if exist get-pip.py (
echo         "%%PYTHON_EXE%%" get-pip.py --target="%%PYTHON_DIR%%\Lib\site-packages"
echo     ^)
echo     "%%PYTHON_EXE%%" -m pip install flask openpyxl pandas psutil --target="%%PYTHON_DIR%%\Lib\site-packages" --no-user
echo     echo.
echo     if errorlevel 1 (
echo         echo ❌ 依赖安装失败！
echo         echo 请检查网络连接
echo         pause
echo         exit /b 1
echo     ^)
echo     echo ✅ 依赖包安装完成
echo ^) else (
echo     echo ✅ 依赖包检测通过
echo ^)
echo.
echo.
echo echo 🚀 启动服务器...
echo echo.
echo echo ⏳ 服务器启动中，请稍候...
echo echo.
echo echo    访问地址: http://localhost:8888
echo echo    按 Ctrl+C 停止服务器
echo echo.
echo echo ================================================================================
echo echo.
echo.
echo timeout /t 2 /nobreak ^>nul
echo start "" http://localhost:8888
echo "%%PYTHON_EXE%%" web_app.py
echo.
echo if errorlevel 1 (
echo     echo.
echo     echo ❌ 服务器启动失败！
echo     echo.
echo     pause
echo ^)
) > %PORTABLE_DIR%\启动工具.bat

REM 创建依赖安装脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%%~dp0"
echo.
echo set PYTHON_DIR=%%cd%%\Python
echo set PYTHON_EXE=%%PYTHON_DIR%%\python.exe
echo.
echo echo ================================================================================
echo echo   安装Python依赖包
echo echo ================================================================================
echo echo.
echo echo 📦 正在安装依赖包...
echo echo.
echo if exist get-pip.py (
echo     echo 🔧 安装pip...
echo     "%%PYTHON_EXE%%" get-pip.py --target="%%PYTHON_DIR%%\Lib\site-packages"
echo ^)
echo.
echo "%%PYTHON_EXE%%" -m pip install flask openpyxl pandas psutil --target="%%PYTHON_DIR%%\Lib\site-packages" --no-user
echo.
echo if errorlevel 1 (
echo     echo ❌ 安装失败！请检查网络连接
echo ^) else (
echo     echo ✅ 所有依赖包安装完成！
echo ^)
echo echo.
echo pause
) > %PORTABLE_DIR%\安装依赖.bat

REM 创建停止服务器脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%%~dp0"
echo.
echo set PYTHON_DIR=%%cd%%\Python
echo set PYTHON_EXE=%%PYTHON_DIR%%\python.exe
echo.
echo echo ================================================================================
echo echo   停止服务器
echo echo ================================================================================
echo echo.
echo echo 🔍 正在查找占用8888端口的进程...
echo echo.
echo "%%PYTHON_EXE%%" clean_port.py
echo echo.
echo echo ✅ 完成！
echo echo.
echo pause
) > %PORTABLE_DIR%\停止服务器.bat

REM 创建README
echo 📝 创建使用说明...
(
echo # 数据填充工具 - 完整绿色版使用说明
echo.
echo ## 📋 系统要求
echo.
echo - Windows 7/8/10/11 ^(64位^)
echo - 无需安装Python或其他任何软件
echo.
echo ## 🚀 快速开始
echo.
echo ### 首次使用
echo.
echo 1. 双击 `启动工具.bat`
echo 2. 首次运行会自动安装依赖包（需要网络连接）
echo 3. 等待浏览器自动打开
echo 4. 开始使用工具
echo.
echo ### 离线使用
echo.
echo 首次安装依赖后，可离线使用：
echo 1. 确保已联网并运行过一次（依赖已安装）
echo 2. 之后可断网使用
echo.
echo ## 📁 文件说明
echo.
echo - `启动工具.bat` - 启动服务器（推荐使用）
echo - `安装依赖.bat` - 手动安装Python依赖包
echo - `停止服务器.bat` - 停止所有占用8888端口的进程
echo - `Python/` - 内置的Python环境（请勿删除）
echo - `web_app.py` - Web应用程序主文件
echo - `数据填充工具.py` - 核心业务逻辑
echo - `suppliers_config.json` - 供应商配置文件
echo.
echo ## 🎯 使用方法
echo.
echo 1. 启动工具后，浏览器会自动打开
echo 2. 上传Excel报表文件
echo 3. 选择需要生成的单据类型（交接单、入库单、需求单等）
echo 4. 点击生成并下载
echo.
echo ## ⚙️ 常见问题
echo.
echo ### Q1: 首次运行提示"缺少依赖包"？
echo **A**: 正常现象，工具会自动下载并安装依赖，需要网络连接
echo.
echo ### Q2: 依赖安装失败？
echo **A**: 检查网络连接，或手动运行 `安装依赖.bat`
echo.
echo ### Q3: 端口8888被占用？
echo **A**: 双击 `停止服务器.bat` 清理端口
echo.
echo ### Q4: 如何停止服务器？
echo **A**: 在命令行窗口按 Ctrl+C，或运行 `停止服务器.bat`
echo.
echo ### Q5: 可以删除Python文件夹吗？
echo **A**: 不可以！Python文件夹是工具运行必需的，删除后将无法使用
echo.
echo ### Q6: 文件夹大小？
echo **A**: 完整绿色版约200-300MB（包含Python和所有依赖）
echo.
echo ## 📦 分发说明
echo.
echo - 可直接压缩整个文件夹分发
echo - 解压后双击 `启动工具.bat` 即可使用
echo - 无需任何安装，真正的绿色软件
echo.
echo ## 📞 技术支持
echo.
echo 如遇问题，请检查：
echo 1. 是否为64位Windows系统
echo 2. 首次运行是否有网络连接
echo 3. 防火墙是否允许程序运行
echo.
echo ---
echo **版本**: 完整绿色版 v1.0
echo **Python版本**: 3.11.9
echo **更新日期**: %date%
) > %PORTABLE_DIR%\使用说明.txt

echo.
echo ================================================================================
echo ✅ 完整绿色版打包完成！
echo ================================================================================
echo.
echo 📁 输出目录: %PORTABLE_DIR%
echo.
echo 📦 打包内容：
echo    ✅ 内置Python环境（无需安装）
echo    ✅ 核心程序文件
echo    ✅ 启动脚本（自动检测和安装依赖）
echo    ✅ 依赖安装脚本
echo    ✅ 停止服务器脚本
echo    ✅ 使用说明文档
echo.
echo 💡 使用方法：
echo    将 "%PORTABLE_DIR%" 文件夹复制到任意位置即可使用
echo    双击 "启动工具.bat" 启动工具
echo.
echo ⚠️  注意：
echo    - 首次运行需要网络连接（自动安装依赖）
echo    - 文件夹较大（约200-300MB）
echo    - 请勿删除Python文件夹
echo.
echo ================================================================================
echo.
pause
