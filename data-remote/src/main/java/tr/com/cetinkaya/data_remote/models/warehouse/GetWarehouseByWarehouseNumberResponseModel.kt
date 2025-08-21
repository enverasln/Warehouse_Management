package tr.com.cetinkaya.data_remote.models.warehouse

import tr.com.cetinkaya.data_repository.models.warehouse.WarehouseDataModel
import java.util.Date

data class GetWarehouseByWarehouseNumberResponseModel(
    val id: String, val code: Int, val name: String, val isActive: Boolean, val lockDate: Date
)


fun GetWarehouseByWarehouseNumberResponseModel.toDataModel() = WarehouseDataModel(
    id = id,
    warehouseNumber = code,
    name = name,
    isActive = isActive,
    lockDate = lockDate.time
)