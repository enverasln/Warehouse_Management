# Rollback Procedures

## Genel Bakış
Production ortamında sorun çıktığında hızlı ve güvenli bir şekilde önceki stabil versiyona dönüş prosedürleri.

## Rollback Decision Criteria

### Otomatik Rollback Tetikleyicileri
- Health check failures > 3 consecutive times
- Error rate > 5% in 5 minutes
- Response time > 2000ms for 10 minutes
- CPU usage > 90% for 5 minutes
- Memory usage > 85% for 5 minutes

### Manual Rollback Durumları
- Critical bug discovered
- Security vulnerability detected
- Data corruption observed
- Business logic error
- Integration failures

## Rollback Levels

### Level 1: Configuration Rollback
Sadece konfigürasyon değişiklikleri geri alınır.

**Duration:** ~2 minutes

```bash
# Revert configuration
git checkout HEAD~1 -- config/production.json
docker-compose restart backend-api

# Verify
curl http://localhost:5000/health
```

### Level 2: Application Rollback
Uygulama kodu önceki versiyona geri alınır, database değişmez.

**Duration:** ~5 minutes

```bash
# Tag: Previous version
PREVIOUS_VERSION=$(git describe --tags --abbrev=0 HEAD~1)

# Checkout previous version
git checkout $PREVIOUS_VERSION

# Rebuild and deploy
./scripts/deploy.sh $PREVIOUS_VERSION

# Verify
./scripts/smoke-tests.sh
```

### Level 3: Database Rollback
Hem uygulama hem de database önceki duruma döner.

**Duration:** ~15 minutes

```bash
# Stop services
docker-compose down

# Restore database
docker-compose up -d postgres-db
cat backup_YYYYMMDD_HHMMSS.sql | docker-compose exec -T postgres-db psql -U warehouse warehouse_db

# Deploy previous version
./scripts/deploy.sh $PREVIOUS_VERSION

# Verify
./scripts/full-verification.sh
```

## Rollback Procedures

### Procedure 1: Blue-Green Rollback
En hızlı rollback yöntemi.

**Prerequisites:**
- Blue environment still running
- No database changes

**Steps:**
```bash
# 1. Switch load balancer back to blue
# Edit nginx.conf or load balancer config
upstream backend {
    server backend-blue:80;  # Switch from green to blue
}

# 2. Reload load balancer
docker-compose exec nginx nginx -s reload

# 3. Verify traffic on blue
curl http://localhost/health

# 4. Monitor for 15 minutes
watch -n 10 'curl -s http://localhost/metrics'

# 5. If stable, remove green
docker-compose stop backend-api-green
docker-compose rm backend-api-green
```

**Duration:** ~2 minutes
**Risk:** Low
**Data Loss:** None

### Procedure 2: Container Rollback
Docker image'ı önceki versiyona döndür.

**Steps:**
```bash
# 1. Identify current and target versions
CURRENT=$(docker images warehouse-api --format "{{.Tag}}" | head -n 1)
TARGET=$(docker images warehouse-api --format "{{.Tag}}" | head -n 2 | tail -n 1)

echo "Rolling back from $CURRENT to $TARGET"

# 2. Update docker-compose.yml
sed -i "s/warehouse-api:$CURRENT/warehouse-api:$TARGET/" docker-compose.yml

# 3. Recreate containers
docker-compose up -d --force-recreate backend-api

# 4. Wait for health
timeout 60 bash -c 'until curl -f http://localhost:5000/health; do sleep 2; done'

# 5. Verify
./scripts/smoke-tests.sh
```

**Duration:** ~5 minutes
**Risk:** Medium
**Data Loss:** Possible if DB schema changed

### Procedure 3: Git-Based Rollback
Complete source code rollback.

**Steps:**
```bash
# 1. Create rollback branch
git checkout -b rollback-$(date +%Y%m%d-%H%M%S)

# 2. Revert to previous commit
PREVIOUS_COMMIT=$(git rev-parse HEAD~1)
git revert --no-commit $PREVIOUS_COMMIT..HEAD
git commit -m "Rollback to $PREVIOUS_COMMIT"

# 3. Rebuild and deploy
docker-compose build
docker-compose up -d

# 4. Run migrations (if needed)
docker-compose run --rm backend-api dotnet ef database update

# 5. Verify
./scripts/full-verification.sh
```

**Duration:** ~10 minutes
**Risk:** Medium-High
**Data Loss:** Depends on migrations

### Procedure 4: Database Migration Rollback
Revert database schema changes.

**Steps:**
```bash
# 1. List migrations
docker-compose run --rm backend-api dotnet ef migrations list

# 2. Identify target migration
TARGET_MIGRATION="20260210_InitialCreate"  # Previous stable migration

# 3. Backup current state
docker-compose exec -T postgres-db pg_dump -U warehouse warehouse_db > rollback_backup_$(date +%Y%m%d_%H%M%S).sql

# 4. Revert migration
docker-compose run --rm backend-api dotnet ef database update $TARGET_MIGRATION

# 5. Verify schema
docker-compose exec postgres-db psql -U warehouse -d warehouse_db -c "\dt"

# 6. Restart application with compatible version
./scripts/deploy.sh compatible-version
```

**Duration:** ~15 minutes
**Risk:** High
**Data Loss:** Possible

## Emergency Rollback Script

### emergency-rollback.sh
```bash
#!/bin/bash
set -e

echo "🚨 EMERGENCY ROLLBACK INITIATED 🚨"

ROLLBACK_TYPE=${1:-blue-green}  # blue-green, container, full
REASON=${2:-"Manual trigger"}

# Log rollback
echo "$(date): Rollback started - Type: $ROLLBACK_TYPE, Reason: $REASON" >> /var/log/rollback.log

case $ROLLBACK_TYPE in
  "blue-green")
    echo "➤ Performing Blue-Green rollback..."
    # Switch to blue environment
    sed -i 's/backend-green/backend-blue/' nginx/nginx.conf
    docker-compose exec nginx nginx -s reload
    ;;
    
  "container")
    echo "➤ Performing Container rollback..."
    # Get previous image tag
    PREVIOUS=$(docker images warehouse-api --format "{{.Tag}}" | sed -n '2p')
    sed -i "s/warehouse-api:.*/warehouse-api:$PREVIOUS/" docker-compose.yml
    docker-compose up -d --force-recreate backend-api
    ;;
    
  "full")
    echo "➤ Performing Full rollback (including DB)..."
    # Find latest backup
    LATEST_BACKUP=$(ls -t backup_*.sql | head -n 1)
    
    # Stop services
    docker-compose down
    
    # Restore database
    docker-compose up -d postgres-db
    sleep 10
    cat $LATEST_BACKUP | docker-compose exec -T postgres-db psql -U warehouse warehouse_db
    
    # Deploy previous version
    PREVIOUS_TAG=$(git describe --tags --abbrev=0 HEAD~1)
    git checkout $PREVIOUS_TAG
    ./scripts/deploy.sh $PREVIOUS_TAG
    ;;
    
  *)
    echo "❌ Unknown rollback type: $ROLLBACK_TYPE"
    exit 1
    ;;
esac

# Wait and verify
sleep 10
if curl -f http://localhost:5000/health; then
    echo "✅ Rollback completed successfully"
    echo "$(date): Rollback successful" >> /var/log/rollback.log
else
    echo "❌ Rollback verification failed"
    echo "$(date): Rollback FAILED" >> /var/log/rollback.log
    exit 1
fi

# Notify team
./scripts/notify.sh "Rollback completed: $ROLLBACK_TYPE - $REASON"
```

## Rollback Verification Checklist

### Critical Checks
- [ ] All containers running and healthy
- [ ] Health check endpoints responding
- [ ] Database connections working
- [ ] API endpoints returning correct responses
- [ ] Authentication/authorization working
- [ ] No errors in logs (last 5 minutes)

### Functional Checks
- [ ] Critical business workflows functional
  - [ ] User login
  - [ ] Product creation
  - [ ] Order processing
  - [ ] Inventory updates
- [ ] Mobile app can connect
- [ ] Web admin panel accessible
- [ ] Reports generating correctly

### Data Integrity Checks
```sql
-- Check record counts
SELECT 
    'products' as table_name, COUNT(*) as count FROM products
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'inventory', COUNT(*) FROM inventory;

-- Check for data anomalies
SELECT * FROM orders WHERE created_at > NOW() - INTERVAL '1 hour' AND status = 'corrupted';

-- Verify foreign key constraints
SELECT * FROM information_schema.table_constraints WHERE constraint_type = 'FOREIGN KEY';
```

## Post-Rollback Actions

### Immediate Actions (0-30 minutes)
1. Monitor application logs
2. Watch error rates and metrics
3. Verify critical user workflows
4. Check database consistency
5. Review rollback logs

### Short-term Actions (1-4 hours)
1. Notify stakeholders of rollback
2. Create incident report
3. Analyze root cause
4. Plan fix for rolled-back changes
5. Update deployment checklist

### Long-term Actions (1-7 days)
1. Conduct post-mortem meeting
2. Update rollback procedures (if needed)
3. Improve testing to catch similar issues
4. Re-deploy fixed version
5. Document lessons learned

## Rollback Decision Matrix

| Severity | Impact | Rollback Level | Max Decision Time |
|----------|--------|----------------|-------------------|
| Critical | System down | Full (Level 3) | 5 minutes |
| High | Major features broken | Application (Level 2) | 15 minutes |
| Medium | Minor features affected | Container | 30 minutes |
| Low | Cosmetic issues | Configuration | 1 hour |

## Communication Template

### Rollback Notification
```
🚨 ROLLBACK NOTIFICATION

Environment: Production
Date/Time: 2026-02-15 16:30 UTC
Rollback Type: Container Rollback
From Version: v1.2.3
To Version: v1.2.2

Reason: High error rate detected in order processing

Status: ✅ Completed
Duration: 5 minutes
Impact: ~500 users experienced errors during rollback

Next Steps:
- Monitor system for 1 hour
- Root cause analysis scheduled for tomorrow
- Fix deployment planned for next week

Point of Contact: DevOps Team
```

## Best Practices

1. **Always have a backup** before deployment
2. **Keep previous versions** readily available
3. **Test rollback procedures** regularly
4. **Document every rollback** with details
5. **Automate common rollbacks** where possible
6. **Monitor closely** after rollback
7. **Communicate clearly** with all stakeholders
8. **Learn from each rollback** to prevent future issues

## Rollback Prevention

To minimize need for rollbacks:

1. ✅ Comprehensive testing in staging
2. ✅ Gradual rollout (canary deployments)
3. ✅ Feature flags for new functionality
4. ✅ Automated health checks
5. ✅ Performance testing before deployment
6. ✅ Code review and security scans
7. ✅ Database migration testing
8. ✅ Backup before every deployment
