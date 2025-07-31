package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes

class StockTransactionDocumentTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionDocumentTypes = StockTransactionDocumentTypes.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionDocumentTypes): Byte = type.value

}