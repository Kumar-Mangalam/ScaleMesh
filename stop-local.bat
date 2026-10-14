@echo off
echo Stopping all running ScaleMesh Java processes (ports 8081-8086)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr /R ":808[1-6] " ^| findstr "LISTENING"') do (
    echo Terminating PID %%a...
    taskkill /F /PID %%a 2>nul
)
echo ScaleMesh processes stopped.
