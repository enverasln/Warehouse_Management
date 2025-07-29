package tr.com.cetinkaya.data_local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

@Entity(
    tableName = "transferred_documents",
)
data class TransferredDocument(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val synchronizationStatus: Boolean,
    val description: String
)