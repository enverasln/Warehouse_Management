package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.TransferredDocumentType

class TransferredDocumentTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): TransferredDocumentType = TransferredDocumentType.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: TransferredDocumentType): Byte = type.value

}