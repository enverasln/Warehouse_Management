package tr.com.cetinkaya.feature_goods_acceptance.planned.models.order

import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel

data class GetNextOrderDocumentSeriesAndNumberUiModel(
    val documentSeries: String, val documentNumber: Int
)

fun GetNextDocumentSeriesAndNumberDomainModel.toUiModel() = GetNextOrderDocumentSeriesAndNumberUiModel(
    documentSeries = this.documentSeries, documentNumber = this.documentNumber
)