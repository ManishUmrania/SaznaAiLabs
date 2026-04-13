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

REM Configure expected local Postgres connection (change these if your local DB uses different credentials/port)
set "PG_HOST=localhost"
set "PG_PORT=5432"
set "PG_DB=sazna_db"
set "PG_USER=postgres"
set "PG_PASSWORD=admin"

echo Checking for local PostgreSQL at %PG_HOST%:%PG_PORT%...
powershell -Command "try { $c = New-Object Net.Sockets.TcpClient; $c.Connect('%PG_HOST%', %PG_PORT%); $c.Close(); exit 0 } catch { exit 1 }"
if %ERRORLEVEL% EQU 0 (
    echo Found a listening process on %PG_HOST%:%PG_PORT%; assuming local PostgreSQL.
    echo Please ensure database %PG_DB% and user %PG_USER% exist and are accessible.
) else (
    echo No local PostgreSQL detected on %PG_HOST%:%PG_PORT%.
    where docker >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo Docker is not installed and no local PostgreSQL detected.
        echo Please install Docker Desktop or start PostgreSQL locally and re-run this script.
        exit /b 1
    )
    echo Starting PostgreSQL database in Docker...
    docker run -d ^
      --name sazna-postgres ^
      -e POSTGRES_DB=%PG_DB% ^
      -e POSTGRES_USER=%PG_USER% ^
      -e POSTGRES_PASSWORD=%PG_PASSWORD% ^
      -p 5432:5432 ^
      -v postgres_data:/var/lib/postgresql/data ^
      postgres:15-alpine

    echo Waiting for PostgreSQL to be ready in Docker...
    timeout /t 10 /nobreak >nul
)

REM Compose JDBC URL for downstream messages
set "JDBC_URL=jdbc:postgresql://%PG_HOST%:%PG_PORT%/%PG_DB%"

echo PostgreSQL is ready (or will be shortly)!

@echo off

REM Move to project root
cd /d %~dp0..

echo Starting services in Windows Terminal tabs...

wt ^
new-tab cmd /k "cd /d C:\work\sazna-platform && gradlew.bat :identity:bootRun" ^
; new-tab cmd /k "cd /d C:\work\sazna-platform && gradlew.bat :cipher:bootRun"

echo All services started!
echo Identity Service: http://localhost:8080
echo Cipher Service: http://localhost:8081
echo PostgreSQL: %JDBC_URL%

echo Press any key to stop all services
pause >nul

REM Stop and remove containers
echo Stopping services...
docker stop sazna-postgres >nul 2>&1
docker rm sazna-postgres >nul 2>&1
