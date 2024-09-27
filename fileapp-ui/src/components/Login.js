import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Login = () => {
  const navigate = useNavigate();

  const handleLogin = () => {
    // Redirect to the OAuth2 login endpoint on the backend
    window.location.href = "http://api.fileapp.click/oauth2/authorization/google";  // Replace with your OAuth2 provider if needed
  };

  useEffect(() => {
    // Optionally, check if the user is already logged in by making a request to your backend (e.g., a user info endpoint)
    axios.get('http://api.fileapp.click/api/userinfo', { withCredentials: true })
      .then(response => {
        // If the user is already logged in, redirect them to the /metadata page
        if (response.status === 200) {
          navigate('/metadata');  // Redirect to /metadata if already authenticated
        }
      })
      .catch(error => {
        console.log('User is not authenticated:', error);
      });
  }, [navigate]);

  return (
    <div className="Login">
      <h2>Login with Google</h2>
      <button onClick={handleLogin}>Login</button>
    </div>
  );
};

export default Login;
