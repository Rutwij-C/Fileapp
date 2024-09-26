import React, { useEffect, useState } from 'react';
import axios from 'axios';

const S3Metadata = () => {
  const [objectName, setObjectName] = useState('');
  const [metadata, setMetadata] = useState(null);
  const [error, setError] = useState('');
  const [userInfo, setUserInfo] = useState(null);

  const handleInputChange = (event) => {
    setObjectName(event.target.value);
  };

  const fetchMetadata = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/object_metadata/${objectName}`, {
        withCredentials: true, // To ensure cookies/tokens are sent with the request
      });
      setMetadata(response.data);
      setError(''); // Clear any previous errors
    } catch (err) {
      setError('Error fetching metadata. Please check the object name and try again.');
      setMetadata(null);
    }
  };

  useEffect(() => {
    axios.get('http://localhost:8080/api/userinfo', { withCredentials: true })
      .then(response => {
        setUserInfo(response.data);  // Set the user info state
      })
      .catch(error => {
        console.error('Error fetching user info:', error);
      });
  }, []);

  return (
    <div className="S3Metadata">
      {userInfo ? (
        <div>
          <h2>Welcome, {userInfo.name}!</h2>
          <p>Your email: {userInfo.email}</p>
        </div>
      ) : (
        <p>Loading user information...</p>
      )}

      <h3>Enter the S3 object name to fetch metadata:</h3>
      <input
        type="text"
        value={objectName}
        onChange={handleInputChange}
        placeholder="Enter S3 object name"
      />
      <button onClick={fetchMetadata}>Fetch Metadata</button>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {metadata && (
        <div>
          <h3>Metadata for {objectName}</h3>
          <p><strong>Version ID:</strong> {metadata.versionId}</p>
          <p><strong>Storage Class:</strong> {metadata.storageClass}</p>
          <p><strong>Parts Count:</strong> {metadata.partsCount}</p>
          <p><strong>Last Modified:</strong> {metadata.lastModified}</p>
        </div>
      )}
    </div>
  );
};

export default S3Metadata;
