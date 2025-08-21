package tr.com.cetinkaya.data_remote.models.warehouse

import tr.com.cetinkaya.data_repository.models.warehouse.WarehouseDataModel
import java.util.Date

data class GetWarehousesResponseModel(
    val id: String, val warehouseNumber: Int, val name: String, val isActive: Boolean, val lockDate: Date
)

fun GetWarehousesResponseModel.toDataModel() = WarehouseDataModel(
    id = this.id, warehouseNumber = this.warehouseNumber, name = this.name, isActive = this.isActive, lockDate = lockDate.time
)