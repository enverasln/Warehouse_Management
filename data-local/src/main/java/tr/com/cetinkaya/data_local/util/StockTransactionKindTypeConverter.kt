package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionKind

class StockTransactionKindTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionKind = StockTransactionKind.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionKind): Byte = type.value

}

