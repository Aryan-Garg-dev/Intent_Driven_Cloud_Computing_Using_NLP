@echo off
REM Intent-Driven Cloud Computing Simulation - Build & Run Script
REM Windows Version

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║  Intent-Driven Cloud Computing Simulation (JavaFX UI)     ║
echo ║  Build & Run Script                                       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ✗ Java is not installed or not in PATH
    echo   Please install Java 17+ from https://adoptium.net/
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| find "version"') do (
    set JAVA_VERSION=%%i
    echo ✓ Java !JAVA_VERSION! found
)

echo.
echo Select an option:
echo.
echo 1. Build project with Maven
echo 2. Run JavaFX UI Application
echo 3. Run CLI Simulation
echo 4. Clean and rebuild
echo 5. Generate project documentation
echo 6. Exit
echo.

set /p CHOICE="Enter your choice (1-6): "

if "%CHOICE%"=="1" (
    echo.
    echo Building project...
    mvn clean package
    if errorlevel 1 (
        echo ✗ Build failed. Make sure Maven is installed and in PATH.
        echo   Install Maven from: https://maven.apache.org/download.cgi
        exit /b 1
    )
    echo ✓ Build completed successfully!
    
) else if "%CHOICE%"=="2" (
    echo.
    echo Starting JavaFX UI Application...
    echo (This may take a few seconds on first run)
    echo.
    mvn javafx:run
    if errorlevel 1 (
        echo.
        echo Falling back to exec method...
        mvn exec:java -Dexec.mainClass=org.intentcloudsim.ui.SimulationUI
    )
    
) else if "%CHOICE%"=="3" (
    echo.
    echo Running CLI Simulation (All 8 Experiments)...
    echo.
    mvn exec:java -Dexec.mainClass=org.intentcloudsim.MainSimulation
    
) else if "%CHOICE%"=="4" (
    echo.
    echo Cleaning and rebuilding project...
    mvn clean
    echo ✓ Clean complete
    echo.
    mvn package
    if errorlevel 1 (
        echo ✗ Build failed
        exit /b 1
    )
    echo ✓ Rebuild completed successfully!
    
) else if "%CHOICE%"=="5" (
    echo.
    echo Generating documentation...
    echo Documentation available in README.md
    start notepad README.md
    
) else if "%CHOICE%"=="6" (
    echo.
    echo Exiting...
    exit /b 0
    
) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

echo.
echo Press any key to continue...
pause >nul

endlocal
