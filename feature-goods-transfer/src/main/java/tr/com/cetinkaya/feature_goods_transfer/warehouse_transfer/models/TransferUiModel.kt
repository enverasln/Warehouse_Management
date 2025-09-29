package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models

import androidx.recyclerview.widget.ListUpdateCallback
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.GetWarehouseTransfersByDocumentDomainModel

data class TransferUiModel(
    val refRecordId: String,
    val assortmentBarcode: String,
    val stockBarcode: String,
    val sizeTransactionType: SizeTransactionType?,
    val quantity: Double,
    val stockCode: String,
    val stockName: String,
    val documentSeries: String,
    val documentNumber: Int,
    val transactionType: StockTransactionType,
    val transactionKind: StockTransactionKind,
    val isNormalOrReturn: Byte,
    val transactionDocumentType: StockTransactionDocumentType
)

fun GetWarehouseTransfersByDocumentDomainModel.toTransferUiModel(): TransferUiModel = TransferUiModel(
    refRecordId = this.refRecordId,
    assortmentBarcode = this.assortmentBarcode,
    stockBarcode = this.stockBarcode,
    sizeTransactionType = this.sizeTransactionType,
    quantity = this.quantity,
    stockCode = this.stockCode,
    stockName = this.stockName,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    transactionType = this.transactionType,
    transactionKind = this.transactionKind,
    isNormalOrReturn = this.isNormalOrReturn,
    transactionDocumentType = this.transactionDocumentType
)

fun List<GetWarehouseTransfersByDocumentDomainModel>.toTransferUiModel(): List<TransferUiModel> = this.map { it.toTransferUiModel() }
