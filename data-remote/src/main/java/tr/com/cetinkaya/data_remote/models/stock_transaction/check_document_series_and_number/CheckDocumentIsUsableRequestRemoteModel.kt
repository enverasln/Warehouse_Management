package tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number

import com.google.gson.annotations.SerializedName

data class CheckDocumentIsUsableRequestRemoteModel(
    @SerializedName("evrakNoSeri")val documentSeries: String,
    @SerializedName("evrakNoSira")val documentNumber: Int,
    @SerializedName("cariKod")val companyCode: String,
    @SerializedName("belgeNo")val paperNumber: String,
    @SerializedName("StokHareketTipi")val stockTransactionType: Int,
    @SerializedName("StokHareketCinsi")val stockTransactionKind: Int,
    @SerializedName("StokHareketEvrakTipi")val documentType: Int,
    @SerializedName("StokHareketIslemTipi")val isNormalOrReturn: Int
)