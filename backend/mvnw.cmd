@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for ACME Salary Management System
@REM ----------------------------------------------------------------------------
@echo off
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    mvn %*
) else (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\plugins\maven\lib\maven3\bin\mvn.cmd" (
        "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\plugins\maven\lib\maven3\bin\mvn.cmd" %*
    ) else (
        echo [ERROR] Maven not found on PATH or bundled IntelliJ path.
        exit /b 1
    )
)
