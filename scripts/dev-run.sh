#!/bin/bash

# Development run script for Sazna Platform services

echo "Starting Sazna Platform services in development mode..."

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "Port $port is already in use"
        return 1
    fi
    return 0
}

# Check if required ports are free
if ! check_port 8080 || ! check_port 8081 || ! check_port 5432; then
    echo "Please free up the required ports and try again"
    exit 1
fi

# Start PostgreSQL in the background
echo "Starting PostgreSQL database..."
docker run -d \
  --name sazna-postgres \
  -e POSTGRES_DB=sazna_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker exec sazna-postgres pg_isready -U postgres > /dev/null 2>&1
do
    sleep 1
done

echo "PostgreSQL is ready!"

# Start Identity Service
echo "Starting Identity Service..."
./gradlew :sazna-backend:identity:bootRun &

# Start Cipher Service
echo "Starting Cipher Service..."
./gradlew :sazna-backend:cipher:bootRun &

echo "All services started!"
echo "Identity Service: http://localhost:8080"
echo "Cipher Service: http://localhost:8081"
echo "PostgreSQL: jdbc:postgresql://localhost:5432/sazna_db"

# Wait for user input to stop services
echo "Press Ctrl+C to stop all services"
wait