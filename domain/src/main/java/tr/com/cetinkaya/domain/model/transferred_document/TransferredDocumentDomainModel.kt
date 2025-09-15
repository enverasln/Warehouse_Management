package tr.com.cetinkaya.domain.model.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentType

data class TransferredDocumentDomainModel(
    val id: Long,
    val transferredDocumentType: TransferredDocumentType,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String? = null,
    val paperNumber: String? = null,
    val synchronizationStatus: Boolean,
    val description: String
)
