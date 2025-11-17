# Ticket Microservices System

A simple microservices-based ticketing system designed for DevOps learning purposes. The system allows users to purchase event tickets through a web portal while administrators manage events and orders.

## Architecture

### Frontend Applications
- **User Portal (React)**: Customer-facing application for browsing events and purchasing tickets
- **Admin Portal (React)**: Administrative interface for managing events, users, and orders

### Backend Microservices
1. **User Service (Spring Boot)**: User registration, authentication, and profile management
2. **Event Service (Spring Boot)**: Event creation, management, and querying
3. **Ticket Service (Spring Boot)**: Ticket inventory and reservation management
4. **Order Service (Spring Boot)**: Order processing and management
5. **Payment Service (Express.js)**: Payment processing (fake implementation)
6. **Notification Service (Express.js with Socket.io)**: Send notifications back to Frontend with Socket
7. **Chat Service (Express.js with Socket.io)**: Getting help from admin

### Infrastructure Components
1. **API Gateway**: Single entry point for all client requests
2. **Kafka**: Event-driven messaging between services
3. **Redis**: Caching and session management
4. **PostgreSQL**: Primary database for transactional data
5. **MongoDB**: Document storage for event details and logs

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Node.js (for local development)
- Java 11+ (for local development)
- Maven (for building Spring Boot services)

### Deployment

1. **Clone the repository**
```bash
git clone <repository-url>
cd ticket-microservices
```

2. **Deploy the system**
```bash
chmod +x deploy.sh
./deploy.sh
```

3. **Access the applications**
- User Portal: http://localhost:3000
- Admin Portal: http://localhost:3001
- API Gateway: http://localhost:8080

### Monitoring

Run the monitoring script to check system health:
```bash
chmod +x monitor.sh
./monitor.sh
```

### Load Testing

Run load tests to trigger autoscaling and test system resilience:
```bash
chmod +x load-test.sh
./load-test.sh
```

## DevOps Challenges

### 1. Autoscaling Triggers
The system includes endpoints that simulate high CPU and memory usage:
- **CPU Intensive**: `GET /api/users/challenge/cpu-intensive`
- **Memory Intensive**: `GET /api/users/challenge/memory-intensive`
- **API Gateway Autoscale**: `GET /api/challenge/autoscale`

### 2. Circuit Breakers
The API Gateway implements circuit breakers for all service proxies with:
- 5 failure threshold
- 1 minute timeout
- 30 second reset time

### 3. Health Checks
All services provide health check endpoints:
- User Service: `GET /api/users/health`
- Event Service: `GET /api/events/health`
- Ticket Service: `GET /api/tickets/health`
- Order Service: `GET /api/orders/health`
- Payment Service: `GET /api/payments/health`
- Notification Service: `GET /api/notifications/health`
- Chat Service: `GET /api/chat/health`

### 4. Error Simulation
Test error handling and circuit breakers:
- **Error Simulation**: `GET /api/users/challenge/error-simulation` (30% failure rate)

## Service Communication

### Event-Driven Architecture
Services communicate through Kafka topics:
- `user-events`: User registration and updates
- `event-notifications`: Event creation and updates
- `order-events`: Order lifecycle events
- `payment-events`: Payment processing events
- `ticket-events`: Ticket reservation and sales

### REST APIs
Services expose REST APIs for synchronous operations:
- User management, authentication
- Event CRUD operations
- Ticket reservation and management
- Order processing
- Payment processing
- Real-time notifications
- Chat support

## Development

### Building Individual Services

**Spring Boot Services:**
```bash
cd user-service
mvn clean package
cd ..
```

**Node.js Services:**
```bash
cd payment-service
npm install
npm run build
cd ..
```

**React Applications:**
```bash
cd user-portal
npm install
npm run build
cd ..
```

### Running Locally

1. **Start infrastructure services:**
```bash
docker-compose up -d postgres redis kafka mongodb zookeeper
```

2. **Start microservices:**
```bash
# In separate terminals
cd user-service && mvn spring-boot:run
cd event-service && mvn spring-boot:run
cd ticket-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && npm run dev
cd notification-service && npm run dev
cd chat-service && npm run dev
cd api-gateway && npm run dev
```

3. **Start frontend applications:**
```bash
# In separate terminals
cd user-portal && npm start
cd admin-portal && npm start
```

## Testing

### Unit Tests
Run unit tests for individual services:
```bash
# Spring Boot services
cd user-service && mvn test

# Node.js services
cd payment-service && npm test

# React applications
cd user-portal && npm test
```

### Integration Tests
Use the load testing script to test system integration:
```bash
./load-test.sh
```

## Monitoring and Observability

### Health Checks
All services provide health check endpoints for monitoring systems like Kubernetes, Docker Swarm, or custom monitoring solutions.

### Metrics
Services expose metrics through:
- Spring Boot Actuator endpoints
- Custom metrics endpoints
- Application logs

### Logging
Services log important events for debugging and monitoring:
- Service startup and shutdown
- Request processing
- Error conditions
- Business events

## Security Considerations

### Authentication
- JWT-based authentication for user services
- Session management with Redis
- Role-based access control (USER, ADMIN)

### Data Protection
- Password hashing with BCrypt
- Input validation and sanitization
- CORS configuration

## Scaling Considerations

### Horizontal Scaling
- Stateless service design
- External session storage (Redis)
- Database connection pooling
- Load balancing ready

### Vertical Scaling
- Memory-efficient algorithms
- CPU-intensive task identification
- Resource monitoring endpoints

## Troubleshooting

### Common Issues

1. **Services not starting**: Check Docker logs with `docker-compose logs <service-name>`
2. **Database connection issues**: Ensure PostgreSQL is running and accessible
3. **Kafka connection issues**: Verify Kafka and Zookeeper are running
4. **Port conflicts**: Ensure required ports are available

### Debug Endpoints
- Service health: `/health` on each service
- Service metrics: `/metrics` on Spring Boot services
- System status: `/api/services/health` on API Gateway

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.