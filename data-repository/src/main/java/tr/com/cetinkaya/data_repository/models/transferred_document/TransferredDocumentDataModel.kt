package tr.com.cetinkaya.data_repository.models.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

data class TransferredDocumentDataModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentType,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String?,
    val paperNumber: String?,
    val synchronizationStatus: Boolean,
    val description: String
)

fun TransferredDocumentDataModel.toDomainModel() = TransferredDocumentDomainModel(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description,
    currentCode = currentCode,
    paperNumber = paperNumber
)

fun TransferredDocumentDomainModel.toDataModel(): TransferredDocumentDataModel = TransferredDocumentDataModel(
    id = this.id,
    transferredDocumentType = this.transferredDocumentType,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    synchronizationStatus = this.synchronizationStatus,
    description = this.description,
    currentCode = this.currentCode,
    paperNumber = this.paperNumber
)

fun List<TransferredDocumentDomainModel>.toDataModel(): List<TransferredDocumentDataModel> = this.map { it.toDataModel() }