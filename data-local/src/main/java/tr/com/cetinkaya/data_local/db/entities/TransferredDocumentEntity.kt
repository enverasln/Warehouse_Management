package tr.com.cetinkaya.data_local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

@Entity(
    tableName = "transferred_documents",
    indices = [androidx.room.Index(value = ["documentSeries", "documentNumber", "transferredDocumentType"], unique = true)]
)
data class TransferredDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transferredDocumentType: TransferredDocumentType,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String?,
    val paperNumber: String?,
    val synchronizationStatus: Boolean,
    val description: String
) {
    companion object {
        fun create(
            transferredDocumentType: TransferredDocumentType,
            documentSeries: String,
            documentNumber: Int,
            currentCode: String? = null,
            paperNumber: String? = null,
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
    transferredDocumentType = this.transferredDocumentType,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    synchronizationStatus = this.synchronizationStatus,
    description = this.description,
    currentCode = this.currentCode,
    paperNumber = this.paperNumber
)

fun List<TransferredDocumentDataModel>.toEntity(): List<TransferredDocumentEntity> = this.map { it.toEntity() }

fun TransferredDocumentEntity.toProductDataModel() = TransferredDocumentDataModel(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description,
    currentCode = currentCode,
    paperNumber = paperNumber
)