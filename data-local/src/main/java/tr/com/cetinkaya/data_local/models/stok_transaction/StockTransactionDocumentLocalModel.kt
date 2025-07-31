package tr.com.cetinkaya.data_local.models.stok_transaction

import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel

data class StockTransactionDocumentLocalModel(
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionTypes,
    val transactionKind: Byte,
    val isNormalOrReturn: Byte,
    val documentType: Byte
)

fun StockTransactionDocumentLocalModel.toDataModel() = StockTransactionDocumentDataModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)

fun StockTransactionDocumentDataModel.toLocalModel() = StockTransactionDocumentLocalModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)