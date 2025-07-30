package tr.com.cetinkaya.domain.model.warehouse

data class WarehouseDomainModel(
    val id: String,
    val number: Int,
    val name: String,
    val isActive: Boolean
)