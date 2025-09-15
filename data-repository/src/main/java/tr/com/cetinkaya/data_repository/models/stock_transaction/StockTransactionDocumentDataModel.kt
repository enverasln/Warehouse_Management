package tr.com.cetinkaya.data_repository.models.stock_transaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel

data class StockTransactionDocumentDataModel(
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType
)

fun StockTransactionDocumentDataModel.toDomainModel() = StockTransactionDocumentDomainModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    transactionDocumentType = transactionDocumentType
)

fun StockTransactionDocumentDomainModel.toDataModel() = StockTransactionDocumentDataModel(
    documentDate = documentDate,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    transactionDocumentType = transactionDocumentType
)