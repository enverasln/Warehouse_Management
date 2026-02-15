# Health Check Protocols

## Genel Bakış
Warehouse Management sisteminin sağlığını sürekli izleyen, sorunları erken tespit eden ve otomatik müdahale mekanizmalarını tetikleyen protokoller.

## Health Check Seviyeleri

### Level 1: Liveness Check
Container'ın çalışıp çalışmadığını kontrol eder.

**Endpoint:** `GET /health/live`

**Response:**
```json
{
  "status": "alive",
  "timestamp": "2026-02-15T10:30:00Z"
}
```

**Check Interval:** 10 seconds
**Timeout:** 2 seconds
**Failure Action:** Restart container

### Level 2: Readiness Check
Container'ın istekleri kabul edip edemeyeceğini kontrol eder.

**Endpoint:** `GET /health/ready`

**Response:**
```json
{
  "status": "ready",
  "timestamp": "2026-02-15T10:30:00Z",
  "checks": {
    "database": "connected",
    "cache": "available",
    "external_api": "reachable"
  }
}
```

**Check Interval:** 30 seconds
**Timeout:** 5 seconds
**Failure Action:** Remove from load balancer

### Level 3: Detailed Health Check
Tüm sistemin detaylı sağlık durumu.

**Endpoint:** `GET /health`

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2026-02-15T10:30:00Z",
  "version": "1.2.3",
  "uptime": "72h 15m",
  "components": {
    "database": {
      "status": "healthy",
      "response_time_ms": 15,
      "connection_pool": {
        "active": 5,
        "idle": 10,
        "max": 20
      }
    },
    "cache": {
      "status": "healthy",
      "hit_rate": 0.85,
      "memory_used_mb": 256
    },
    "storage": {
      "status": "healthy",
      "disk_usage_percent": 45,
      "available_gb": 100
    },
    "external_services": {
      "carrier_api": {
        "status": "healthy",
        "response_time_ms": 234
      },
      "payment_gateway": {
        "status": "degraded",
        "response_time_ms": 1500
      }
    }
  },
  "metrics": {
    "requests_per_minute": 450,
    "average_response_time_ms": 125,
    "error_rate_percent": 0.5
  }
}
```

**Check Interval:** 60 seconds
**Timeout:** 10 seconds
**Failure Action:** Alert + detailed logging

## Component Health Checks

### Database Health Check
```csharp
public async Task<HealthCheckResult> CheckDatabaseAsync()
{
    try
    {
        var stopwatch = Stopwatch.StartNew();
        await _dbContext.Database.CanConnectAsync();
        stopwatch.Stop();
        
        if (stopwatch.ElapsedMilliseconds > 1000)
        {
            return HealthCheckResult.Degraded(
                $"Database response time is high: {stopwatch.ElapsedMilliseconds}ms"
            );
        }
        
        return HealthCheckResult.Healthy($"Database connected in {stopwatch.ElapsedMilliseconds}ms");
    }
    catch (Exception ex)
    {
        return HealthCheckResult.Unhealthy("Database connection failed", ex);
    }
}
```

### Cache Health Check
```csharp
public async Task<HealthCheckResult> CheckCacheAsync()
{
    try
    {
        var testKey = $"health_check_{Guid.NewGuid()}";
        var testValue = "test";
        
        await _cache.SetAsync(testKey, testValue, TimeSpan.FromSeconds(10));
        var retrieved = await _cache.GetAsync(testKey);
        
        if (retrieved != testValue)
        {
            return HealthCheckResult.Unhealthy("Cache read/write verification failed");
        }
        
        return HealthCheckResult.Healthy("Cache is operational");
    }
    catch (Exception ex)
    {
        return HealthCheckResult.Unhealthy("Cache check failed", ex);
    }
}
```

### External API Health Check
```csharp
public async Task<HealthCheckResult> CheckExternalApiAsync()
{
    try
    {
        var stopwatch = Stopwatch.StartNew();
        var response = await _httpClient.GetAsync("/health");
        stopwatch.Stop();
        
        if (!response.IsSuccessStatusCode)
        {
            return HealthCheckResult.Unhealthy($"API returned status code: {response.StatusCode}");
        }
        
        if (stopwatch.ElapsedMilliseconds > 2000)
        {
            return HealthCheckResult.Degraded(
                $"API response time is high: {stopwatch.ElapsedMilliseconds}ms"
            );
        }
        
        return HealthCheckResult.Healthy($"API healthy ({stopwatch.ElapsedMilliseconds}ms)");
    }
    catch (Exception ex)
    {
        return HealthCheckResult.Unhealthy("API unreachable", ex);
    }
}
```

## Docker Health Checks

### Backend API Container
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost/health/live || exit 1
```

### Database Container
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD pg_isready -U warehouse -d warehouse_db || exit 1
```

### Docker Compose Health Checks
```yaml
services:
  backend-api:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health/live"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    depends_on:
      postgres-db:
        condition: service_healthy

  postgres-db:
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U warehouse"]
      interval: 10s
      timeout: 5s
      retries: 5
```

## Monitoring ve Alerting

### Health Status Thresholds

| Status | Criteria | Action |
|--------|----------|--------|
| Healthy | All checks passing, response time < 200ms | Normal operation |
| Degraded | Some checks slow (200-1000ms) | Warning alert |
| Unhealthy | Checks failing or response time > 1000ms | Critical alert + auto-remediation |
| Down | Container not responding | Restart container |

### Alert Configuration
```yaml
alerts:
  - name: "High Error Rate"
    condition: error_rate > 5%
    duration: 5 minutes
    severity: critical
    actions:
      - notify: ["slack", "pagerduty"]
      - execute: "./scripts/gather-diagnostics.sh"
  
  - name: "Slow Response Time"
    condition: avg_response_time > 1000ms
    duration: 10 minutes
    severity: warning
    actions:
      - notify: ["slack"]
      - execute: "./scripts/scale-up.sh"
  
  - name: "Database Connection Pool Exhausted"
    condition: db_connections_active / db_connections_max > 0.9
    duration: 2 minutes
    severity: critical
    actions:
      - notify: ["slack", "pagerduty"]
      - execute: "./scripts/increase-pool-size.sh"
  
  - name: "Disk Space Low"
    condition: disk_usage > 85%
    duration: 5 minutes
    severity: warning
    actions:
      - notify: ["slack"]
      - execute: "./scripts/cleanup-old-logs.sh"
```

## Automated Remediation

### Auto-Restart on Failure
```bash
# Docker auto-restart policy
docker run --restart=unless-stopped warehouse-api:latest

# Docker Compose
services:
  backend-api:
    restart: unless-stopped
```

### Auto-Scale on Load
```bash
#!/bin/bash
# auto-scale.sh

CURRENT_LOAD=$(docker stats --no-stream --format "{{.CPUPerc}}" backend-api | sed 's/%//')

if (( $(echo "$CURRENT_LOAD > 80" | bc -l) )); then
    echo "High load detected: ${CURRENT_LOAD}%"
    CURRENT_REPLICAS=$(docker-compose ps -q backend-api | wc -l)
    NEW_REPLICAS=$((CURRENT_REPLICAS + 1))
    docker-compose up -d --scale backend-api=$NEW_REPLICAS
    echo "Scaled to $NEW_REPLICAS replicas"
fi
```

### Circuit Breaker
```csharp
public class CircuitBreakerHealthCheck : IHealthCheck
{
    private readonly ICircuitBreakerPolicy _circuitBreaker;
    
    public async Task<HealthCheckResult> CheckHealthAsync(HealthCheckContext context)
    {
        var state = _circuitBreaker.GetState();
        
        return state switch
        {
            CircuitState.Closed => HealthCheckResult.Healthy("Circuit closed"),
            CircuitState.HalfOpen => HealthCheckResult.Degraded("Circuit half-open"),
            CircuitState.Open => HealthCheckResult.Unhealthy("Circuit open"),
            _ => HealthCheckResult.Unhealthy("Unknown circuit state")
        };
    }
}
```

## Health Check Dashboard

### ASCII Dashboard
```
┌─────────────────────────────────────────────────────────────┐
│ Warehouse Management System - Health Status                 │
├─────────────────────────────────────────────────────────────┤
│ Overall Status: ✅ HEALTHY                                  │
│ Last Updated: 2026-02-15 10:30:45                           │
├─────────────────────────────────────────────────────────────┤
│ Backend API:        ✅ Healthy   (125ms avg)                │
│ Database:           ✅ Healthy   (15ms)                     │
│ Cache:              ✅ Healthy   (85% hit rate)             │
│ Storage:            ✅ Healthy   (45% used)                 │
│ Carrier API:        ✅ Healthy   (234ms)                    │
│ Payment Gateway:    ⚠️  Degraded (1500ms)                   │
├─────────────────────────────────────────────────────────────┤
│ Metrics:                                                     │
│ Requests/min:       450                                      │
│ Error Rate:         0.5%                                     │
│ Active Users:       127                                      │
│ Uptime:             72h 15m                                  │
└─────────────────────────────────────────────────────────────┘
```

### Health Check Script
```bash
#!/bin/bash
# health-check.sh

check_service() {
    SERVICE=$1
    URL=$2
    
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $URL)
    
    if [ $RESPONSE -eq 200 ]; then
        echo "✅ $SERVICE: Healthy"
        return 0
    else
        echo "❌ $SERVICE: Unhealthy (HTTP $RESPONSE)"
        return 1
    fi
}

echo "═══════════════════════════════════════"
echo "  Health Check Report"
echo "  $(date)"
echo "═══════════════════════════════════════"

FAILURES=0

check_service "Backend API" "http://localhost:5000/health" || ((FAILURES++))
check_service "Web Admin" "http://localhost:3000/health" || ((FAILURES++))

echo "═══════════════════════════════════════"

if [ $FAILURES -eq 0 ]; then
    echo "✅ All services healthy"
    exit 0
else
    echo "❌ $FAILURES service(s) unhealthy"
    exit 1
fi
```

## Integration with CI/CD

### Pre-Deployment Health Check
```yaml
# .github/workflows/deploy.yml
- name: Health Check Before Deployment
  run: |
    ./scripts/health-check.sh || exit 1
    
- name: Deploy
  run: |
    ./scripts/deploy.sh
    
- name: Post-Deployment Health Check
  run: |
    sleep 30
    ./scripts/health-check.sh || ./scripts/rollback.sh
```

### Smoke Tests
```bash
#!/bin/bash
# smoke-tests.sh

echo "Running smoke tests..."

# Test 1: Health endpoint
if ! curl -f http://localhost:5000/health; then
    echo "❌ Health endpoint failed"
    exit 1
fi

# Test 2: Authentication
TOKEN=$(curl -s -X POST http://localhost:5000/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"test"}' \
    | jq -r '.token')

if [ -z "$TOKEN" ]; then
    echo "❌ Authentication failed"
    exit 1
fi

# Test 3: CRUD operations
PRODUCT=$(curl -s -X POST http://localhost:5000/api/products \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test Product","price":100}')

if [ -z "$PRODUCT" ]; then
    echo "❌ Product creation failed"
    exit 1
fi

echo "✅ All smoke tests passed"
```

## Best Practices

1. **Implement all three levels** of health checks
2. **Set appropriate timeouts** for each check
3. **Use meaningful error messages** for debugging
4. **Monitor health check latency** over time
5. **Test health checks** in failure scenarios
6. **Log all health check failures** with context
7. **Implement graceful degradation** when possible
8. **Don't make health checks too heavy** (they run frequently)
9. **Include version information** in health responses
10. **Automate remediation** for common failure scenarios

## Health Check Metrics to Track

- Health check success rate
- Average response time per component
- Time to detect failures
- Time to recover from failures
- False positive rate
- Alert fatigue metrics
