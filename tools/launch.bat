@echo off
rem ---------------------------------------------------------------------------
rem  Starts the installed app on the connected device.
rem  Usage: launch.bat debug | release
rem ---------------------------------------------------------------------------
setlocal
set "ROOT=%~dp0.."

rem The app id comes from config\build.properties.
set "APP_ID="
for /f "usebackq tokens=1,* delims==" %%a in ("%ROOT%\config\build.properties") do if "%%a"=="app.id" set "APP_ID=%%b"
if not defined APP_ID (
    echo Could not read app.id from config\build.properties.
    exit /b 1
)
if /i "%~1"=="debug" set "APP_ID=%APP_ID%.debug"

rem adb is part of the Android SDK that Android Studio installs.
set "ADB=adb"
if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if "%ADB%"=="adb" if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo Starting %APP_ID%...
"%ADB%" shell am start -n %APP_ID%/com.vislupus.mindmath.MainActivity >nul
if errorlevel 1 (
    echo The app is installed, but could not be started automatically. Open it from the phone.
    exit /b 0
)
echo Done.
exit /b 0
