# Example: Sipariş İşleme Workflow'u

## Senaryo
Warehouse Management sisteminde müşteri siparişlerini işleme alma, onaylama ve sevkiyata hazırlama sürecinin otomatikleştirilmesi.

## İş Gereksinimleri

### Functional Requirements
- Sipariş oluşturma ve validasyon
- Stok kontrolü ve rezervasyon
- Sipariş durumu takibi
- Otomatik bildirimler
- Sipariş iptal/güncelleme

### Non-Functional Requirements
- Sipariş işleme süresi < 5 saniye
- Eşzamanlı 100+ sipariş işlenebilmeli
- %99.9 uptime
- Audit trail for compliance

## Planner Agent Output

### Task Breakdown
```json
{
  "epic_id": "EPIC-200",
  "epic_title": "Order Processing System",
  "total_estimate": "24h",
  "tasks": [
    {
      "id": "TASK-201",
      "title": "Create Order entity and relationships",
      "agent": "backend-coder",
      "estimate": "2h",
      "dependencies": [],
      "priority": "critical"
    },
    {
      "id": "TASK-202",
      "title": "Implement OrderController with CRUD endpoints",
      "agent": "backend-coder",
      "estimate": "3h",
      "dependencies": ["TASK-201"],
      "priority": "high"
    },
    {
      "id": "TASK-203",
      "title": "Add order state machine logic",
      "agent": "backend-coder",
      "estimate": "3h",
      "dependencies": ["TASK-201"],
      "priority": "high"
    },
    {
      "id": "TASK-204",
      "title": "Implement stock reservation service",
      "agent": "backend-coder",
      "estimate": "2h",
      "dependencies": ["TASK-201"],
      "priority": "high"
    },
    {
      "id": "TASK-205",
      "title": "Create OrderListScreen (mobile)",
      "agent": "mobile-coder",
      "estimate": "3h",
      "dependencies": ["TASK-202"],
      "priority": "medium"
    },
    {
      "id": "TASK-206",
      "title": "Create OrderDetailScreen (mobile)",
      "agent": "mobile-coder",
      "estimate": "2h",
      "dependencies": ["TASK-202"],
      "priority": "medium"
    },
    {
      "id": "TASK-207",
      "title": "Create OrderManagement dashboard (web)",
      "agent": "web-coder",
      "estimate": "4h",
      "dependencies": ["TASK-202"],
      "priority": "medium"
    },
    {
      "id": "TASK-208",
      "title": "Write backend tests",
      "agent": "tester",
      "estimate": "3h",
      "dependencies": ["TASK-202", "TASK-203", "TASK-204"],
      "priority": "high"
    },
    {
      "id": "TASK-209",
      "title": "Write mobile tests",
      "agent": "tester",
      "estimate": "2h",
      "dependencies": ["TASK-205", "TASK-206"],
      "priority": "medium"
    },
    {
      "id": "TASK-210",
      "title": "Write web E2E tests",
      "agent": "tester",
      "estimate": "2h",
      "dependencies": ["TASK-207"],
      "priority": "medium"
    },
    {
      "id": "TASK-211",
      "title": "Code review and security audit",
      "agent": "reviewer",
      "estimate": "2h",
      "dependencies": ["TASK-208", "TASK-209", "TASK-210"],
      "priority": "high"
    },
    {
      "id": "TASK-212",
      "title": "Deploy order processing system",
      "agent": "deployer",
      "estimate": "1h",
      "dependencies": ["TASK-211"],
      "priority": "critical"
    }
  ]
}
```

## Execution Waves

### Wave 1: Entity Setup (2h)
```
Backend-Coder: TASK-201
└─> Create Order, OrderItem, OrderStatus entities
└─> Configure relationships and constraints
└─> Generate database migration
```

### Wave 2: Business Logic (3h - Parallel)
```
Backend-Coder Thread 1: TASK-202
└─> OrderController endpoints

Backend-Coder Thread 2: TASK-203
└─> State machine implementation

Backend-Coder Thread 3: TASK-204
└─> Stock reservation logic
```

### Wave 3: Frontend Development (4h - Parallel)
```
Mobile-Coder: TASK-205, TASK-206
└─> Order list and detail screens

Web-Coder: TASK-207
└─> Order management dashboard
```

### Wave 4: Testing (3h - Parallel)
```
Tester Thread 1: TASK-208
└─> Backend API tests

Tester Thread 2: TASK-209
└─> Mobile UI tests

Tester Thread 3: TASK-210
└─> Web E2E tests
```

### Wave 5: Review & Deploy (3h)
```
Reviewer: TASK-211 (2h)
└─> Code quality check
└─> Security audit

Deployer: TASK-212 (1h)
└─> Production deployment
```

## Implementation Details

### Order Entity (TASK-201)
```csharp
public class Order
{
    public int Id { get; set; }
    public string OrderNumber { get; set; }
    public int CustomerId { get; set; }
    public Customer Customer { get; set; }
    public OrderStatus Status { get; set; }
    public decimal TotalAmount { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public List<OrderItem> Items { get; set; }
}

public class OrderItem
{
    public int Id { get; set; }
    public int OrderId { get; set; }
    public Order Order { get; set; }
    public int ProductId { get; set; }
    public Product Product { get; set; }
    public int Quantity { get; set; }
    public decimal UnitPrice { get; set; }
    public decimal TotalPrice { get; set; }
}

public enum OrderStatus
{
    Pending,
    Confirmed,
    Processing,
    ReadyToShip,
    Shipped,
    Delivered,
    Cancelled
}
```

### State Machine Logic (TASK-203)
```csharp
public class OrderStateMachine
{
    private readonly Dictionary<OrderStatus, List<OrderStatus>> _allowedTransitions = new()
    {
        { OrderStatus.Pending, new() { OrderStatus.Confirmed, OrderStatus.Cancelled } },
        { OrderStatus.Confirmed, new() { OrderStatus.Processing, OrderStatus.Cancelled } },
        { OrderStatus.Processing, new() { OrderStatus.ReadyToShip, OrderStatus.Cancelled } },
        { OrderStatus.ReadyToShip, new() { OrderStatus.Shipped } },
        { OrderStatus.Shipped, new() { OrderStatus.Delivered } },
        { OrderStatus.Delivered, new() { } },
        { OrderStatus.Cancelled, new() { } }
    };
    
    public bool CanTransition(OrderStatus from, OrderStatus to)
    {
        return _allowedTransitions.ContainsKey(from) && 
               _allowedTransitions[from].Contains(to);
    }
    
    public async Task<Result> TransitionAsync(Order order, OrderStatus newStatus)
    {
        if (!CanTransition(order.Status, newStatus))
        {
            return Result.Failure($"Cannot transition from {order.Status} to {newStatus}");
        }
        
        var oldStatus = order.Status;
        order.Status = newStatus;
        
        await _context.SaveChangesAsync();
        
        // Trigger side effects
        await OnStatusChanged(order, oldStatus, newStatus);
        
        return Result.Success();
    }
    
    private async Task OnStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus)
    {
        // Send notifications
        await _notificationService.NotifyOrderStatusChange(order, oldStatus, newStatus);
        
        // Update inventory
        if (newStatus == OrderStatus.Confirmed)
        {
            await _stockService.ReserveStock(order);
        }
        
        // Trigger shipping
        if (newStatus == OrderStatus.ReadyToShip)
        {
            await _shippingService.CreateShipment(order);
        }
    }
}
```

### Stock Reservation Service (TASK-204)
```csharp
public class StockReservationService
{
    public async Task<Result> ReserveStock(Order order)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        
        try
        {
            foreach (var item in order.Items)
            {
                var product = await _context.Products
                    .FirstOrDefaultAsync(p => p.Id == item.ProductId);
                
                if (product == null)
                {
                    return Result.Failure($"Product {item.ProductId} not found");
                }
                
                if (product.StockQuantity < item.Quantity)
                {
                    return Result.Failure(
                        $"Insufficient stock for {product.Name}. Available: {product.StockQuantity}, Required: {item.Quantity}"
                    );
                }
                
                // Reserve stock
                product.StockQuantity -= item.Quantity;
                
                // Create reservation record
                var reservation = new StockReservation
                {
                    OrderId = order.Id,
                    ProductId = item.ProductId,
                    Quantity = item.Quantity,
                    ReservedAt = DateTime.UtcNow
                };
                
                _context.StockReservations.Add(reservation);
            }
            
            await _context.SaveChangesAsync();
            await transaction.CommitAsync();
            
            return Result.Success();
        }
        catch (Exception ex)
        {
            await transaction.RollbackAsync();
            return Result.Failure($"Stock reservation failed: {ex.Message}");
        }
    }
    
    public async Task ReleaseReservation(Order order)
    {
        var reservations = await _context.StockReservations
            .Where(r => r.OrderId == order.Id)
            .ToListAsync();
        
        foreach (var reservation in reservations)
        {
            var product = await _context.Products.FindAsync(reservation.ProductId);
            product.StockQuantity += reservation.Quantity;
            
            _context.StockReservations.Remove(reservation);
        }
        
        await _context.SaveChangesAsync();
    }
}
```

### Mobile Order List (TASK-205)
```kotlin
@Composable
fun OrderListScreen(
    viewModel: OrderListViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderStatus.values().forEach { status ->
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.filterByStatus(status) },
                        label = { Text(status.name) }
                    )
                }
            }
        }
        
        // Order list
        LazyColumn {
            items(orders) { order ->
                OrderListItem(
                    order = order,
                    onClick = { viewModel.navigateToDetail(order.id) }
                )
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Customer: ${order.customerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Total: $${order.totalAmount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = formatDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

## Testing Strategy

### Backend Tests (TASK-208)
```csharp
[Fact]
public async Task CreateOrder_ValidOrder_ReturnsCreated()
{
    // Arrange
    var request = new CreateOrderRequest
    {
        CustomerId = 1,
        Items = new List<OrderItemRequest>
        {
            new() { ProductId = 1, Quantity = 2 }
        }
    };
    
    // Act
    var result = await _controller.CreateOrder(request);
    
    // Assert
    var createdResult = Assert.IsType<CreatedAtActionResult>(result.Result);
    var order = Assert.IsType<Order>(createdResult.Value);
    Assert.Equal(OrderStatus.Pending, order.Status);
}

[Fact]
public async Task TransitionOrder_ValidTransition_UpdatesStatus()
{
    // Arrange
    var order = await CreateTestOrder();
    
    // Act
    var result = await _stateMachine.TransitionAsync(order, OrderStatus.Confirmed);
    
    // Assert
    Assert.True(result.IsSuccess);
    Assert.Equal(OrderStatus.Confirmed, order.Status);
}

[Fact]
public async Task ReserveStock_InsufficientStock_ReturnsFailed()
{
    // Arrange
    var order = await CreateTestOrder(quantity: 100);
    var product = await _context.Products.FindAsync(1);
    product.StockQuantity = 50;
    
    // Act
    var result = await _stockService.ReserveStock(order);
    
    // Assert
    Assert.False(result.IsSuccess);
    Assert.Contains("Insufficient stock", result.Error);
}
```

## Timeline Summary

| Wave | Duration | Parallel Tasks | Cumulative Time |
|------|----------|----------------|-----------------|
| Wave 1 | 2h | 1 | 2h |
| Wave 2 | 3h | 3 | 5h |
| Wave 3 | 4h | 2 | 9h |
| Wave 4 | 3h | 3 | 12h |
| Wave 5 | 3h | 1 (then 1) | 15h |

**Total Sequential Time:** 24h
**Total Parallel Time:** 15h
**Efficiency Gain:** 37.5%

## Success Metrics

✅ **Functionality**
- Order creation: Working
- State transitions: All valid transitions work
- Stock reservation: Accurate and atomic

✅ **Performance**
- Order processing: 3.2s (target: <5s)
- Concurrent orders: 150 (target: >100)
- Database queries: Optimized with indexes

✅ **Quality**
- Code coverage: 87%
- Security scan: 0 vulnerabilities
- All tests passing: 78/78

✅ **User Experience**
- Mobile app: Responsive and intuitive
- Web dashboard: Real-time updates
- Notifications: Working correctly
