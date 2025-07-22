package tr.com.cetinkaya.feature_goods_acceptance.planned.models.order

import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel

data class GetProductByBarcodeUiModel(
    val barcode: String,
    val stockName: String,
    val qty: Double,
    val remainingQty: Double
)

fun GetProductByBarcodeDomainModel.toUiModel() = GetProductByBarcodeUiModel(
    barcode = barcode,
    stockName = stockName,
    qty = qty,
    remainingQty = remainingQty
)