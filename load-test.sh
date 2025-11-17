#!/bin/bash

# Load testing script for ticket microservices
# This script simulates various load scenarios for testing autoscaling and performance

echo "=== Ticket Microservices Load Testing ==="
echo "Timestamp: $(date)"
echo ""

# Configuration
API_GATEWAY="http://localhost:8080"
CONCURRENT_REQUESTS=10
TOTAL_REQUESTS=100

# Function to make HTTP requests
make_request() {
    local endpoint=$1
    local method=${2:-GET}
    local data=$3
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        curl -s -X POST -H "Content-Type: application/json" -d "$data" "$endpoint"
    else
        curl -s "$endpoint"
    fi
}

# Function to run load test
run_load_test() {
    local test_name=$1
    local endpoint=$2
    local method=${3:-GET}
    local data=$4
    
    echo "Running $test_name load test..."
    echo "Endpoint: $endpoint"
    echo "Concurrent requests: $CONCURRENT_REQUESTS"
    echo "Total requests: $TOTAL_REQUESTS"
    echo ""
    
    # Start time
    start_time=$(date +%s)
    
    # Run requests in parallel
    for i in $(seq 1 $TOTAL_REQUESTS); do
        make_request "$endpoint" "$method" "$data" &
        
        # Limit concurrent requests
        if (( i % CONCURRENT_REQUESTS == 0 )); then
            wait
        fi
    done
    
    # Wait for remaining requests
    wait
    
    # End time
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    echo "✅ $test_name completed in ${duration}s"
    echo ""
}

# Test 1: Basic health check load test
echo "=== Test 1: Health Check Load Test ==="
run_load_test "Health Check" "$API_GATEWAY/health"

# Test 2: User service load test
echo "=== Test 2: User Service Load Test ==="
run_load_test "User Service Health" "$API_GATEWAY/api/users/health"

# Test 3: CPU Intensive Challenge
echo "=== Test 3: CPU Intensive Challenge ==="
run_load_test "CPU Intensive" "$API_GATEWAY/api/users/challenge/cpu-intensive"

# Test 4: Memory Intensive Challenge
echo "=== Test 4: Memory Intensive Challenge ==="
run_load_test "Memory Intensive" "$API_GATEWAY/api/users/challenge/memory-intensive"

# Test 5: Error Simulation Challenge
echo "=== Test 5: Error Simulation Challenge ==="
run_load_test "Error Simulation" "$API_GATEWAY/api/users/challenge/error-simulation"

# Test 6: API Gateway Autoscale Challenge
echo "=== Test 6: API Gateway Autoscale Challenge ==="
run_load_test "Autoscale Challenge" "$API_GATEWAY/api/challenge/autoscale"

# Test 7: Mixed load test (various endpoints)
echo "=== Test 7: Mixed Load Test ==="
echo "Testing multiple endpoints concurrently..."

# Start background load
for i in $(seq 1 50); do
    curl -s "$API_GATEWAY/api/events/active" > /dev/null &
    curl -s "$API_GATEWAY/api/users/challenge/cpu-intensive" > /dev/null &
    curl -s "$API_GATEWAY/api/challenge/autoscale" > /dev/null &
    
    if (( i % 10 == 0 )); then
        wait
    fi
done

wait
echo "✅ Mixed load test completed"
echo ""

# Test 8: Sustained load test
echo "=== Test 8: Sustained Load Test ==="
echo "Running sustained load for 60 seconds..."

sustained_start=$(date +%s)
while [ $(($(date +%s) - sustained_start)) -lt 60 ]; do
    curl -s "$API_GATEWAY/api/challenge/autoscale" > /dev/null &
    curl -s "$API_GATEWAY/api/users/challenge/cpu-intensive" > /dev/null &
    sleep 0.5
done

wait
echo "✅ Sustained load test completed"
echo ""

# Final health check
echo "=== Final Health Check ==="
curl -s "$API_GATEWAY/health" | jq -r '.status' 2>/dev/null || echo "Health check failed"

# Check service status
echo ""
echo "=== Service Status After Load Test ==="
bash monitor.sh

echo ""
echo "=== Load Testing Complete ==="
echo "Check your monitoring dashboard for autoscaling metrics and performance data."