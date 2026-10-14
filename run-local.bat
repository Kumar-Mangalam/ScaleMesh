@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo Starting ScaleMesh Microservices Stack Locally (Windows)
echo ========================================================

if not exist logs mkdir logs

echo Checking JAR packages...
set MISSING_JARS=0
if not exist "services\fleet-telemetry-service\target\fleet-telemetry-service-1.0.0.jar" set MISSING_JARS=1
if not exist "services\trip-dispatch-service\target\trip-dispatch-service-1.0.0.jar" set MISSING_JARS=1
if not exist "services\route-engine-service\target\route-engine-service-1.0.0.jar" set MISSING_JARS=1
if not exist "services\demand-analysis-service\target\demand-analysis-service-1.0.0.jar" set MISSING_JARS=1
if not exist "services\incident-manager-service\target\incident-manager-service-1.0.0.jar" set MISSING_JARS=1
if not exist "services\api-gateway\target\api-gateway-1.0.0.jar" set MISSING_JARS=1

if !MISSING_JARS! EQU 1 (
    echo Missing service JARs detected. Building packages with Maven...
    call .\mvnw.cmd package -DskipTests
    if !ERRORLEVEL! NEQ 0 (
        echo [ERROR] Maven build failed. Aborting startup.
        exit /b 1
    )
)

echo [1/6] Launching Fleet Telemetry Service (:8082)...
start "ScaleMesh :: Fleet Telemetry (:8082)" /min cmd /c "java -jar services\fleet-telemetry-service\target\fleet-telemetry-service-1.0.0.jar > logs\fleet-telemetry-service.log 2>&1"
call :waitForService 8082 "Fleet Telemetry" logs\fleet-telemetry-service.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo [2/6] Launching Trip Dispatch Service (:8083)...
start "ScaleMesh :: Trip Dispatch (:8083)" /min cmd /c "java -jar services\trip-dispatch-service\target\trip-dispatch-service-1.0.0.jar > logs\trip-dispatch-service.log 2>&1"
call :waitForService 8083 "Trip Dispatch" logs\trip-dispatch-service.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo [3/6] Launching Route Engine Service (:8084)...
start "ScaleMesh :: Route Engine (:8084)" /min cmd /c "java -jar services\route-engine-service\target\route-engine-service-1.0.0.jar > logs\route-engine-service.log 2>&1"
call :waitForService 8084 "Route Engine" logs\route-engine-service.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo [4/6] Launching Demand Analysis Service (:8085)...
start "ScaleMesh :: Demand Analysis (:8085)" /min cmd /c "java -jar services\demand-analysis-service\target\demand-analysis-service-1.0.0.jar > logs\demand-analysis-service.log 2>&1"
call :waitForService 8085 "Demand Analysis" logs\demand-analysis-service.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo [5/6] Launching Incident Manager Service (:8086)...
start "ScaleMesh :: Incident Manager (:8086)" /min cmd /c "java -jar services\incident-manager-service\target\incident-manager-service-1.0.0.jar > logs\incident-manager-service.log 2>&1"
call :waitForService 8086 "Incident Manager" logs\incident-manager-service.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo [6/6] Launching API Gateway (:8081)...
start "ScaleMesh :: API Gateway (:8081)" /min cmd /c "java -jar services\api-gateway\target\api-gateway-1.0.0.jar > logs\api-gateway.log 2>&1"
call :waitForService 8081 "API Gateway" logs\api-gateway.log
if !ERRORLEVEL! NEQ 0 exit /b 1

echo.
echo Opening Web Dashboard...
start web-dashboard\index.html

echo ========================================================
echo ScaleMesh Platform successfully started!
echo All 6 services are healthy and listening.
echo Gateway URL: http://localhost:8081
echo System Health: http://localhost:8081/api/system/health
echo Dashboard: web-dashboard\index.html
echo Service logs are located in .\logs\
echo Run .\stop-local.bat to stop all services.
echo ========================================================
exit /b 0

:waitForService
set PORT=%~1
set NAME=%~2
set LOGFILE=%~3
echo Waiting for %NAME% on port %PORT% to become ready...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$port = %PORT%; $name = '%NAME%'; $log = '%LOGFILE%'; $url = 'http://localhost:' + $port + '/actuator/health'; $timeout = 40; $start = [System.Diagnostics.Stopwatch]::StartNew(); while ($start.Elapsed.TotalSeconds -lt $timeout) { try { $res = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop; if ($res.StatusCode -eq 200) { Write-Host ('  [OK] ' + $name + ' is healthy on port ' + $port + ' (' + [math]::Round($start.Elapsed.TotalSeconds, 1) + 's)'); exit 0 } } catch {} Start-Sleep -Milliseconds 750 }; Write-Host ('  [FAILED] ' + $name + ' did not respond within ' + $timeout + 's on port ' + $port); Write-Host ('  Check log file: ' + $log); if (Test-Path $log) { Write-Host '  Recent log output:'; Get-Content $log -Tail 15 | ForEach-Object { Write-Host ('    ' + $_) } }; exit 1"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] %NAME% failed to start.
    exit /b 1
)
exit /b 0
