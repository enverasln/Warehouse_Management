# Docker Deployment Workflow

## Genel Bakış
Warehouse Management sisteminin Docker Desktop üzerinde deployment workflow'u. Multi-container orchestration ile backend, database ve frontend servislerin yönetimi.

## Deployment Architecture

```
docker-compose.yml
├── backend-api (ASP.NET Core 8)
├── postgres-db (PostgreSQL 15)
├── mobile-app-dev (Android Emulator için)
└── web-admin (React + Nginx)
```

## Pre-Deployment Checklist

- [ ] All tests passed (unit, integration, E2E)
- [ ] Code review approved
- [ ] Security scan completed
- [ ] Database migrations prepared
- [ ] Configuration files updated
- [ ] Docker images built successfully
- [ ] Health check endpoints working

## Deployment Agent Responsibilities

### 1. Build Phase
```bash
# Backend API
cd backend
dotnet publish -c Release -o ./publish
docker build -t warehouse-api:latest .

# Web Admin
cd web-admin
npm run build
docker build -t warehouse-web:latest .

# Mobile (for emulator testing)
cd mobile
./gradlew assembleDebug
```

### 2. Database Migration
```bash
# Run migrations
docker-compose run --rm backend-api dotnet ef database update

# Verify migration
docker-compose exec postgres-db psql -U warehouse -c "\dt"
```

### 3. Container Deployment
```bash
# Stop existing containers
docker-compose down

# Pull latest images (if from registry)
docker-compose pull

# Start services
docker-compose up -d

# Wait for healthy status
docker-compose ps
```

### 4. Health Checks
```bash
# Backend API health
curl http://localhost:5000/health

# Database health
docker-compose exec postgres-db pg_isready

# Web Admin health
curl http://localhost:3000/health
```

## Deployment Strategies

### Strategy 1: Blue-Green Deployment
```yaml
# docker-compose.blue.yml
services:
  backend-api-blue:
    image: warehouse-api:v1.0
    ports: ["5000:80"]

# docker-compose.green.yml
services:
  backend-api-green:
    image: warehouse-api:v1.1
    ports: ["5001:80"]

# Switch traffic from blue to green
# nginx configuration update
```

**Steps:**
1. Deploy green environment
2. Run smoke tests on green
3. Switch load balancer to green
4. Monitor for issues
5. Keep blue for rollback
6. After stability, remove blue

### Strategy 2: Rolling Update
```bash
# Update backend API (one container at a time)
docker-compose up -d --scale backend-api=3 --no-recreate
docker-compose up -d --force-recreate --no-deps backend-api

# Verify each container before proceeding
```

### Strategy 3: Canary Deployment
```yaml
# 10% of traffic to new version
services:
  backend-api-stable:
    image: warehouse-api:v1.0
    deploy:
      replicas: 9
  
  backend-api-canary:
    image: warehouse-api:v1.1
    deploy:
      replicas: 1
```

## Docker Compose Configuration

### docker-compose.yml
```yaml
version: '3.8'

services:
  postgres-db:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: warehouse
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: warehouse_db
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U warehouse"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend-api:
    build:
      context: ./backend
      dockerfile: Dockerfile
    image: warehouse-api:latest
    environment:
      - ASPNETCORE_ENVIRONMENT=Production
      - ConnectionStrings__DefaultConnection=Host=postgres-db;Database=warehouse_db;Username=warehouse;Password=${DB_PASSWORD}
    ports:
      - "5000:80"
    depends_on:
      postgres-db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  web-admin:
    build:
      context: ./web-admin
      dockerfile: Dockerfile
    image: warehouse-web:latest
    environment:
      - REACT_APP_API_URL=http://localhost:5000
    ports:
      - "3000:80"
    depends_on:
      - backend-api

volumes:
  postgres_data:

networks:
  default:
    name: warehouse-network
```

### Backend Dockerfile
```dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 80

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["WarehouseAPI/WarehouseAPI.csproj", "WarehouseAPI/"]
RUN dotnet restore "WarehouseAPI/WarehouseAPI.csproj"
COPY . .
WORKDIR "/src/WarehouseAPI"
RUN dotnet build "WarehouseAPI.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "WarehouseAPI.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "WarehouseAPI.dll"]

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost/health || exit 1
```

### Web Admin Dockerfile
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1
```

## Deployment Automation Script

### deploy.sh
```bash
#!/bin/bash
set -e

echo "🚀 Starting Warehouse Management Deployment..."

# Configuration
VERSION=${1:-latest}
ENVIRONMENT=${2:-production}

# 1. Pre-deployment checks
echo "✓ Running pre-deployment checks..."
docker --version || exit 1
docker-compose --version || exit 1

# 2. Build images
echo "✓ Building Docker images..."
docker-compose build --no-cache

# 3. Run tests in container
echo "✓ Running tests..."
docker-compose run --rm backend-api dotnet test

# 4. Database backup
echo "✓ Backing up database..."
docker-compose exec -T postgres-db pg_dump -U warehouse warehouse_db > backup_$(date +%Y%m%d_%H%M%S).sql

# 5. Stop old containers
echo "✓ Stopping old containers..."
docker-compose down

# 6. Database migration
echo "✓ Running database migrations..."
docker-compose up -d postgres-db
sleep 10
docker-compose run --rm backend-api dotnet ef database update

# 7. Start all services
echo "✓ Starting all services..."
docker-compose up -d

# 8. Wait for health checks
echo "✓ Waiting for services to be healthy..."
timeout 120 bash -c 'until docker-compose ps | grep -q "healthy"; do sleep 5; done'

# 9. Verify deployment
echo "✓ Verifying deployment..."
curl -f http://localhost:5000/health || exit 1
curl -f http://localhost:3000/health || exit 1

# 10. Run smoke tests
echo "✓ Running smoke tests..."
./scripts/smoke-tests.sh

echo "✅ Deployment completed successfully!"
docker-compose ps
```

## Monitoring ve Logging

### Container Logs
```bash
# View all logs
docker-compose logs -f

# Specific service logs
docker-compose logs -f backend-api
docker-compose logs -f postgres-db

# Last 100 lines
docker-compose logs --tail=100 backend-api
```

### Resource Monitoring
```bash
# Container stats
docker stats

# Disk usage
docker system df

# Container inspection
docker-compose exec backend-api ps aux
docker-compose exec backend-api df -h
```

## Troubleshooting

### Common Issues

#### Issue 1: Container fails to start
```bash
# Check logs
docker-compose logs backend-api

# Check events
docker events --since 1h

# Inspect container
docker inspect <container_id>
```

#### Issue 2: Database connection fails
```bash
# Verify database is running
docker-compose ps postgres-db

# Test connection
docker-compose exec postgres-db psql -U warehouse -d warehouse_db

# Check connection string
docker-compose exec backend-api env | grep ConnectionStrings
```

#### Issue 3: Port conflicts
```bash
# Find process using port
lsof -i :5000
netstat -ano | findstr :5000  # Windows

# Kill process or change port in docker-compose.yml
```

## Security Best Practices

1. **Use secrets management**
```yaml
secrets:
  db_password:
    external: true
```

2. **Run as non-root user**
```dockerfile
RUN adduser -D appuser
USER appuser
```

3. **Scan images for vulnerabilities**
```bash
docker scan warehouse-api:latest
```

4. **Use minimal base images**
```dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0-alpine
```

5. **Enable Docker Content Trust**
```bash
export DOCKER_CONTENT_TRUST=1
```

## Post-Deployment Tasks

- [ ] Verify all services are running
- [ ] Check application logs for errors
- [ ] Monitor resource usage
- [ ] Run smoke tests
- [ ] Update documentation
- [ ] Notify stakeholders
- [ ] Create deployment tag in Git
- [ ] Archive deployment artifacts

## Cleanup

```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove unused networks
docker network prune

# Complete cleanup
docker system prune -a --volumes
```
