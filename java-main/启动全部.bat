@echo off
chcp 936 >nul
setlocal
title IDV-START-ALL

set "ROOT=%~dp0"
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

echo ============================================================
echo   一键启动：数据库 + 后端 8081 + 前端 5173
echo ============================================================
echo.

echo [1/3] 启动 MySQL80 服务并初始化数据库...
net start MySQL80 >nul 2>&1
ping -n 3 127.0.0.1 >nul
if exist "%MYSQL%" (
    "%MYSQL%" -uroot -p123456 --default-character-set=utf8mb4 < "%ROOT%springBootTest\sql\init.sql"
    if errorlevel 1 (
        echo       [警告] 数据库初始化失败，后端可能连不上数据库。
    ) else (
        echo       [成功] 数据库 idv_auth 已就绪。
    )
) else (
    echo       [警告] 找不到 mysql.exe，跳过数据库初始化。
)

echo.
echo [2/3] 启动后端，新窗口 IDV-BACKEND ...
start "IDV-BACKEND" cmd /k ""%ROOT%启动后端.bat""

echo [3/3] 启动前端，新窗口 IDV-FRONTEND ...
start "IDV-FRONTEND" cmd /k ""%ROOT%启动前端.bat""

echo.
echo 已打开两个窗口，稍等十几秒后访问：
echo    后端  http://localhost:8081
echo    前端  http://localhost:5173
echo.
if not "%NO_PAUSE%"=="1" pause
