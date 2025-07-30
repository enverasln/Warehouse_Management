package tr.com.cetinkaya.data_repository.models.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

data class TransferredDocumentDataModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
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
)