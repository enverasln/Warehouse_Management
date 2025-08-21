package tr.com.cetinkaya.common.enums

enum class StockTransactionKinds(val value: Byte, val description: String) {
    Wholesale(0, "Toptan"),
    Retail(1, "Perakende"),
    ForeignTrade(2, "Dış Ticaret"),
    StockTransfer(3, "Stok Virman"),
    Waste(4, "Fire"),
    Consumption(5, "Sarf"),
    InternalTransfer(6, "Transfer"),
    Production(7, "Üretim"),
    ContractManufacturing(8, "Fason"),
    ValueDifference(9, "Değer Farkı"),
    InventoryCount(10, "Sayım"),
    StockOpening(11, "Stok Açılış"),
    ImportExport(12, "İthalat-İhracat"),
    Market(13, "Hal"),
    Producer(14, "Müstahsil"),
    ProducerValueDifference(15, "Müstahsil Değer Farkı"),
    Wholesaler(16, "Kabzımal"),
    ExpenseReceipt(17, "Gider Pusulası")
}