import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Dashboard.css';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  const { user, isAuthenticated } = useAuth();

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    navigate('/login');
    return null;
  }

  // Mock data
  const stats = [
    { title: 'Total Projects', value: '24', change: '+12%', icon: '📋' },
    { title: 'Completed Tasks', value: '18', change: '+5%', icon: '✅' },
    { title: 'Team Members', value: '12', change: '+2', icon: '👥' },
    { title: 'Pending Tasks', value: '6', change: '-3', icon: '⏳' }
  ];

  const recentActivity = [
    { id: 1, user: 'Alex Johnson', action: 'updated project timeline', time: '2 hours ago', project: 'Website Redesign' },
    { id: 2, user: 'Sarah Miller', action: 'uploaded new designs', time: '4 hours ago', project: 'Mobile App' },
    { id: 3, user: 'Mike Chen', action: 'completed milestone', time: '1 day ago', project: 'API Integration' },
    { id: 4, user: 'Emma Davis', action: 'commented on task', time: '1 day ago', project: 'Documentation' }
  ];

  const projects = [
    { id: 1, name: 'Website Redesign', progress: 75, status: 'In Progress', dueDate: 'May 15, 2026' },
    { id: 2, name: 'Mobile App', progress: 45, status: 'In Progress', dueDate: 'June 30, 2026' },
    { id: 3, name: 'API Integration', progress: 100, status: 'Completed', dueDate: 'Apr 10, 2026' },
    { id: 4, name: 'Documentation', progress: 30, status: 'Planning', dueDate: 'July 15, 2026' }
  ];

  return (
    <div className="dashboard-container">
      {/* Welcome Section */}
      <div className="welcome-section">
        <h1 className="welcome-title">
          Good morning, {user?.firstName || 'User'}!
        </h1>
        <p className="welcome-subtitle">
          Here's what's happening with your projects today.
        </p>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        {stats.map((stat, index) => (
          <div key={index} className="card stat-card">
            <div className="stat-icon">
              {stat.icon}
            </div>
            <div className="stat-value">
              {stat.value}
            </div>
            <div className="stat-title">
              {stat.title}
            </div>
            <div className={`stat-change ${stat.change.startsWith('+') ? 'positive' : 'negative'}`}>
              {stat.change} from last week
            </div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="card">
        <div className="tabs-container">
          {['overview', 'projects', 'activity'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`tab-button ${activeTab === tab ? 'active' : ''}`}
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
              {activeTab === tab && (
                <div className="tab-indicator" />
              )}
            </button>
          ))}
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <div>
            <div className="content-grid">
              {/* Projects List */}
              <div>
                <h3 className="projects-list">
                  Recent Projects
                </h3>
                <div className="d-flex flex-column gap-3">
                  {projects.map((project) => (
                    <div key={project.id} className="card project-card">
                      <div className="project-header">
                        <div className="project-info">
                          <h4>
                            {project.name}
                          </h4>
                          <p>
                            Due: {project.dueDate}
                          </p>
                        </div>
                        <span className={`project-status ${project.status.toLowerCase().replace(' ', '-')}`}>
                          {project.status}
                        </span>
                      </div>
                      <div className="progress-container">
                        <div className="progress-label">
                          <span>Progress</span>
                          <span>{project.progress}%</span>
                        </div>
                        <div className="progress-bar">
                          <div
                            className={`progress-fill ${project.progress === 100 ? 'completed' : 'in-progress'}`}
                            style={{ width: `${project.progress}%` }}
                          />
                        </div>
                      </div>
                      <button className="btn btn-outline" style={{ width: 'fit-content' }}>
                        View Details
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Recent Activity */}
              <div>
                <h3 className="activity-list">
                  Recent Activity
                </h3>
                <div className="d-flex flex-column gap-3">
                  {recentActivity.map((activity) => (
                    <div key={activity.id} className="activity-item">
                      <div className="activity-avatar">
                        {activity.user.charAt(0)}
                      </div>
                      <div className="activity-content">
                        <div>
                          <strong>{activity.user}</strong> {activity.action}
                        </div>
                        <div>
                          {activity.time} • {activity.project}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Projects Tab */}
        {activeTab === 'projects' && (
          <div>
            <h3 className="projects-list">
              All Projects
            </h3>
            <div className="all-projects-grid">
              {projects.map((project) => (
                <div key={project.id} className="card">
                  <div className="project-detail-card">
                    <div className="project-info">
                      <h4>
                        {project.name}
                      </h4>
                      <p>
                        Due: {project.dueDate}
                      </p>
                    </div>
                    <span className={`project-status ${project.status.toLowerCase().replace(' ', '-')}`}>
                      {project.status}
                    </span>
                  </div>
                  <div className="progress-container">
                    <div className="progress-label">
                      <span>Progress</span>
                      <span>{project.progress}%</span>
                    </div>
                    <div className="progress-bar">
                      <div
                        className={`progress-fill ${project.progress === 100 ? 'completed' : 'in-progress'}`}
                        style={{ width: `${project.progress}%` }}
                      />
                    </div>
                  </div>
                  <div className="button-group">
                    <button className="btn btn-outline">
                      View
                    </button>
                    <button className="btn btn-primary">
                      Edit
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Activity Tab */}
        {activeTab === 'activity' && (
          <div>
            <h3 className="activity-list">
              Activity Feed
            </h3>
            <div className="d-flex flex-column gap-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="card activity-feed-item">
                  <div className="activity-feed-content">
                    <div className="activity-feed-avatar">
                      {activity.user.charAt(0)}
                    </div>
                    <div className="flex-grow-1">
                      <div className="activity-feed-header">
                        <div className="activity-feed-user">
                          <h4>
                            {activity.user}
                          </h4>
                          <p className="activity-feed-action">
                            {activity.action}
                          </p>
                        </div>
                        <span className="activity-feed-time">
                          {activity.time}
                        </span>
                      </div>
                      <div className="activity-feed-project">
                        <strong>{activity.project}</strong>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="card quick-actions-card">
        <h3 className="quick-actions-title">
          Quick Actions
        </h3>
        <div className="quick-actions-buttons">
          <button className="btn btn-primary">
            Create New Project
          </button>
          <button className="btn btn-outline">
            Invite Team Member
          </button>
          <button className="btn btn-outline">
            Generate Report
          </button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;