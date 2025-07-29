package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_local.db.entities.TransferredDocumentEntity

@Dao
interface TransferredDocumentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(transferredDocument: TransferredDocumentEntity) : Long
}