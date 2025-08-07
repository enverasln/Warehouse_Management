package tr.com.cetinkaya.common.enums

enum class OrderTransactionTypes(val value: Byte, val description: String) {
    Demand(0, "Talep"),
    Supply(1, "Temin")
}