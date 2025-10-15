# Backend IDS - Spring Boot Application# Backend IDS - Spring Boot Application



This is a Spring Boot backend application for the IDS project, featuring PostgreSQL database integration and Kafka messaging, designed to run in containerized environments.This is a Spring Boot backend application for the IDS project, featuring PostgreSQL database integration and Kafka messaging, designed to run in containerized environments.



## Prerequisites## Prerequisites



- Java 21+- Java 21+

- Maven 3.6+- Maven 3.6+

- Docker and Docker Compose- Docker and Docker Compose



## Local Development (with local PostgreSQL and Kafka)## Local Development (with local PostgreSQL and Kafka)



```bash
# Start all services locally
docker-compose up -d

# Access the application
# Backend API: http://localhost:8080
# Health check: http://localhost:8080/actuator/health

# Stop services
docker-compose down
```

## Frontend Integration 🔗

This backend is configured to work with a frontend application running on:
- **Frontend URL:** `http://localhost:5173` (Vite/React)
- **Backend API:** `http://localhost:8080`

### Available API Endpoints:

#### Authentication 🔐
- **POST** `/api/auth/signup` - Register new user
- **POST** `/api/auth/signin` - Login user
- **POST** `/api/auth/refresh` - Refresh JWT token
- **GET** `/api/auth/check-username?username=xxx` - Check username availability
- **GET** `/api/auth/check-email?email=xxx` - Check email availability

#### Public Endpoints 🌍
- **GET** `/api/test/public` - Public content (no auth required)

#### Protected Endpoints 🔒
- **GET** `/api/test/user` - User content (requires JWT)
- **GET** `/api/test/admin` - Admin content (requires ADMIN role)

### Example Frontend Axios Configuration:
```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```



## Production with AWS (requires .env configuration)## Production with AWS (requires .env configuration)



1. **Configure environment variables**1. **Configure environment variables**

   ```bash   ```bash

   cp .env.example .env   cp .env.example .env

   # Edit .env with your AWS RDS and MSK endpoints   # Edit .env with your AWS RDS and MSK endpoints

   ```   ```



2. **Run with AWS services**2. **Run with AWS services**

   ```bash   ```bash

   docker-compose -f docker-compose.production.yml up -d   docker-compose -f docker-compose.production.yml up -d

      

   # Check logs   # Check logs

   docker-compose -f docker-compose.production.yml logs -f   docker-compose -f docker-compose.production.yml logs -f

      

   # Stop   # Stop

   docker-compose -f docker-compose.production.yml down   docker-compose -f docker-compose.production.yml down

   ```   ```