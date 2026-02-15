# Example: Yeni Ürün Ekleme Workflow'u

## Senaryo
Warehouse Management sistemine yeni bir ürün eklenmesi gerekiyor. Ürün bilgileri backend'de saklanacak, mobil uygulama ve web admin panelinde görüntülenebilecek.

## Workflow Adımları

### 1. Orchestrator (Başlangıç)
```json
{
  "from": "human",
  "to": "orchestrator",
  "request": "Yeni ürün ekleme özelliğini implement et",
  "mode": "assisted"
}
```

### 2. Planner (Task Decomposition)
```json
{
  "from": "orchestrator",
  "to": "planner",
  "task": "Break down product addition feature"
}
```

**Planner Output:**
- TASK-001: Create Product entity and DbContext configuration (.NET Core 8)
- TASK-002: Implement ProductController with POST endpoint
- TASK-003: Add product validation logic
- TASK-004: Create AddProductScreen in mobile app (Kotlin Compose)
- TASK-005: Create product form in web admin (React + Tailwind)
- TASK-006: Write backend unit tests (xUnit)
- TASK-007: Write mobile UI tests (Compose Testing)
- TASK-008: Write web E2E tests (Playwright)
- TASK-009: Code review and security scan
- TASK-010: Deploy to Docker Desktop

### 3. Parallel Execution (Wave 1)
```
Backend-Coder: TASK-001 (2h)
└─> Create Product.cs entity
└─> Add to ApplicationDbContext
└─> Generate migration
```

### 4. Parallel Execution (Wave 2)
```
Backend-Coder: TASK-002, TASK-003 (3h)
Mobile-Coder: TASK-004 (3h) [parallel]
Web-Coder: TASK-005 (2h) [parallel]
```

### 5. Testing Phase
```
Tester: TASK-006, TASK-007, TASK-008 (4h)
└─> All tests must pass
└─> Coverage threshold: 80%
```

### 6. Review Phase
```
Reviewer: TASK-009 (1h)
└─> Static analysis
└─> Security scan
└─> Best practices check
```

### 7. Deployment Phase
```
Deployer: TASK-010 (1h)
└─> Build Docker images
└─> Update docker-compose.yml
└─> Run health checks
└─> Deploy containers
```

## Total Timeline
- Estimated: 16h
- With parallel execution: ~8-10h
- Agents involved: 7/8 (except orchestrator itself)

## Artifacts Generated
1. Planning artifact (Planner)
2. Code artifacts (3 coders)
3. Test artifacts (Tester)
4. Review artifact (Reviewer)
5. Deployment artifact (Deployer)

## Success Criteria
- ✅ All tests passed
- ✅ Code coverage > 80%
- ✅ Security scan clean
- ✅ Deployment successful
- ✅ Health checks passing

## Detailed Implementation

### TASK-001: Create Product Entity

**Agent:** Backend-Coder
**Duration:** 2h

**Implementation:**
```csharp
// Product.cs
public class Product
{
    public int Id { get; set; }
    public string Name { get; set; }
    public string Description { get; set; }
    public string SKU { get; set; }
    public decimal Price { get; set; }
    public int StockQuantity { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? UpdatedAt { get; set; }
}

// ApplicationDbContext.cs
public class ApplicationDbContext : DbContext
{
    public DbSet<Product> Products { get; set; }
    
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Product>()
            .HasIndex(p => p.SKU)
            .IsUnique();
    }
}
```

**Migration:**
```bash
dotnet ef migrations add AddProductEntity
```

### TASK-002: Implement ProductController

**Agent:** Backend-Coder
**Duration:** 2h

**Implementation:**
```csharp
[ApiController]
[Route("api/[controller]")]
public class ProductController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    
    public ProductController(ApplicationDbContext context)
    {
        _context = context;
    }
    
    [HttpPost]
    public async Task<ActionResult<Product>> CreateProduct(CreateProductRequest request)
    {
        var product = new Product
        {
            Name = request.Name,
            Description = request.Description,
            SKU = request.SKU,
            Price = request.Price,
            StockQuantity = request.StockQuantity,
            CreatedAt = DateTime.UtcNow
        };
        
        _context.Products.Add(product);
        await _context.SaveChangesAsync();
        
        return CreatedAtAction(nameof(GetProduct), new { id = product.Id }, product);
    }
    
    [HttpGet("{id}")]
    public async Task<ActionResult<Product>> GetProduct(int id)
    {
        var product = await _context.Products.FindAsync(id);
        
        if (product == null)
            return NotFound();
        
        return product;
    }
}
```

### TASK-003: Add Validation Logic

**Agent:** Backend-Coder
**Duration:** 1h

**Implementation:**
```csharp
public class CreateProductRequest
{
    [Required]
    [StringLength(100)]
    public string Name { get; set; }
    
    [StringLength(500)]
    public string Description { get; set; }
    
    [Required]
    [StringLength(50)]
    public string SKU { get; set; }
    
    [Required]
    [Range(0.01, 999999.99)]
    public decimal Price { get; set; }
    
    [Required]
    [Range(0, int.MaxValue)]
    public int StockQuantity { get; set; }
}

public class CreateProductValidator : AbstractValidator<CreateProductRequest>
{
    public CreateProductValidator(ApplicationDbContext context)
    {
        RuleFor(x => x.Name).NotEmpty().MaximumLength(100);
        RuleFor(x => x.SKU).NotEmpty().MaximumLength(50)
            .MustAsync(async (sku, cancellation) => 
                !await context.Products.AnyAsync(p => p.SKU == sku))
            .WithMessage("SKU must be unique");
        RuleFor(x => x.Price).GreaterThan(0);
        RuleFor(x => x.StockQuantity).GreaterThanOrEqualTo(0);
    }
}
```

### TASK-004: Create Mobile Product Screen

**Agent:** Mobile-Coder
**Duration:** 3h

**Implementation:**
```kotlin
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel = hiltViewModel(),
    onProductAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = uiState.sku,
            onValueChange = { viewModel.updateSKU(it) },
            label = { Text("SKU") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = uiState.price,
            onValueChange = { viewModel.updatePrice(it) },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.addProduct() },
            enabled = uiState.isValid && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Add Product")
            }
        }
    }
    
    LaunchedEffect(uiState.isProductAdded) {
        if (uiState.isProductAdded) {
            onProductAdded()
        }
    }
}
```

### TASK-005: Create Web Product Form

**Agent:** Web-Coder
**Duration:** 2h

**Implementation:**
```tsx
export function AddProductForm() {
  const [formData, setFormData] = useState({
    name: '',
    sku: '',
    description: '',
    price: '',
    stockQuantity: ''
  });
  
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      await api.post('/products', {
        ...formData,
        price: parseFloat(formData.price),
        stockQuantity: parseInt(formData.stockQuantity)
      });
      
      toast({
        title: 'Success',
        description: 'Product added successfully'
      });
      
      // Reset form
      setFormData({
        name: '',
        sku: '',
        description: '',
        price: '',
        stockQuantity: ''
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to add product',
        variant: 'destructive'
      });
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium mb-2">
          Product Name
        </label>
        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          className="w-full px-3 py-2 border rounded-md"
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">
          SKU
        </label>
        <input
          type="text"
          value={formData.sku}
          onChange={(e) => setFormData({...formData, sku: e.target.value})}
          className="w-full px-3 py-2 border rounded-md"
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">
          Price
        </label>
        <input
          type="number"
          step="0.01"
          value={formData.price}
          onChange={(e) => setFormData({...formData, price: e.target.value})}
          className="w-full px-3 py-2 border rounded-md"
          required
        />
      </div>
      
      <button
        type="submit"
        disabled={isLoading}
        className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
      >
        {isLoading ? 'Adding...' : 'Add Product'}
      </button>
    </form>
  );
}
```

## Communication Flow

```
1. Human -> Orchestrator: "Add product feature"
2. Orchestrator -> Planner: "Break down into tasks"
3. Planner -> Orchestrator: Planning artifact (10 tasks)
4. Orchestrator -> Backend-Coder: TASK-001
5. Backend-Coder -> Orchestrator: Code artifact (Product entity)
6. Orchestrator -> [Backend, Mobile, Web]: TASK-002, 004, 005 (parallel)
7. All Coders -> Orchestrator: Code artifacts
8. Orchestrator -> Tester: TASK-006, 007, 008
9. Tester -> Orchestrator: Test artifact (all passed)
10. Orchestrator -> Reviewer: TASK-009
11. Reviewer -> Orchestrator: Review artifact (approved)
12. Orchestrator -> Deployer: TASK-010
13. Deployer -> Orchestrator: Deployment artifact (success)
14. Orchestrator -> Human: "Feature completed successfully"
```

## Quality Gates Passed

✅ **Planning Gate**
- All requirements defined
- No circular dependencies
- Realistic estimates

✅ **Code Quality Gate**
- Coverage: 85%
- Complexity: Avg 3.2
- No code smells

✅ **Test Quality Gate**
- Tests passed: 45/45
- No flaky tests
- E2E tests passed

✅ **Security Gate**
- No vulnerabilities
- Input validation present
- SQL injection protected

✅ **Deployment Gate**
- Health checks: All passing
- Smoke tests: All passing
- Rollback plan: Ready
