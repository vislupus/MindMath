@echo off
rem ---------------------------------------------------------------------------
rem  Builds the signed release APK.
rem
rem      build-release.bat            build only
rem      build-release.bat install    build, then install and start it on the
rem                                   phone connected over USB
rem
rem  Result: app\build\outputs\apk\release\app-release.apk
rem ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

rem Keep the window open at the end when the file was double-clicked.
set "PAUSE_AT_END="
echo %cmdcmdline% | findstr /i /c:"%~nx0" >nul && set "PAUSE_AT_END=1"

call "%~dp0tools\java-env.bat"
if errorlevel 1 (
    if defined PAUSE_AT_END pause
    exit /b 1
)

echo.
echo Building the release APK...
call gradlew.bat assembleRelease --no-configuration-cache
if errorlevel 1 (
    echo.
    echo BUILD FAILED - see the messages above.
    if defined PAUSE_AT_END pause
    exit /b 1
)

set "APK=%~dp0app\build\outputs\apk\release\app-release.apk"
if not exist "%APK%" (
    echo.
    echo BUILD FAILED - the APK was not created: "%APK%"
    if defined PAUSE_AT_END pause
    exit /b 1
)

echo.
echo BUILD SUCCESSFUL
echo APK: %APK%

if /i "%~1"=="install" (
    echo.
    echo Installing on the connected phone...
    call gradlew.bat installRelease --no-configuration-cache
    if errorlevel 1 (
        echo.
        echo INSTALL FAILED - is the phone connected with USB debugging enabled?
        if defined PAUSE_AT_END pause
        exit /b 1
    )
    call "%~dp0tools\launch.bat" release
)

if defined PAUSE_AT_END pause
exit /b 0
