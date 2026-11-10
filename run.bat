@echo off
rem ---------------------------------------------------------------------------
rem  One command to preview the app: builds the development version, installs
rem  it on the connected phone (or running emulator) and starts it.
rem
rem  The development version is a separate app called "MindMath Dev", so it
rem  never touches the data of the real app.
rem ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

set "PAUSE_AT_END="
echo %cmdcmdline% | findstr /i /c:"%~nx0" >nul && set "PAUSE_AT_END=1"

call "%~dp0tools\java-env.bat"
if errorlevel 1 (
    if defined PAUSE_AT_END pause
    exit /b 1
)

echo.
echo Building and installing MindMath Dev...
call gradlew.bat installDebug --no-configuration-cache
if errorlevel 1 (
    echo.
    echo FAILED - see the messages above.
    echo If no device was found: connect the phone with USB debugging enabled,
    echo or start an emulator from Android Studio ^(Device Manager^).
    if defined PAUSE_AT_END pause
    exit /b 1
)

call "%~dp0tools\launch.bat" debug
if defined PAUSE_AT_END pause
exit /b 0
