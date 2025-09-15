package tr.com.cetinkaya.domain.model.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentType

data class AddTransferredDocumentDomainModel(
    val transferredDocumentType: TransferredDocumentType,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String?,
    val paperNumber: String?,
)