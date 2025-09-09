package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.DataOrigin

class DataOriginTypeConvert {
    @TypeConverter
    fun toEnum(value: Byte): DataOrigin = DataOrigin.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: DataOrigin): Byte = type.value
}