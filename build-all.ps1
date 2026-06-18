# One-click build: frontend -> copy into gateway static -> package backend (gateway jar bundles frontend)
# NOTE: npm/Vite write warnings to stderr; PowerShell 5.1 would treat that as a terminating error,
# so we run npm via `cmd /c` and check $LASTEXITCODE explicitly instead of relying on ErrorActionPreference.
$env:JAVA_HOME = "D:\Program Files\Java\jdk-21.0.10"
$root = $PSScriptRoot

Write-Host "[1/3] building frontend..." -ForegroundColor Cyan
cmd /c "cd /d `"$root\frontend`" && npm install --no-audit --no-fund && npm run build"
if ($LASTEXITCODE -ne 0) { Write-Host "frontend build failed" -ForegroundColor Red; exit 1 }

Write-Host "[2/3] copying dist to gateway static..." -ForegroundColor Cyan
$static = "$root\auth-gateway\src\main\resources\static"
if (Test-Path $static) { Get-ChildItem $static -Exclude ".gitkeep" | Remove-Item -Recurse -Force }
New-Item -ItemType Directory -Force -Path $static | Out-Null
Copy-Item "$root\frontend\dist\*" -Destination $static -Recurse -Force

Write-Host "[3/3] packaging backend..." -ForegroundColor Cyan
cmd /c "mvn -f `"$root\pom.xml`" -DskipTests package"
if ($LASTEXITCODE -ne 0) { Write-Host "backend package failed" -ForegroundColor Red; exit 1 }

Write-Host "DONE: auth-gateway jar bundles the frontend. Start auth-service + auth-gateway, then open the gateway port." -ForegroundColor Green
