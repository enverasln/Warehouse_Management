# Envanter Güncelleme Workflow

## Agent Koordinasyonu

### Planner Agent
- Task: Envanter güncelleme gereksinimlerini analiz et
- Output: Task breakdown artifact
- Focus Areas:
  - Real-time inventory sync
  - Stock level monitoring
  - Automated reorder logic

### Backend-Coder Agent
- Task 1: Inventory entity ve repository pattern oluştur
- Task 2: InventoryController ile CRUD endpoint'leri
- Task 3: Real-time stock update logic
- Task 4: Low stock alert system
- Task 5: Automated reorder trigger implementation
- Dependencies: None (paralel başlayabilir)

### Mobile-Coder Agent
- Task 1: Envanter listesi ekranı (Kotlin Compose)
- Task 2: Stok seviyesi güncelleme UI
- Task 3: Real-time sync indicator
- Task 4: Low stock alert notifications
- Dependencies: Backend API contract'ı

### Web-Coder Agent
- Task 1: Admin panel envanter dashboard (React + Tailwind)
- Task 2: Toplu envanter güncelleme formu
- Task 3: Real-time inventory monitoring charts
- Task 4: Alert configuration panel
- Dependencies: Backend API contract'ı

### Tester Agent
- Task 1: Backend API testleri (xUnit)
  - CRUD operation tests
  - Real-time update tests
  - Alert trigger tests
  - Concurrency tests
- Task 2: Mobile UI testleri (Compose Testing)
  - List and update screen tests
  - Sync indicator tests
- Task 3: Web E2E testleri (Playwright)
  - Dashboard functionality tests
  - Bulk update tests
  - Real-time monitoring tests
- Dependencies: Tüm implementation'lar

### Reviewer Agent
- Task: Code quality, performance ve security review
- Quality Gates:
  - Code coverage > 85%
  - No race conditions in concurrent updates
  - Proper error handling
  - Security audit passed
- Dependencies: Tüm testler başarılı

### Deployer Agent
- Task: Docker container güncelleme ve deployment
- Deployment Strategy:
  - Zero-downtime deployment
  - Database migration with rollback plan
  - Health checks for real-time updates
- Dependencies: Review onayı

## Artifact Flow
[Planning] -> [Code] -> [Test] -> [Review] -> [Deployment]

## Critical Success Factors
- ✅ Real-time inventory sync working correctly
- ✅ No data loss during concurrent updates
- ✅ Alert system functioning properly
- ✅ Performance meets SLA (<200ms response time)

## Parallel Execution Strategy
Wave 1: Backend-Coder (Task 1-2)
Wave 2: Backend-Coder (Task 3-5) | Mobile-Coder (All Tasks) | Web-Coder (All Tasks)
Wave 3: Tester (All Tasks)
Wave 4: Reviewer
Wave 5: Deployer

## Estimated Timeline
- Planning: 1h
- Development: 10-12h (with parallel execution)
- Testing: 5h
- Review: 2h
- Deployment: 1h
- **Total: ~19-21h**
