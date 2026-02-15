# Sevkiyat Takibi Workflow

## Agent Koordinasyonu

### Planner Agent
- Task: Sevkiyat takibi gereksinimlerini analiz et
- Output: Task breakdown artifact
- Focus Areas:
  - Shipment creation and tracking
  - Carrier integration
  - Status updates and notifications
  - Delivery confirmation

### Backend-Coder Agent
- Task 1: Shipment entity ve ilişkili modeller oluştur
- Task 2: ShipmentController ile tracking endpoints
- Task 3: Carrier integration service (abstraction layer)
- Task 4: Status update webhook handlers
- Task 5: Notification service integration
- Dependencies: Order entity (must exist)

### Mobile-Coder Agent
- Task 1: Sevkiyat listesi ekranı (Kotlin Compose)
- Task 2: Sevkiyat detay ve tracking ekranı
- Task 3: QR code scanner for shipment verification
- Task 4: Push notification integration
- Dependencies: Backend API contract'ı

### Web-Coder Agent
- Task 1: Admin panel sevkiyat yönetimi (React + Tailwind)
- Task 2: Shipment creation wizard
- Task 3: Real-time tracking map view
- Task 4: Carrier management interface
- Dependencies: Backend API contract'ı

### Tester Agent
- Task 1: Backend API testleri (xUnit)
  - Shipment CRUD tests
  - Carrier integration tests (mocked)
  - Webhook handler tests
  - Notification tests
- Task 2: Mobile UI testleri (Compose Testing)
  - List and detail screen tests
  - QR scanner tests
  - Notification tests
- Task 3: Web E2E testleri (Playwright)
  - Shipment creation flow tests
  - Tracking functionality tests
  - Map view tests
- Dependencies: Tüm implementation'lar

### Reviewer Agent
- Task: Code quality, integration ve security review
- Quality Gates:
  - Code coverage > 80%
  - Carrier integration properly abstracted
  - Webhook security validated
  - Error handling comprehensive
- Dependencies: Tüm testler başarılı

### Deployer Agent
- Task: Docker container güncelleme ve deployment
- Deployment Strategy:
  - Deploy backend services
  - Configure webhook endpoints
  - Update mobile and web apps
  - Verify carrier integration
- Dependencies: Review onayı

## Artifact Flow
[Planning] -> [Code] -> [Test] -> [Review] -> [Deployment]

## Integration Points
- Order Management System
- Carrier APIs (DHL, UPS, FedEx, etc.)
- Notification Service
- QR Code Generator/Scanner

## Parallel Execution Strategy
Wave 1: Backend-Coder (Task 1-2)
Wave 2: Backend-Coder (Task 3-5) | Mobile-Coder (All Tasks) | Web-Coder (All Tasks)
Wave 3: Tester (All Tasks)
Wave 4: Reviewer
Wave 5: Deployer

## Estimated Timeline
- Planning: 2h
- Development: 12-14h (with parallel execution)
- Testing: 5h
- Review: 2h
- Deployment: 1h
- **Total: ~22-24h**

## Success Criteria
- ✅ Shipments can be created and tracked
- ✅ Real-time status updates working
- ✅ Notifications sent correctly
- ✅ QR code verification functional
- ✅ Carrier integration stable
