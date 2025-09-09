package tr.com.cetinkaya.common.enums

enum class SyncStatus(val value: Byte, val description: String) {
    New(0, "Yeni Kayıt"), ToTransfer(1, "Aktarılacak"), Transferred(2, "Aktarıldı"), NotTransferred(3, "Aktarılamadı");

    companion object {
        fun from(value: Byte): SyncStatus =
            SyncStatus.entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}