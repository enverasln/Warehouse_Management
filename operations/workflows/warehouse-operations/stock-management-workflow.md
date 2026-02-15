# Stok Yönetimi Workflow

## Agent Koordinasyonu

### Planner Agent
- Task: Stok ekleme gereksinimlerini analiz et
- Output: Task breakdown artifact

### Backend-Coder Agent
- Task 1: Product entity ve API endpoint oluştur (.NET Core 8)
- Task 2: Stok validation logic implement et
- Dependencies: None (paralel başlayabilir)

### Mobile-Coder Agent
- Task: Stok ekleme ekranı oluştur (Kotlin Compose)
- Dependencies: Backend API contract'ı

### Web-Coder Agent
- Task: Admin panel stok yönetimi sayfası (React + Tailwind)
- Dependencies: Backend API contract'ı

### Tester Agent
- Task 1: Backend API testleri (xUnit)
- Task 2: Mobile UI testleri (Compose Testing)
- Task 3: Web E2E testleri (Playwright)
- Dependencies: Tüm implementation'lar

### Reviewer Agent
- Task: Code quality ve security review
- Dependencies: Tüm testler başarılı

### Deployer Agent
- Task: Docker container güncelleme ve deployment
- Dependencies: Review onayı

## Artifact Flow
[Planning] -> [Code] -> [Test] -> [Review] -> [Deployment]
