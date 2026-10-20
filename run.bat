@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"

if not exist "%~dp0target\personal-finance-platform-1.0.0.jar" (
    echo Building project jar...
    call "C:\Users\namra\apache-maven-3.9.6\bin\mvn.cmd" package -DskipTests
)

echo Launching Online Personal Finance Management Platform...
start "" "%JAVA_HOME%\bin\javaw.exe" -jar "%~dp0target\personal-finance-platform-1.0.0.jar"
endlocal
