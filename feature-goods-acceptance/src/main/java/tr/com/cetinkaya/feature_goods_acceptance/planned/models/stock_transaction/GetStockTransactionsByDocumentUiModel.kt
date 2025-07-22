package tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction

import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel

data class GetStockTransactionsByDocumentUiModel(
    val id: String, val barcode: String, val quantity: Double, val deliveredQuantity: Double, val stockName: String
)

fun GetStockTransactionsByDocumentDomainModel.toUiModel() = GetStockTransactionsByDocumentUiModel(
    id = id, barcode = barcode, quantity = quantity, deliveredQuantity = deliveredQuantity, stockName = stockName
)