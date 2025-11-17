const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');
const rateLimit = require('express-rate-limit');
const circuitBreaker = require('express-circuit-breaker');

const app = express();

// Respect X-Forwarded-* headers when running behind a proxy/load balancer
// so rate limiting and logging use the real client address.
app.set('trust proxy', 1);
app.use(cors());
app.use(express.json());

// Request logging middleware
app.use((req, res, next) => {
    const requestId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
    req.requestId = requestId;
    console.log(`[${new Date().toISOString()}] [REQUEST] [${requestId}] ${req.method} ${req.url}`);
    
    // Log response status
    res.on('finish', () => {
        console.log(`[${new Date().toISOString()}] [RESPONSE] [${requestId}] ${req.method} ${req.url} - ${res.statusCode}`);
    });
    
    next();
});

// Service URLs (defaults target docker-compose service names/ports; override for local dev)
const services = {
    user: process.env.USER_SERVICE_URL || 'http://user-service:8080',
    event: process.env.EVENT_SERVICE_URL || 'http://event-service:8080',
    ticket: process.env.TICKET_SERVICE_URL || 'http://ticket-service:8080',
    order: process.env.ORDER_SERVICE_URL || 'http://order-service:8080',
    payment: process.env.PAYMENT_SERVICE_URL || 'http://payment-service:3000',
    notification: process.env.NOTIFICATION_SERVICE_URL || 'http://notification-service:3000',
    chat: process.env.CHAT_SERVICE_URL || 'http://chat-service:3000'
};

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // limit each IP to 100 requests per windowMs
    message: 'Too many requests from this IP, please try again later.',
    standardHeaders: true,
    legacyHeaders: false
});

app.use(limiter);

// Circuit breaker configuration
const circuitBreakerOptions = {
    threshold: 5, // 5 failures
    timeout: 60000, // 1 minute
    reset: 30000, // 30 seconds
    catchError: (err) => {
        console.error('Circuit breaker error:', err);
        return true; // Consider all errors as failures
    }
};

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'API Gateway is running',
        timestamp: new Date().toISOString(),
        services: Object.keys(services)
    });
});

// User Service routes
console.log('Setting up user service proxy for:', services.user);

// Add a simple test route first
app.get('/api/users/test', (req, res) => {
    console.log('Test route hit!');
    res.json({ message: 'User service test route working' });
});

// Add debugging middleware before the proxy
app.use('/api/users', (req, res, next) => {
    console.log(`[DEBUG] [USER_SERVICE] [${req.requestId}] Request received: ${req.method} ${req.url}`);
    console.log(`[DEBUG] [USER_SERVICE] [${req.requestId}] Headers:`, req.headers);
    next();
});

app.use('/api/users', createServiceProxy(services.user, 'users'));

// Service health checks
app.get('/services/health', async (req, res) => {
    const healthStatus = {};
    
    for (const [serviceName, serviceUrl] of Object.entries(services)) {
        try {
            const response = await fetch(`${serviceUrl}/health`);
            healthStatus[serviceName] = response.ok ? 'healthy' : 'unhealthy';
        } catch (error) {
            healthStatus[serviceName] = 'down';
        }
    }
    
    res.json(healthStatus);
});

// Proxy middleware with circuit breaker
function createServiceProxy(serviceUrl, serviceName) {
    console.log(`Creating proxy for ${serviceName} at ${serviceUrl}`);
    console.log(`Proxy will pass through paths unchanged for ${serviceName}`);

    const proxyMiddleware = createProxyMiddleware({
        target: serviceUrl,
        changeOrigin: true,
        proxyTimeout: 15000,
        timeout: 15000,
        onError: (err, req, res) => {
            console.error(`[ERROR] [${serviceName.toUpperCase()}] [${req.requestId}] Proxy error:`, {
                message: err.message,
                code: err.code,
                url: req.url,
                method: req.method,
                service: serviceName,
                serviceUrl: serviceUrl
            });
            
            res.status(503).json({
                error: 'Service unavailable',
                service: serviceName,
                serviceName: serviceName,
                message: `The ${serviceName} service is currently unavailable. Please try again later.`,
                requestId: req.requestId,
                timestamp: new Date().toISOString()
            });
        },
        onProxyReq: (proxyReq, req, res) => {
            // Add request ID for tracing
            const requestId = req.headers['x-request-id'] || Date.now().toString();
            proxyReq.setHeader('x-request-id', requestId);
            proxyReq.setHeader('x-forwarded-for', req.ip);
            
            // Forward user context if available
            if (req.headers['x-user-id']) {
                proxyReq.setHeader('x-user-id', req.headers['x-user-id']);
            }
            if (req.headers['x-user-role']) {
                proxyReq.setHeader('x-user-role', req.headers['x-user-role']);
            }

            // If body was parsed by express.json(), re-send it to target
            if (req.body && Object.keys(req.body).length && req.method !== 'GET' && req.method !== 'HEAD') {
                const bodyData = JSON.stringify(req.body);
                proxyReq.setHeader('Content-Type', 'application/json');
                proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
                proxyReq.write(bodyData);
            }
            
            console.log(`[PROXY] Proxying request to ${serviceName}: ${req.method} ${req.url}`);
        }
    });
    
    return (req, res, next) => {
        console.log(`[PROXY_WRAPPER] [${serviceName.toUpperCase()}] [${req.requestId}] Calling proxy: ${req.method} ${req.url}`);
        proxyMiddleware(req, res, (err) => {
            if (err) {
                console.error(`[PROXY_WRAPPER] [${serviceName.toUpperCase()}] [${req.requestId}] Proxy error:`, err);
                next(err);
            }
            // Do not call next() on success; proxyMiddleware already handled the response
        });
    };
}

// Event Service routes
app.use('/api/events', createServiceProxy(services.event, 'event'));

// Ticket Service routes
app.use('/api/tickets', createServiceProxy(services.ticket, 'ticket'));

// Order Service routes
app.use('/api/orders', createServiceProxy(services.order, 'order'));

// Payment Service routes
app.use('/api/payments', createServiceProxy(services.payment, 'payment'));

// Notification Service routes
app.use('/api/notifications', createServiceProxy(services.notification, 'notification'));

// Chat Service routes
app.use('/api/chat', createServiceProxy(services.chat, 'chat'));

// Global error handler - must be last
app.use((err, req, res, next) => {
    console.error(`[ERROR] [GLOBAL] [${req.requestId}] Unhandled error:`, {
        message: err.message,
        stack: err.stack,
        url: req.url,
        method: req.method,
        headers: req.headers,
        timestamp: new Date().toISOString()
    });
    
    res.status(500).json({
        error: 'Internal server error',
        message: 'An unexpected error occurred',
        requestId: req.requestId,
        service: 'api-gateway',
        timestamp: new Date().toISOString()
    });
});

// 404 handler
app.use((req, res) => {
    console.error(`[ERROR] [NOT_FOUND] [${req.requestId}] Route not found:`, {
        url: req.url,
        method: req.method,
        service: 'api-gateway'
    });
    
    res.status(404).json({
        error: 'Not found',
        message: 'The requested endpoint does not exist',
        path: req.url,
        method: req.method,
        service: 'api-gateway',
        requestId: req.requestId,
        timestamp: new Date().toISOString()
    });
});

// DevOps challenge: Autoscaling trigger endpoint
app.get('/api/challenge/autoscale', (req, res) => {
    // Simulate high CPU usage for autoscaling
    const start = Date.now();
    while (Date.now() - start < 1000) {
        // Busy loop to simulate CPU intensive task
        Math.sqrt(Math.random() * 1000000);
    }
    
    res.json({
        message: 'Autoscaling challenge triggered',
        cpu_intensive_task: 'completed',
        timestamp: new Date().toISOString()
    });
});

// DevOps challenge: Memory pressure endpoint
app.get('/api/challenge/memory', (req, res) => {
    // Allocate memory to simulate memory pressure
    const memoryHog = [];
    for (let i = 0; i < 1000000; i++) {
        memoryHog.push(new Array(1000).fill('memory pressure test'));
    }
    
    res.json({
        message: 'Memory pressure challenge triggered',
        memory_allocated: '1GB',
        timestamp: new Date().toISOString()
    });
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({
        error: 'Internal server error',
        message: 'An unexpected error occurred',
        requestId: req.headers['x-request-id']
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        error: 'Not found',
        message: 'The requested endpoint does not exist',
        path: req.originalUrl
    });
});

// Start server
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`API Gateway running on port ${PORT}`);
    console.log('Available services:', Object.keys(services));
});
