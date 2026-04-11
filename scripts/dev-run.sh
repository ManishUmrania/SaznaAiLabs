#!/bin/bash

# Development run script for Sazna Platform services

echo "Starting Sazna Platform services in development mode..."

# Defaults (for local Postgres)
PG_HOST="localhost"
PG_PORT=5432
PG_DB="sazna_db"
PG_USER="postgres"
PG_PASSWORD="admin"
USE_DOCKER=false
DOCKER_CONTAINER_NAME="sazna-postgres"

# helper: check if a given port on localhost is listening
is_port_listening() {
  local port=$1
  if command -v nc >/dev/null 2>&1; then
    nc -z localhost $port >/dev/null 2>&1
    return $?
  elif command -v ss >/dev/null 2>&1; then
    ss -ltn | grep -q ":$port " >/dev/null 2>&1
    return $?
  elif command -v lsof >/dev/null 2>&1; then
    lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1
    return $?
  else
    # conservative fallback: assume not listening
    return 1
  fi
}

# Check required service ports (8080, 8081) are free
for p in 8080 8081; do
  if is_port_listening $p; then
    echo "Required port $p is already in use — please free it and try again"
    exit 1
  fi
done

# Detect local PostgreSQL on 5432
if is_port_listening $PG_PORT; then
  echo "Found a listening process on $PG_HOST:$PG_PORT; assuming local PostgreSQL."
  echo "Please ensure database $PG_DB and user $PG_USER exist and are accessible."
else
  echo "No local PostgreSQL detected on $PG_HOST:$PG_PORT. Will start a Docker PostgreSQL container on host port 5433."
  # Ensure Docker is available
  if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is not installed or not in PATH and no local PostgreSQL detected."
    echo "Please install Docker or start PostgreSQL locally and re-run this script."
    exit 1
  fi

  # ensure host port 5433 is free for the container
  if is_port_listening 5433; then
    echo "Host port 5433 is already in use — cannot start Docker PostgreSQL on that port."
    exit 1
  fi

  echo "Starting PostgreSQL database in Docker..."
  docker run -d \
    --name ${DOCKER_CONTAINER_NAME} \
    -e POSTGRES_DB=${PG_DB} \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=admin2 \
    -p 5433:5432 \
    -v postgres_data:/var/lib/postgresql/data \
    postgres:15-alpine >/dev/null

  USE_DOCKER=true
  PG_PORT=5433
  PG_USER="admin"
  PG_PASSWORD="admin2"

  echo "Waiting for PostgreSQL to be ready in Docker..."
  # wait until pg_isready inside the container says it's ready
  until docker exec ${DOCKER_CONTAINER_NAME} pg_isready -U ${PG_USER} >/dev/null 2>&1; do
    sleep 1
  done
fi

JDBC_URL="jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DB}"

echo "PostgreSQL is ready (or will be shortly)!"

echo "Starting Identity Service..."
./gradlew :sazna-backend:identity:bootRun &

echo "Starting Cipher Service..."
./gradlew :sazna-backend:cipher:bootRun &

echo "All services started!"
echo "Identity Service: http://localhost:8080"
echo "Cipher Service: http://localhost:8081"
echo "PostgreSQL: ${JDBC_URL}"

# Cleanup function to stop docker container if we started it
cleanup() {
  echo "Stopping services..."
  if [ "$USE_DOCKER" = true ]; then
    echo "Stopping Docker container ${DOCKER_CONTAINER_NAME}..."
    docker stop ${DOCKER_CONTAINER_NAME} >/dev/null 2>&1 || true
    docker rm ${DOCKER_CONTAINER_NAME} >/dev/null 2>&1 || true
  fi
  exit 0
}

trap cleanup INT TERM

# Wait for user to press Ctrl+C
echo "Press Ctrl+C to stop all services"
wait

