package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionType

class StockTransactionTypeTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionType = StockTransactionType.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionType): Byte = type.value
}

