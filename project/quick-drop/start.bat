@echo off
chcp 65001 >nul
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    闪念笔记 - QuickDrop                      ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 正在启动开发服务器...
echo.

cd frontend
call npm run dev

pause
