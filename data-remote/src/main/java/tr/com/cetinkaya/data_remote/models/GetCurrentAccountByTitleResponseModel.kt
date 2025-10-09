package tr.com.cetinkaya.data_remote.models

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.current_account.GetCurrentAccountByTitleDataModel

data class GetCurrentAccountByTitleResponseModel(
    @SerializedName("currentCode") val currentCode: String,
    @SerializedName("currentTitle1") val currentTitle1: String,
    @SerializedName("currentTitle2") val currentTitle2: String
)

fun GetCurrentAccountByTitleResponseModel.toDataModel(): GetCurrentAccountByTitleDataModel = GetCurrentAccountByTitleDataModel(
    currentCode = this.currentCode, currentTitle1 = this.currentTitle1, currentTitle2 = this.currentTitle2
)

fun List<GetCurrentAccountByTitleResponseModel>.toDataModel(): List<GetCurrentAccountByTitleDataModel> = this.map { it.toDataModel() }