@echo off

REM Build script for Sazna Platform services

echo Building Sazna Platform services...

REM Build the entire project first
echo Building the project...
call gradlew.bat clean build -x test

REM Check if build was successful
if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Exiting.
    exit /b 1
)

echo Build completed successfully!

REM Copy JAR files to Docker build contexts
echo Copying JAR files to Docker contexts...

REM Create directories if they don't exist
if not exist sazna-backend\identity\build\libs mkdir sazna-backend\identity\build\libs
if not exist sazna-backend\cipher\build\libs mkdir sazna-backend\cipher\build\libs

echo JAR files are ready for Docker builds.
echo To build Docker images, run: docker-compose build
echo To start services, run: docker-compose up