package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models

data class AddOrderTransactionParams(
    val barcode: String,
    val stockName: String,
    val totalQty: Double,
    val totalRemainingQty: Double,
    val deliveredQty: Double
)