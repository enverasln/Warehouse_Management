# Operations - Google Antigravity Agent-First Workflows

Bu klasör, Warehouse Management sisteminin Google Antigravity platformu multi-agent orchestration sistemine uygun olarak düzenlenmiş operasyon dokümantasyonunu içerir.

## 📁 Klasör Yapısı

```
operations/
├── workflows/                    # Workflow tanımları
│   ├── agent-coordination/      # Agent koordinasyon workflow'ları
│   ├── warehouse-operations/    # Warehouse-specific workflow'lar
│   └── deployment/              # Deployment ve ops workflow'ları
├── task-templates/              # Agent task template'leri
├── artifact-schemas/            # Artifact JSON schema'ları
├── agent-coordination/          # Agent koordinasyon kuralları
└── examples/                    # Gerçek dünya workflow örnekleri
```

## 🎯 Amaç

Bu klasör şu amaçlarla oluşturulmuştur:

1. **Agent-First Development**: Tüm development süreçleri agent'lar tarafından yürütülür
2. **Paralel Çalışma**: Bağımsız task'lar paralel olarak çalıştırılır
3. **Standardizasyon**: Her agent tipi için standart template'ler
4. **Artifact Traceability**: Tüm artifact'lar şema'ya uygun ve trace edilebilir
5. **Quality Gates**: Her aşamada kalite kontrol mekanizmaları

## 📚 İçerik Detayları

### 1. Workflows

#### Agent Coordination (`workflows/agent-coordination/`)
- **orchestrator-workflows.md**: Merkezi orkestrasyon workflow'ları
- **multi-agent-parallel-execution.md**: Paralel çalışma stratejileri
- **agent-communication-protocols.md**: Agent'lar arası iletişim protokolleri

#### Warehouse Operations (`workflows/warehouse-operations/`)
- **stock-management-workflow.md**: Stok yönetimi workflow'u
- **order-processing-workflow.md**: Sipariş işleme workflow'u
- **inventory-update-workflow.md**: Envanter güncelleme workflow'u
- **shipping-workflow.md**: Sevkiyat takibi workflow'u

#### Deployment (`workflows/deployment/`)
- **docker-deployment-workflow.md**: Docker deployment prosedürleri
- **rollback-procedures.md**: Rollback stratejileri
- **health-check-protocols.md**: Health check protokolleri

### 2. Task Templates (`task-templates/`)

Her agent tipi için standart task template'leri:
- **backend-task-template.md**: Backend-Coder agent için
- **mobile-task-template.md**: Mobile-Coder agent için
- **web-task-template.md**: Web-Coder agent için
- **testing-task-template.md**: Tester agent için
- **deployment-task-template.md**: Deployer agent için

### 3. Artifact Schemas (`artifact-schemas/`)

JSON Schema ile tanımlanmış artifact tipleri:
- **planning-artifact-schema.json**: Planner agent çıktısı
- **code-artifact-schema.json**: Code artifact standardı
- **test-artifact-schema.json**: Test sonuçları şeması
- **review-artifact-schema.json**: Review sonuçları şeması
- **deployment-artifact-schema.json**: Deployment artifact'ı

### 4. Agent Coordination (`agent-coordination/`)

Agent koordinasyon kuralları ve stratejiler:
- **task-assignment-rules.md**: Task atama kuralları
- **dependency-management.md**: Dependency yönetimi
- **parallel-execution-strategy.md**: Paralel çalıştırma stratejileri
- **quality-gates.md**: Kalite kapıları ve threshold'lar

### 5. Examples (`examples/`)

Gerçek dünya senaryoları ile workflow örnekleri:
- **add-new-product-workflow.md**: Yeni ürün ekleme
- **process-order-workflow.md**: Sipariş işleme
- **update-inventory-workflow.md**: Envanter güncelleme
- **generate-report-workflow.md**: Rapor oluşturma

## 🚀 Kullanım

### Yeni Feature Development

1. **Planning Phase**: Planner agent requirements'ı analiz eder ve task'lara böler
2. **Assignment**: Orchestrator task'ları uygun agent'lara atar
3. **Parallel Execution**: Bağımsız task'lar paralel çalışır
4. **Quality Gates**: Her phase'de quality check'ler yapılır
5. **Deployment**: Deployer agent production'a deploy eder

### Workflow Örneği

```json
{
  "from": "human",
  "to": "orchestrator",
  "request": "Add product review feature",
  "mode": "assisted"
}
```

Orchestrator şu adımları izler:
1. Planner → Task breakdown
2. Backend-Coder → API implementation
3. Mobile-Coder + Web-Coder → UI (parallel)
4. Tester → All tests
5. Reviewer → Code review
6. Deployer → Production deployment

## 📊 Metrikler

### Paralel Çalışma Kazanımları

| Senaryo | Sequential | Parallel | Kazanım |
|---------|-----------|----------|---------|
| Small (5 tasks) | 10h | 4h | 2.5x |
| Medium (15 tasks) | 30h | 9h | 3.3x |
| Large (30 tasks) | 60h | 18h | 3.3x |

### Quality Gate Thresholds

- **Code Coverage**: ≥ 80%
- **Cyclomatic Complexity**: ≤ 10
- **Security Vulnerabilities**: 0 critical, 0 high
- **API Response Time**: P95 < 200ms

## 🔧 Teknoloji Stack

### Backend
- .NET Core 8
- Entity Framework Core 8
- C# 12
- xUnit

### Mobile
- Kotlin
- Jetpack Compose
- Android Architecture Components

### Web
- React 18
- TypeScript
- Tailwind CSS
- Vite

### DevOps
- Docker & Docker Compose
- GitHub Actions
- PostgreSQL 15

## 📖 Daha Fazla Bilgi

Her klasör içinde detaylı dokümantasyon bulunmaktadır. Specific workflow'lar için ilgili markdown dosyalarına bakınız.

## 🤝 Contributing

Yeni workflow'lar eklerken:
1. İlgili template'leri kullanın
2. Artifact schema'larına uyun
3. Quality gate'leri tanımlayın
4. Paralel çalışma fırsatlarını belirleyin
5. Gerçek örnek ekleyin

## 📝 Notlar

- Tüm workflow'lar agent-first yaklaşımla tasarlanmıştır
- Paralel çalışma her zaman optimize edilmelidir
- Quality gate'ler asla atlanmamalıdır
- Her artifact trace edilebilir olmalıdır
- Communication protokollerine uyulmalıdır

## 🔗 İlgili Kaynaklar

- [Google Antigravity Documentation](https://antigravity.dev)
- [Multi-Agent Systems Best Practices](https://mas-bestpractices.dev)
- [Warehouse Management Architecture](../docs/architecture.md)

---

**Version**: 1.0.0  
**Last Updated**: 2026-02-15  
**Maintained By**: DevOps Team
