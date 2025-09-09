package tr.com.cetinkaya.common.enums

enum class TransferUnit {
    Adet, Paket, Koli;

    companion object {
        fun fromDisplay(value: String): TransferUnit = when (value.lowercase()) {
            "Adet" -> Adet
            "Paket" -> Paket
            "Koli"  -> Koli
            else    -> Adet
        }
    }
}