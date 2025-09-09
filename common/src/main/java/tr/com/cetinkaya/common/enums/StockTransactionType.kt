package tr.com.cetinkaya.common.enums

enum class StockTransactionType(val value: Byte, val description: String) {
    Input(0, "Giriş"),
    Output(1, "Çıkış"),
    WarehouseTransfer(2, "Depo Transfer");

    companion object {
        fun from(value: Byte?): StockTransactionType =
            entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}