package tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number

data class CheckDocumentSeriesAndNumberResponseRemoteModel (
    val message: String, val isDocumentNew: Boolean,
    val isUsed: Boolean?,
    val canBecause: Boolean?
)