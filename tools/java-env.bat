@echo off
rem ---------------------------------------------------------------------------
rem  Points JAVA_HOME at the JDK bundled with Android Studio (JDK 21) and
rem  checks its version. Called by run.bat and build-release.bat.
rem  Java 25 breaks this Gradle/AGP combination; JDK 17-21 works.
rem ---------------------------------------------------------------------------
if not defined MINDMATH_JDK set "MINDMATH_JDK=C:\Program Files\Android\Android Studio\jbr"
set "JAVA_HOME=%MINDMATH_JDK%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo ERROR: Java was not found in "%JAVA_HOME%".
    echo Install Android Studio, or set MINDMATH_JDK to a JDK 21 folder, e.g.
    echo     set "MINDMATH_JDK=C:\path\to\jdk-21"
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "JAVA_VERSION="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i /c:"version"') do if not defined JAVA_VERSION set "JAVA_VERSION=%%~v"
set "JAVA_MAJOR="
for /f "delims=." %%m in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%m"
echo Using Java %JAVA_VERSION% from "%JAVA_HOME%"

if not defined JAVA_MAJOR (
    echo ERROR: Could not read the Java version.
    exit /b 1
)
if %JAVA_MAJOR% LSS 17 (
    echo ERROR: Java %JAVA_VERSION% is too old. This project needs JDK 17-21.
    exit /b 1
)
if %JAVA_MAJOR% GTR 21 (
    echo ERROR: Java %JAVA_VERSION% is too new. This project is verified with JDK 21.
    exit /b 1
)
exit /b 0
