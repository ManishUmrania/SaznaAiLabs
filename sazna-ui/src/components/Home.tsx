import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css';

const Home: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <div className="card home-card">
        <h1 className="home-title">
          Welcome to Sazna Platform
        </h1>
        <p className="home-subtitle">
          Your all-in-one solution for managing projects, collaborating with teams,
          and tracking progress with ease.
        </p>
        <div className="button-container">
          <button
            onClick={() => navigate('/signup')}
            className="btn btn-primary"
          >
            Get Started
          </button>
          <button
            onClick={() => navigate('/login')}
            className="btn btn-outline"
          >
            Login
          </button>
        </div>

        <div className="grid features-grid">
          <div className="feature-card">
            <div className="feature-icon">
              📋
            </div>
            <h3 className="feature-title">Project Management</h3>
            <p className="feature-description">
              Organize your projects with intuitive tools designed to streamline your workflow.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              👥
            </div>
            <h3 className="feature-title">Team Collaboration</h3>
            <p className="feature-description">
              Work seamlessly with your team members in real-time with our collaborative features.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">
              📊
            </div>
            <h3 className="feature-title">Analytics Dashboard</h3>
            <p className="feature-description">
              Gain insights with our comprehensive analytics and reporting tools.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;