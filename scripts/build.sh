#!/bin/bash

# Build script for Sazna Platform services

echo "Building Sazna Platform services..."

# Build the entire project first
echo "Building the project..."
./gradlew clean build -x test

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi

echo "Build completed successfully!"

# Copy JAR files to Docker build contexts
echo "Copying JAR files to Docker contexts..."

# Create directories if they don't exist
mkdir -p sazna-backend/identity/build/libs/
mkdir -p sazna-backend/cipher/build/libs/

echo "JAR files are ready for Docker builds."
echo "To build Docker images, run: docker-compose build"
echo "To start services, run: docker-compose up"