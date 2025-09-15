package tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number

import tr.com.cetinkaya.data_repository.models.order.CheckStockTxDocDataModel

data class CheckStockTxDocResponseModel(
    val message: String,
    val isDocumentNew: Boolean,
    val isUsed: Boolean?,
    val canBeUsed: Boolean?,
)

fun CheckStockTxDocResponseModel.toDataModel() = CheckStockTxDocDataModel(
    message = message,
    isDocumentNew = isDocumentNew,
    isUsed = isUsed,
    canBeUsed = canBeUsed,
)