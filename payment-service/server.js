const express = require('express');
const cors = require('cors');
const { Kafka } = require('kafkajs');
const redis = require('redis');

const app = express();
app.use(cors());
app.use(express.json());

// Redis client
const redisClient = redis.createClient({
    host: process.env.REDIS_HOST || 'localhost',
    port: 6379
});

// Kafka setup
const kafka = new Kafka({
    clientId: 'payment-service',
    brokers: [process.env.KAFKA_BROKERS || 'localhost:9092']
});

const producer = kafka.producer();
const consumer = kafka.consumer({ groupId: 'payment-service' });

// Connect to Kafka
async function connectKafka() {
    await producer.connect();
    await consumer.connect();
    
    // Subscribe to order events
    await consumer.subscribe({ topic: 'order-events', fromBeginning: true });
    
    // Process order events
    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            const event = JSON.parse(message.value.toString());
            
            if (event.type === 'ORDER_CREATED') {
                // Simulate payment processing
                setTimeout(async () => {
                    const paymentSuccess = Math.random() > 0.1; // 90% success rate
                    
                    if (paymentSuccess) {
                        // Payment successful
                        await producer.send({
                            topic: 'payment-events',
                            messages: [{ value: JSON.stringify({
                                type: 'PAYMENT_SUCCESS',
                                orderId: event.orderId,
                                amount: event.amount,
                                timestamp: new Date().toISOString()
                            })}]
                        });
                    } else {
                        // Payment failed
                        await producer.send({
                            topic: 'payment-events',
                            messages: [{ value: JSON.stringify({
                                type: 'PAYMENT_FAILED',
                                orderId: event.orderId,
                                reason: 'Card declined',
                                timestamp: new Date().toISOString()
                            })}]
                        });
                    }
                }, 2000); // Simulate 2 second processing time
            }
        }
    });
}

// Routes
app.get('/health', (req, res) => {
    res.json({ status: 'Payment Service is running', timestamp: new Date().toISOString() });
});

app.post('/process', async (req, res) => {
    const { orderId, amount, paymentMethod } = req.body;
    
    try {
        // Simulate payment processing
        const paymentSuccess = Math.random() > 0.1; // 90% success rate
        
        if (paymentSuccess) {
            // Cache payment result
            await redisClient.setEx(`payment:${orderId}`, 3600, JSON.stringify({
                status: 'SUCCESS',
                amount: amount,
                timestamp: new Date().toISOString()
            }));
            
            res.json({
                success: true,
                message: 'Payment processed successfully',
                transactionId: `TXN-${Date.now()}`
            });
        } else {
            res.status(400).json({
                success: false,
                message: 'Payment failed - Card declined'
            });
        }
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Payment processing error',
            error: error.message
        });
    }
});

app.get('/status/:orderId', async (req, res) => {
    try {
        const paymentData = await redisClient.get(`payment:${req.params.orderId}`);
        
        if (paymentData) {
            const payment = JSON.parse(paymentData);
            res.json(payment);
        } else {
            res.status(404).json({ message: 'Payment not found' });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
    console.log(`Payment Service running on port ${PORT}`);
    await connectKafka();
});