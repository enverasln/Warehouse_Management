package tr.com.cetinkaya.common.enums

enum class TransferredDocumentTypes(value: Byte) {
    REGULAR_PURCHASE_ORDER(0),
    RETURN_PURCHASE_ORDER(1),
    REGULAR_SALES_ORDER(2),
    RETURN_SALES_ORDER(3),
    WAREHOUSE_TRANSFER(4),
    SALES_INVOICE(5)
}
