import React, { useState, useEffect } from 'react';
import io from 'socket.io-client';

function Chat() {
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [activeUsers, setActiveUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);

  useEffect(() => {
    // Connect to chat service as admin
    const newSocket = io('http://localhost:3003');
    setSocket(newSocket);

    // Register as agent
    newSocket.emit('register_agent', 'admin');

    // Listen for messages
    newSocket.on('new_message', (message) => {
      setMessages(prev => [...prev, message]);
    });

    // Listen for user typing
    newSocket.on('user_typing', (data) => {
      setIsTyping(true);
      console.log(`User ${data.userId} is typing`);
    });

    newSocket.on('user_stop_typing', () => {
      setIsTyping(false);
    });

    return () => {
      newSocket.close();
    };
  }, []);

  const sendMessage = (e) => {
    e.preventDefault();
    if (inputMessage.trim() && socket && selectedUser) {
      socket.emit('send_message', { 
        message: inputMessage,
        recipientId: selectedUser
      });
      setInputMessage('');
    }
  };

  const joinUserChat = (userId) => {
    setSelectedUser(userId);
    if (socket) {
      socket.emit('join_chat', userId);
    }
  };

  const handleTyping = () => {
    if (socket && selectedUser) {
      socket.emit('typing', { recipientId: selectedUser });
      setTimeout(() => {
        socket.emit('stop_typing', { recipientId: selectedUser });
      }, 1000);
    }
  };

  return (
    <div className="chat-container">
      <div className="card">
        <div className="card-header">
          <h3>Support Chat - Admin Panel</h3>
        </div>
        <div className="chat-layout">
          <div className="users-list">
            <h4>Active Users</h4>
            <p>Click on a user to start chatting</p>
            {/* In a real app, this would show actual active users */}
            <div className="user-item" onClick={() => joinUserChat('user123')}>
              User #123
            </div>
          </div>
          
          <div className="chat-area">
            <div className="chat-messages">
              {selectedUser ? (
                messages.length === 0 ? (
                  <p className="no-messages">No messages yet. Start the conversation!</p>
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
                )
              ) : (
                <p className="no-messages">Select a user to start chatting</p>
              )}
              {isTyping && (
                <div className="typing-indicator">
                  User is typing...
                </div>
              )}
            </div>
            
            {selectedUser && (
              <form onSubmit={sendMessage} className="chat-input">
                <input
                  type="text"
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onKeyPress={handleTyping}
                  placeholder="Type your message..."
                  className="form-control"
                />
                <button type="submit" className="btn btn-primary">
                  Send
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Chat;