@echo off
title ShelterStorm: Eco-Resilience
cd /d "%~dp0"

echo ========================================================
echo         Launching ShelterStorm: Eco-Resilience          
echo ========================================================
echo.

java -cp bin shelterstorm.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Something went wrong running the game.
    echo Make sure Java is installed on your computer.
    pause
)
