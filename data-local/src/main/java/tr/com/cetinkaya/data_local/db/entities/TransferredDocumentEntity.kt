package tr.com.cetinkaya.data_local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

@Entity(
    tableName = "transferred_documents",
)
data class TransferredDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val synchronizationStatus: Boolean,
    val description: String
)

fun TransferredDocumentDataModel.toEntity() = TransferredDocumentEntity(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description
)

fun TransferredDocumentEntity.toDataModel() = TransferredDocumentDataModel(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description
)