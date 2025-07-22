package tr.com.cetinkaya.domain.model.stok_transaction

data class StockTransactionDocumentDomainModel (
    val documentDate: Long,
    val documentSeries: String,
    val documentNumber: Int,
    val paperNumber: String,
    val transactionType: Byte,
    val transactionKind: Byte,
    val isNormalOrReturn: Byte,
    val documentType: Byte
)