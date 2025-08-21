package tr.com.cetinkaya.feature_sync.models

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

data class TransferredDocumentUiModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val synchronizationStatus: Boolean,
    val description: String
)

fun TransferredDocumentDomainModel.toUiModel() = TransferredDocumentUiModel(
    id = id,
    transferredDocumentType = transferredDocumentType,
    documentSeries = documentSeries,
    documentNumber = documentNumber,
    synchronizationStatus = synchronizationStatus,
    description = description
)