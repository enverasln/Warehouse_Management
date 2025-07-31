package tr.com.cetinkaya.domain.model.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes

data class StockTransactionDocumentDomainModel (
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionTypes,
    val transactionKind: StockTransactionKinds,
    val isNormalOrReturn: Byte,
    val documentType: StockTransactionDocumentTypes
)