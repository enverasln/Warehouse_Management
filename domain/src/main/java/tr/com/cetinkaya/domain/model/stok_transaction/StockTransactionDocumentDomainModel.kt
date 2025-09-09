package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType

data class StockTransactionDocumentDomainModel (
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType
)