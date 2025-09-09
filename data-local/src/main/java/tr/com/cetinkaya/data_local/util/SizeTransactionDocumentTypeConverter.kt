package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.SizeTransactionType

class SizeTransactionDocumentTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): SizeTransactionType = SizeTransactionType.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(value: SizeTransactionType): Byte = value.value
}