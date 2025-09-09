package tr.com.cetinkaya.data_local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

@Entity(
    tableName = "transferred_documents",
    indices = [androidx.room.Index(value = ["documentSeries", "documentNumber", "transferredDocumentType"], unique = true)]
)
data class TransferredDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String?,
    val paperNumber: String?,
    val synchronizationStatus: Boolean,
    val description: String
) {
    companion object {
        fun create(
            transferredDocumentType: TransferredDocumentTypes,
            documentSeries: String,
            documentNumber: Int,
            currentCode: String?,
            paperNumber: String?,
            synchronizationStatus: Boolean,
            description: String
        ): TransferredDocumentEntity {
            return TransferredDocumentEntity(
                id = 0,
                transferredDocumentType = transferredDocumentType,
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                currentCode = currentCode,
                paperNumber = paperNumber,
                synchronizationStatus = synchronizationStatus,
                description = description
            )
        }
    }
}

fun TransferredDocumentDataModel.toEntity() = TransferredDocumentEntity(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description,
    currentCode = currentCode,
    paperNumber = paperNumber

)

fun TransferredDocumentEntity.toDataModel() = TransferredDocumentDataModel(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description,
    currentCode = currentCode,
    paperNumber = paperNumber
)