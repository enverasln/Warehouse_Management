package tr.com.cetinkaya.data_repository.models.stocktransaction

import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel

data class GetStockTransactionsByDocumentDataModel(
    val id: String, val barcode: String, val quantity: Double, val deliveredQuantity: Double, val stockName: String
)

fun GetStockTransactionsByDocumentDataModel.toDomain() = GetStockTransactionsByDocumentDomainModel(
    id = id, barcode = barcode, quantity = quantity, deliveredQuantity = deliveredQuantity, stockName = stockName
)