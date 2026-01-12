@echo off
chcp 65001 >nul
echo ================================================================================
echo   为绿色版添加Python环境
echo ================================================================================
echo.

cd /d "%~dp0"

set PORTABLE_DIR="数据填充工具-绿色版"

if not exist %PORTABLE_DIR% (
    echo ❌ 错误：找不到绿色版目录！
    echo 请先确保 "数据填充工具-绿色版" 文件夹存在
    pause
    exit /b 1
)

echo 📦 正在为绿色版添加Python环境...
echo.
echo 🔍 检查Python便携版...
echo.

REM 检查是否已下载Python
set PYTHON_ZIP=python-3.11.9-embed-amd64.zip

if not exist "%PYTHON_ZIP%" (
    echo ⚠️  未找到Python便携版文件！
    echo.
    echo 请按以下步骤获取Python便携版：
    echo.
    echo 1. 访问Python官网下载页面：
    echo    https://www.python.org/downloads/windows/
    echo.
    echo 2. 下载 "Windows embeddable package (64-bit)"
    echo    文件名：%PYTHON_ZIP%
    echo.
    echo 3. 将下载的文件放到当前目录：
    echo    %cd%
    echo.
    echo 4. 重新运行此脚本
    echo.
    pause
    exit /b 1
)

echo ✅ 找到Python便携版文件
echo.

REM 解压Python到绿色版
echo 📦 正在解压Python到绿色版目录...
powershell -Command "Expand-Archive -Path '%PYTHON_ZIP%' -DestinationPath '%PORTABLE_DIR%\Python' -Force"

if errorlevel 1 (
    echo ❌ Python解压失败！
    pause
    exit /b 1
)

echo ✅ Python解压完成
echo.

REM 修改Python配置（启用site-packages）
echo ⚙️  配置Python环境...

set PTH_FILE=%PORTABLE_DIR%\Python\python311._pth

(
echo #see https://docs.python.org/3/library/site.html
echo import site
echo.
echo # 添加site-packages路径
echo import os
echo import sys
echo.
echo python_dir = os.path.dirname(sys.executable)
echo site_packages = os.path.join(python_dir, 'Lib', 'site-packages')
echo.
echo if site_packages not in sys.path:
echo     sys.path.insert(0, site_packages)
) > %PTH_FILE%

echo ✅ Python配置完成
echo.

REM 下载get-pip.py
echo 📝 下载pip安装脚本...
powershell -Command "Invoke-WebRequest -Uri 'https://bootstrap.pypa.io/get-pip.py' -OutFile '%PORTABLE_DIR%\get-pip.py'"

if errorlevel 1 (
    echo ⚠️  pip下载失败，将在首次运行时尝试安装
) else (
    echo ✅ pip下载完成
)

echo.
echo 📝 更新启动脚本...

REM 备份原启动脚本
copy %PORTABLE_DIR%\启动工具.bat %PORTABLE_DIR%\启动工具.bat.bak >nul

REM 创建新的启动脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo title 数据填充工具
echo cd /d "%%~dp0"
echo.
echo REM 设置Python路径
echo set "PYTHON_DIR=%%cd%%\Python"
echo set "PYTHON_EXE=%%PYTHON_DIR%%\python.exe"
echo.
echo echo ================================================================================
echo echo   数据填充工具 - 完整绿色版（含Python）
echo echo ================================================================================
echo echo.
echo.
echo echo 🔍 检查Python环境...
echo if not exist "%%PYTHON_EXE%%" (
echo     echo ❌ 错误：Python环境不存在！
echo     echo    请检查Python文件夹是否完整
echo     pause
echo     exit /b 1
echo ^)
echo echo ✅ Python环境检测通过
echo.
echo.
echo echo 🔍 检查依赖包...
echo "%%PYTHON_EXE%%" -c "import flask" ^>nul 2^>^&1
echo if errorlevel 1 (
echo     echo ⚠️  首次运行，正在安装依赖包...
echo     echo    需要网络连接，请稍候...
echo     echo.
echo     REM 安装pip
echo     if exist get-pip.py (
echo         echo 🔧 安装pip...
echo         "%%PYTHON_EXE%%" get-pip.py --target="%%PYTHON_DIR%%\Lib\site-packages"
echo     ^)
echo     REM 安装依赖包
echo     echo 📦 安装依赖包...
echo     "%%PYTHON_EXE%%" -m pip install flask openpyxl pandas psutil --target="%%PYTHON_DIR%%\Lib\site-packages" --no-user
echo     echo.
echo     if errorlevel 1 (
echo         echo ❌ 依赖安装失败！
echo         echo    请检查网络连接
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

echo ✅ 启动脚本已更新
echo.

REM 更新安装依赖脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%%~dp0"
echo.
echo set "PYTHON_DIR=%%cd%%\Python"
echo set "PYTHON_EXE=%%PYTHON_DIR%%\python.exe"
echo.
echo echo ================================================================================
echo echo   安装Python依赖包
echo echo ================================================================================
echo echo.
echo if not exist "%%PYTHON_EXE%%" (
echo     echo ❌ 错误：找不到Python环境！
echo     pause
echo     exit /b 1
echo ^)
echo.
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

echo ✅ 安装依赖脚本已更新
echo.

REM 更新停止服务器脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo cd /d "%%~dp0"
echo.
echo set "PYTHON_DIR=%%cd%%\Python"
echo set "PYTHON_EXE=%%PYTHON_DIR%%\python.exe"
echo.
echo echo ================================================================================
echo echo   停止服务器
echo echo ================================================================================
echo echo.
echo echo 🔍 正在查找占用8888端口的进程...
echo echo.
echo if exist "%%PYTHON_EXE%%" (
echo     "%%PYTHON_EXE%%" clean_port.py
echo ^) else (
echo     echo ⚠️  找不到Python环境，请使用任务管理器结束Python进程
echo ^)
echo echo.
echo echo ✅ 完成！
echo echo.
echo pause
) > %PORTABLE_DIR%\停止服务器.bat

echo ✅ 停止服务器脚本已更新
echo.

REM 更新使用说明
(
echo # 数据填充工具 - 完整绿色版使用说明
echo.
echo ## 📋 系统要求
echo.
echo - Windows 7/8/10/11 ^(64位^)
echo - **无需安装Python或其他任何软件**
echo.
echo ## 🚀 快速开始
echo.
echo ### 首次使用
echo.
echo 1. 双击 `启动工具.bat`
echo 2. 首次运行会自动安装依赖包（需要网络连接，约2-3分钟）
echo 3. 等待浏览器自动打开
echo 4. 开始使用工具
echo.
echo ### 离线使用
echo.
echo 首次安装依赖后，可完全离线使用。
echo.
echo ## 📁 文件说明
echo.
echo - `启动工具.bat` - 启动服务器（推荐使用）
echo - `安装依赖.bat` - 手动安装Python依赖包
echo - `停止服务器.bat` - 停止所有占用8888端口的进程
echo - `Python/` - **内置的Python环境（请勿删除或移动）**
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
echo **A**: **绝对不可以！** Python文件夹是工具运行必需的，删除后将无法使用
echo.
echo ### Q6: 文件夹大小？
echo **A**: 完整绿色版约50-100MB（包含Python，不含依赖）
echo     首次运行后会增加到200-300MB（含依赖包）
echo.
echo ### Q7: 可以复制给别人使用吗？
echo **A**: 可以！复制整个文件夹给任何人，他们都可以直接使用，无需安装任何软件
echo.
echo ## 📦 分发说明
echo.
echo - 可直接压缩整个文件夹分发
echo - 解压后双击 `启动工具.bat` 即可使用
echo - 无需任何安装，真正的绿色软件
echo - 接收者无需安装Python
echo.
echo ## 📞 技术支持
echo.
echo 如遇问题，请检查：
echo 1. 是否为64位Windows系统
echo 2. 首次运行是否有网络连接
echo 3. Python文件夹是否完整
echo 4. 防火墙是否允许程序运行
echo.
echo ---
echo **版本**: 完整绿色版 v2.0
echo **Python版本**: 3.11.9（内置）
echo **更新日期**: %date%
) > %PORTABLE_DIR%\使用说明.txt

echo ✅ 使用说明已更新
echo.

echo ================================================================================
echo ✅ Python环境添加完成！
echo ================================================================================
echo.
echo 📁 更新的文件：
echo    ✅ Python/ 文件夹（内置Python环境）
echo    ✅ 启动工具.bat（使用内置Python）
echo    ✅ 安装依赖.bat（更新Python路径）
echo    ✅ 停止服务器.bat（更新Python路径）
echo    ✅ 使用说明.txt（更新说明）
echo.
echo 💡 现在可以：
echo    1. 将整个 "数据填充工具-绿色版" 文件夹
echo    2. 压缩成zip文件
echo    3. 发送给任何人使用
echo    4. 他们无需安装Python，双击启动即可
echo.
echo ⚠️  注意：
echo    - 文件夹约50-100MB
echo    - 首次运行需要网络（安装依赖）
echo    - 请勿删除Python文件夹
echo.
echo ================================================================================
echo.
pause
