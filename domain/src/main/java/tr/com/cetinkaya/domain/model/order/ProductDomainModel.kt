package tr.com.cetinkaya.domain.model.order

data class ProductDomainModel (
    val barcode: String,
    val stockName: String,
    val quantity: Double,
    val remainingQuantity: Double,
    val deliveredQuantity: Double
)



