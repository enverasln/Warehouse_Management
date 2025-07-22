package tr.com.cetinkaya.data_repository.models.order

import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel

data class GetProductByBarcodeDataModel(
    val barcode: String,
    val stockName: String,
    val qty: Double,
    val remainingQty: Double
)

fun GetProductByBarcodeDataModel.toDomainModel(): GetProductByBarcodeDomainModel {
    return GetProductByBarcodeDomainModel(
        barcode = this.barcode,
        stockName = this.stockName,
        qty = this.qty,
        remainingQty = this.remainingQty
    )
}