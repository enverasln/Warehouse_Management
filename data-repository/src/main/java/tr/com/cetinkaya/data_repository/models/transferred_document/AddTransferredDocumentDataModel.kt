package tr.com.cetinkaya.data_repository.models.transferred_document

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel

data class AddTransferredDocumentDataModel(
    val transferredDocumentType: TransferredDocumentTypes,
    val documentSeries: String,
    val documentNumber: Int,
    val currentCode: String? = null,
    val paperNumber: String? = null
)

fun AddTransferredDocumentDomainModel.toDataModel() = AddTransferredDocumentDataModel(
    transferredDocumentType =  this.transferredDocumentType,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    currentCode = this.currentCode,
    paperNumber = this.paperNumber
)