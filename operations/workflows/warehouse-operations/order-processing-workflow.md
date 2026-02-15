# Sipariş İşleme Workflow

## Agent Koordinasyonu

### Planner Agent
- Task: Sipariş işleme akışını analiz et
- Output: Task breakdown artifact
- Artifact: Planning artifact with order processing tasks

### Backend-Coder Agent
- Task 1: Order entity ve DbContext configuration oluştur
- Task 2: OrderController ile POST/GET/PUT endpoint'leri implement et
- Task 3: Order status management logic ekle
- Task 4: Order validation rules implement et
- Dependencies: None (paralel başlayabilir)

### Mobile-Coder Agent
- Task 1: Sipariş listesi ekranı (Kotlin Compose)
- Task 2: Sipariş detay ekranı
- Task 3: Sipariş durumu güncelleme UI
- Dependencies: Backend API contract'ı

### Web-Coder Agent
- Task 1: Admin panel sipariş yönetimi sayfası (React + Tailwind)
- Task 2: Sipariş filtreleme ve arama özellikleri
- Task 3: Sipariş durumu dashboard'u
- Dependencies: Backend API contract'ı

### Tester Agent
- Task 1: Backend API testleri (xUnit)
  - Order creation tests
  - Status transition tests
  - Validation tests
- Task 2: Mobile UI testleri (Compose Testing)
  - Order list screen tests
  - Order detail screen tests
- Task 3: Web E2E testleri (Playwright)
  - Order management workflow tests
  - Filter and search tests
- Dependencies: Tüm implementation'lar

### Reviewer Agent
- Task: Code quality, security ve business logic review
- Quality Gates:
  - Code coverage > 80%
  - No security vulnerabilities
  - Business rules correctly implemented
- Dependencies: Tüm testler başarılı

### Deployer Agent
- Task: Docker container güncelleme ve deployment
- Deployment Steps:
  - Build Docker images
  - Update database schema
  - Deploy containers
  - Run smoke tests
- Dependencies: Review onayı

## Artifact Flow
[Planning] -> [Code] -> [Test] -> [Review] -> [Deployment]

## Parallel Execution Strategy
Wave 1: Backend-Coder (Task 1-2)
Wave 2: Backend-Coder (Task 3-4) | Mobile-Coder (All Tasks) | Web-Coder (All Tasks)
Wave 3: Tester (All Tasks)
Wave 4: Reviewer
Wave 5: Deployer

## Estimated Timeline
- Planning: 1h
- Development: 8-10h (with parallel execution)
- Testing: 4h
- Review: 1h
- Deployment: 1h
- **Total: ~15-17h**
