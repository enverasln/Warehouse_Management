package tr.com.cetinkaya.common.enums

enum class StockTransactionTypes(val value: Byte) {
    Input(0),
    Output(1),
    WarehouseTransfer(2)
}