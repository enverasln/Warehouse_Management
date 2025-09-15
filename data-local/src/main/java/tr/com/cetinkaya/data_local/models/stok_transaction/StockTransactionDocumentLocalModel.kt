package tr.com.cetinkaya.data_local.models.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDocumentDataModel

data class StockTransactionDocumentLocalModel(
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val documentType: StockTransactionDocumentType
)

fun StockTransactionDocumentLocalModel.toDataModel() = StockTransactionDocumentDataModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    transactionDocumentType = documentType
)

fun StockTransactionDocumentDataModel.toLocalModel() = StockTransactionDocumentLocalModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = transactionDocumentType
)