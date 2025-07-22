package tr.com.cetinkaya.data_remote.models.order.planned_goods_acceptance

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import java.util.Date

data class PlannedGoodsAcceptanceProductResponseRemoteModel(
    @SerializedName("id") val id: String,
    @SerializedName("tarih") val orderDate: Date,
    @SerializedName("evrakSeri") val documentSeries: String,
    @SerializedName("evrakSira") val documentNumber: Int,
    @SerializedName("evrakSatirNumarasi") val documentRowNumber: Int,
    @SerializedName("stokId") val stockId: String,
    @SerializedName("stokKodu") val stockCode: String,
    @SerializedName("stokAdi") val stockName: String,
    @SerializedName("barkod") val barcode: String,
    @SerializedName("firmaId") val companyId: String,
    @SerializedName("firmaKodu") val companyCode: String,
    @SerializedName("firmaAdi") val companyName: String,
    @SerializedName("odemePlanNo") val paymentPlanNumber: Int,
    @SerializedName("depoId") val warehouseId: String,
    @SerializedName("depoNo") val warehouseNumber: Int,
    @SerializedName("depoAdi") val warehouseName: String,
    @SerializedName("miktar") val quantity: Double,
    @SerializedName("dovizCinsi") val currencyType: Byte,
    @SerializedName("iskonto1") val discount1: Double,
    @SerializedName("iskonto2") val discount2: Double,
    @SerializedName("iskonto3") val discount3: Double,
    @SerializedName("iskonto4") val discount4: Double,
    @SerializedName("iskonto5") val discount5: Double,
    @SerializedName("birimFiyat") val unitPrice: Double,
    @SerializedName("tutar") val totalPrice: Double,
    @SerializedName("vergiPntr") val vatPointer: Byte,
    @SerializedName("cariSorumlulukMerkezi") val currentResponsibilityCenter: String,
    @SerializedName("stokSorumlulukMerkezi") val stockResponsibilityCenter: String,
    @SerializedName("kalanMiktar") val remainingQuantity: Double,
    @SerializedName("renkliBedenliMi") val isColoredAndSized: Boolean
)

fun PlannedGoodsAcceptanceProductResponseRemoteModel.toDataModel() : ProductDataModel {
    return ProductDataModel(
        id = this.id,
        orderDate = this.orderDate,
        documentSeries = this.documentSeries,
        documentNumber = this.documentNumber,
        lineNumber = this.documentRowNumber,
        stockId = this.stockId,
        stockCode = this.stockCode,
        stockName = this.stockName,
        barcode = this.barcode,
        companyId = this.companyId,
        companyCode = this.companyCode,
        companyName = this.companyName,
        paymentPlanNumber = this.paymentPlanNumber,
        warehouseId = this.warehouseId,
        warehouseNumber = this.warehouseNumber,
        warehouseName = this.warehouseName,
        quantity = this.quantity,
        currencyType = this.currencyType,
        discount1 = this.discount1,
        discount2 = this.discount2,
        discount3 = this.discount3,
        discount4 = this.discount4,
        discount5 = this.discount5,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
        vatPointer = this.vatPointer,
        currentResponsibilityCenter = this.currentResponsibilityCenter,
        stockResponsibilityCenter = this.stockResponsibilityCenter,
        remainingQuantity = this.remainingQuantity,
        deliveredQuantity = 0.0,
        isColoredAndSized = this.isColoredAndSized,
        synchronizationStatus = "Sunucudan Gelen Kayıt"
    )
}