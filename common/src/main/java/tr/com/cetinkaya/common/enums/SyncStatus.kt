package tr.com.cetinkaya.common.enums

enum class SyncStatus(val value: Byte, val description: String) {
    None(0, "Hiçbir durum yok"),
    New(1, "Yeni Kayıt"),
    PendingTransfer(2, "Aktarım Bekliyor"),
    Transferred(3, "Aktarıldı"),
    Failed(4, "Hata Oluştu");

    companion object {
        fun from(value: Byte): SyncStatus =
            SyncStatus.entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Invalid value: $value")
    }
}