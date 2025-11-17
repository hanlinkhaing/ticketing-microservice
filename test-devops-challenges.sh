#!/bin/bash

echo "=== Testing DevOps Challenges ==="
echo "Timestamp: $(date)"

# Test API Gateway autoscaling challenge
echo "Testing API Gateway autoscaling challenge..."
curl -s -w "\nTime: %{time_total}s\n" http://localhost:8080/api/challenge/autoscale

# Test User Service CPU intensive challenge
echo -e "\nTesting User Service CPU intensive challenge..."
curl -s -w "\nTime: %{time_total}s\n" http://localhost:8081/api/users/challenge/cpu-intensive

# Test User Service memory intensive challenge
echo -e "\nTesting User Service memory intensive challenge..."
curl -s -w "\nTime: %{time_total}s\n" http://localhost:8081/api/users/challenge/memory-intensive

# Test circuit breaker
echo -e "\nTesting circuit breaker (simulating service failure)..."
curl -s http://localhost:8080/api/challenge/circuit-breaker

echo -e "\n=== DevOps Challenges Test Complete ==="