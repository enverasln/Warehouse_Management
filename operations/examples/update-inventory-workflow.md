# Example: Envanter Güncelleme Workflow'u

## Senaryo
Warehouse Management sisteminde real-time envanter takibi, otomatik stok uyarıları ve toplu envanter güncellemeleri özelliğinin implement edilmesi.

## İş Gereksinimleri

### Functional Requirements
- Real-time stok seviyesi güncelleme
- Düşük stok seviyesi uyarıları
- Toplu envanter sayımı
- Envanter hareketleri geçmişi
- Otomatik reorder tetikleme

### Non-Functional Requirements
- Real-time sync latency < 1 saniye
- 99.99% data accuracy
- Concurrent update handling (optimistic locking)
- Audit trail for compliance

## Agent Görevleri

### Backend Tasks
```
TASK-301: Inventory entity ve tracking system (3h)
TASK-302: InventoryController with batch update support (2h)
TASK-303: Real-time sync service with SignalR (3h)
TASK-304: Low stock alert system (2h)
TASK-305: Automated reorder service (2h)
```

### Mobile Tasks
```
TASK-306: Inventory list screen with filters (2h)
TASK-307: Inventory adjustment screen (2h)
TASK-308: QR/Barcode scanner for quick update (2h)
TASK-309: Real-time sync indicator (1h)
```

### Web Tasks
```
TASK-310: Inventory dashboard with charts (3h)
TASK-311: Bulk inventory update interface (2h)
TASK-312: Inventory history report (2h)
```

### Test & Deploy
```
TASK-313: Comprehensive testing (4h)
TASK-314: Code review & security (2h)
TASK-315: Deployment (1h)
```

## Implementation Details

### Inventory Entity (TASK-301)
```csharp
public class Inventory
{
    public int Id { get; set; }
    public int ProductId { get; set; }
    public Product Product { get; set; }
    public int WarehouseId { get; set; }
    public Warehouse Warehouse { get; set; }
    public int Quantity { get; set; }
    public int ReorderLevel { get; set; }
    public int MaxStockLevel { get; set; }
    public DateTime LastUpdated { get; set; }
    public string LastUpdatedBy { get; set; }
    
    // Optimistic concurrency
    [Timestamp]
    public byte[] RowVersion { get; set; }
}

public class InventoryMovement
{
    public int Id { get; set; }
    public int InventoryId { get; set; }
    public Inventory Inventory { get; set; }
    public MovementType Type { get; set; }
    public int QuantityChange { get; set; }
    public int PreviousQuantity { get; set; }
    public int NewQuantity { get; set; }
    public string Reason { get; set; }
    public string PerformedBy { get; set; }
    public DateTime PerformedAt { get; set; }
}

public enum MovementType
{
    Adjustment,
    Purchase,
    Sale,
    Return,
    Transfer,
    Damage,
    Count
}
```

### Real-Time Sync Service (TASK-303)
```csharp
public class InventoryHub : Hub
{
    public async Task SubscribeToInventory(int warehouseId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, $"warehouse_{warehouseId}");
    }
    
    public async Task UnsubscribeFromInventory(int warehouseId)
    {
        await Groups.RemoveFromGroupAsync(Context.ConnectionId, $"warehouse_{warehouseId}");
    }
}

public class InventoryService
{
    private readonly IHubContext<InventoryHub> _hubContext;
    
    public async Task<Result> UpdateInventoryAsync(int inventoryId, int newQuantity, string reason)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        
        try
        {
            var inventory = await _context.Inventories
                .Include(i => i.Product)
                .Include(i => i.Warehouse)
                .FirstOrDefaultAsync(i => i.Id == inventoryId);
            
            if (inventory == null)
                return Result.Failure("Inventory not found");
            
            var previousQuantity = inventory.Quantity;
            var quantityChange = newQuantity - previousQuantity;
            
            // Update inventory with optimistic concurrency
            inventory.Quantity = newQuantity;
            inventory.LastUpdated = DateTime.UtcNow;
            inventory.LastUpdatedBy = _currentUser.Id;
            
            // Record movement
            var movement = new InventoryMovement
            {
                InventoryId = inventoryId,
                Type = MovementType.Adjustment,
                QuantityChange = quantityChange,
                PreviousQuantity = previousQuantity,
                NewQuantity = newQuantity,
                Reason = reason,
                PerformedBy = _currentUser.Id,
                PerformedAt = DateTime.UtcNow
            };
            
            _context.InventoryMovements.Add(movement);
            
            await _context.SaveChangesAsync();
            await transaction.CommitAsync();
            
            // Broadcast real-time update
            await _hubContext.Clients
                .Group($"warehouse_{inventory.WarehouseId}")
                .SendAsync("InventoryUpdated", new
                {
                    inventoryId,
                    productId = inventory.ProductId,
                    productName = inventory.Product.Name,
                    quantity = newQuantity,
                    previousQuantity,
                    timestamp = DateTime.UtcNow
                });
            
            // Check for low stock
            if (newQuantity <= inventory.ReorderLevel)
            {
                await TriggerLowStockAlert(inventory);
            }
            
            return Result.Success();
        }
        catch (DbUpdateConcurrencyException)
        {
            await transaction.RollbackAsync();
            return Result.Failure("Inventory was updated by another user. Please refresh and try again.");
        }
        catch (Exception ex)
        {
            await transaction.RollbackAsync();
            return Result.Failure($"Update failed: {ex.Message}");
        }
    }
}
```

### Low Stock Alert System (TASK-304)
```csharp
public class LowStockAlertService : IHostedService
{
    private Timer _timer;
    
    public Task StartAsync(CancellationToken cancellationToken)
    {
        _timer = new Timer(CheckLowStock, null, TimeSpan.Zero, TimeSpan.FromMinutes(15));
        return Task.CompletedTask;
    }
    
    private async void CheckLowStock(object state)
    {
        var lowStockItems = await _context.Inventories
            .Include(i => i.Product)
            .Include(i => i.Warehouse)
            .Where(i => i.Quantity <= i.ReorderLevel)
            .ToListAsync();
        
        foreach (var item in lowStockItems)
        {
            await SendLowStockNotification(item);
            
            // Check if we should auto-reorder
            if (item.Quantity < item.ReorderLevel * 0.5)
            {
                await _reorderService.CreateReorderRequest(item);
            }
        }
    }
    
    private async Task SendLowStockNotification(Inventory inventory)
    {
        var notification = new Notification
        {
            Type = NotificationType.LowStock,
            Severity = inventory.Quantity == 0 ? "Critical" : "Warning",
            Title = $"Low Stock: {inventory.Product.Name}",
            Message = $"Current quantity: {inventory.Quantity}, Reorder level: {inventory.ReorderLevel}",
            Data = new
            {
                inventoryId = inventory.Id,
                productId = inventory.ProductId,
                warehouseId = inventory.WarehouseId,
                quantity = inventory.Quantity
            }
        };
        
        await _notificationService.SendToWarehouseManagers(inventory.WarehouseId, notification);
    }
}
```

### Automated Reorder Service (TASK-305)
```csharp
public class AutomatedReorderService
{
    public async Task CreateReorderRequest(Inventory inventory)
    {
        // Calculate order quantity
        var orderQuantity = inventory.MaxStockLevel - inventory.Quantity;
        
        // Get preferred supplier
        var supplier = await GetPreferredSupplier(inventory.ProductId);
        
        if (supplier == null)
        {
            await NotifyManualReorderRequired(inventory);
            return;
        }
        
        var purchaseOrder = new PurchaseOrder
        {
            SupplierId = supplier.Id,
            Status = PurchaseOrderStatus.Draft,
            CreatedAt = DateTime.UtcNow,
            CreatedBy = "AutomatedSystem",
            Items = new List<PurchaseOrderItem>
            {
                new()
                {
                    ProductId = inventory.ProductId,
                    Quantity = orderQuantity,
                    UnitPrice = supplier.GetPrice(inventory.ProductId)
                }
            }
        };
        
        _context.PurchaseOrders.Add(purchaseOrder);
        await _context.SaveChangesAsync();
        
        // Notify purchasing department
        await _notificationService.NotifyPurchasingTeam(purchaseOrder);
    }
}
```

### Mobile Inventory Screen (TASK-306)
```kotlin
@Composable
fun InventoryListScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventory.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Low stock alert banner
        if (lowStockItems.isNotEmpty()) {
            LowStockBanner(
                count = lowStockItems.size,
                onClick = { viewModel.showLowStockItems() }
            )
        }
        
        // Search and filter
        SearchBar(
            query = viewModel.searchQuery.value,
            onQueryChange = { viewModel.search(it) }
        )
        
        Row(modifier = Modifier.padding(16.dp)) {
            FilterChip(
                selected = viewModel.filterLowStock.value,
                onClick = { viewModel.toggleLowStockFilter() },
                label = { Text("Low Stock") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = viewModel.filterOutOfStock.value,
                onClick = { viewModel.toggleOutOfStockFilter() },
                label = { Text("Out of Stock") }
            )
        }
        
        // Inventory list
        LazyColumn {
            items(inventoryItems) { item ->
                InventoryListItem(
                    item = item,
                    onAdjust = { viewModel.navigateToAdjustment(item.id) }
                )
            }
        }
    }
    
    // Real-time sync indicator
    SyncIndicator(
        isConnected = viewModel.isConnected.value,
        lastSyncTime = viewModel.lastSyncTime.value
    )
}

@Composable
fun InventoryListItem(
    item: InventoryItem,
    onAdjust: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "SKU: ${item.productSKU}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Quantity: ${item.quantity}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.quantity <= item.reorderLevel) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low stock",
                            tint = if (item.quantity == 0) Color.Red else Color.Yellow,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            IconButton(onClick = onAdjust) {
                Icon(Icons.Default.Edit, contentDescription = "Adjust")
            }
        }
    }
}
```

### Barcode Scanner (TASK-308)
```kotlin
@Composable
fun BarcodeScannerScreen(
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val scanResult by viewModel.scanResult.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                BarcodeAnalyzer { barcode ->
                                    viewModel.onBarcodeScanned(barcode)
                                }
                            )
                        }
                    
                    cameraProvider.bindToLifecycle(
                        context as LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                }, ContextCompat.getMainExecutor(context))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Scan result overlay
        scanResult?.let { result ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = result.productName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = "Current Stock: ${result.quantity}")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { viewModel.incrementStock() }) {
                            Text("+1")
                        }
                        Button(onClick = { viewModel.decrementStock() }) {
                            Text("-1")
                        }
                        Button(onClick = { viewModel.openAdjustmentDialog() }) {
                            Text("Adjust")
                        }
                    }
                }
            }
        }
    }
}
```

### Web Dashboard (TASK-310)
```tsx
export function InventoryDashboard() {
  const { data: overview } = useInventoryOverview();
  const { data: lowStockItems } = useLowStockItems();
  const { data: recentMovements } = useRecentMovements();
  
  return (
    <div className="p-6 space-y-6">
      {/* Overview cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <MetricCard
          title="Total Items"
          value={overview?.totalItems}
          icon={<Package className="w-6 h-6" />}
        />
        <MetricCard
          title="Total Value"
          value={`$${overview?.totalValue.toLocaleString()}`}
          icon={<DollarSign className="w-6 h-6" />}
        />
        <MetricCard
          title="Low Stock Items"
          value={overview?.lowStockCount}
          icon={<AlertTriangle className="w-6 h-6 text-yellow-500" />}
          alert
        />
        <MetricCard
          title="Out of Stock"
          value={overview?.outOfStockCount}
          icon={<XCircle className="w-6 h-6 text-red-500" />}
          alert
        />
      </div>
      
      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Inventory Value by Category</CardTitle>
          </CardHeader>
          <CardContent>
            <PieChart data={overview?.valueByCategory} />
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle>Stock Levels Trend</CardTitle>
          </CardHeader>
          <CardContent>
            <LineChart data={overview?.stockTrend} />
          </CardContent>
        </Card>
      </div>
      
      {/* Low stock alerts */}
      <Card>
        <CardHeader>
          <CardTitle>Low Stock Alerts</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Product</TableHead>
                <TableHead>SKU</TableHead>
                <TableHead>Current Stock</TableHead>
                <TableHead>Reorder Level</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {lowStockItems?.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.productName}</TableCell>
                  <TableCell>{item.sku}</TableCell>
                  <TableCell className="text-yellow-600 font-bold">
                    {item.quantity}
                  </TableCell>
                  <TableCell>{item.reorderLevel}</TableCell>
                  <TableCell>
                    <Button size="sm" onClick={() => createReorder(item)}>
                      Reorder
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
```

## Execution Timeline

| Phase | Tasks | Duration | Mode |
|-------|-------|----------|------|
| 1. Backend Core | TASK-301, 302 | 5h | Sequential |
| 2. Real-time & Alerts | TASK-303, 304, 305 | 7h | Parallel (3) |
| 3. Frontend | TASK-306-312 | 7h | Parallel (2 teams) |
| 4. Testing | TASK-313 | 4h | Parallel (3) |
| 5. Review & Deploy | TASK-314, 315 | 3h | Sequential |

**Total: 26h sequential → 15h parallel (42% faster)**

## Success Criteria

✅ **Real-time Sync**
- Latency: 0.8s (target: <1s)
- Connection stability: 99.8%

✅ **Accuracy**
- Concurrent update handling: 100%
- Data consistency: 100%

✅ **Performance**
- Bulk update (1000 items): 12s
- Dashboard load time: 1.2s

✅ **Quality**
- Test coverage: 89%
- No vulnerabilities
- All acceptance criteria met
