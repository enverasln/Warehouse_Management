package tr.com.cetinkaya.data_repository.models.order

import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel

data class GetNextDocumentSeriesAndNumberDataModel  (
    val documentSeries: String,
    val documentNumber: Int
)

fun GetNextDocumentSeriesAndNumberDataModel.toProductDomainModel() = GetNextDocumentSeriesAndNumberDomainModel(
    documentSeries = this.documentSeries,
    documentNumber = this.documentNumber
)