package com.ticketmicroservices.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class DevOpsChallengeController {

    @GetMapping("/challenge/cpu-intensive")
    public Map<String, Object> cpuIntensiveChallenge() {
        // Simulate CPU-intensive task for autoscaling
        long startTime = System.currentTimeMillis();
        
        // CPU-intensive calculation
        double result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += Math.sqrt(Math.random() * 1000000);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("challenge", "cpu-intensive");
        response.put("result", result);
        response.put("duration_ms", duration);
        response.put("message", "CPU-intensive task completed");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @GetMapping("/challenge/memory-intensive")
    public Map<String, Object> memoryIntensiveChallenge() {
        // Simulate memory-intensive task
        long startTime = System.currentTimeMillis();
        
        // Allocate memory
        int[][] memoryHog = new int[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                memoryHog[i][j] = (int)(Math.random() * 1000);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        
        Map<String, Object> response = new HashMap<>();
        response.put("challenge", "memory-intensive");
        response.put("memory_allocated_mb", memoryUsed / (1024 * 1024));
        response.put("duration_ms", duration);
        response.put("message", "Memory-intensive task completed");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @GetMapping("/challenge/error-simulation")
    public Map<String, Object> errorSimulationChallenge() {
        // Simulate random errors for testing circuit breakers
        double random = Math.random();
        
        if (random < 0.3) { // 30% chance of error
            throw new RuntimeException("Simulated service error for circuit breaker testing");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("challenge", "error-simulation");
        response.put("success", true);
        response.put("message", "Request completed successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("available_processors", runtime.availableProcessors());
        metrics.put("free_memory_mb", runtime.freeMemory() / (1024 * 1024));
        metrics.put("total_memory_mb", runtime.totalMemory() / (1024 * 1024));
        metrics.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
        metrics.put("timestamp", System.currentTimeMillis());
        
        return metrics;
    }
}