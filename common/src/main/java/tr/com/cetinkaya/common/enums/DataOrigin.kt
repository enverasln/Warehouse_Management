package tr.com.cetinkaya.common.enums

enum class DataOrigin(val value: Byte, val description: String) {
    Local(0, "Yerel"),
    Server(0, "Sunucudan Gelen");

    companion object {
        fun from(value: Byte): DataOrigin =
            DataOrigin.entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}
