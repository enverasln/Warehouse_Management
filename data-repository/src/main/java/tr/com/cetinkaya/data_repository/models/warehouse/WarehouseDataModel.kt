package tr.com.cetinkaya.data_repository.models.warehouse

import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel

data class WarehouseDataModel(
    val id: String,
    val code: Int,
    val name: String,
    val isActive: Boolean
)


fun WarehouseDataModel.toDomainModel() = WarehouseDomainModel(
    id = this.id,
    number = this.code,
    name = this.name,
    isActive = this.isActive
)