package tr.com.cetinkaya.data_remote.models.order.planned_goods_acceptance

import com.google.gson.annotations.SerializedName
import java.util.Date

data class DocumentRemoteModel(
    @SerializedName("tarih") val documentDate: Date,
    @SerializedName("depoNo") val warehouseNumber: Int,
    @SerializedName("firmaKodu") val companyCode: String,
    @SerializedName("firmaAdi") val companyName: String,
    @SerializedName("evrakSeri") val documentSeries: String,
    @SerializedName("evrakSira") val documentSeriesNumber: Int
)