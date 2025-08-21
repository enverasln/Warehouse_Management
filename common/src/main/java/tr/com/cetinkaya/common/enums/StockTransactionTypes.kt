package tr.com.cetinkaya.common.enums

enum class StockTransactionTypes(val value: Byte, val description: String) {
    Input(0, "Giriş"),
    Output(1, "Çıkış"),
    WarehouseTransfer(2, "Depo Transfer")
}