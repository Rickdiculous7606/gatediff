# Windows & PowerShell Developer Guide for GateDiff

This guide explains how to set up, build, debug, and run GateDiff on Windows 10/11 using PowerShell and IntelliJ IDEA.

## Prerequisites Installation

Open PowerShell as Administrator:

```powershell
# 1. Install Java 21 (Eclipse Temurin)
winget install EclipseAdoptium.Temurin.21.JDK

# 2. Install Apache Maven
winget install Apache.Maven

# 3. Install Git (if not already installed)
winget install Git.Git
```

Close and re-open PowerShell to refresh environment variables.

Verify versions:
```powershell
java -version
mvn -version
git --version
```

## Building GateDiff on Windows

```powershell
# Clone the repository
git clone https://github.com/gatediff/gatediff.git
cd gatediff

# Run tests
mvn clean test

# Package standalone executable JAR
mvn package -DskipTests
```

The resulting JAR will be in `target\gatediff.jar`.

## Running the Mock Migration Lab on Windows

In PowerShell Terminal 1:
```powershell
java -jar target\gatediff.jar mock start --scenario MATCHING
```

In PowerShell Terminal 2:
```powershell
# Validate configuration
java -jar target\gatediff.jar config validate --file examples\configs\migration.yaml

# Run migration validation suite
java -jar target\gatediff.jar run --config examples\configs\migration.yaml --output-html reports\latest.html

# Open HTML report in Windows default browser
Start-Process reports\latest.html
```

## Checking Exit Codes in PowerShell

In PowerShell, `$LASTEXITCODE` holds the process exit status:
```powershell
java -jar target\gatediff.jar run --config examples\configs\migration.yaml
Write-Host "Process exit code was: $LASTEXITCODE"
```
- `0`: All tests passed!
- `1`: Validation regressions found.
- `2`: Configuration or command error.
- `3`: Infrastructure / network error.
