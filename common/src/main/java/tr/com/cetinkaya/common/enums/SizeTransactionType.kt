package tr.com.cetinkaya.common.enums

import java.lang.IllegalArgumentException

enum class SizeTransactionType(val value: Byte, val description: String) {
    Package(0, "Paket"),
    Order(9, "Sipariş"),
    StockTransaction(11, "Stok Hareket");

    companion object {
        fun from(value: Byte?) : SizeTransactionType =
            entries.firstOrNull{it.value == value} ?: throw IllegalArgumentException("Invalid value: $value")
    }
}