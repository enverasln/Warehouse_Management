package tr.com.cetinkaya.data_repository.models.order

import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.order.ProductDomainModel
import java.util.Date
import java.util.UUID

data class ProductDataModel(
    val id: String,
    val orderDate: Date,
    val documentSeries: String,
    val documentNumber: Int,
    val lineNumber: Int,
    val stockId: String,
    val stockCode: String,
    val stockName: String,
    val barcode: String,
    val companyId: String,
    val companyCode: String,
    val companyName: String,
    val paymentPlanNumber: Int,
    val warehouseId: String,
    val warehouseNumber: Int,
    val warehouseName: String,
    val quantity: Double,
    val currencyType: Byte,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val vatPointer: Byte,
    val currentResponsibilityCenter: String,
    val stockResponsibilityCenter: String,
    val remainingQuantity: Double,
    val deliveredQuantity: Double,
    val isColoredAndSized: Boolean,
    val synchronizationStatus: String,
)

fun ProductDataModel.toDomainModel(): ProductDomainModel {
    return ProductDomainModel(
        barcode = this.barcode,
        stockName = this.stockName,
        quantity = this.quantity,
        remainingQuantity = this.remainingQuantity,
        deliveredQuantity = this.deliveredQuantity
    )
}

fun ProductDataModel.toStockTransactionDataModel(
    stockTransactionDocument: StockTransactionDocumentDataModel,
    lineNumber: Long,
    quantity: Double,
    userCode: Int,
    barcode: String,
    synchronizationStatus: String
): StockTransactionDataModel {
    val safeQty = if (this.quantity == 0.0) 1.0 else this.quantity
    val now = Date().time
    return StockTransactionDataModel(
        id = UUID.randomUUID().toString(),
        transactionType = stockTransactionDocument.transactionType,
        transactionKind = stockTransactionDocument.transactionKind,
        isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
        documentType = stockTransactionDocument.documentType,
        documentDate = stockTransactionDocument.documentDate,
        documentSeries = stockTransactionDocument.documentSeries,
        documentNumber = stockTransactionDocument.documentNumber,
        lineNumber = lineNumber,
        stockCode = this.stockCode,
        companyCode = this.companyCode,
        quantity = quantity,
        inputWarehouseNumber = this.warehouseNumber,
        outputWarehouseNumber = this.warehouseNumber,
        paymentPlanNumber = this.paymentPlanNumber,
        salesman = "El Terminali",
        responsibilityCenter = this.stockResponsibilityCenter,
        userCode = userCode,
        totalPrice = this.unitPrice * quantity,
        discount1 = (this.discount1 / safeQty) * quantity,
        discount2 = (this.discount2 / safeQty) * quantity,
        discount3 = (this.discount3 / safeQty) * quantity,
        discount4 = (this.discount4 / safeQty) * quantity,
        discount5 = (this.discount5 / safeQty) * quantity,
        taxPointer = this.vatPointer,
        orderId = this.id,
        price = this.unitPrice,
        paperNumber = stockTransactionDocument.paperNumber,
        companyNumber = 0,
        storeNumber = 0,
        barcode = barcode,
        transportationStatus = 0,
        createdAt = now,
        updatedAt = now,
        isColoredAndSized = this.isColoredAndSized,
        synchronizationStatus = synchronizationStatus

    )
}

fun ProductDataModel.toOrderDomainModel(userId: Int): OrderDomainModel = OrderDomainModel (
    id = this.id,
    orderDate = DateConverter.timeStampToApi(this.orderDate.time),
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    lineNumber = this.lineNumber,
    stockCode = this.stockCode,
    currentCode = this.companyCode,
    quantity = this.quantity,
    inputWarehouseNumber = this.warehouseNumber,
    outputWarehouseNumber = this.warehouseNumber,
    salesman = "El Terminali",
    responsibilityCenter = this.stockResponsibilityCenter,
    userCode = userId,
    totalPrice = this.totalPrice,
    discount1 = this.discount1,
    discount2 = this.discount2,
    discount3 = this.discount3,
    discount4 = this.discount4,
    discount5 = this.discount5,
    vatPointer = this.vatPointer,
    orderId = this.id,
    price = this.unitPrice,
    paperNumber = "",
    companyNumber = 0,
    storeNumber = 0,
    barcode = this.barcode,
    isColoredAndSized = this.isColoredAndSized,
)