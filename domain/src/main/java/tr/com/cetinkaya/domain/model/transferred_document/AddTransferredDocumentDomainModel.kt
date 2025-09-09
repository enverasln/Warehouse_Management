package tr.com.cetinkaya.domain.model.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

data class AddTransferredDocumentDomainModel(
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String?,
    val paperNumber: String?,
)