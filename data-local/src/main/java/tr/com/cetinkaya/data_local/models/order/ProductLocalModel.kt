package tr.com.cetinkaya.data_local.models.order

import tr.com.cetinkaya.data_repository.models.order.ProductDataModel

data class ProductLocalModel (
    val orderDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val documentRowNumber: Int,
    val stockCode: String,
    val companyCode: String,
    val quantity: Double,
    val inputWarehouse: Int,
    val outputWarehouse: Int,
    val salesman: String,
    val responsibilityCenter: String,
    val userCode: Int,
    val total: Double,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val taxPointer: Byte,
    val unitPrice: Double,
    val paperNumber: String,
    val companyNumber: Int,
    val storeNumber: Int,
    val barcode: String,
    val recordId: String,
    val isColoredAndSized: Boolean
)

fun ProductDataModel.toLocalModel () : ProductLocalModel {
    return ProductLocalModel(
        orderDate = this.orderDate.time,
        documentSeries = this.documentSeries,
        documentNumber = this.documentNumber,
        documentRowNumber = this.lineNumber,
        stockCode = this.stockCode,
        companyCode = this.companyCode,
        quantity = this.quantity,
        inputWarehouse = this.warehouseNumber,
        outputWarehouse = this.warehouseNumber,
        salesman = "",
        responsibilityCenter = "",
        userCode = 0,
        total = this.totalPrice,
        discount1 = this.discount1,
        discount2 = this.discount2,
        discount3 = this.discount3,
        discount4 = this.discount4,
        discount5 = this.discount5,
        taxPointer = this.vatPointer,
        unitPrice = this.unitPrice,
        paperNumber = "",
        companyNumber = 0,
        storeNumber = 0,
        barcode = this.barcode,
        recordId = this.id,
        isColoredAndSized = this.isColoredAndSized
    )
}

