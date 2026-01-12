@echo off
chcp 65001 >nul
echo ╔══════════════════════════════════════════════════════════════╗
echo ║           后台管理系统 - 一键安装脚本                       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo [1/4] 检查环境...
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 未检测到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)
echo ✅ Node.js 已安装

echo.
echo [2/4] 配置环境ID...
set /p ENV_ID="请输入你的云开发环境ID（格式如 cloud1-xxx）: "
(
echo # 云开发环境配置
echo CLOUD_ENV_ID=%ENV_ID%
echo.
echo # 服务器配置
echo PORT=3000
) > server\.env
echo ✅ 环境ID已配置

echo.
echo [3/4] 安装后端依赖...
cd server
call npm install --registry=https://registry.npmmirror.com
if %errorlevel% neq 0 (
    echo ❌ 后端依赖安装失败
    pause
    exit /b 1
)
echo ✅ 后端依赖安装完成

echo.
echo [4/4] 安装前端依赖...
cd ..\admin
call npm install --registry=https://registry.npmmirror.com
if %errorlevel% neq 0 (
    echo ❌ 前端依赖安装失败
    pause
    exit /b 1
)
echo ✅ 前端依赖安装完成

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                  安装完成！                                 ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║                                                              ║
echo ║  启动方式：                                                  ║
echo ║  1. 双击运行 "启动服务.bat"                                 ║
echo ║  2. 访问 http://localhost:3000                              ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
pause
