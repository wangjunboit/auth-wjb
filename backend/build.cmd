@echo off
REM 本项目专用 Maven 包装脚本(CMD)
REM 仅在本次调用内把 JAVA_HOME 指向 JDK 21,不修改系统/用户环境变量。
REM 用法示例:
REM   build.cmd clean compile
REM   build.cmd test
REM   build.cmd spring-boot:run
set "JAVA_HOME=D:\Program Files\Java\jdk-21.0.10"
echo [build.cmd] JAVA_HOME(local) = %JAVA_HOME%
mvn -f "%~dp0pom.xml" %*
