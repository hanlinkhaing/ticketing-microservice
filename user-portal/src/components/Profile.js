import React from 'react';

function Profile({ user }) {
  return (
    <div className="profile-container">
      <div className="card">
        <h2>User Profile</h2>
        {user ? (
          <div className="profile-info">
            <p><strong>Username:</strong> {user.username}</p>
            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Full Name:</strong> {user.fullName}</p>
            <p><strong>Phone:</strong> {user.phoneNumber || 'Not provided'}</p>
            <p><strong>Role:</strong> {user.role}</p>
          </div>
        ) : (
          <p>Loading profile...</p>
        )}
      </div>
    </div>
  );
}

export default Profile;