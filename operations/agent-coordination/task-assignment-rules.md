# Task Assignment Rules

## Genel Bakış
Agent'lara task atanmasını yöneten kurallar ve stratejiler. Doğru agent'ın doğru task'ı alması, sistemin verimliliğini doğrudan etkiler.

## Agent Tipleri ve Expertise Alanları

### Planner Agent
**Expertise:**
- Requirements analysis
- Task decomposition
- Dependency identification
- Timeline estimation

**Atanabilir Task'lar:**
- Epic breakdown
- Feature planning
- Architecture planning
- Risk assessment

### Backend-Coder Agent
**Expertise:**
- .NET Core 8 / C# 12
- Entity Framework Core 8
- RESTful API design
- Database design
- Authentication/Authorization

**Atanabilir Task'lar:**
- Entity model creation
- API endpoint implementation
- Business logic development
- Database migration creation
- Unit test writing

### Mobile-Coder Agent
**Expertise:**
- Kotlin
- Jetpack Compose
- Android Architecture Components
- MVVM pattern
- Mobile UI/UX

**Atanabilir Task'lar:**
- Compose UI screens
- ViewModel implementation
- Repository pattern
- Navigation setup
- Mobile testing

### Web-Coder Agent
**Expertise:**
- React 18 / TypeScript
- Tailwind CSS
- State management (Redux/Zustand)
- Responsive design
- Web accessibility

**Atanabilir Task'lar:**
- React component creation
- State management setup
- API integration
- Responsive layouts
- Web testing

### Tester Agent
**Expertise:**
- Test automation
- xUnit / JUnit / Jest
- Test strategy
- Performance testing
- Security testing

**Atanabilir Task'lar:**
- Unit test creation
- Integration testing
- E2E testing
- Performance testing
- Test report generation

### Reviewer Agent
**Expertise:**
- Code quality analysis
- Security scanning
- Best practices enforcement
- Performance optimization
- Documentation review

**Atanabilir Task'lar:**
- Code review
- Security audit
- Quality gate validation
- Documentation review
- Architecture review

### Deployer Agent
**Expertise:**
- Docker / Docker Compose
- CI/CD pipelines
- Infrastructure as Code
- Deployment strategies
- Monitoring setup

**Atanabilir Task'lar:**
- Docker image building
- Container deployment
- Database migration execution
- Health check implementation
- Rollback execution

## Assignment Rules

### Rule 1: Expertise Matching
Task, gerekli expertise'e sahip agent'a atanır.

```python
def assign_by_expertise(task):
    required_skills = task.required_skills
    
    for agent in available_agents:
        if agent.has_skills(required_skills):
            return agent
    
    return None  # No suitable agent found
```

**Example:**
- Task: "Create Product API endpoint"
- Required Skills: [".NET Core", "Entity Framework", "RESTful API"]
- Assigned To: Backend-Coder Agent

### Rule 2: Load Balancing
Agents arasında yük dengeli dağıtılır.

```python
def assign_by_load(task, agent_type):
    agents = get_agents_by_type(agent_type)
    least_loaded = min(agents, key=lambda a: a.current_workload)
    
    if least_loaded.current_workload < MAX_WORKLOAD:
        return least_loaded
    
    return None  # All agents at capacity
```

**Metrics:**
- Current task count
- Estimated remaining time
- Resource utilization

### Rule 3: Priority-Based Assignment
Yüksek öncelikli task'lar önce atanır.

```python
def prioritize_tasks(tasks):
    return sorted(tasks, key=lambda t: (
        PRIORITY_WEIGHTS[t.priority],
        t.deadline,
        -t.dependencies_count
    ))
```

**Priority Levels:**
1. Critical: System down, security vulnerability
2. High: Major feature, customer commitment
3. Medium: Standard feature development
4. Low: Nice-to-have, tech debt

### Rule 4: Dependency-Aware Assignment
Dependency'leri tamamlanmış task'lar önce atanır.

```python
def can_assign(task):
    return all(
        dependency.status == "completed"
        for dependency in task.dependencies
    )

def assign_ready_tasks():
    ready_tasks = [t for t in pending_tasks if can_assign(t)]
    for task in ready_tasks:
        assign_task(task)
```

### Rule 5: Time-Zone Optimization (Opsiyonel)
Agent'ın çalışma saatlerine göre atama yapılır.

```python
def assign_by_timezone(task, agent):
    agent_local_time = get_local_time(agent.timezone)
    
    if is_working_hours(agent_local_time):
        return agent
    
    return None
```

## Assignment Strategies

### Strategy 1: Round-Robin
Task'lar sırayla agents'lara dağıtılır.

```python
current_agent_index = 0

def round_robin_assign(task, agent_type):
    global current_agent_index
    agents = get_agents_by_type(agent_type)
    
    agent = agents[current_agent_index % len(agents)]
    current_agent_index += 1
    
    return agent
```

**장점:** Simple, fair distribution
**단점:** Doesn't consider workload or expertise level

### Strategy 2: Least Loaded First
En az yüklü agent'a atanır.

```python
def least_loaded_assign(task, agent_type):
    agents = get_agents_by_type(agent_type)
    return min(agents, key=lambda a: a.current_workload)
```

**장점:** Optimal resource utilization
**단점:** May overload skilled agents

### Strategy 3: Skill-Based
Agent'ın skill level'ına göre atanır.

```python
def skill_based_assign(task, agent_type):
    agents = get_agents_by_type(agent_type)
    
    # Score each agent
    scored = [
        (agent, calculate_skill_match(agent, task))
        for agent in agents
    ]
    
    # Assign to best match
    return max(scored, key=lambda x: x[1])[0]
```

**장점:** Best quality output
**단점:** May create bottlenecks

### Strategy 4: Hybrid (Recommended)
Birden fazla faktörü kombine eder.

```python
def hybrid_assign(task, agent_type):
    agents = get_agents_by_type(agent_type)
    
    scored = [
        (agent, calculate_score(agent, task))
        for agent in agents
        if agent.current_workload < MAX_WORKLOAD
    ]
    
    return max(scored, key=lambda x: x[1])[0] if scored else None

def calculate_score(agent, task):
    return (
        0.4 * skill_match_score(agent, task) +
        0.3 * (1 - workload_ratio(agent)) +
        0.2 * availability_score(agent) +
        0.1 * past_performance_score(agent)
    )
```

## Assignment Constraints

### Hard Constraints (Must be satisfied)
- Agent has required skills
- Agent is available (not offline)
- Agent's workload < max capacity
- Dependencies are met

### Soft Constraints (Should be satisfied)
- Agent has experience with similar tasks
- Agent has capacity for task's estimated time
- Agent's timezone matches preferred work hours
- Agent has good performance history

## Assignment Workflow

```
1. Task Created
   └─> Validate task requirements
   
2. Identify Eligible Agents
   └─> Filter by expertise
   └─> Filter by availability
   └─> Check hard constraints
   
3. Score Eligible Agents
   └─> Calculate composite score
   └─> Rank agents
   
4. Assign to Best Agent
   └─> Update agent's workload
   └─> Notify agent
   └─> Update task status
   
5. Monitor Assignment
   └─> Track progress
   └─> Adjust if needed
```

## Re-Assignment Rules

### Automatic Re-Assignment Triggers
- Agent becomes unavailable
- Agent fails to start task within timeout
- Agent explicitly rejects task
- Task deadline at risk

### Re-Assignment Process
```python
def reassign_task(task, reason):
    # Log reassignment
    log_reassignment(task, task.assigned_agent, reason)
    
    # Release from current agent
    task.assigned_agent.release_task(task)
    
    # Reset task status
    task.status = "pending"
    task.assigned_agent = None
    
    # Increase priority
    task.priority = increase_priority(task.priority)
    
    # Re-assign with higher priority
    assign_task(task)
```

## Special Assignment Scenarios

### Scenario 1: No Available Agent
```python
if not available_agent:
    # Queue task
    task_queue.add(task)
    
    # Notify orchestrator
    notify("No available agent for task", task_id=task.id)
    
    # Consider scaling up
    if can_scale_up():
        create_new_agent(task.required_agent_type)
```

### Scenario 2: Critical Task
```python
if task.priority == "critical":
    # Interrupt lower priority tasks
    agent = find_agent_with_interruptible_task(task.agent_type)
    
    if agent:
        # Pause current task
        pause_task(agent.current_task)
        
        # Assign critical task
        assign_task(task, agent)
```

### Scenario 3: Team Assignment
```python
if task.requires_collaboration:
    # Assign multiple agents
    team = [
        select_agent("backend-coder"),
        select_agent("mobile-coder"),
        select_agent("web-coder")
    ]
    
    assign_team(task, team)
```

## Performance Metrics

### Assignment Efficiency
- Average time to assignment: < 5 seconds
- Assignment success rate: > 95%
- Re-assignment rate: < 5%

### Agent Utilization
- Average workload: 70-85%
- Idle time: < 15%
- Overtime rate: < 10%

### Task Completion
- On-time completion rate: > 90%
- Quality score: > 80/100
- Rework rate: < 10%

## Best Practices

1. **Always check dependencies** before assignment
2. **Balance workload** across agents
3. **Consider agent expertise** level
4. **Monitor assignment metrics** continuously
5. **Be ready to re-assign** when needed
6. **Document assignment decisions** for audit
7. **Use hybrid strategy** for best results
8. **Set appropriate timeouts** for each task type
9. **Implement graceful degradation** when agents unavailable
10. **Collect feedback** from agents to improve assignment logic

## Assignment Configuration Example

```json
{
  "assignment_strategy": "hybrid",
  "weights": {
    "skill_match": 0.4,
    "workload": 0.3,
    "availability": 0.2,
    "performance": 0.1
  },
  "constraints": {
    "max_workload_per_agent": 5,
    "max_concurrent_tasks": 3,
    "assignment_timeout_seconds": 30
  },
  "retry_policy": {
    "max_retries": 3,
    "retry_delay_seconds": 60
  }
}
```
