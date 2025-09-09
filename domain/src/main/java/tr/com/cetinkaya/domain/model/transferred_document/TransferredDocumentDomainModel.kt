package tr.com.cetinkaya.domain.model.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

data class TransferredDocumentDomainModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String? = null,
    val paperNumber: String? = null,
    val synchronizationStatus: Boolean,
    val description: String
)
