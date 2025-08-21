package tr.com.cetinkaya.data_local.models.order

import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel

data class GetNextAvailableDocumentLocalModel(
    val documentSeries: String, val documentNumber: Int
)

fun GetNextAvailableDocumentLocalModel.toDataModel(): GetNextDocumentSeriesAndNumberDataModel {
    return GetNextDocumentSeriesAndNumberDataModel(
        documentSeries = documentSeries, documentNumber = documentNumber
    )

}