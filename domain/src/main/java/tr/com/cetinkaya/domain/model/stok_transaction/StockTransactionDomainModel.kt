package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes

data class StockTransactionDomainModel(
    val id: String,
    val transactionType: StockTransactionTypes,
    val transactionKind: StockTransactionKinds,
    val isNormalOrReturn: Byte,
    val documentType: StockTransactionDocumentTypes,
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val lineNumber: Long,
    val stockCode: String,
    val stockName: String,
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
    val orderId: String?,
    val price: Double,
    val paperNumber: String,
    val companyNumber: Int,
    val storeNumber: Int,
    val barcode: String,
    val isColoredAndSized: Boolean,
    val transportationStatus: Byte,
    val createdAt: Long,
    val updatedAt: Long,
    val synchronizationStatus: String
)

