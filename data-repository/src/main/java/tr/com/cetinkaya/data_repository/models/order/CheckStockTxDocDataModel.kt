package tr.com.cetinkaya.data_repository.models.order

import tr.com.cetinkaya.domain.model.stok_transaction.CheckStockTxDocIsUsableDomainModel

data class CheckStockTxDocDataModel(
    val message: String, val isDocumentNew: Boolean,
    val isUsed: Boolean?, val canBeUsed: Boolean?
)



fun CheckStockTxDocDataModel.toDomainModel() : CheckStockTxDocIsUsableDomainModel = CheckStockTxDocIsUsableDomainModel (
    message = this.message,
    isDocumentNew = this.isDocumentNew,
    isUsed = this.isUsed,
    canBeUsed = this.canBeUsed
)