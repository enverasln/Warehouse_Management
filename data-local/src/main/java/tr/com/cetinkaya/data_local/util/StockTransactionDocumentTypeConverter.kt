package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType

class StockTransactionDocumentTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionDocumentType = StockTransactionDocumentType.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionDocumentType): Byte = type.value

}

