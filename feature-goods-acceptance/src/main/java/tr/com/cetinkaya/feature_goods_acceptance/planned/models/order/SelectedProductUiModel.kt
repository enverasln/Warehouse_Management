package tr.com.cetinkaya.feature_goods_acceptance.planned.models.order

data class SelectedProductUiModel(
    val barcode: String,
    val companyName: String,
    val stockName: String,
    val totalQuantity: Double,
    val isSingleQuantity: Boolean
)