package tr.com.cetinkaya.domain.model.order_transaction

import tr.com.cetinkaya.common.enums.DataOrigin
import tr.com.cetinkaya.common.enums.SyncStatus

data class AddOrderTransactionDomainModel(
    val orderDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val rowNumber: Int,
    val stockId: String,
    val stockCode: String,
    val stockName: String,
    val barcode: String,
    val currentId: String,
    val currentCode: String,
    val currentName: String,
    val paymentPlanNumber: Int,
    val warehouseId: String,
    val warehouseNumber: Int,
    val warehouseName: String,
    val quantity: Double,
    val unitPrice: Double,
    val currencyType: Byte,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val totalPrice: Double,
    val taxPointer: Byte,
    val currentResponsibilityCenter: String,
    val stockResponsibilityCenter: String,
    val remainingQuantity: Double,
    val deliveredQuantity: Double,
    val isColoredAndSized: Boolean,
    val syncStatus: SyncStatus,
    val dataOrigin: DataOrigin,
    val userCode: Int
)
