@echo off
chcp 65001 >nul
echo ================================================================================
echo   数据填充工具 - 绿色版打包脚本
echo ================================================================================
echo.

cd /d "%~dp0"

set PORTABLE_DIR="数据填充工具-绿色版"
set SOURCE_DIR="."

echo 📦 开始打包绿色版...
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

REM 复制核心文件
echo 📄 复制核心文件...
copy %SOURCE_DIR%\数据填充工具.py %PORTABLE_DIR%\ >nul
copy %SOURCE_DIR%\web_app.py %PORTABLE_DIR%\ >nul
copy %SOURCE_DIR%\suppliers_config.json %PORTABLE_DIR%\ >nul

REM 复制模板文件
copy %SOURCE_DIR%\templates\index.html %PORTABLE_DIR%\templates\ >nul
copy %SOURCE_DIR%\templates\config.html %PORTABLE_DIR%\templates\ >nul

REM 复制静态文件
if exist %SOURCE_DIR%\static\css\style.css (
    copy %SOURCE_DIR%\static\css\style.css %PORTABLE_DIR%\static\css\ >nul
)

REM 复制工具脚本
copy %SOURCE_DIR%\clean_port.py %PORTABLE_DIR%\ >nul
copy %SOURCE_DIR%\start_simple.bat %PORTABLE_DIR%\ >nul

REM 创建增强版启动脚本
echo 📝 创建启动脚本...

(
echo @echo off
echo chcp 65001 ^>nul
echo title 数据填充工具
echo cd /d "%%~dp0"
echo.
echo echo ================================================================================
echo echo   数据填充工具 - 绿色版
echo echo ================================================================================
echo echo.
echo echo 🔍 检查Python环境...
echo.
echo python --version ^>nul 2^>^&1
echo if errorlevel 1 (
echo     echo ❌ 错误：未检测到Python环境！
echo     echo.
echo     echo 请先安装Python 3.7或更高版本：
echo     echo 1. 访问 https://www.python.org/downloads/
echo     echo 2. 下载并安装Python
echo     echo 3. 安装时勾选 "Add Python to PATH"
echo     echo.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo ✅ Python环境检测通过
echo echo.
echo echo 🔍 检查依赖包...
echo.
echo python -c "import flask" ^>nul 2^>^&1
echo if errorlevel 1 (
echo     echo ⚠️  缺少依赖包，正在自动安装...
echo     echo.
echo     python -m pip install flask openpyxl pandas psutil -i https://pypi.tuna.tsinghua.edu.cn/simple
echo     echo.
echo     if errorlevel 1 (
echo         echo ❌ 依赖安装失败！
echo         echo 请手动运行: pip install flask openpyxl pandas psutil
echo         pause
echo         exit /b 1
echo     ^)
echo     echo ✅ 依赖包安装完成
echo ^) else (
echo     echo ✅ 依赖包检测通过
echo ^)
echo.
echo echo 🚀 启动服务器...
echo echo.
echo echo ⏳ 服务器启动中，请稍候...
echo echo.
echo echo    访问地址: http://localhost:8888
echo echo    按 Ctrl+C 停止服务器
echo echo.
echo echo ================================================================================
echo.
echo start "" http://localhost:8888
echo python web_app.py
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
echo echo ================================================================================
echo echo   安装Python依赖包
echo echo ================================================================================
echo echo.
echo echo 📦 正在安装依赖包...
echo echo.
echo python -m pip install flask openpyxl pandas psutil -i https://pypi.tuna.tsinghua.edu.cn/simple
echo echo.
echo if errorlevel 1 (
echo     echo ❌ 安装失败！请检查网络连接
echo     echo.
echo     echo 尝试使用官方源：
echo     python -m pip install flask openpyxl pandas psutil
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
echo echo ================================================================================
echo echo   停止服务器
echo echo ================================================================================
echo echo.
echo echo 🔍 正在查找占用8888端口的进程...
echo echo.
echo python clean_port.py
echo echo.
echo echo ✅ 完成！
echo echo.
echo pause
) > %PORTABLE_DIR%\停止服务器.bat

REM 创建README
echo 📝 创建使用说明...
(
echo # 数据填充工具 - 绿色版使用说明
echo.
echo ## 📋 系统要求
echo.
echo - Windows 7/8/10/11
echo - Python 3.7 或更高版本
echo.
echo ## 🚀 快速开始
echo.
echo ### 方法一：自动启动（推荐）
echo.
echo 双击 `启动工具.bat` 即可自动：
echo 1. 检查Python环境
echo 2. 安装缺失的依赖包
echo 3. 启动Web服务器
echo 4. 打开浏览器访问 http://localhost:8888
echo.
echo ### 方法二：手动安装依赖
echo.
echo 1. 安装Python（如未安装）
echo    - 访问 https://www.python.org/downloads/
echo    - 下载并安装Python 3.7+
echo    - ⚠️ 安装时务必勾选 "Add Python to PATH"
echo.
echo 2. 安装依赖包
echo    - 双击 `安装依赖.bat`
echo    - 或在命令行运行：
echo      ```
echo      pip install flask openpyxl pandas psutil
echo      ```
echo.
echo 3. 启动工具
echo    - 双击 `启动工具.bat`
echo.
echo ## 📁 文件说明
echo.
echo - `启动工具.bat` - 启动服务器（推荐使用）
echo - `安装依赖.bat` - 手动安装Python依赖包
echo - `停止服务器.bat` - 停止所有占用8888端口的进程
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
echo ### Q1: 提示"未检测到Python环境"？
echo **A**: 请先安装Python并确保添加到PATH环境变量
echo.
echo ### Q2: 依赖安装失败？
echo **A**: 尝试手动运行 `pip install flask openpyxl pandas psutil`
echo.
echo ### Q3: 端口8888被占用？
echo **A**: 双击 `停止服务器.bat` 清理端口
echo.
echo ### Q4: 如何停止服务器？
echo **A**: 在命令行窗口按 Ctrl+C，或运行 `停止服务器.bat`
echo.
echo ## 📞 技术支持
echo.
echo 如遇问题，请检查：
echo 1. Python版本是否为3.7+
echo 2. 依赖包是否正确安装
echo 3. 防火墙是否允许Python运行
echo.
echo ---
echo **版本**: 绿色版 v1.0
echo **更新日期**: %date%
) > %PORTABLE_DIR%\使用说明.txt

REM 创建依赖列表文件
(
echo flask
echo openpyxl
echo pandas
echo psutil
) > %PORTABLE_DIR%\requirements.txt

echo.
echo ================================================================================
echo ✅ 绿色版打包完成！
echo ================================================================================
echo.
echo 📁 输出目录: %PORTABLE_DIR%
echo.
echo 📦 打包内容：
echo    ✅ 核心程序文件
echo    ✅ 启动脚本（自动检测Python和依赖）
echo    ✅ 依赖安装脚本
echo    ✅ 停止服务器脚本
echo    ✅ 使用说明文档
echo.
echo 💡 使用方法：
echo    将 "%PORTABLE_DIR%" 文件夹复制到任意位置即可使用
echo.
echo ================================================================================
echo.
pause
