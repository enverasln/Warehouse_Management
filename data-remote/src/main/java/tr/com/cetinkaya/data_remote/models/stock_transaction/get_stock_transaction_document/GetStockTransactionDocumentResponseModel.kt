package tr.com.cetinkaya.data_remote.models.stock_transaction.get_stock_transaction_document

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionDocumentDataModel

data class GetStockTransactionDocumentResponseModel(
    @SerializedName("evrakNoSeri") val documentSeries: String,
    @SerializedName("evrakNoSira") val documentNumber: Int,
    @SerializedName("stokHareketTipi") val transactionType: StockTransactionType,
    @SerializedName("stokHareketCinsi") val transactionKind: StockTransactionKind,
    @SerializedName("stokHareketIslemTipi") val isNormalOrReturn: Byte,
    @SerializedName("stokHareketEvrakTipi") val transactionDocumentType: StockTransactionDocumentType,
    @SerializedName("faturaId") val invoiceId: String,
    @SerializedName("belgeNo") val paperNumber: String,
    @SerializedName("cariKod") val currentCode: String,
    @SerializedName("cariAdi") val currentName: String
)

fun GetStockTransactionDocumentResponseModel.toDataModel() = GetStockTransactionDocumentDataModel(
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    transactionType = this.transactionType,
    transactionKind = this.transactionKind,
    isNormalOrReturn = this.isNormalOrReturn,
    documentType = this.transactionDocumentType,
    invoiceId = this.invoiceId,
    paperNumber = this.paperNumber,
    currentCode = this.currentCode,
    currentName = this.currentName
)