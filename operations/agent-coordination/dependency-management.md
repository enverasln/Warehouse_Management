# Dependency Management

## Genel Bakış
Task'lar arası bağımlılıkların yönetimi, multi-agent sistemde kritik öneme sahiptir. Doğru dependency management, deadlock'ları önler ve optimal paralel çalışmayı sağlar.

## Dependency Types

### 1. Strong Dependency (Zorunlu)
Task B, Task A tamamlanmadan başlayamaz.

```
Task A (Create API) -> Task B (Write Tests)
```

**Example:**
- Backend API endpoint oluşturulmadan mobil app o endpoint'i kullanamaz
- Database migration çalışmadan yeni entity kullanılamaz

### 2. Weak Dependency (Opsiyonel)
Task B, Task A'dan faydalanabilir ama zorunlu değil.

```
Task A (Design mockup) ~> Task B (Implement UI)
```

**Example:**
- Design mockup hazırsa UI daha iyi olur ama mecburi değil
- Performance optimization yapılmışsa test daha hızlı çalışır

### 3. Mutual Exclusion
Task A ve Task B aynı anda çalışamaz (conflicting resources).

```
Task A ⊗ Task B
```

**Example:**
- Aynı database migration'ı iki agent aynı anda çalıştıramaz
- Aynı dosyayı iki agent aynı anda modify edemez

### 4. Circular Dependency (ANTI-PATTERN)
Task A, Task B'ye bağlı; Task B, Task A'ya bağlı.

```
Task A <-> Task B  ❌ INVALID
```

**Prevention:**
- Dependency graph validation
- Break circular dependencies during planning

## Dependency Declaration

### Task Definition with Dependencies
```json
{
  "task_id": "TASK-123",
  "title": "Create mobile product screen",
  "agent": "mobile-coder",
  "dependencies": {
    "strong": [
      {
        "task_id": "TASK-120",
        "title": "Create Product API",
        "type": "artifact",
        "artifact_type": "code"
      }
    ],
    "weak": [
      {
        "task_id": "TASK-119",
        "title": "Design product screen mockup",
        "type": "reference"
      }
    ]
  }
}
```

## Dependency Graph

### Graph Representation
```python
class DependencyGraph:
    def __init__(self):
        self.nodes = {}  # task_id -> Task
        self.edges = {}  # task_id -> [dependent_task_ids]
    
    def add_task(self, task):
        self.nodes[task.id] = task
        self.edges[task.id] = []
    
    def add_dependency(self, from_task_id, to_task_id):
        self.edges[from_task_id].append(to_task_id)
    
    def get_dependencies(self, task_id):
        return self.edges.get(task_id, [])
    
    def topological_sort(self):
        """Returns tasks in execution order"""
        in_degree = {node: 0 for node in self.nodes}
        
        for deps in self.edges.values():
            for dep in deps:
                in_degree[dep] += 1
        
        queue = [node for node, degree in in_degree.items() if degree == 0]
        result = []
        
        while queue:
            node = queue.pop(0)
            result.append(node)
            
            for neighbor in self.edges[node]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)
        
        if len(result) != len(self.nodes):
            raise Exception("Circular dependency detected!")
        
        return result
```

### Visualization
```
TASK-001: Create Product Entity
    └─> TASK-002: Create Product API
        ├─> TASK-003: Create Mobile UI
        ├─> TASK-004: Create Web UI
        └─> TASK-005: Write API Tests
            └─> TASK-006: Code Review
                └─> TASK-007: Deploy
```

## Dependency Resolution

### Algorithm: Determine Ready Tasks
```python
def get_ready_tasks(all_tasks):
    ready = []
    
    for task in all_tasks:
        if task.status != "pending":
            continue
        
        # Check all dependencies
        dependencies_met = all(
            dep.status == "completed"
            for dep in task.dependencies
        )
        
        if dependencies_met:
            ready.append(task)
    
    return ready
```

### Wave-Based Execution
```python
def create_execution_waves(tasks):
    graph = build_dependency_graph(tasks)
    waves = []
    current_wave = []
    
    while tasks:
        # Find tasks with no pending dependencies
        ready = get_ready_tasks(tasks)
        
        if not ready:
            if tasks:
                raise Exception("Circular dependency or stuck tasks")
            break
        
        current_wave = ready
        waves.append(current_wave)
        
        # Remove completed from tasks list
        for task in ready:
            tasks.remove(task)
            mark_completed(task)
    
    return waves
```

**Example:**
```
Wave 1: [TASK-001, TASK-010, TASK-020]  # No dependencies
Wave 2: [TASK-002, TASK-011]             # Depend on Wave 1
Wave 3: [TASK-003, TASK-004, TASK-005]   # Depend on Wave 2
Wave 4: [TASK-006]                       # Depends on Wave 3
Wave 5: [TASK-007]                       # Depends on Wave 4
```

## Dependency Tracking

### Dependency Status
```json
{
  "task_id": "TASK-123",
  "dependencies": [
    {
      "task_id": "TASK-120",
      "status": "completed",
      "completed_at": "2026-02-15T10:30:00Z",
      "artifact_available": true
    },
    {
      "task_id": "TASK-121",
      "status": "in_progress",
      "progress": 75,
      "estimated_completion": "2026-02-15T11:00:00Z",
      "blocking": true
    }
  ],
  "can_start": false,
  "blocking_dependencies": ["TASK-121"]
}
```

### Dependency Notification
```python
def on_task_completed(task):
    # Find tasks waiting for this task
    dependent_tasks = find_dependent_tasks(task.id)
    
    for dependent in dependent_tasks:
        # Update dependency status
        update_dependency_status(dependent, task)
        
        # Check if ready to start
        if all_dependencies_met(dependent):
            notify_orchestrator(dependent, "ready_to_start")
            
            # Auto-assign if configured
            if AUTO_ASSIGN_ON_READY:
                assign_task(dependent)
```

## Handling Dependency Failures

### Failure Propagation
```python
def on_task_failed(task):
    # Find dependent tasks
    dependent_tasks = find_dependent_tasks(task.id)
    
    for dependent in dependent_tasks:
        if dependent.dependency_on(task).is_strong():
            # Strong dependency: block dependent task
            block_task(dependent, reason=f"Dependency {task.id} failed")
            
            # Notify stakeholders
            notify_failure(dependent)
        else:
            # Weak dependency: just warn
            warn_task(dependent, f"Optional dependency {task.id} failed")
```

### Retry Strategy
```python
def handle_failed_dependency(task, failed_dependency):
    if failed_dependency.retry_count < MAX_RETRIES:
        # Retry the failed dependency
        retry_task(failed_dependency)
        
        # Wait for retry
        return "waiting"
    else:
        # Max retries exceeded
        if task.has_alternative_dependency():
            # Use alternative
            switch_to_alternative(task)
            return "proceeding_with_alternative"
        else:
            # Block the task
            block_task(task)
            return "blocked"
```

## Dependency Artifacts

### Artifact Requirements
```json
{
  "task_id": "TASK-123",
  "required_artifacts": [
    {
      "artifact_id": "ART-456",
      "type": "code",
      "from_task": "TASK-120",
      "files_needed": [
        "ProductController.cs",
        "ProductDto.cs"
      ]
    },
    {
      "artifact_id": "ART-457",
      "type": "documentation",
      "from_task": "TASK-119",
      "optional": true
    }
  ]
}
```

### Artifact Validation
```python
def validate_artifacts(task):
    for required_artifact in task.required_artifacts:
        artifact = get_artifact(required_artifact.id)
        
        if not artifact:
            return False, f"Artifact {required_artifact.id} not found"
        
        if not artifact.is_valid():
            return False, f"Artifact {required_artifact.id} is corrupted"
        
        # Check specific files if needed
        if required_artifact.files_needed:
            missing_files = check_files(artifact, required_artifact.files_needed)
            if missing_files:
                return False, f"Missing files: {missing_files}"
    
    return True, "All artifacts valid"
```

## Dependency Optimization

### Minimize Dependencies
```python
# BEFORE: Too many dependencies
Task_A -> Task_B -> Task_C -> Task_D

# AFTER: Parallel where possible
Task_A -> Task_B
       -> Task_C  # Can run in parallel
       -> Task_D  # Can run in parallel
```

### Break Down Complex Dependencies
```python
# BEFORE: Large monolithic task
Task_Large (10h) -> Task_Dependent

# AFTER: Smaller tasks
Task_Small_1 (2h) -> Task_Dependent  # Can start earlier!
Task_Small_2 (3h)
Task_Small_3 (5h)
```

## Deadlock Prevention

### Deadlock Detection
```python
def detect_deadlock(tasks):
    visited = set()
    rec_stack = set()
    
    def has_cycle(task_id):
        visited.add(task_id)
        rec_stack.add(task_id)
        
        for neighbor in get_dependencies(task_id):
            if neighbor not in visited:
                if has_cycle(neighbor):
                    return True
            elif neighbor in rec_stack:
                return True  # Cycle detected!
        
        rec_stack.remove(task_id)
        return False
    
    for task in tasks:
        if task.id not in visited:
            if has_cycle(task.id):
                return True, "Deadlock detected"
    
    return False, "No deadlock"
```

### Resolution Strategy
```python
def resolve_deadlock(cycle):
    # Find weakest link in cycle
    weakest = find_weakest_dependency(cycle)
    
    # Break the cycle
    remove_dependency(weakest.from_task, weakest.to_task)
    
    # Log for manual review
    log_dependency_break(weakest, reason="deadlock_resolution")
    
    # Notify orchestrator
    notify("Deadlock resolved by breaking dependency", weakest)
```

## Cross-Agent Dependencies

### Communication Protocol
```json
{
  "from_agent": "backend-coder",
  "to_agent": "mobile-coder",
  "message_type": "dependency_ready",
  "payload": {
    "task_id": "TASK-120",
    "artifact_id": "ART-456",
    "artifact_location": "s3://artifacts/ART-456.zip",
    "api_contract": {
      "endpoint": "/api/products",
      "methods": ["GET", "POST", "PUT", "DELETE"],
      "documentation_url": "http://localhost:5000/swagger"
    }
  }
}
```

## Monitoring Dependencies

### Dependency Dashboard
```
┌────────────────────────────────────────────────────┐
│ Dependency Status                                  │
├────────────────────────────────────────────────────┤
│ Total Tasks: 20                                    │
│ Blocked by Dependencies: 8                         │
│ Ready to Execute: 5                                │
│ In Progress: 4                                     │
│ Completed: 3                                       │
├────────────────────────────────────────────────────┤
│ Critical Path: TASK-001 -> TASK-002 -> TASK-006   │
│ Estimated Completion: 2026-02-16 14:00            │
├────────────────────────────────────────────────────┤
│ Warnings:                                          │
│ ⚠️ TASK-010 waiting for TASK-005 (3h overdue)     │
└────────────────────────────────────────────────────┘
```

### Metrics
- Average dependency wait time
- Dependency-related delays
- Blocked task count
- Circular dependency detection count
- Artifact availability rate

## Best Practices

1. **Keep dependencies explicit** - Document all task dependencies clearly
2. **Minimize dependency chains** - Reduce sequential dependencies where possible
3. **Use weak dependencies** when appropriate - Don't over-constrain
4. **Validate dependency graph** before execution - Detect cycles early
5. **Monitor dependency health** - Track blocked tasks
6. **Plan for failure** - Have fallback strategies
7. **Version artifacts** - Ensure correct artifact versions
8. **Communicate changes** - Notify dependent tasks of upstream changes
9. **Cache artifacts** - Make dependencies readily available
10. **Review critical path** - Optimize bottlenecks

## Dependency Configuration Example

```json
{
  "dependency_management": {
    "validation": {
      "check_circular_dependencies": true,
      "check_artifact_availability": true,
      "validate_before_assignment": true
    },
    "execution": {
      "parallel_execution_enabled": true,
      "max_parallel_waves": 3,
      "wait_timeout_minutes": 60
    },
    "failure_handling": {
      "propagate_failures": true,
      "max_retry_attempts": 3,
      "alternative_path_enabled": true
    },
    "optimization": {
      "minimize_critical_path": true,
      "optimize_for_parallelism": true
    }
  }
}
```
