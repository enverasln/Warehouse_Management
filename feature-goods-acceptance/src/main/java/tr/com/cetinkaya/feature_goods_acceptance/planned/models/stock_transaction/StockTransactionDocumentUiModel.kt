package tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction

import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel

data class StockTransactionDocumentUiModel(
    val documentDate: String,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: Byte,
    val transactionKind: Byte,
    val isNormalOrReturn: Byte,
    val documentType: Byte
)

fun StockTransactionDocumentUiModel.toDomainModel() = StockTransactionDocumentDomainModel(
    documentDate = DateConverter.uiToTimestamp(documentDate) ?: 0,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)

fun StockTransactionDocumentDomainModel.toUiModel() = StockTransactionDocumentUiModel(
    documentDate = DateConverter.timestampToUi(documentDate),
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    paperNumber = paperNumber,
    transactionType = transactionType,
    transactionKind = transactionKind,
    isNormalOrReturn = isNormalOrReturn,
    documentType = documentType
)

