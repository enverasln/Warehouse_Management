package tr.com.cetinkaya.data_local.models.order

import tr.com.cetinkaya.data_repository.models.order.GetProductByBarcodeDataModel

data class GetProductByBarcodeLocalModel(
    val barcode: String,
    val stockName: String,
    val qty: Double,
    val remainingQty: Double
)

fun GetProductByBarcodeLocalModel.toDataModel(): GetProductByBarcodeDataModel {
    return GetProductByBarcodeDataModel(
        barcode = this.barcode,
        stockName = this.stockName,
        qty = this.qty,
        remainingQty = this.remainingQty
    )
}