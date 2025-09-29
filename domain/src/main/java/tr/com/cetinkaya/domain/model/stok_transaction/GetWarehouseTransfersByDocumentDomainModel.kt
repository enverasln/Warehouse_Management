package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType

data class GetWarehouseTransfersByDocumentDomainModel(
    val refRecordId: String,
    val assortmentBarcode: String,
    val stockBarcode: String,
    val sizeTransactionType: SizeTransactionType?,
    val quantity: Double,
    val stockCode: String,
    val stockName: String,
    val documentSeries: String,
    val documentNumber: Int,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType
)