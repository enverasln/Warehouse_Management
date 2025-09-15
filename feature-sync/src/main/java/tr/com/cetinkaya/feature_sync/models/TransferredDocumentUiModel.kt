package tr.com.cetinkaya.feature_sync.models

import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

data class TransferredDocumentUiModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentType,
    val documentSeries: String,
    val documentNumber: Int,
    val synchronizationStatus: Boolean,
    val description: String
)

fun TransferredDocumentDomainModel.toUiModel() = TransferredDocumentUiModel(
    id = this.id,
    transferredDocumentType = this.transferredDocumentType,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    synchronizationStatus = this.synchronizationStatus,
    description = this.description
)