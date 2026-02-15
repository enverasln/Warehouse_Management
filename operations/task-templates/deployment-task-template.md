# Deployment Task Template

## Task Information
- **Task ID**: TASK-XXX
- **Agent**: deployer
- **Priority**: high/medium/low
- **Estimated Time**: Xh

## Deployment Target
- Environment: development/staging/production
- Platform: Docker Desktop / Cloud / On-premise

## Pre-Deployment Checklist
- [ ] All tests passed
- [ ] Code review approved
- [ ] Security scan completed
- [ ] Performance tests passed
- [ ] Database migrations prepared
- [ ] Configuration files updated
- [ ] Backup created
- [ ] Rollback plan ready

## Deployment Steps
1. [ ] Build Docker images
2. [ ] Tag images with version
3. [ ] Run database migrations
4. [ ] Deploy containers
5. [ ] Run health checks
6. [ ] Run smoke tests
7. [ ] Verify deployment
8. [ ] Monitor for issues

## Technology Stack
- Docker & Docker Compose
- PostgreSQL
- nginx (if applicable)
- Monitoring tools

## Deployment Configuration

### docker-compose.yml
```yaml
version: '3.8'
services:
  # Define services here
```

### Environment Variables
```bash
# Backend
ASPNETCORE_ENVIRONMENT=Production
DB_CONNECTION_STRING=...
API_KEY=...

# Database
POSTGRES_USER=warehouse
POSTGRES_PASSWORD=...
POSTGRES_DB=warehouse_db
```

## Deployment Commands

### Build
```bash
# Backend
docker build -t warehouse-api:v1.0.0 ./backend

# Web
docker build -t warehouse-web:v1.0.0 ./web-admin
```

### Deploy
```bash
# Full deployment
docker-compose up -d

# Specific service
docker-compose up -d backend-api

# With scaling
docker-compose up -d --scale backend-api=3
```

### Verify
```bash
# Check status
docker-compose ps

# Check logs
docker-compose logs -f backend-api

# Health checks
curl http://localhost:5000/health
```

## Database Migration

### Migration Commands
```bash
# Generate migration
dotnet ef migrations add MigrationName

# Apply migration
dotnet ef database update

# Rollback migration
dotnet ef database update PreviousMigration
```

### Migration Checklist
- [ ] Migration scripts reviewed
- [ ] Backward compatible (if possible)
- [ ] Tested in staging environment
- [ ] Rollback script prepared
- [ ] Data integrity verified

## Health Checks

### Liveness Check
```bash
curl http://localhost:5000/health/live
```

### Readiness Check
```bash
curl http://localhost:5000/health/ready
```

### Detailed Health
```bash
curl http://localhost:5000/health | jq
```

## Smoke Tests
```bash
#!/bin/bash
# smoke-tests.sh

# Test authentication
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"test"}'

# Test product API
curl http://localhost:5000/api/products

# Test web admin
curl http://localhost:3000
```

## Monitoring

### Metrics to Monitor (First 30 minutes)
- [ ] Response time < 200ms
- [ ] Error rate < 1%
- [ ] CPU usage < 70%
- [ ] Memory usage < 80%
- [ ] No 5xx errors

### Logs to Check
- [ ] Application logs (no errors)
- [ ] Database logs (no connection issues)
- [ ] nginx logs (if applicable)
- [ ] System logs

## Rollback Plan

### Rollback Trigger Conditions
- Health checks failing for > 5 minutes
- Error rate > 5%
- Critical functionality broken
- Data corruption detected

### Rollback Steps
1. [ ] Execute rollback script
2. [ ] Verify previous version running
3. [ ] Restore database (if needed)
4. [ ] Verify functionality
5. [ ] Notify stakeholders

### Rollback Command
```bash
./scripts/emergency-rollback.sh [type] "[reason]"
```

## Post-Deployment Tasks
- [ ] Monitor system for 1 hour
- [ ] Verify all critical workflows
- [ ] Check error logs
- [ ] Update documentation
- [ ] Notify stakeholders
- [ ] Create deployment tag in Git
- [ ] Archive deployment artifacts
- [ ] Schedule post-deployment review

## Deployment Artifact
```json
{
  "artifact_type": "deployment",
  "version": "1.0.0",
  "environment": "production",
  "deployed_at": "2026-02-15T10:00:00Z",
  "deployed_by": "deployer",
  "components": {
    "backend-api": {
      "image": "warehouse-api:v1.0.0",
      "status": "running",
      "health": "healthy"
    },
    "database": {
      "version": "PostgreSQL 15",
      "migrations_applied": 5,
      "status": "healthy"
    },
    "web-admin": {
      "image": "warehouse-web:v1.0.0",
      "status": "running",
      "health": "healthy"
    }
  },
  "smoke_tests": {
    "passed": true,
    "duration_seconds": 45
  },
  "rollback_plan": {
    "available": true,
    "previous_version": "0.9.5"
  }
}
```

## Communication

### Deployment Notification Template
```
🚀 DEPLOYMENT NOTIFICATION

Environment: Production
Version: v1.0.0
Date/Time: 2026-02-15 10:00 UTC

Components Deployed:
- Backend API: v1.0.0
- Web Admin: v1.0.0
- Database: Migration #12 applied

Status: ✅ Successful
Duration: 15 minutes
Downtime: None (blue-green deployment)

Health Checks: All Passing
Smoke Tests: All Passing

New Features:
- Product review system
- Enhanced search functionality
- Performance improvements

Known Issues: None

Point of Contact: DevOps Team
Documentation: https://wiki.example.com/deployments/v1.0.0
```

## Deployment Strategies

### Blue-Green Deployment
- Zero downtime
- Easy rollback
- Requires double resources

### Rolling Deployment
- Gradual rollout
- Minimal resource overhead
- Slower rollback

### Canary Deployment
- Test with subset of users
- Risk mitigation
- Complex setup

## Best Practices
1. Always create backup before deployment
2. Use versioned Docker images
3. Test deployment in staging first
4. Have rollback plan ready
5. Monitor closely after deployment
6. Document all changes
7. Communicate with stakeholders
8. Automate as much as possible
