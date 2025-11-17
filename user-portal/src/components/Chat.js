import React, { useState, useEffect } from 'react';
import io from 'socket.io-client';

function Chat({ user }) {
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [agentOnline, setAgentOnline] = useState(false);

  useEffect(() => {
    // Connect to chat service
    const newSocket = io('http://localhost:3003');
    setSocket(newSocket);

    // Register user
    newSocket.emit('register_user', user?.id || '1');

    // Listen for messages
    newSocket.on('new_message', (message) => {
      setMessages(prev => [...prev, message]);
    });

    // Listen for chat history
    newSocket.on('chat_history', (history) => {
      setMessages(history);
    });

    // Listen for agent typing
    newSocket.on('agent_typing', () => {
      setIsTyping(true);
    });

    newSocket.on('agent_stop_typing', () => {
      setIsTyping(false);
    });

    // Check if agents are online
    fetch('/api/chat/agents/online')
      .then(res => res.json())
      .then(data => setAgentOnline(data.online));

    return () => {
      newSocket.close();
    };
  }, [user]);

  const sendMessage = (e) => {
    e.preventDefault();
    if (inputMessage.trim() && socket) {
      socket.emit('send_message', { message: inputMessage });
      setInputMessage('');
    }
  };

  const handleTyping = () => {
    if (socket) {
      socket.emit('typing');
      setTimeout(() => {
        socket.emit('stop_typing');
      }, 1000);
    }
  };

  return (
    <div className="chat-container">
      <div className="card">
        <div className="card-header">
          <h3>Support Chat</h3>
          <div className="agent-status">
            {agentOnline ? '🟢 Agent Online' : '🔴 Agent Offline'}
          </div>
        </div>
        <div className="chat-messages">
          {messages.length === 0 ? (
            <p className="no-messages">Start a conversation with our support team.</p>
          ) : (
            messages.map(message => (
              <div key={message.id} className={`message ${message.senderType}`}>
                <div className="message-content">
                  {message.message}
                </div>
                <div className="message-timestamp">
                  {new Date(message.timestamp).toLocaleTimeString()}
                </div>
              </div>
            ))
          )}
          {isTyping && (
            <div className="typing-indicator">
              Agent is typing...
            </div>
          )}
        </div>
        <form onSubmit={sendMessage} className="chat-input">
          <input
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyPress={handleTyping}
            placeholder="Type your message..."
            className="form-control"
            disabled={!agentOnline}
          />
          <button type="submit" className="btn btn-primary" disabled={!agentOnline}>
            Send
          </button>
        </form>
      </div>
    </div>
  );
}

export default Chat;