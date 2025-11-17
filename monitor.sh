#!/bin/bash

# Monitoring script for the ticket microservices system
# This script checks the health of all services and provides metrics

echo "=== Ticket Microservices Health Check ==="
echo "Timestamp: $(date)"
echo ""

# Function to check service health
check_service() {
    local service_name=$1
    local service_url=$2
    local expected_status=$3
    
    if [ -z "$expected_status" ]; then
        expected_status="200"
    fi
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$service_url" 2>/dev/null)
    
    if [ "$response" = "$expected_status" ]; then
        echo "✅ $service_name: HEALTHY (HTTP $response)"
        return 0
    else
        echo "❌ $service_name: UNHEALTHY (HTTP $response)"
        return 1
    fi
}

# Function to check database connectivity
check_database() {
    local db_name=$1
    local container_name=$2
    
    if docker exec "$container_name" pg_isready -U ticketuser > /dev/null 2>&1; then
        echo "✅ $db_name: CONNECTED"
        return 0
    else
        echo "❌ $db_name: DISCONNECTED"
        return 1
    fi
}

# Function to check container status
check_container() {
    local container_name=$1
    local status=$(docker inspect -f '{{.State.Status}}' "$container_name" 2>/dev/null)
    
    if [ "$status" = "running" ]; then
        echo "✅ $container_name: RUNNING"
        return 0
    else
        echo "❌ $container_name: NOT RUNNING ($status)"
        return 1
    fi
}

echo "=== Container Status ==="
check_container "ticket-microservices-postgres-1"
check_container "ticket-microservices-redis-1"
check_container "ticket-microservices-kafka-1"
check_container "ticket-microservices-zookeeper-1"
check_container "ticket-microservices-mongodb-1"

echo ""
echo "=== Service Health Checks ==="
check_service "API Gateway" "http://localhost:8080/health"
check_service "User Service" "http://localhost:8081/api/users/health"
check_service "Event Service" "http://localhost:8082/api/events/health"
check_service "Ticket Service" "http://localhost:8083/api/tickets/health"
check_service "Order Service" "http://localhost:8084/api/orders/health"
check_service "Payment Service" "http://localhost:3001/health"
check_service "Notification Service" "http://localhost:3002/health"
check_service "Chat Service" "http://localhost:3003/health"

echo ""
echo "=== Database Connectivity ==="
check_database "PostgreSQL" "ticket-microservices-postgres-1"

echo ""
echo "=== Service Metrics ==="
echo "API Gateway: $(curl -s http://localhost:8080/health | jq -r '.status' 2>/dev/null || echo 'N/A')"
echo "User Service Metrics:"
curl -s http://localhost:8081/api/users/metrics | jq -r '.available_processors + " processors, " + (.free_memory_mb | floor) + " MB free"' 2>/dev/null || echo 'N/A'

echo ""
echo "=== Load Testing Endpoints ==="
echo "CPU Intensive Challenge: http://localhost:8081/api/users/challenge/cpu-intensive"
echo "Memory Intensive Challenge: http://localhost:8081/api/users/challenge/memory-intensive"
echo "Error Simulation Challenge: http://localhost:8081/api/users/challenge/error-simulation"
echo "API Gateway Autoscale Challenge: http://localhost:8080/api/challenge/autoscale"
echo "API Gateway Memory Challenge: http://localhost:8080/api/challenge/memory"

echo ""
echo "=== System Resources ==="
echo "Docker Containers: $(docker ps -q | wc -l) running"
echo "Memory Usage: $(free -h | grep '^Mem:' | awk '{print $3 "/" $2}')"
echo "CPU Load: $(uptime | awk -F'load average:' '{print $2}')"

echo ""
echo "=== Health Check Complete ==="