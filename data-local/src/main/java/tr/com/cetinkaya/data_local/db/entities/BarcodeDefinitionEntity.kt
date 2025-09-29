package tr.com.cetinkaya.data_local.db.entities

data class BarcodeDefinitionEntity(
    val id: String,
    val barcode: String,
    val stockCode: String,
    val barcodeType: Byte,
    val unitPointer: Byte,
    val sizePointer: Byte,
    val createdDate: Long,
    val updatedDate: Long
)