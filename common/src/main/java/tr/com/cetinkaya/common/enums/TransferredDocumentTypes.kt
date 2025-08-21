package tr.com.cetinkaya.common.enums

enum class TransferredDocumentTypes(val value: Byte, val description: String) {
    NormalPurchaseDispatch(0, "Normal alış irsaliye"),
    ReturnPurchaseDispatch(1, "İade alış irsaliye"),
    NormalSalesDispatch(2, "Normal satış irsaliye"),
    ReturnSalesDispatch(3, "İade satış irsaliye"),
    WarehouseShipmentDocument(4, "Depo sevk evrağı"),
    NormalGivenOrder(5, "Normal verilen sipariş"),
    StandardReceivedOrder(6, "Normal alınan sipariş"),
    BranchOrder(7, "Şube siparişi"),
    ProformaOrder(8, "Proforma sipariş");
}
