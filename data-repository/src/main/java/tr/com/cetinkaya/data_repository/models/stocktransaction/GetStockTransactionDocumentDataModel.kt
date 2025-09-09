package tr.com.cetinkaya.data_repository.models.stocktransaction

import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionDocumentDomainModel

data class GetStockTransactionDocumentDataModel(
    val documentSeries: String,
    val documentNumber: Int,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val documentType: StockTransactionDocumentType,
    val invoiceId: String,
    val paperNumber: String,
    val currentCode: String,
    val currentName: String
)

fun GetStockTransactionDocumentDataModel.toDomainModel() = GetStockTransactionDocumentDomainModel(
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    transactionType = this.transactionType,
    transactionKind = this.transactionKind,
    isNormalOrReturn = this.isNormalOrReturn,
    documentType = this.documentType,
    invoiceId = this.invoiceId,
    paperNumber = this.paperNumber,
    currentCode = this.currentCode,
    currentName = this.currentName
)