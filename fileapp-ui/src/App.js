import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Login from './components/Login';
import S3Metadata from './components/S3Metadata';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/metadata" element={<S3Metadata />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
