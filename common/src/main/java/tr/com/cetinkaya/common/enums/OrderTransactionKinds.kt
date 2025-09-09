package tr.com.cetinkaya.common.enums

enum class OrderTransactionKinds(val value: Byte, val description: String) {
    NormalOrder(0, "Normal Sipariş"),
    ConsignmentOrder(1, "Konsinye Sipariş"),
    ProformaOrder(2, "Proforma Sipariş"),
    ForeignTradeOrder(3, "Dış Ticaret Siparişi"),
    ContractManufacturingOrder(4, "Fason Siparişi"),
    InternalConsumptionOrder(5, "Dahili Sarf Siparişi"),
    InterWarehouseOrder(6, "Depolar Arası Sipariş"),
    PurchaseRequest(7, "Satın Alma Talebi"),
    ProductionRequest(8, "Üretim Talebi"),
    WorkOrders(9, "İş Emirleri");

    companion object {
        fun from(value: Byte?): OrderTransactionKinds =
            entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}