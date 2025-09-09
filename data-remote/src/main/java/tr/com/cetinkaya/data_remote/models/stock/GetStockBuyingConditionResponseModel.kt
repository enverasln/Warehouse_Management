package tr.com.cetinkaya.data_remote.models.stock

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.stock.GetStockBuyingConditionDataModel

data class GetStockBuyingConditionResponseModel(
    @SerializedName("grossPrice") val grossPrice: Double,
    @SerializedName("discount1") val discount1: Double,
    @SerializedName("discount2") val discount2: Double,
    @SerializedName("discount3") val discount3: Double,
    @SerializedName("discount4") val discount4: Double,
    @SerializedName("discount5") val discount5: Double,
    @SerializedName("vatRate") val vatRate: Byte
)

fun GetStockBuyingConditionResponseModel.toDataModel() = GetStockBuyingConditionDataModel(
    grossPrice = this.grossPrice,
    discount1 = this.discount1,
    discount2 = this.discount2,
    discount3 = this.discount3,
    discount4 = this.discount4,
    discount5 = this.discount5,
    vatRate = vatRate
)
