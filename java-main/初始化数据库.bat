@echo off
chcp 936 >nul
setlocal
title IDV-INIT-DB

set "ROOT=%~dp0"
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set "SQL=%ROOT%springBootTest\sql\init.sql"

echo ============================================================
echo   初始化数据库：idv_auth    表：t_user
echo ============================================================
echo.

if not exist "%MYSQL%" (
    echo [错误] 找不到 mysql.exe：
    echo        %MYSQL%
    echo        请检查 MySQL 8.0 的安装路径是否正确。
    goto :END
)
if not exist "%SQL%" (
    echo [错误] 找不到建表脚本：
    echo        %SQL%
    goto :END
)

echo [1/3] 启动 MySQL80 服务，已启动会自动跳过...
net start MySQL80 >nul 2>&1

echo [2/3] 等待 2 秒，确认数据库就绪...
ping -n 3 127.0.0.1 >nul

echo [3/3] 执行 init.sql 建库建表...
"%MYSQL%" -uroot -p123456 --default-character-set=utf8mb4 < "%SQL%"
if errorlevel 1 (
    echo.
    echo [失败] 初始化失败，请检查：
    echo        1) MySQL80 服务是否已启动
    echo        2) root 密码是否为 123456，见 application-dev.properties
    goto :END
)

echo.
echo [成功] 数据库 idv_auth 已就绪，现有表：
"%MYSQL%" -uroot -p123456 --default-character-set=utf8mb4 -e "use idv_auth; show tables;"

:END
echo.
if not "%NO_PAUSE%"=="1" pause
