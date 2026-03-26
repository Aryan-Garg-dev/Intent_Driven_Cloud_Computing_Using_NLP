#!/bin/bash

# Intent-Driven Cloud Computing Simulation - Build & Run Script
# Linux/Mac Version

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Intent-Driven Cloud Computing Simulation (JavaFX UI)     ║"
echo "║  Build & Run Script                                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "✗ Java is not installed or not in PATH"
    echo "  Please install Java 17+ from https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "✓ Java $JAVA_VERSION found"
echo ""

# Check Maven installation
if ! command -v mvn &> /dev/null; then
    echo "✗ Maven is not installed or not in PATH"
    echo "  Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "Select an option:"
echo ""
echo "1. Build project with Maven"
echo "2. Run JavaFX UI Application"
echo "3. Run CLI Simulation"
echo "4. Clean and rebuild"
echo "5. Generate project documentation"
echo "6. Exit"
echo ""

read -p "Enter your choice (1-6): " CHOICE

case $CHOICE in
    1)
        echo ""
        echo "Building project..."
        mvn clean package
        if [ $? -eq 0 ]; then
            echo "✓ Build completed successfully!"
        else
            echo "✗ Build failed"
            exit 1
        fi
        ;;
    2)
        echo ""
        echo "Starting JavaFX UI Application..."
        echo "(This may take a few seconds on first run)"
        echo ""
        mvn javafx:run
        if [ $? -ne 0 ]; then
            echo ""
            echo "Falling back to exec method..."
            mvn exec:java -Dexec.mainClass=org.intentcloudsim.ui.SimulationUI
        fi
        ;;
    3)
        echo ""
        echo "Running CLI Simulation (All 8 Experiments)..."
        echo ""
        mvn exec:java -Dexec.mainClass=org.intentcloudsim.MainSimulation
        ;;
    4)
        echo ""
        echo "Cleaning and rebuilding project..."
        mvn clean
        echo "✓ Clean complete"
        echo ""
        mvn package
        if [ $? -eq 0 ]; then
            echo "✓ Rebuild completed successfully!"
        else
            echo "✗ Build failed"
            exit 1
        fi
        ;;
    5)
        echo ""
        echo "Opening documentation..."
        if command -v less &> /dev/null; then
            less README.md
        else
            cat README.md
        fi
        ;;
    6)
        echo ""
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
read -p "Press Enter to continue..."

