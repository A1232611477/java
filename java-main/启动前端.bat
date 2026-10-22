@echo off
chcp 936 >nul
setlocal
title IDV-FRONTEND

set "ROOT=%~dp0"
set "PROJ=%ROOT%第五人格"
set "NODE_DIR=D:\Program Files\nodejs"
if exist "%NODE_DIR%\node.exe" set "PATH=%NODE_DIR%;%PATH%"

echo ============================================================
echo   前端 第五人格（Vue3 + Vite）   地址：http://localhost:5173
echo   按 Ctrl+C 或直接关闭本窗口即可停止
echo ============================================================
echo.

if not exist "%PROJ%\package.json" (
    echo [错误] 找不到前端项目：%PROJ%
    goto :END
)

where node >nul 2>&1
if errorlevel 1 (
    echo [错误] 没有找到 node 命令，请先安装 Node.js 18 以上版本。
    goto :END
)

cd /d "%PROJ%"

if not exist "node_modules" (
    echo [1/2] 首次运行，正在安装前端依赖 npm install ...
    call npm install
    if errorlevel 1 (
        echo [失败] npm install 失败，请检查网络。
        goto :END
    )
) else (
    echo [1/2] 依赖已存在，跳过 npm install。
)

echo [2/2] 启动 Vite 开发服务器，浏览器会自动打开...
echo.
call npm run dev

echo.
echo [已退出] 前端进程结束。

:END
echo.
if not "%NO_PAUSE%"=="1" pause
