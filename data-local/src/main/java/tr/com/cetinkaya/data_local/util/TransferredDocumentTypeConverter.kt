package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

class TransferredDocumentTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): TransferredDocumentTypes = TransferredDocumentTypes.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: TransferredDocumentTypes): Byte = type.value

}