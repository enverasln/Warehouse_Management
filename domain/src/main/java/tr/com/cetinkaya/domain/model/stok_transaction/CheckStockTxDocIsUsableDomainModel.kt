package tr.com.cetinkaya.domain.model.stok_transaction

data class CheckStockTxDocIsUsableDomainModel(
    val message : String,
    val isDocumentNew: Boolean,
    val isUsed: Boolean?,
    val canBeUsed: Boolean?

)