package com.ticketmicroservices.userservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Simulate various health checks
        boolean databaseHealthy = checkDatabase();
        boolean redisHealthy = checkRedis();
        boolean kafkaHealthy = checkKafka();
        
        if (databaseHealthy && redisHealthy && kafkaHealthy) {
            return Health.up()
                .withDetail("database", "healthy")
                .withDetail("redis", "healthy")
                .withDetail("kafka", "healthy")
                .withDetail("service", "user-service")
                .build();
        } else {
            return Health.down()
                .withDetail("database", databaseHealthy ? "healthy" : "unhealthy")
                .withDetail("redis", redisHealthy ? "healthy" : "unhealthy")
                .withDetail("kafka", kafkaHealthy ? "healthy" : "unhealthy")
                .withDetail("service", "user-service")
                .build();
        }
    }
    
    private boolean checkDatabase() {
        // Simulate database health check
        return Math.random() > 0.1; // 90% success rate
    }
    
    private boolean checkRedis() {
        // Simulate Redis health check
        return Math.random() > 0.1; // 90% success rate
    }
    
    private boolean checkKafka() {
        // Simulate Kafka health check
        return Math.random() > 0.1; // 90% success rate
    }
}