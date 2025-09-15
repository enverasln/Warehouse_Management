package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models

import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel


data class StockTransactionOldUiModel(
    val id: String,
    val barcode: String,
    val stockName: String,
    val quantity: Double
)

fun StockTransactionDomainModel.toOldUiModel() = StockTransactionOldUiModel(
    id = id,
    barcode = barcode,
    stockName = stockName,
    quantity = quantity
)