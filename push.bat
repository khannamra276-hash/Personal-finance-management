@echo off
setlocal
set "PATH=C:\Users\namra\mingit\cmd;%PATH%"
cd /d "%~dp0"

echo Pushing Personal Finance Management Platform to GitHub...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo Successfully pushed to GitHub!
) else (
    echo.
    echo If prompted, enter your GitHub Username and Personal Access Token.
)
pause
endlocal
