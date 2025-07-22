package tr.com.cetinkaya.feature_goods_acceptance.planned.models.order

import tr.com.cetinkaya.domain.model.order.ProductDomainModel

data class ProductUiModel(
    val barcode: String, val stockName: String, val quantity: Double, val remainingQuantity: Double, val deliveredQuantity: Double
)

fun ProductDomainModel.toUiModel(): ProductUiModel {
    return ProductUiModel(
        barcode = this.barcode,
        stockName = this.stockName,
        quantity = this.quantity,
        remainingQuantity = this.remainingQuantity,
        deliveredQuantity = this.deliveredQuantity
    )
}