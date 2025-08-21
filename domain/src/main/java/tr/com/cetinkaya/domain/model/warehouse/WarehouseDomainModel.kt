package tr.com.cetinkaya.domain.model.warehouse

data class WarehouseDomainModel(
    val id: String,
    val warehouseNumber: Int,
    val name: String,
    val isActive: Boolean
)