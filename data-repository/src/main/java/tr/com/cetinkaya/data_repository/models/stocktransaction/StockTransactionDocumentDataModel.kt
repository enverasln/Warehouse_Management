package tr.com.cetinkaya.data_repository.models.stocktransaction

import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel

data class StockTransactionDocumentDataModel(
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: Byte,
    val transactionKind: Byte,
    val isNormalOrReturn: Byte,
    val documentType: Byte
)

fun StockTransactionDocumentDataModel.toDomainModel() = StockTransactionDocumentDomainModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)

fun StockTransactionDocumentDomainModel.toDataModel() = StockTransactionDocumentDataModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)