# 根级 Maven 包装脚本:固定 JDK21,作用于整个多模块工程
$env:JAVA_HOME = "D:\Program Files\Java\jdk-21.0.10"
Write-Host "[build.ps1] JAVA_HOME(本次) = $env:JAVA_HOME" -ForegroundColor Cyan
& mvn -f "$PSScriptRoot\pom.xml" @args
