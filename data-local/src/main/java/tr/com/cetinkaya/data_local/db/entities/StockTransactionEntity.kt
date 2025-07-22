package tr.com.cetinkaya.data_local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import java.util.Date

@Entity(
    tableName = "stock_transactions",
    primaryKeys = ["id", "barcode"],
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id", "barcode"],
            childColumns = ["orderId", "barcode"],
            onDelete = ForeignKey.NO_ACTION,
        )
    ],
    indices = [Index(value = ["orderId", "barcode"])]
)
data class StockTransactionEntity(
    val id: String,
    val transactionType: Byte,
    val transactionKind: Byte,
    val isNormalOrReturn: Byte,
    val documentType: Byte,
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val lineNumber: Long,
    val stockCode: String,
    val companyCode: String,
    val quantity: Double,
    val inputWarehouseNumber: Int,
    val outputWarehouseNumber: Int,
    val paymentPlanNumber: Int,
    val salesman: String,
    val responsibilityCenter: String,
    val userCode: Int,
    val totalPrice: Double,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val taxPointer: Byte,
    val orderId: String,
    val price: Double,
    val paperNumber: String,
    val companyNumber: Int,
    val storeNumber: Int,
    val barcode: String,
    @ColumnInfo(defaultValue = "0")
    val isColoredAndSized: Boolean = false,
    val transportationStatus: Byte,
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    @ColumnInfo(defaultValue = "Aktarılacak")
    val synchronizationStatus: String = "Aktarılacak"
)

fun StockTransactionEntity.toDataModel() : StockTransactionDataModel {
    return StockTransactionDataModel(
        id = id,
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentDate = documentDate,
        documentSeries = documentSeries,
        documentNumber = documentNumber,
        lineNumber = lineNumber,
        stockCode = stockCode,
        companyCode = companyCode,
        quantity = quantity,
        inputWarehouseNumber = inputWarehouseNumber,
        outputWarehouseNumber = outputWarehouseNumber,
        paymentPlanNumber = paymentPlanNumber,
        salesman = salesman,
        responsibilityCenter = responsibilityCenter,
        userCode = userCode,
        totalPrice = totalPrice,
        discount1 = discount1,
        discount2 = discount2,
        discount3 = discount3,
        discount4 = discount4,
        discount5 = discount5,
        taxPointer = taxPointer,
        orderId = orderId,
        price = price,
        paperNumber = paperNumber,
        companyNumber = companyNumber,
        storeNumber = storeNumber,
        barcode = barcode,
        isColoredAndSized = isColoredAndSized,
        transportationStatus = transportationStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        synchronizationStatus = synchronizationStatus
    )
}

fun StockTransactionDataModel.toEntity(): StockTransactionEntity {
    return StockTransactionEntity(
        id = id,
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentDate = documentDate,
        documentSeries = documentSeries,
        documentNumber = documentNumber,
        lineNumber = lineNumber,
        stockCode = stockCode,
        companyCode = companyCode,
        quantity = quantity,
        inputWarehouseNumber = inputWarehouseNumber,
        outputWarehouseNumber = outputWarehouseNumber,
        paymentPlanNumber = paymentPlanNumber,
        salesman = salesman,
        responsibilityCenter = responsibilityCenter,
        userCode = userCode,
        totalPrice = totalPrice,
        discount1 = discount1,
        discount2 = discount2,
        discount3 = discount3,
        discount4 = discount4,
        discount5 = discount5,
        taxPointer = taxPointer,
        orderId = orderId,
        price = price,
        paperNumber = paperNumber,
        companyNumber = companyNumber,
        storeNumber = storeNumber,
        barcode = barcode,
        isColoredAndSized = isColoredAndSized,
        transportationStatus = transportationStatus,
        synchronizationStatus = synchronizationStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
