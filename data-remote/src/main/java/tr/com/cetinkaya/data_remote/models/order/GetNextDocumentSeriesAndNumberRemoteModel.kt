package tr.com.cetinkaya.data_remote.models.order

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel

data class GetNextDocumentSeriesAndNumberRemoteModel(
    @SerializedName("evraknoSeri")val documentSeries: String,
    @SerializedName("evraknoSira")val documentNumber: Int
)

fun GetNextDocumentSeriesAndNumberRemoteModel.toDataModel() = GetNextDocumentSeriesAndNumberDataModel(
    documentSeries = documentSeries,
    documentNumber = documentNumber
)
