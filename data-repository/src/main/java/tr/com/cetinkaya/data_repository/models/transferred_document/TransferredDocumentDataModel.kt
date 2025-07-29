package tr.com.cetinkaya.data_repository.models.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

data class TransferredDocumentDataModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val synchronizationStatus: Boolean,
    val description: String
)