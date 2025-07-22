package tr.com.cetinkaya.data_remote.models.order.add_order


import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.order.OrderDataModel

data class AddOrderRequestModel(
    @SerializedName("recordId") val id: String,
    @SerializedName("evrakTarihi") val orderDate: String,
    @SerializedName("seri") val documentSeries: String,
    @SerializedName("sira") val documentNumber: Int,
    @SerializedName("satir") val lineNumber: Int,
    @SerializedName("stokKod") val stockCode: String,
    @SerializedName("cariKod") val currentCode: String,
    @SerializedName("miktar") val quantity: Double,
    @SerializedName("girisDepo") val inputWarehouseNumber: Int,
    @SerializedName("cikisDepo") val outputWarehouseNumber: Int,
    @SerializedName("plasiyer") val salesman: String,
    @SerializedName("srMerkez") val responsibilityCenter: String,
    @SerializedName("kullanici") val userCode: Int,
    @SerializedName("yekun") val totalPrice: Double,
    @SerializedName("isk1Tutar") val discount1: Double,
    @SerializedName("isk2Tutar") val discount2: Double,
    @SerializedName("isk3Tutar") val discount3: Double,
    @SerializedName("isk4Tutar") val discount4: Double,
    @SerializedName("isk5Tutar") val discount5: Double,
    @SerializedName("kdv") val vatPointer: Byte,
    @SerializedName("fiyat") val price: Double,
    @SerializedName("belgeNo") val paperNumber: String,
    @SerializedName("firmaNo") val companyNumber: Int,
    @SerializedName("subeNo") val storeNumber: Int,
    @SerializedName("barkod") val barcode: String,
    @SerializedName("renkliBedenliMi") val isColoredAndSized: Boolean
)

fun OrderDataModel.toAddOrderRequestModel(): AddOrderRequestModel = AddOrderRequestModel(
    id = this.id,
    orderDate = this.orderDate,
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber,
    lineNumber = this.lineNumber,
    stockCode = this.stockCode,
    currentCode = this.currentCode,
    quantity = this.quantity,
    inputWarehouseNumber = this.inputWarehouseNumber,
    outputWarehouseNumber = this.outputWarehouseNumber,
    salesman = this.salesman,
    responsibilityCenter = this.responsibilityCenter,
    userCode = this.userCode,
    totalPrice = this.totalPrice,
    discount1 = this.discount1,
    discount2 = this.discount2,
    discount3 = this.discount3,
    discount4 = this.discount4,
    discount5 = this.discount5,
    vatPointer = this.vatPointer,
    price = this.price,
    paperNumber = this.paperNumber,
    companyNumber = this.companyNumber,
    storeNumber = this.storeNumber,
    barcode = this.barcode,
    isColoredAndSized = this.isColoredAndSized
)