package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.StockTransactionKinds

class StockTransactionKindTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): StockTransactionKinds = StockTransactionKinds.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: StockTransactionKinds): Byte = type.value

}