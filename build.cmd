@echo off
set "JAVA_HOME=D:\Program Files\Java\jdk-21.0.10"
echo [build.cmd] JAVA_HOME(local) = %JAVA_HOME%
mvn -f "%~dp0pom.xml" %*
