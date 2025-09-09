package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType

data class AddStockTransactionDomainModel(
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType,
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val lineNumber: Long,
    val stockCode: String,
    val stockName: String,
    val currentCode: String,
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
)