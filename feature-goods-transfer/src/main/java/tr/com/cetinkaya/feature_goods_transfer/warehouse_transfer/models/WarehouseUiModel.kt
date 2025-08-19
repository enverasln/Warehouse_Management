package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models

import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel

data class WarehouseUiModel(
    val id: String,
    val number: Int,
    val name: String,
    val isActive: Boolean
) {

    override fun toString(): String {
        return name
    }
}

fun WarehouseDomainModel.toUiModel() = WarehouseUiModel(
    id = this.id,
    number = this.warehouseNumber,
    name = this.name,
    isActive = this.isActive
)
