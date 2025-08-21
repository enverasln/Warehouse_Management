package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionTypes

class StockTransactionTypeTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionTypes = StockTransactionTypes.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionTypes): Byte = type.value
}

