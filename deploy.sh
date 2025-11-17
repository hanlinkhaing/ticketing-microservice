#!/bin/bash

# Deployment script for ticket microservices
# This script builds and deploys all services using Docker Compose

echo "=== Ticket Microservices Deployment Script ==="
echo "Timestamp: $(date)"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "=== Checking Prerequisites ==="
if ! command_exists docker; then
    echo "❌ Docker is not installed"
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed"
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Build Spring Boot services
echo ""
echo "=== Building Spring Boot Services ==="

services=("user-service" "event-service" "ticket-service" "order-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd "$service" || exit 1
    
    # Check if Maven wrapper exists, if not use system Maven
    if [ -f "./mvnw" ]; then
        ./mvnw clean package -DskipTests
    else
        mvn clean package -DskipTests
    fi
    
    if [ $? -eq 0 ]; then
        echo "✅ $service built successfully"
    else
        echo "❌ $service build failed"
        exit 1
    fi
    
    cd ..
done

# Build Node.js services
echo ""
echo "=== Building Node.js Services ==="

node_services=("payment-service" "notification-service" "chat-service" "api-gateway")

for service in "${node_services[@]}"; do
    echo "Installing dependencies for $service..."
    cd "$service" || exit 1
    npm install
    
    if [ $? -eq 0 ]; then
        echo "✅ $service dependencies installed"
    else
        echo "❌ $service dependency installation failed"
        exit 1
    fi
    
    cd ..
done

# Build React applications
echo ""
echo "=== Building React Applications ==="

react_apps=("user-portal" "admin-portal")

for app in "${react_apps[@]}"; do
    echo "Building $app..."
    cd "$app" || exit 1
    npm install
    npm run build
    
    if [ $? -eq 0 ]; then
        echo "✅ $app built successfully"
    else
        echo "❌ $app build failed"
        exit 1
    fi
    
    cd ..
done

# Deploy with Docker Compose
echo ""
echo "=== Deploying Services with Docker Compose ==="

# Stop existing containers
docker-compose down

# Remove old images
# docker-compose rm -f

# Build and start services
docker-compose up --build -d

if [ $? -eq 0 ]; then
    echo "✅ Services deployed successfully"
else
    echo "❌ Deployment failed"
    exit 1
fi

# Wait for services to start
echo ""
echo "=== Waiting for Services to Start ==="
sleep 30

# Run health check
echo ""
echo "=== Running Health Check ==="
bash monitor.sh

echo ""
echo "=== Deployment Complete ==="
echo "Services should be available at:"
echo "- API Gateway: http://localhost:8080"
echo "- User Portal: http://localhost:3000"
echo "- Admin Portal: http://localhost:3001"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop services: docker-compose down"