@echo off
title Build and Run ShelterStorm
cd /d "%~dp0"

echo ========================================================
echo          Compiling ShelterStorm: Eco-Resilience         
echo ========================================================
echo.

if not exist bin mkdir bin

javac --release 8 -d bin src\shelterstorm\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Check the errors above.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation successful!
echo.
echo Starting game...
java -cp bin shelterstorm.Main

pause
