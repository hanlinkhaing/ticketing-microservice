const express = require('express');
const cors = require('cors');
const { createServer } = require('http');
const { Server } = require('socket.io');
const { Kafka } = require('kafkajs');
const redis = require('redis');

const app = express();
const server = createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

app.use(cors());
app.use(express.json());

// Redis client
const redisClient = redis.createClient({
    host: process.env.REDIS_HOST || 'localhost',
    port: 6379
});

// Store connected users
const connectedUsers = new Map();

// Kafka setup
const kafka = new Kafka({
    clientId: 'notification-service',
    brokers: [process.env.KAFKA_BROKERS || 'localhost:9092']
});

const consumer = kafka.consumer({ groupId: 'notification-service' });

// Socket.io connection handling
io.on('connection', (socket) => {
    console.log('User connected:', socket.id);
    
    socket.on('register', async (userId) => {
        connectedUsers.set(userId, socket.id);
        await redisClient.setEx(`socket:${userId}`, 3600, socket.id);
        console.log(`User ${userId} registered with socket ${socket.id}`);
    });
    
    socket.on('disconnect', async () => {
        // Find and remove user from connected users
        for (let [userId, socketId] of connectedUsers.entries()) {
            if (socketId === socket.id) {
                connectedUsers.delete(userId);
                await redisClient.del(`socket:${userId}`);
                console.log(`User ${userId} disconnected`);
                break;
            }
        }
    });
});

// Connect to Kafka and process events
async function connectKafka() {
    await consumer.connect();
    
    // Subscribe to all relevant topics
    await consumer.subscribe({ topics: ['order-events', 'payment-events', 'ticket-events', 'user-events'] });
    
    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            const event = JSON.parse(message.value.toString());
            
            // Process different event types
            if (topic === 'order-events') {
                await handleOrderEvent(event);
            } else if (topic === 'payment-events') {
                await handlePaymentEvent(event);
            } else if (topic === 'ticket-events') {
                await handleTicketEvent(event);
            } else if (topic === 'user-events') {
                await handleUserEvent(event);
            }
        }
    });
}

async function handleOrderEvent(event) {
    const notification = {
        type: 'order',
        title: 'Order Update',
        message: getOrderMessage(event.type, event),
        timestamp: new Date().toISOString(),
        data: event
    };
    
    // Send notification to user
    await sendNotificationToUser(event.userId, notification);
}

async function handlePaymentEvent(event) {
    const notification = {
        type: 'payment',
        title: 'Payment Update',
        message: getPaymentMessage(event.type, event),
        timestamp: new Date().toISOString(),
        data: event
    };
    
    // Send notification to user
    await sendNotificationToUser(event.userId, notification);
}

async function handleTicketEvent(event) {
    const notification = {
        type: 'ticket',
        title: 'Ticket Update',
        message: getTicketMessage(event.type, event),
        timestamp: new Date().toISOString(),
        data: event
    };
    
    // Send notification to user
    await sendNotificationToUser(event.userId, notification);
}

async function handleUserEvent(event) {
    const notification = {
        type: 'user',
        title: 'Account Update',
        message: getUserMessage(event.type, event),
        timestamp: new Date().toISOString(),
        data: event
    };
    
    // Send notification to user
    await sendNotificationToUser(event.userId, notification);
}

function getOrderMessage(type, event) {
    switch (type) {
        case 'ORDER_CREATED':
            return `Order #${event.orderId} has been created successfully.`;
        case 'ORDER_CONFIRMED':
            return `Order #${event.orderId} has been confirmed.`;
        case 'ORDER_CANCELLED':
            return `Order #${event.orderId} has been cancelled.`;
        default:
            return 'Order status updated.';
    }
}

function getPaymentMessage(type, event) {
    switch (type) {
        case 'PAYMENT_SUCCESS':
            return `Payment of $${event.amount} processed successfully.`;
        case 'PAYMENT_FAILED':
            return `Payment failed: ${event.reason}`;
        default:
            return 'Payment status updated.';
    }
}

function getTicketMessage(type, event) {
    switch (type) {
        case 'TICKETS_RESERVED':
            return `Tickets reserved for order #${event.orderId}.`;
        case 'TICKETS_SOLD':
            return `Tickets purchased successfully for order #${event.orderId}.`;
        case 'TICKETS_CANCELLED':
            return `Tickets cancelled for order #${event.orderId}.`;
        default:
            return 'Ticket status updated.';
    }
}

function getUserMessage(type, event) {
    switch (type) {
        case 'USER_CREATED':
            return 'Welcome! Your account has been created successfully.';
        case 'USER_UPDATED':
            return 'Your profile has been updated.';
        default:
            return 'Account updated.';
    }
}

async function sendNotificationToUser(userId, notification) {
    try {
        // Check if user is connected via WebSocket
        const socketId = connectedUsers.get(userId);
        
        if (socketId) {
            io.to(socketId).emit('notification', notification);
            console.log(`Notification sent to user ${userId}:`, notification.message);
        } else {
            // Store notification for later delivery
            const notifications = await redisClient.get(`notifications:${userId}`);
            const userNotifications = notifications ? JSON.parse(notifications) : [];
            userNotifications.push(notification);
            await redisClient.setEx(`notifications:${userId}`, 86400, JSON.stringify(userNotifications));
            console.log(`Notification stored for user ${userId}:`, notification.message);
        }
    } catch (error) {
        console.error('Error sending notification:', error);
    }
}

// Routes
app.get('/health', (req, res) => {
    res.json({ status: 'Notification Service is running', timestamp: new Date().toISOString() });
});

app.get('/notifications/:userId', async (req, res) => {
    try {
        const notifications = await redisClient.get(`notifications:${req.params.userId}`);
        res.json(notifications ? JSON.parse(notifications) : []);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, async () => {
    console.log(`Notification Service running on port ${PORT}`);
    await connectKafka();
});