package tr.com.cetinkaya.data_remote.models.barcode

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.barcode.GetAssortmentBarcodeByStockCodeDataModel

data class GetAssortmentBarcodeByStockCodeResponseModel(
    @SerializedName("barcode") val barcode: String
)

fun GetAssortmentBarcodeByStockCodeResponseModel.toDataModel(): GetAssortmentBarcodeByStockCodeDataModel = GetAssortmentBarcodeByStockCodeDataModel(
    barcode = this.barcode
)