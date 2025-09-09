package tr.com.cetinkaya.common.enums

enum class OrderTransactionTypes(val value: Byte, val description: String) {
    Demand(0, "Talep"),
    Supply(1, "Temin");

    companion object {
        fun from(value: Byte?): OrderTransactionTypes =
            entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}