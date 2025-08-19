package tr.com.cetinkaya.data_repository.models.warehouse

import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel

data class WarehouseDataModel(
    val id: String,
    val warehouseNumber: Int,
    val name: String,
    val isActive: Boolean,
    val lockDate: Long?
)


fun WarehouseDataModel.toDomainModel() = WarehouseDomainModel(
    id = this.id,
    warehouseNumber = this.warehouseNumber,
    name = this.name,
    isActive = this.isActive
)