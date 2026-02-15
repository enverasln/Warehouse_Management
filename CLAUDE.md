# CLAUDE.md

This file provides guidance to Claude (Claude.ai/code and Google Antigravity) when working with code in this repository.

## Project Overview

**Warehouse Management System** - A multi-platform warehouse management application with Android mobile app (Kotlin + Jetpack Compose), .NET Core 8 backend API, and React web admin panel. Built using agent-first development approach with Google Antigravity platform.

**Current state:** Active Android development with multi-module architecture. Features include goods acceptance, goods transfer, authentication, and data synchronization. The project follows clean architecture principles with distinct data, domain, and feature layers.

## Repository Structure

```
Warehouse_Management/
├── app/                          # Main Android application module
├── feature-goods-acceptance/     # Goods acceptance feature module
├── feature-goods-transfer/       # Goods transfer feature module
├── feature-auth/                 # Authentication feature module
├── feature-home/                 # Home screen feature module
├── feature-sync/                 # Data synchronization feature module
├── feature-common/               # Shared feature components
├── domain/                       # Business logic and use cases
├── data-repository/              # Repository pattern implementations
├── data-remote/                  # Remote data sources (API)
├── data-local/                   # Local data sources (Room DB)
├── common/                       # Shared utilities and enums
├── build.gradle.kts              # Root build configuration
└── settings.gradle.kts           # Project modules configuration
```

## Target Technology Stack

### Mobile (Android)
- **Kotlin** with coroutines and Flow
- **Jetpack Compose** for UI (Material Design 3)
- **Hilt** for dependency injection
- **Room Database** for local storage (offline-first)
- **Retrofit** + **OkHttp** for network calls
- **CameraX** for barcode scanning
- **Navigation Component** with SafeArgs
- **JUnit** + **Compose Testing** + **MockK** for testing

### Backend (Planned/Future)
- **.NET Core 8** (C#)
- **ASP.NET Core Web API** (RESTful)
- **Entity Framework Core** for ORM
- **SQL Server** database
- **JWT** authentication (Bearer tokens)
- **SignalR** for real-time updates
- **Swagger/OpenAPI** for API documentation
- **xUnit** + **Moq** + **FluentAssertions** for testing

### Web Admin Panel (Planned/Future)
- **Vite** build tool
- **React 18** with TypeScript
- **Tailwind CSS** for styling
- **TanStack Query** (React Query) for data fetching
- **React Router** for navigation
- **shadcn/ui** component library
- **Recharts** for data visualization
- **Vitest** + **React Testing Library** + **Playwright** for testing

### DevOps
- **Docker Desktop** for containerization
- **Docker Compose** for local development
- **GitHub Actions** for CI/CD
- **Git** with feature branch workflow

## Key Conventions

### Naming Conventions
| Element | Convention | Example |
|---------|-----------|---------|
| Kotlin Classes | PascalCase | `GoodsAcceptanceViewModel` |
| Kotlin Functions | camelCase | `fetchGoodsData` |
| Kotlin Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Kotlin Files | PascalCase | `GoodsAcceptanceScreen.kt` |
| Packages | lowercase, dot-separated | `tr.com.cetinkaya.warehouse` |
| Composables | PascalCase | `@Composable fun GoodsAcceptanceScreen()` |
| C# Classes | PascalCase | `StockTransferController` |
| C# Methods | PascalCase | `CreateTransferAsync` |
| C# Properties | PascalCase | `FromWarehouseId` |
| DB Tables | snake_case, plural | `stock_transactions`, `warehouses` |
| DB Columns | snake_case | `created_at`, `warehouse_id` |
| API Endpoints | kebab-case, versioned | `/api/v1/stock-transfers` |
| React Components | PascalCase | `StockTransferList.tsx` |
| React Hooks | camelCase with 'use' prefix | `useStockTransfer` |

### Mobile Architecture Pattern
Clean Architecture with MVVM:
```
feature-module/
├── presentation/
│   ├── screens/        # Composable screens
│   ├── viewmodels/     # ViewModels with StateFlow
│   └── components/     # Reusable UI components
├── domain/
│   ├── models/         # Domain entities
│   └── usecases/       # Business logic
└── data/
    ├── repository/     # Repository implementations
    ├── local/          # Room entities and DAOs
    └── remote/         # Retrofit services and DTOs
```

### State Management Pattern
- **UI State** → StateFlow in ViewModel
- **One-time Events** → SharedFlow or Channel
- **Form State** → MutableState in ViewModel
- **Navigation State** → Navigation Component
- **Offline Data** → Room Database with Repository pattern
- **Network Data** → Retrofit with coroutines

### API Response Format (Backend)
All endpoints follow this structure:
```json
{
  "success": true,
  "data": { },
  "message": "Operation successful",
  "timestamp": "2024-02-15T18:00:00Z"
}
```
Error response:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": []
  },
  "timestamp": "2024-02-15T18:00:00Z"
}
```

### Database Conventions
- Primary keys: `id` (INT or BIGINT with auto-increment)
- Foreign keys: `{entity}_id` (e.g., `warehouse_id`, `product_id`)
- Timestamps: `created_at`, `updated_at`, `deleted_at` (for soft deletes)
- Boolean columns: `is_active`, `has_error`, `is_synced`
- Enums stored as: VARCHAR or INT with lookup tables

## Google Antigravity Platform

Google Antigravity is an agent-first IDE platform that uses specialized AI agents to automate and accelerate software development. Features include parallel agent execution, artifact-based validation, and intelligent orchestration.

## Google Antigravity Platform

Google Antigravity is an agent-first IDE platform that uses specialized AI agents to automate and accelerate software development. Features include parallel agent execution, artifact-based validation, and intelligent orchestration.

## Development Workflow

1. **Agent-first approach**: Specialized agents handle different aspects of development
2. **Iterative development**: Each agent works on focused tasks with clear deliverables
3. **Artifact-based validation**: All outputs are validated through structured artifacts
4. **Human-in-the-loop**: Critical decisions require human approval (Assisted/Review-Driven modes)
5. **Parallel execution**: Independent tasks run concurrently for faster delivery
6. **Quality gates**: Each phase must meet quality criteria before proceeding
7. **Feature branch strategy**: All features developed in isolated branches
8. **Continuous integration**: Automated tests run on every commit

## Performance Targets

### Mobile App
- **Startup Time**: <2 seconds cold start
- **UI Performance**: 60 FPS smooth scrolling and animations
- **Memory Usage**: <100MB peak memory for typical workflows
- **APK Size**: <50MB release APK
- **Battery Impact**: <5% per hour of active use
- **Offline Support**: Full CRUD operations available offline
- **Test Coverage**: >80% unit test coverage, >60% UI test coverage

### Backend API
- **Response Time**: <200ms (p95) for CRUD operations
- **Throughput**: >1000 requests/second under load
- **Database Queries**: <50ms average query time
- **Uptime**: >99.9% availability
- **Concurrent Users**: Support 1000+ concurrent connections
- **Test Coverage**: >85% code coverage

### Web Admin Panel
- **Time to Interactive**: <3 seconds
- **First Contentful Paint**: <1.5 seconds
- **Lighthouse Score**: >90 (Performance, Accessibility, Best Practices)
- **Bundle Size**: <500KB gzipped initial bundle
- **Test Coverage**: >75% component coverage

## Warehouse Management Domain

### Core Entities
- **Warehouse**: Physical storage locations with capacity tracking
- **Product/Goods**: Items stored and transferred
- **StockTransaction**: Record of all stock movements (acceptance, transfer, adjustment)
- **Transfer**: Movement of goods between warehouses
- **User**: System users with role-based access
- **Barcode**: Unique identifiers for products

### Business Workflows
1. **Goods Acceptance**: Receive incoming goods, scan barcodes, update inventory
2. **Goods Transfer**: Move stock between warehouses with approval workflow
3. **Stock Count**: Physical inventory verification and adjustment
4. **Synchronization**: Offline-to-online data sync with conflict resolution

### Agent Working Modes

#### 1. Autopilot Mode
- **Description**: Agents work with minimal human intervention
- **Use Case**: Rapid prototyping, MVP development, routine tasks
- **Warehouse Example**: Adding new barcode format support - Backend-Coder creates validation logic, Mobile-Coder updates scanner UI, Tester validates end-to-end flow
- **Best For**: Well-defined tasks, standard implementations, non-critical features

#### 2. Assisted Mode
- **Description**: Agents provide recommendations and await approval for critical decisions
- **Use Case**: Business logic changes, architecture decisions, security-sensitive code
- **Warehouse Example**: Modifying stock transfer approval rules - Agent proposes changes to business logic, human reviews and approves before implementation
- **Best For**: Business-critical features, complex algorithms, regulatory compliance

#### 3. Review-Driven Mode
- **Description**: Every change goes through human review process
- **Use Case**: Production hotfixes, security updates, high-risk changes
- **Warehouse Example**: Fixing inventory calculation bug - Agent creates fix, Reviewer analyzes, human approves before deployment
- **Best For**: Production systems, financial calculations, audit-critical code

### Agent Roles

### Agent Roles

#### 1. Orchestrator
**Responsibilities**: Multi-agent coordination, task distribution, workflow optimization, conflict resolution

**Warehouse Management Tasks**:
- Coordinate feature development across mobile, backend, and web teams
- Manage dependencies (e.g., Backend API must be ready before Mobile UI)
- Trigger parallel work streams when possible
- Monitor progress and adjust priorities

**Example Coordination**:
```
Feature: Quick Stock Count
├── [Parallel] Backend-Coder: POST /api/v1/quick-count endpoint
├── [Parallel] Web-Coder: Quick count dashboard preparation
├── [Depends on Backend] Mobile-Coder: Quick count screen UI
└── [Depends on All] Tester: Integration tests
```

#### 2. Planner
**Responsibilities**: Task decomposition, technical design, dependency analysis, time estimation

**Warehouse Management Tasks**:
- Break down features into implementable tasks
- Identify technical dependencies and risks
- Create architecture decision records (ADRs)
- Estimate effort and timelines

**Example Task Breakdown**:
```
Feature: Multi-Warehouse Transfer
├── Backend Tasks (8h)
│   ├── API: POST /api/v1/transfers/multi
│   ├── Service: TransferOrchestrationService
│   └── DB: Migration for multi-transfer support
├── Mobile Tasks (10h)
│   ├── UI: MultiWarehouseSelector composable
│   ├── ViewModel: MultiTransferViewModel
│   └── Repository: Multi-transfer data layer
├── Web Tasks (6h)
│   └── Dashboard: Transfer monitoring component
└── Testing Tasks (8h)
    ├── Unit tests for all components
    └── E2E multi-warehouse transfer flow
```

#### 3. Backend-Coder (.NET Core 8)
**Responsibilities**: RESTful API development, database design, business logic, API documentation

**Technologies**: .NET Core 8, Entity Framework Core, ASP.NET Web API, SQL Server, SignalR, Swagger

**Example Task**: Create Stock Transfer API
```csharp
[ApiController]
[Route("api/v1/[controller]")]
public class StockTransferController : ControllerBase
{
    private readonly IStockTransferService _service;
    
    [HttpPost]
    [ProducesResponseType(typeof(StockTransferResponse), 201)]
    [ProducesResponseType(typeof(ErrorResponse), 400)]
    public async Task<IActionResult> CreateTransfer(
        [FromBody] CreateStockTransferRequest request)
    {
        var result = await _service.CreateTransferAsync(request);
        return CreatedAtAction(nameof(GetTransfer), 
            new { id = result.Id }, result);
    }
}
```

#### 4. Mobile-Coder (Kotlin + Jetpack Compose)
**Responsibilities**: Android UI development, MVVM architecture, offline-first sync, barcode integration

**Technologies**: Kotlin, Jetpack Compose, Hilt, Room, Retrofit, CameraX, Navigation Component

**Example Task**: Goods Acceptance Screen
```kotlin
@Composable
fun GoodsAcceptanceScreen(
    viewModel: GoodsAcceptanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mal Kabul") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onScanBarcode() }) {
                Icon(Icons.Default.QrCodeScanner, "Barkod Tara")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(state.acceptedGoods) { goods ->
                GoodsAcceptanceCard(goods)
            }
        }
    }
}
```

#### 5. Web-Coder (Vite + React)
**Responsibilities**: Admin dashboard, data visualization, responsive design, component library integration

**Technologies**: Vite, React 18, TypeScript, Tailwind CSS, TanStack Query, shadcn/ui, Recharts

**Example Task**: Stock Transfer Monitoring
```typescript
export function StockTransferList() {
  const { data, isLoading } = useQuery({
    queryKey: ['stock-transfers'],
    queryFn: () => api.getStockTransfers(),
  });

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6">Stock Transfers</h1>
      <DataTable 
        columns={columns} 
        data={data?.items ?? []}
        isLoading={isLoading}
      />
    </div>
  );
}
```

#### 6. Tester
**Responsibilities**: Test automation, unit/integration/E2E testing, test coverage reporting, bug verification

**Test Stack**:
- **Backend**: xUnit, Moq, FluentAssertions, Bogus (test data)
- **Mobile**: JUnit, Compose Testing, Turbine (Flow testing), MockK
- **Web**: Vitest, React Testing Library, Playwright
- **E2E**: Playwright for full workflow testing

**Example Test**:
```kotlin
@Test
fun `when barcode scanned, goods added to acceptance list`() = runTest {
    // Arrange
    val barcode = "1234567890"
    val expectedGoods = testGoods(barcode = barcode)
    coEvery { repository.getGoodsByBarcode(barcode) } returns expectedGoods
    
    // Act
    viewModel.onBarcodeScanned(barcode)
    
    // Assert
    viewModel.state.test {
        awaitItem().acceptedGoods.shouldContain(expectedGoods)
    }
}
```

#### 7. Reviewer
**Responsibilities**: Code quality review, security scanning, architecture compliance, performance optimization

**Review Criteria**:
- **Backend**: SOLID principles, SQL injection prevention, proper error handling, API rate limiting
- **Mobile**: MVVM compliance, memory leak checks, offline-first patterns, Material Design adherence
- **Web**: Component reusability, accessibility (WCAG 2.1), performance (Lighthouse), security (XSS/CSRF)
- **All**: Test coverage, documentation completeness, naming conventions, code duplication

**Example Review**:
```markdown
## Security Issue - SQL Injection Risk
**File**: StockTransferService.cs:45
**Severity**: HIGH

Current code uses string concatenation:
```csharp
var query = $"SELECT * FROM Transfers WHERE Id = {id}";
```

Recommendation: Use parameterized queries
```csharp
var query = "SELECT * FROM Transfers WHERE Id = @Id";
var parameters = new { Id = id };
```
```

#### 8. Deployer
**Responsibilities**: Docker containerization, CI/CD pipelines, environment config, database migrations, monitoring setup

**Deployment Stack**: Docker Desktop, Docker Compose, GitHub Actions, SQL Server, Nginx

**Example Docker Compose**:
```yaml
services:
  api:
    build: ./backend
    ports: ["5000:80"]
    environment:
      - ConnectionStrings__Default=Server=db;Database=WarehouseDb
    depends_on: [db]
  
  web:
    build: ./web
    ports: ["3000:80"]
    environment:
      - VITE_API_URL=http://api:80
  
  db:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      - SA_PASSWORD=YourStrong@Pass
    ports: ["1433:1433"]
```

### Artifact System

Artifacts are structured outputs produced by agents. Each artifact type has its own schema and validation rules for quality assurance and progress tracking.

#### Artifact Types

**1. Planning Artifacts** - Task breakdown and architecture decisions
```json
{
  "artifactType": "planning",
  "featureName": "Quick Count",
  "tasks": [
    {
      "id": "TASK-001",
      "title": "Create QuickCount API endpoint",
      "assignedAgent": "Backend-Coder",
      "estimatedHours": 4,
      "dependencies": []
    }
  ],
  "architecture": {
    "backend": { "newEndpoints": ["/api/v1/quick-count"] },
    "mobile": { "newScreens": ["QuickCountScreen"] }
  }
}
```

**2. Code Artifacts** - Implementation details and code metrics
```json
{
  "artifactType": "code",
  "taskId": "TASK-001",
  "agent": "Backend-Coder",
  "files": [
    {
      "path": "src/Controllers/QuickCountController.cs",
      "language": "csharp",
      "linesOfCode": 150,
      "changes": { "added": 150, "modified": 0, "deleted": 0 }
    }
  ]
}
```

**3. Test Artifacts** - Test results and coverage
```json
{
  "artifactType": "test",
  "testSuites": [
    {
      "name": "QuickCount API Tests",
      "type": "integration",
      "framework": "xUnit",
      "totalTests": 15,
      "passed": 15,
      "failed": 0,
      "duration": "2.5s"
    }
  ],
  "coverage": { "overall": 87.5 }
}
```

**4. Review Artifacts** - Code review findings
```json
{
  "artifactType": "review",
  "findings": [
    {
      "severity": "high",
      "category": "security",
      "file": "QuickCountController.cs",
      "line": 45,
      "issue": "Potential SQL injection",
      "recommendation": "Use parameterized queries"
    }
  ],
  "metrics": { "codeQualityScore": 8.5, "securityScore": 7.0 }
}
```

**5. Deployment Artifacts** - Deployment status
```json
{
  "artifactType": "deployment",
  "environment": "staging",
  "components": [
    {
      "name": "warehouse-api",
      "version": "1.2.0",
      "status": "running",
      "healthCheck": "healthy"
    }
  ]
}
```

### Warehouse Management Agent Workflows

#### Workflow 1: New Feature Development - "Auto Stock Alert System"

**Scenario**: Automatic notification when stock level falls below minimum threshold

**Phase 1 - Planning (Planner)**
```
Tasks Identified:
├── Backend: Stock monitoring job + Notification API
├── Mobile: Notification UI + Push notification setup
├── Web: Alert configuration dashboard
└── Testing: E2E notification flow
```

**Phase 2 - Parallel Development**
```
[Backend-Coder]           [Mobile-Coder]         [Web-Coder]
StockMonitoringJob.cs     (waits for API)        (waits for API)
NotificationAPI.cs        ↓                       ↓
    ↓                     NotificationScreen.kt   AlertConfig.tsx
    └─────> API Ready ────┴────────────────────────┘
                         ↓
                    [Tester]
                    E2E Tests
                         ↓
                    [Reviewer]
                    Code Review
                         ↓
                    [Deployer]
                    Deploy to Staging
```

**Agent Communication**:
```
Orchestrator → Backend-Coder: "Create stock monitoring background job"
Backend-Coder → Orchestrator: [Code Artifact + API docs]
Orchestrator → Mobile-Coder & Web-Coder: "Integrate with API: {docs}"
Mobile/Web-Coder → Orchestrator: [Code Artifacts]
Orchestrator → Reviewer: "Review all code"
Reviewer → Orchestrator: [Review Artifact with findings]
Backend-Coder → Orchestrator: "Fixed security issues"
Orchestrator → Tester: "Run full test suite"
Tester → Orchestrator: [Test Artifact - all passed]
Orchestrator → Deployer: "Deploy to staging"
Deployer → Orchestrator: [Deployment Artifact - healthy]
```

#### Workflow 2: Bug Fix - "Goods Acceptance Sync Failure"

**Scenario**: Offline accepted goods data lost when syncing online

**Mode**: Assisted (requires approval)

```
Issue: Offline data disappears during sync
↓
[Orchestrator] Root Cause Analysis
├── Affected: Mobile (Room DB), Backend (Sync API)
├── Analysis: Conflict resolution missing
└── Strategy: Implement server-wins with local backup
↓
[Mobile-Coder] Proposes Fix
┌────────────────────────────────────┐
│ 1. Add lastModified timestamp      │
│ 2. Server-wins conflict resolution │
│ 3. Retry with exponential backoff  │
│                                    │
│ ✓ Approve  ✗ Reject  📝 Modify    │
└────────────────────────────────────┘
↓
[Human Approval] → Approved
↓
[Mobile-Coder] Implements Fix
↓
[Tester] Sync failure test scenarios
↓
[Reviewer] Data integrity verification
↓
[Deployer] Hotfix to production
```

#### Workflow 3: Performance Optimization - "Slow Inventory Report"

**Scenario**: Inventory report for 10,000+ products takes 30+ seconds

**Mode**: Review-Driven

```
Problem Identified: N+1 queries, missing indexes
↓
[Planner] Optimization Strategy
├── Backend: Query batching + indexes
├── Web: Virtual scrolling
└── Performance benchmarks
↓
[Backend-Coder] Draft PR
┌────────────────────────────────────┐
│ feat: Optimize inventory queries   │
│ - Eager loading for relations      │
│ - 5min cache with Redis            │
│ - Composite indexes added          │
│                                    │
│ Performance: 30.2s → 2.1s (93% ↓)  │
│                                    │
│ 👤 Awaiting Human Review           │
└────────────────────────────────────┘
↓
[Reviewer] Automated Review
✓ Code quality good
✓ Performance verified
⚠ Add cache invalidation strategy
⚠ Consider pagination for 10K+ items
↓
[Human Review] → Approved with changes
↓
[Backend-Coder] Implements feedback
↓
[Tester] Performance benchmarks
↓
[Deployer] Deploy to production
```

### Agent Coordination Patterns

#### Pattern 1: Feature Branch Strategy
```
main
  │
  ├── feature/stock-alert
  │   ├── backend (Backend-Coder) → PR #1
  │   ├── mobile (Mobile-Coder)   → PR #2
  │   ├── web (Web-Coder)         → PR #3
  │   └── tests (Tester)          → PR #4
  │
  └── [All PRs merged] → Orchestrator merges to main → Deployer auto-deploys
```

#### Pattern 2: Emergency Hotfix
```
00:00 - Production login failure detected
00:02 - [Planner] Root cause: SQL connection pool exhausted
00:05 - [Backend-Coder] Implements pool fix
00:08 - [Reviewer] Fast-track approval (critical path)
00:10 - [Tester] Smoke tests pass
00:12 - [Deployer] Hot deploy with rollback plan
00:15 - Health checks confirm fix
00:20 - [Planner] Post-mortem document generated
```

### Best Practices

#### Communication Protocol
```
Task Assignment:
{
  "taskId": "TASK-123",
  "priority": "high",
  "description": "Clear, actionable description",
  "acceptance_criteria": ["Tests pass", "Coverage > 80%"]
}

Artifact Submission:
{
  "artifactId": "ART-456",
  "taskId": "TASK-123",
  "status": "completed",
  "files": ["changed", "files"],
  "tests": "passed"
}
```

#### Error Handling
- **Agent fails**: Orchestrator rollback + reassign
- **Tests fail**: Block deployment + return to coder
- **Review rejects**: Send feedback + request revisions
- **Deploy error**: Automatic rollback + alert team

#### Quality Gates
All must pass before deployment:
- ✓ Unit tests passing (>80% coverage)
- ✓ Integration tests passing
- ✓ No high/critical security vulnerabilities
- ✓ Performance benchmarks met
- ✓ Code review approved
- ✓ Documentation updated

## Summary

This document defines the agent-first development approach for the Warehouse Management project using Google Antigravity platform. Each agent has specific responsibilities and operates within structured workflows using artifacts for validation and coordination.

**Key Points**:
- Agents work independently but are coordinated by Orchestrator
- Multiple working modes support different risk levels
- Artifacts provide quality control and progress tracking
- Parallel execution accelerates development
- Human-in-the-loop for critical decisions

**Support**:
- Coordination issues: Contact Orchestrator
- Technical questions: Contact relevant agent (Backend-Coder, Mobile-Coder, etc.)
- Deployment issues: Contact Deployer
