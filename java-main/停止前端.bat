@echo off
chcp 936 >nul
setlocal enabledelayedexpansion
title IDV-STOP-FRONTEND

echo ============================================================
echo   停止前端（端口 5173）
echo ============================================================
echo.

echo [1/2] 关闭前端命令行窗口，窗口标题 IDV-FRONTEND ...
taskkill /f /t /fi "WINDOWTITLE eq IDV-FRONTEND" >nul 2>&1

echo [2/2] 结束占用 5173 端口的进程 ...
call :KILLPORT 5173

echo.
if not "%NO_PAUSE%"=="1" pause
exit /b 0

:KILLPORT
set "PORT=%~1"
set "HIT="
for /f "tokens=5" %%q in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    set "HIT=1"
    echo       结束进程 PID %%q
    taskkill /f /pid %%q >nul 2>&1
)
if not defined HIT echo       端口 %PORT% 没有被监听，无需停止。
exit /b 0
