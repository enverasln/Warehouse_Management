package tr.com.cetinkaya.data_local.db.views

import androidx.room.DatabaseView
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_repository.models.stock_transaction.GetWarehouseTransferByDocumentDataModel

@DatabaseView(
    viewName = "vw_stock_transactions_with_size_transactions",
    value = "SELECT st.id AS refRecordId, st.barcode AS assortmentBarcode, CASE WHEN sz.barcode IS NULL THEN st.barcode ELSE sz.barcode END AS stockBarcode, sz.sizeTransactionType, CASE WHEN sz.quantity IS NULL THEN SUM(st.quantity) ELSE SUM(sz.quantity) END AS quantity, st.stockCode, st.stockName, st.documentSeries, st.documentNumber, st.transactionType, st.transactionKind, st.isNormalOrReturn, st.transactionDocumentType, st.updatedAt FROM stock_transactions st LEFT JOIN size_transactions sz ON st.id = sz.refRecordId GROUP BY st.id, st.barcode, CASE WHEN sz.barcode IS NULL THEN st.barcode ELSE sz.barcode END, sz.sizeTransactionType, st.stockCode, st.stockName, st.transactionType, st.transactionKind, st.isNormalOrReturn, st.transactionDocumentType, st.updatedAt ORDER BY st.updatedAt DESC"
)
data class StockTransactionWithSizeTransactionView(
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
    val transactionDocumentType: StockTransactionDocumentType,
    val updatedAt: Long
)

fun StockTransactionWithSizeTransactionView.toGetWarehouseTransferByDocumentDataModel(): GetWarehouseTransferByDocumentDataModel =
    GetWarehouseTransferByDocumentDataModel(
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

fun List<StockTransactionWithSizeTransactionView>.toGetWarehouseTransferByDocumentDataModel(): List<GetWarehouseTransferByDocumentDataModel> =
    this.map { it.toGetWarehouseTransferByDocumentDataModel() }