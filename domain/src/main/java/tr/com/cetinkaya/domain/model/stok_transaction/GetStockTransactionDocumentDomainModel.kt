package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType

data class GetStockTransactionDocumentDomainModel(
    val documentSeries: String,
    val documentNumber: Int,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val documentType: StockTransactionDocumentType,
    val invoiceId: String,
    val paperNumber: String,
    val currentCode: String,
    val currentName: String
)