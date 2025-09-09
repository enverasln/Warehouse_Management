package tr.com.cetinkaya.data_local.util

import androidx.room.TypeConverter
import tr.com.cetinkaya.common.enums.SyncStatus

class SyncStatusTypeConverter {
    @TypeConverter
    fun toEnum(value: Byte): SyncStatus = SyncStatus.entries.first { it.value == value }

    @TypeConverter
    fun fromEnum(type: SyncStatus): Byte = type.value

}