package tr.com.cetinkaya.data_local.models.stok_transaction

import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionsByDocumentDataModel

data class GetStockTransactionsByDocumentLocalModel(
    val id: String, val barcode: String, val quantity: Double, val deliveredQuantity: Double, val stockName: String
)

fun GetStockTransactionsByDocumentLocalModel.toDataModel() = GetStockTransactionsByDocumentDataModel(
    id = id, barcode = barcode, quantity = quantity, deliveredQuantity = deliveredQuantity, stockName = stockName
)