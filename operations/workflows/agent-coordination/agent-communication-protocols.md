# Agent Communication Protocols

## Genel Bakış
Agent'lar arası iletişim protokolleri, sistemin güvenli, verimli ve trace edilebilir çalışmasını sağlar.

## Communication Layers

### Layer 1: Transport Layer
Mesajların fiziksel iletimi

**Protokoller:**
- HTTP/REST API
- gRPC (high-performance)
- WebSocket (real-time)
- Message Queue (async)

### Layer 2: Message Format Layer
Mesaj yapısı ve serileştirme

**Format:**
```json
{
  "message_id": "MSG-12345",
  "timestamp": "2026-02-15T10:30:00Z",
  "from": "agent-id",
  "to": "agent-id|orchestrator",
  "type": "request|response|notification|error",
  "payload": {},
  "metadata": {
    "correlation_id": "CORR-67890",
    "priority": "high|medium|low",
    "ttl": 3600
  }
}
```

### Layer 3: Semantic Layer
Mesajın anlamı ve işlenmesi

**Message Types:**
- Task assignment
- Status update
- Artifact delivery
- Error notification
- Resource request

## Message Types

### 1. Task Assignment Message
Orchestrator -> Agent

```json
{
  "message_id": "MSG-001",
  "type": "request",
  "from": "orchestrator",
  "to": "backend-coder",
  "timestamp": "2026-02-15T10:00:00Z",
  "payload": {
    "task_id": "TASK-123",
    "task_type": "implement_api",
    "description": "Create Product API endpoint",
    "requirements": {
      "entity": "Product",
      "operations": ["POST", "GET", "PUT", "DELETE"],
      "validation": ["required", "unique"]
    },
    "dependencies": [],
    "artifacts_required": [],
    "deadline": "2026-02-15T13:00:00Z"
  }
}
```

### 2. Status Update Message
Agent -> Orchestrator

```json
{
  "message_id": "MSG-002",
  "type": "notification",
  "from": "backend-coder",
  "to": "orchestrator",
  "timestamp": "2026-02-15T11:00:00Z",
  "payload": {
    "task_id": "TASK-123",
    "status": "in_progress|completed|failed",
    "progress": 75,
    "current_step": "Writing unit tests",
    "estimated_completion": "2026-02-15T12:30:00Z"
  }
}
```

### 3. Artifact Delivery Message
Agent -> Orchestrator/Agent

```json
{
  "message_id": "MSG-003",
  "type": "response",
  "from": "backend-coder",
  "to": "orchestrator",
  "timestamp": "2026-02-15T12:30:00Z",
  "payload": {
    "task_id": "TASK-123",
    "status": "completed",
    "artifact": {
      "artifact_id": "ART-456",
      "type": "code",
      "location": "s3://artifacts/ART-456.zip",
      "checksum": "sha256:abc123...",
      "metadata": {
        "language": "csharp",
        "files": ["Product.cs", "ProductController.cs", "ProductTests.cs"],
        "lines_of_code": 450,
        "test_coverage": 85.5
      }
    }
  }
}
```

### 4. Error Notification Message
Agent -> Orchestrator

```json
{
  "message_id": "MSG-004",
  "type": "error",
  "from": "tester",
  "to": "orchestrator",
  "timestamp": "2026-02-15T14:00:00Z",
  "payload": {
    "task_id": "TASK-125",
    "error_type": "test_failure",
    "severity": "high|medium|low",
    "message": "Unit tests failed: 3 out of 45 tests",
    "details": {
      "failed_tests": [
        "ProductController_CreateProduct_InvalidInput",
        "ProductController_UpdateProduct_NotFound",
        "ProductController_DeleteProduct_InUse"
      ],
      "logs": "s3://logs/TASK-125-error.log"
    },
    "retry_possible": true
  }
}
```

### 5. Resource Request Message
Agent -> Orchestrator

```json
{
  "message_id": "MSG-005",
  "type": "request",
  "from": "deployer",
  "to": "orchestrator",
  "timestamp": "2026-02-15T15:00:00Z",
  "payload": {
    "resource_type": "artifact",
    "resource_id": "ART-456",
    "reason": "Required for deployment",
    "task_id": "TASK-130"
  }
}
```

## Communication Patterns

### Pattern 1: Request-Response (Synchronous)
Agent mesaj gönderir ve yanıt bekler.

```
Agent A  ─────request─────>  Orchestrator
Agent A  <────response──────  Orchestrator
```

**Use Case:** Critical operations, immediate feedback needed

### Pattern 2: Fire-and-Forget (Asynchronous)
Agent mesaj gönderir, yanıt beklemez.

```
Agent A  ─────notification──>  Orchestrator
         (no response expected)
```

**Use Case:** Status updates, logging, non-critical notifications

### Pattern 3: Publish-Subscribe
Agent mesaj yayınlar, ilgilenenler alır.

```
Agent A  ────publish────>  Message Bus
                              │
                              ├──>  Agent B
                              ├──>  Agent C
                              └──>  Orchestrator
```

**Use Case:** Broadcast notifications, event-driven architecture

### Pattern 4: Request-Acknowledge-Response
Agent mesaj gönderir, önce ACK alır, sonra response.

```
Agent A  ─────request─────>  Orchestrator
Agent A  <─────ACK─────────  Orchestrator
         ... processing ...
Agent A  <────response──────  Orchestrator
```

**Use Case:** Long-running operations, progress tracking

## Security

### Authentication
Her agent unique credentials ile authenticate olur.

```json
{
  "agent_id": "backend-coder-001",
  "api_key": "encrypted_key",
  "certificate": "X.509 cert",
  "valid_until": "2026-12-31T23:59:59Z"
}
```

### Authorization
Agent'lar sadece yetkili oldukları işlemleri yapabilir.

```json
{
  "agent_id": "backend-coder-001",
  "permissions": [
    "read:tasks",
    "write:artifacts",
    "update:status",
    "request:resources"
  ],
  "restrictions": [
    "cannot:deploy",
    "cannot:delete:artifacts"
  ]
}
```

### Message Encryption
Hassas data şifrelenir.

```json
{
  "message_id": "MSG-006",
  "encrypted": true,
  "algorithm": "AES-256-GCM",
  "payload_encrypted": "base64_encrypted_data",
  "signature": "digital_signature"
}
```

### Audit Trail
Tüm mesajlar log'lanır.

```json
{
  "log_id": "LOG-789",
  "timestamp": "2026-02-15T16:00:00Z",
  "from": "backend-coder-001",
  "to": "orchestrator",
  "message_id": "MSG-006",
  "action": "task_completed",
  "result": "success",
  "duration_ms": 1250
}
```

## Error Handling

### Retry Policy
```json
{
  "max_retries": 3,
  "retry_delay": "exponential",
  "initial_delay_ms": 1000,
  "max_delay_ms": 30000,
  "retry_on": ["network_error", "timeout", "503_service_unavailable"]
}
```

### Circuit Breaker
Sürekli fail eden agent'lara mesaj gönderilmez.

```json
{
  "agent_id": "mobile-coder-002",
  "circuit_status": "open|closed|half_open",
  "failure_count": 5,
  "failure_threshold": 5,
  "timeout_duration": "60s",
  "next_attempt": "2026-02-15T16:05:00Z"
}
```

### Dead Letter Queue
İşlenemeyen mesajlar ayrı bir queue'ya gider.

```json
{
  "dlq_id": "DLQ-001",
  "original_message": "MSG-007",
  "failure_reason": "Agent not responding",
  "retry_count": 3,
  "moved_to_dlq_at": "2026-02-15T16:10:00Z",
  "requires": "manual_intervention"
}
```

## Quality of Service (QoS)

### Priority Levels
```json
{
  "critical": {
    "priority": 1,
    "max_latency_ms": 100,
    "timeout_ms": 5000
  },
  "high": {
    "priority": 2,
    "max_latency_ms": 500,
    "timeout_ms": 15000
  },
  "medium": {
    "priority": 3,
    "max_latency_ms": 2000,
    "timeout_ms": 60000
  },
  "low": {
    "priority": 4,
    "max_latency_ms": 5000,
    "timeout_ms": 300000
  }
}
```

### Delivery Guarantees
- **At-most-once**: Mesaj en fazla bir kez iletilir (can be lost)
- **At-least-once**: Mesaj en az bir kez iletilir (can duplicate)
- **Exactly-once**: Mesaj tam olarak bir kez işlenir (recommended)

## Monitoring

### Message Metrics
```
┌────────────────────────────────────┐
│ Communication Metrics              │
├────────────────────────────────────┤
│ Messages Sent: 1,234               │
│ Messages Received: 1,230           │
│ Messages Failed: 4                 │
│ Average Latency: 245ms             │
│ Max Latency: 1,234ms               │
│ Circuit Breakers Open: 0           │
│ DLQ Size: 2                        │
└────────────────────────────────────┘
```

### Agent Communication Graph
```
orchestrator (hub)
  ├─> backend-coder (234 msgs)
  ├─> mobile-coder (189 msgs)
  ├─> web-coder (156 msgs)
  ├─> tester (98 msgs)
  ├─> reviewer (45 msgs)
  └─> deployer (23 msgs)
```

## Best Practices

1. **Always include correlation_id** for request tracing
2. **Set appropriate timeouts** for each message type
3. **Implement idempotency** for critical operations
4. **Use structured logging** for debugging
5. **Validate messages** before processing
6. **Handle errors gracefully** with proper fallbacks
7. **Monitor communication patterns** for anomalies
8. **Use compression** for large payloads
9. **Implement rate limiting** to prevent overload
10. **Document all message schemas** for maintainability
