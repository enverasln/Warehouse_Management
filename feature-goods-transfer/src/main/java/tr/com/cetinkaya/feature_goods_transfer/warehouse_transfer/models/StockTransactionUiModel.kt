package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models

import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel

data class StockTransactionUiModel(
    val id: String,
    val barcode: String,
    val stockName: String,
    val quantity: Double
)

fun StockTransactionDomainModel.toUiModel() = StockTransactionUiModel(
    id = id,
    barcode = barcode,
    stockName = stockName,
    quantity = quantity
)