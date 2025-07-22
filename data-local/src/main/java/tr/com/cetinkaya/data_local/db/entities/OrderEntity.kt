package tr.com.cetinkaya.data_local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import java.util.Date

@Entity(tableName = "orders", primaryKeys = ["id", "barcode"])
data class OrderEntity(
    val id: String,
    val orderDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val documentRowNumber: Int,
    val stockId: String,
    val stockCode: String,
    val stockName: String,
    val barcode: String,
    val companyId: String,
    val companyCode: String,
    val companyName: String,
    val paymentPlanNumber: Int = 0,
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
    @ColumnInfo(defaultValue = "0.0")
    val deliveredQuantity: Double = 0.0,
    val isColoredAndSized: Boolean,
    @ColumnInfo(defaultValue = "Sunucudan Gelen")
    val synchronizationStatus: String = "Sunucudan Gelen"
)

fun OrderEntity.toDataModel(): ProductDataModel {
    return ProductDataModel(
        id = this.id,
        orderDate = Date(this.orderDate),
        documentSeries = this.documentSeries,
        documentNumber = this.documentNumber,
        lineNumber = this.documentRowNumber,
        stockId = this.stockId,
        stockCode = this.stockCode,
        stockName = this.stockName,
        barcode = this.barcode,
        companyId = this.companyId,
        companyCode = this.companyCode,
        companyName = this.companyName,
        paymentPlanNumber = this.paymentPlanNumber,
        warehouseId = this.warehouseId,
        warehouseNumber = this.warehouseNumber,
        warehouseName = this.warehouseName,
        quantity = this.quantity,
        currencyType = this.currencyType,
        discount1 = this.discount1,
        discount2 = this.discount2,
        discount3 = this.discount3,
        discount4 = this.discount4,
        discount5 = this.discount5,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
        vatPointer = this.taxPointer,
        currentResponsibilityCenter = this.currentResponsibilityCenter,
        stockResponsibilityCenter = this.stockResponsibilityCenter,
        remainingQuantity = this.remainingQuantity,
        deliveredQuantity = this.deliveredQuantity,
        isColoredAndSized = this.isColoredAndSized,
        synchronizationStatus = this.synchronizationStatus
    )
}

fun ProductDataModel.toEntity(): OrderEntity {
    return OrderEntity(
        id = this.id,
        orderDate = this.orderDate.time,
        documentSeries = this.documentSeries,
        documentNumber = this.documentNumber,
        documentRowNumber = this.lineNumber,
        stockId = this.stockId,
        stockCode = this.stockCode,
        stockName = this.stockName,
        barcode = this.barcode,
        companyId = this.companyId,
        companyCode = this.companyCode,
        companyName = this.companyName,
        paymentPlanNumber = this.paymentPlanNumber,
        warehouseId = this.warehouseId,
        warehouseNumber = this.warehouseNumber,
        warehouseName = this.warehouseName,
        quantity = this.quantity,
        currencyType = this.currencyType,
        discount1 = this.discount1,
        discount2 = this.discount2,
        discount3 = this.discount3,
        discount4 = this.discount4,
        discount5 = this.discount5,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
        taxPointer = this.vatPointer,
        currentResponsibilityCenter = this.currentResponsibilityCenter,
        stockResponsibilityCenter = this.stockResponsibilityCenter,
        remainingQuantity = this.remainingQuantity,
        deliveredQuantity = this.deliveredQuantity,
        isColoredAndSized = this.isColoredAndSized,
        synchronizationStatus = this.synchronizationStatus
    )
}
