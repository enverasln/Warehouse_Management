# Quality Gates

## Genel Bakış
Quality gates, her development phase'de minimum kalite standartlarının sağlanmasını garanti eder. Gate geçilmezse workflow ilerlemez.

## Quality Gate Levels

### Level 1: Planning Phase Gate
**Gatekeeper:** Orchestrator
**Criteria:**
- [ ] All requirements clearly defined
- [ ] No circular dependencies
- [ ] Realistic time estimates
- [ ] Resource availability confirmed
- [ ] Risk assessment completed

**Failure Action:** Return to planning, refine requirements

### Level 2: Code Quality Gate
**Gatekeeper:** Reviewer Agent
**Criteria:**
- [ ] Code compiles without errors
- [ ] Code coverage ≥ 80%
- [ ] No critical code smells
- [ ] Complexity within acceptable range
- [ ] Naming conventions followed

**Thresholds:**
```json
{
  "code_coverage": {
    "minimum": 80,
    "target": 90,
    "unit": "percent"
  },
  "cyclomatic_complexity": {
    "maximum": 10,
    "warning": 7
  },
  "method_length": {
    "maximum": 50,
    "warning": 30,
    "unit": "lines"
  },
  "class_length": {
    "maximum": 300,
    "warning": 200,
    "unit": "lines"
  }
}
```

**Failure Action:** Code changes requested, agent must fix

### Level 3: Test Quality Gate
**Gatekeeper:** Tester Agent
**Criteria:**
- [ ] All tests pass (100%)
- [ ] No flaky tests
- [ ] Performance tests within SLA
- [ ] Security tests pass
- [ ] E2E critical paths tested

**Thresholds:**
```json
{
  "test_success_rate": {
    "minimum": 100,
    "unit": "percent"
  },
  "flaky_test_count": {
    "maximum": 0
  },
  "performance": {
    "api_response_time_ms": {
      "p50": 100,
      "p95": 200,
      "p99": 500
    },
    "page_load_time_ms": {
      "p50": 1000,
      "p95": 2000,
      "p99": 3000
    }
  }
}
```

**Failure Action:** Fix failing tests, rerun

### Level 4: Security Quality Gate
**Gatekeeper:** Reviewer Agent
**Criteria:**
- [ ] No critical vulnerabilities
- [ ] No high vulnerabilities (or documented exceptions)
- [ ] Authentication/authorization implemented
- [ ] Input validation present
- [ ] SQL injection tests passed

**Thresholds:**
```json
{
  "vulnerabilities": {
    "critical": {
      "maximum": 0,
      "blocking": true
    },
    "high": {
      "maximum": 0,
      "blocking": true
    },
    "medium": {
      "maximum": 5,
      "blocking": false
    },
    "low": {
      "maximum": 20,
      "blocking": false
    }
  }
}
```

**Failure Action:** Fix vulnerabilities, rescan

### Level 5: Performance Quality Gate
**Gatekeeper:** Tester Agent
**Criteria:**
- [ ] Response time within SLA
- [ ] Throughput meets requirements
- [ ] Resource usage acceptable
- [ ] No memory leaks
- [ ] Database queries optimized

**Thresholds:**
```json
{
  "api_performance": {
    "avg_response_time_ms": 200,
    "max_response_time_ms": 1000,
    "requests_per_second": 100
  },
  "resource_usage": {
    "cpu_percent": {
      "average": 70,
      "peak": 90
    },
    "memory_mb": {
      "average": 512,
      "peak": 1024
    }
  }
}
```

**Failure Action:** Performance optimization required

### Level 6: Documentation Quality Gate
**Gatekeeper:** Reviewer Agent
**Criteria:**
- [ ] API documentation complete
- [ ] Code comments where needed
- [ ] README updated
- [ ] Architecture docs current
- [ ] Release notes prepared

**Thresholds:**
```json
{
  "documentation_completeness": {
    "minimum": 90,
    "unit": "percent"
  },
  "api_coverage": {
    "minimum": 100,
    "unit": "percent"
  }
}
```

**Failure Action:** Complete documentation

### Level 7: Deployment Quality Gate
**Gatekeeper:** Deployer Agent
**Criteria:**
- [ ] All previous gates passed
- [ ] Health checks pass
- [ ] Smoke tests pass
- [ ] Rollback plan ready
- [ ] Monitoring configured

**Failure Action:** Rollback deployment

## Quality Gate Implementation

### Gate Check Function
```python
class QualityGate:
    def __init__(self, name, criteria, thresholds):
        self.name = name
        self.criteria = criteria
        self.thresholds = thresholds
    
    def check(self, artifact):
        results = []
        
        for criterion in self.criteria:
            result = self.evaluate_criterion(criterion, artifact)
            results.append(result)
        
        passed = all(r.passed for r in results)
        
        return QualityGateResult(
            gate_name=self.name,
            passed=passed,
            criteria_results=results
        )
    
    def evaluate_criterion(self, criterion, artifact):
        actual_value = get_metric(artifact, criterion.metric_name)
        threshold = self.thresholds[criterion.metric_name]
        
        passed = self.compare(actual_value, threshold, criterion.operator)
        
        return CriterionResult(
            criterion_name=criterion.name,
            passed=passed,
            actual_value=actual_value,
            threshold=threshold
        )
```

### Gate Execution Workflow
```python
def execute_quality_gates(artifact, phase):
    gates = get_gates_for_phase(phase)
    
    results = []
    
    for gate in gates:
        print(f"Checking {gate.name}...")
        
        result = gate.check(artifact)
        results.append(result)
        
        if not result.passed:
            if gate.blocking:
                # Stop execution
                notify_failure(gate, result)
                return QualityGateExecution(
                    passed=False,
                    failed_gate=gate,
                    all_results=results
                )
            else:
                # Log warning but continue
                log_warning(gate, result)
    
    # All gates passed
    return QualityGateExecution(
        passed=True,
        all_results=results
    )
```

## Quality Metrics

### Code Quality Metrics
```python
class CodeQualityMetrics:
    def calculate(self, code_artifact):
        return {
            "coverage": self.calculate_coverage(code_artifact),
            "complexity": self.calculate_complexity(code_artifact),
            "duplication": self.calculate_duplication(code_artifact),
            "maintainability": self.calculate_maintainability(code_artifact),
            "technical_debt": self.calculate_tech_debt(code_artifact)
        }
    
    def calculate_coverage(self, artifact):
        lines_covered = artifact.lines_covered
        total_lines = artifact.total_lines
        return (lines_covered / total_lines) * 100
    
    def calculate_complexity(self, artifact):
        # Cyclomatic complexity
        total_complexity = sum(method.complexity for method in artifact.methods)
        return total_complexity / len(artifact.methods)
```

### Test Quality Metrics
```python
class TestQualityMetrics:
    def calculate(self, test_artifact):
        return {
            "pass_rate": self.calculate_pass_rate(test_artifact),
            "flakiness": self.calculate_flakiness(test_artifact),
            "execution_time": self.calculate_execution_time(test_artifact),
            "coverage": self.calculate_test_coverage(test_artifact)
        }
```

## Automated Quality Checks

### Pre-Commit Hooks
```bash
#!/bin/bash
# pre-commit hook

echo "Running quality checks..."

# Linting
npm run lint || exit 1

# Type checking
npm run type-check || exit 1

# Unit tests
npm test || exit 1

echo "✅ All pre-commit checks passed"
```

### CI Pipeline Gates
```yaml
# .github/workflows/quality-gates.yml
name: Quality Gates

on: [push, pull_request]

jobs:
  code-quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Code Coverage
        run: |
          npm test -- --coverage
          COVERAGE=$(cat coverage/coverage-summary.json | jq '.total.lines.pct')
          if (( $(echo "$COVERAGE < 80" | bc -l) )); then
            echo "❌ Coverage $COVERAGE% < 80%"
            exit 1
          fi
      
      - name: Complexity Check
        run: |
          npm run complexity
          # Fail if any file has complexity > 10
      
      - name: Security Scan
        run: |
          npm audit --audit-level=high
  
  test-quality:
    runs-on: ubuntu-latest
    steps:
      - name: Run Tests
        run: npm test
      
      - name: Check for Flaky Tests
        run: |
          # Run tests 3 times
          for i in {1..3}; do
            npm test || exit 1
          done
```

## Quality Gate Dashboard

### Real-Time Status
```
┌─────────────────────────────────────────────────────┐
│ Quality Gates - Feature: Add Product                │
├─────────────────────────────────────────────────────┤
│ ✅ Planning Gate       (passed)                     │
│ ✅ Code Quality Gate   (92% coverage)               │
│ ✅ Test Quality Gate   (120/120 tests passed)       │
│ ✅ Security Gate       (0 vulnerabilities)          │
│ ⚙️  Performance Gate   (in progress...)             │
│ ⏸️  Documentation Gate (pending)                     │
│ ⏸️  Deployment Gate    (pending)                     │
├─────────────────────────────────────────────────────┤
│ Overall: 4/7 passed, 1 in progress, 2 pending      │
└─────────────────────────────────────────────────────┘
```

### Historical Trends
```
Quality Gate Pass Rate (Last 30 Days)
100%|████████████████████████████░░│ 93%
 90%|
 80%|
    └─────────────────────────────┘
     Week 1  Week 2  Week 3  Week 4
```

## Handling Gate Failures

### Automatic Fixes (Where Possible)
```python
def auto_fix_gate_failure(gate, result):
    if gate.name == "code_quality" and "formatting" in result.issues:
        # Auto-format code
        run_formatter(result.artifact)
        return "auto_fixed"
    
    if gate.name == "test_quality" and "missing_tests" in result.issues:
        # Generate test templates
        generate_test_templates(result.artifact)
        return "templates_generated"
    
    return "manual_fix_required"
```

### Manual Review Process
```python
def request_manual_review(gate, result):
    ticket = create_review_ticket(
        title=f"Quality Gate Failure: {gate.name}",
        description=format_failure_details(result),
        assignee=result.artifact.owner,
        priority="high"
    )
    
    notify_assignee(ticket)
    
    return ticket
```

### Exception Process
```python
def request_gate_exception(gate, result, justification):
    exception_request = {
        "gate": gate.name,
        "failure_details": result,
        "justification": justification,
        "requested_by": current_user(),
        "requested_at": datetime.now()
    }
    
    # Requires manager approval
    approval = await_approval(exception_request)
    
    if approval.approved:
        log_exception(exception_request, approval)
        return "exception_granted"
    else:
        return "exception_denied"
```

## Quality Gate Configuration

### Global Configuration
```json
{
  "quality_gates": {
    "enabled": true,
    "blocking_mode": true,
    "gates": [
      {
        "name": "code_quality",
        "phase": "review",
        "blocking": true,
        "thresholds": {
          "coverage": 80,
          "complexity": 10,
          "duplication": 5
        }
      },
      {
        "name": "security",
        "phase": "review",
        "blocking": true,
        "thresholds": {
          "critical_vulnerabilities": 0,
          "high_vulnerabilities": 0
        }
      },
      {
        "name": "performance",
        "phase": "test",
        "blocking": true,
        "thresholds": {
          "api_response_time_p95": 200,
          "page_load_time_p95": 2000
        }
      }
    ]
  }
}
```

### Project-Specific Overrides
```json
{
  "project": "warehouse-management",
  "quality_gates_override": {
    "code_quality": {
      "thresholds": {
        "coverage": 85
      }
    }
  }
}
```

## Best Practices

1. **Define clear, measurable criteria** for each gate
2. **Automate checks** wherever possible
3. **Make gates blocking** for critical quality issues
4. **Provide fast feedback** to agents
5. **Document exceptions** when gates are bypassed
6. **Review gate effectiveness** regularly
7. **Adjust thresholds** based on project needs
8. **Integrate with CI/CD** pipeline
9. **Track metrics over time** to identify trends
10. **Balance strictness** with productivity

## Continuous Improvement

### Gate Effectiveness Review
```python
def analyze_gate_effectiveness():
    metrics = {
        "false_positive_rate": calculate_false_positives(),
        "false_negative_rate": calculate_false_negatives(),
        "average_time_to_fix": calculate_avg_fix_time(),
        "bypass_rate": calculate_bypass_rate()
    }
    
    # Adjust thresholds if needed
    if metrics["false_positive_rate"] > 0.1:
        recommend_threshold_adjustment()
```

### Feedback Loop
```python
def collect_gate_feedback(gate, result):
    feedback = {
        "gate": gate.name,
        "result": result.passed,
        "agent_satisfaction": survey_agent(result.artifact.owner),
        "time_to_fix": result.fix_duration if not result.passed else None,
        "suggestions": get_agent_suggestions(result.artifact.owner)
    }
    
    store_feedback(feedback)
    
    # Quarterly review
    if should_review_gates():
        review_and_adjust_gates()
```
