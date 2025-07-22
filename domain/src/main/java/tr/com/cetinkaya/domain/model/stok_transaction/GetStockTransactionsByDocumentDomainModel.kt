package tr.com.cetinkaya.domain.model.stok_transaction

data class GetStockTransactionsByDocumentDomainModel(
    val id: String,
    val barcode: String,
    val quantity: Double,
    val deliveredQuantity: Double,
    val stockName: String
)