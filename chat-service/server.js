const express = require('express');
const cors = require('cors');
const { createServer } = require('http');
const { Server } = require('socket.io');
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

// Store active support agents
const supportAgents = new Set();

// Socket.io connection handling
io.on('connection', (socket) => {
    console.log('User connected:', socket.id);
    
    socket.on('register_user', (userId) => {
        socket.userId = userId;
        socket.userType = 'user';
        socket.join(`user:${userId}`);
        console.log(`User ${userId} registered`);
    });
    
    socket.on('register_agent', (agentId) => {
        socket.userId = agentId;
        socket.userType = 'agent';
        supportAgents.add(socket.id);
        socket.join('agents');
        console.log(`Agent ${agentId} registered`);
    });
    
    socket.on('send_message', async (data) => {
        const message = {
            id: Date.now().toString(),
            senderId: socket.userId,
            senderType: socket.userType,
            message: data.message,
            timestamp: new Date().toISOString()
        };
        
        // Store message in Redis
        const chatKey = socket.userType === 'user' ? 
            `chat:${socket.userId}:agent` : 
            `chat:${data.recipientId}:agent`;
        
        const existingChat = await redisClient.get(chatKey);
        const chatHistory = existingChat ? JSON.parse(existingChat) : [];
        chatHistory.push(message);
        
        await redisClient.setEx(chatKey, 86400, JSON.stringify(chatHistory));
        
        // Send message to recipient
        if (socket.userType === 'user') {
            // User sending to agent
            io.to('agents').emit('new_message', message);
        } else {
            // Agent sending to user
            io.to(`user:${data.recipientId}`).emit('new_message', message);
        }
    });
    
    socket.on('join_chat', async (userId) => {
        if (socket.userType === 'agent') {
            socket.join(`chat:${userId}:agent`);
            
            // Send chat history
            const chatKey = `chat:${userId}:agent`;
            const existingChat = await redisClient.get(chatKey);
            const chatHistory = existingChat ? JSON.parse(existingChat) : [];
            
            socket.emit('chat_history', chatHistory);
        }
    });
    
    socket.on('typing', (data) => {
        if (socket.userType === 'user') {
            io.to('agents').emit('user_typing', { userId: socket.userId });
        } else {
            io.to(`user:${data.recipientId}`).emit('agent_typing');
        }
    });
    
    socket.on('stop_typing', (data) => {
        if (socket.userType === 'user') {
            io.to('agents').emit('user_stop_typing', { userId: socket.userId });
        } else {
            io.to(`user:${data.recipientId}`).emit('agent_stop_typing');
        }
    });
    
    socket.on('disconnect', () => {
        if (socket.userType === 'agent') {
            supportAgents.delete(socket.id);
        }
        console.log('User disconnected:', socket.id);
    });
});

// Routes
app.get('/health', (req, res) => {
    res.json({ 
        status: 'Chat Service is running', 
        timestamp: new Date().toISOString(),
        activeAgents: supportAgents.size
    });
});

app.get('/chat/history/:userId', async (req, res) => {
    try {
        const chatKey = `chat:${req.params.userId}:agent`;
        const existingChat = await redisClient.get(chatKey);
        const chatHistory = existingChat ? JSON.parse(existingChat) : [];
        
        res.json(chatHistory);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.get('/agents/online', (req, res) => {
    res.json({ 
        online: supportAgents.size > 0,
        count: supportAgents.size
    });
});

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Chat Service running on port ${PORT}`);
});