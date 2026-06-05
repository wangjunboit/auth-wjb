# 本项目专用 Maven 包装脚本(PowerShell)
# 仅在本次调用内把 JAVA_HOME 指向 JDK 21,不修改系统/用户环境变量。
# 用法示例:
#   .\build.ps1 clean compile
#   .\build.ps1 test
#   .\build.ps1 spring-boot:run
$env:JAVA_HOME = "D:\Program Files\Java\jdk-21.0.10"
Write-Host "[build.ps1] JAVA_HOME(本次) = $env:JAVA_HOME" -ForegroundColor Cyan
& mvn -f "$PSScriptRoot\pom.xml" @args
