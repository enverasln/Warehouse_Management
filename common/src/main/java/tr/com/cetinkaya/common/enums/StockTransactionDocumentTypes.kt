package tr.com.cetinkaya.common.enums

enum class StockTransactionDocumentTypes(val value: Byte, val description: String) {
    WarehouseDispatchNote(0, "Depo Çıkış Fişi"),
    ExitDispatchNote(1, "Çıkış İrsaliyesi"),
    WarehouseTransferNote(2, "Depo Transfer Fişi"),
    EntryInvoice(3, "Giriş Faturası"),
    ExitInvoice(4, "Çıkış Faturası"),
    ImportCostReflectionReceipt(5, "Stoklara İthalat Masraf Yansıtma Dekontu"),
    StockTransferNote(6, "Stok Virman Fişi"),
    ProductionNote(7, "Üretim Fişi"),
    AdditionalInflationCostNote(8, "İlave Enflasyon Maliyet Fişi"),
    AdditionalCostDistributionNote(9, "Stoklara İlave Maliyet Yedirme Fişi"),
    CustomsClearanceNote(10, "Antrepolardan Mal Millileştirme Fişi"),
    WarehouseToWarehouseTransferNote(11, "Antrepolar Arası Transfer Fişi"),
    WarehouseEntryNote(12, "Depo Giriş Fişi"),
    EntryDispatchNote(13, "Giriş İrsaliyesi"),
    SubcontractorInOutNote(14, "Fason Giriş Çıkış Fişi"),
    InterWarehouseSalesNote(15, "Depolar Arası Satış Fişi"),
    ExpenseReceiptNote(16, "Stok Gider Pusulası Fişi"),
    InterWarehouseShippingNote(17, "Depolar Arası Nakliye Fişi")
}