package tr.com.cetinkaya.domain.model.order

data class GetProductByBarcodeDomainModel(
    val barcode: String,
    val stockName: String,
    val qty: Double,
    val remainingQty: Double
)