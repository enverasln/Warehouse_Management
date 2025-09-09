package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel

data class StockTransactionDocumentUiModel(
    val documentDate: String,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType
)

fun StockTransactionDocumentUiModel.toDomainModel() = StockTransactionDocumentDomainModel(
    documentDate = DateConverter.uiToTimestamp(documentDate),
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    transactionDocumentType = transactionDocumentType
)

fun StockTransactionDocumentDomainModel.toUiModel() = StockTransactionDocumentUiModel(
    documentDate = DateConverter.timestampToUi(documentDate),
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    transactionDocumentType = transactionDocumentType
)
