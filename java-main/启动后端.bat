@echo off
chcp 936 >nul
setlocal
title IDV-BACKEND

set "ROOT=%~dp0"
set "PROJ=%ROOT%springBootTest"
set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem ============================================================
rem  重要：必须清空下面两个被外部注入的环境变量。
rem  否则 Spring Boot 会绑到随机端口，报 Port xxxxx already in use。
rem ============================================================
set "SERVER__PORT="
set "SERVER__HOST="

echo ============================================================
echo   后端 springBootTest      地址：http://localhost:8081
echo   按 Ctrl+C 或直接关闭本窗口即可停止
echo ============================================================
echo.

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [错误] 找不到 JDK：%JAVA_HOME%\bin\java.exe
    goto :END
)
if not exist "%PROJ%\pom.xml" (
    echo [错误] 找不到项目目录：%PROJ%
    goto :END
)

cd /d "%PROJ%"

rem 优先使用本机已缓存的 Maven，找不到再用项目自带的 mvnw.cmd
set "MVN="
for /d %%a in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\*") do (
    if exist "%%~a\apache-maven-3.9.9\bin\mvn.cmd" set "MVN=%%~a\apache-maven-3.9.9\bin\mvn.cmd"
)
if not defined MVN (
    set "MVN=%PROJ%\mvnw.cmd"
    echo [提示] 未找到本地缓存的 Maven，改用项目自带的 mvnw.cmd
    echo        首次会自动联网下载 Maven，约 9MB，请耐心等待。
    echo.
)

echo [启动中] 编译并运行，首次约 30 秒，之后约 10 秒...
echo.
call "%MVN%" -DskipTests spring-boot:run

echo.
echo [已退出] 后端进程结束。

:END
echo.
if not "%NO_PAUSE%"=="1" pause
