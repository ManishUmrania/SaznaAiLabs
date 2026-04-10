@echo off

REM Development run script for Sazna Platform services

echo Starting Sazna Platform services in development mode...

REM Check if Docker is installed
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Docker is not installed or not in PATH
    echo Please install Docker Desktop and try again
    exit /b 1
)

REM Start PostgreSQL in the background
echo Starting PostgreSQL database...
docker run -d ^
  --name sazna-postgres ^
  -e POSTGRES_DB=sazna_db ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=postgres ^
  -p 5432:5432 ^
  -v postgres_data:/var/lib/postgresql/data ^
  postgres:15-alpine

REM Wait for PostgreSQL to be ready
echo Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak >nul

echo PostgreSQL is ready!

REM Start Identity Service
echo Starting Identity Service...
start "Identity Service" cmd /c "gradlew.bat :sazna-backend:identity:bootRun"

REM Start Cipher Service
echo Starting Cipher Service...
start "Cipher Service" cmd /c "gradlew.bat :sazna-backend:cipher:bootRun"

echo All services started!
echo Identity Service: http://localhost:8080
echo Cipher Service: http://localhost:8081
echo PostgreSQL: jdbc:postgresql://localhost:5432/sazna_db

echo Press any key to stop all services
pause >nul

REM Stop and remove containers
echo Stopping services...
docker stop sazna-postgres >nul 2>&1
docker rm sazna-postgres >nul 2>&1