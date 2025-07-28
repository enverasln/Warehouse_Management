package tr.com.cetinkaya.data_remote.models.warehouse

import tr.com.cetinkaya.data_repository.models.warehouse.WarehouseDataModel

data class GetWarehousesResponseModel(
    val id: String, val code: Int, val name: String, val isActive: Boolean
)

fun GetWarehousesResponseModel.toDataModel() = WarehouseDataModel(
    id = this.id, code = this.code, name = this.name, isActive = this.isActive
)