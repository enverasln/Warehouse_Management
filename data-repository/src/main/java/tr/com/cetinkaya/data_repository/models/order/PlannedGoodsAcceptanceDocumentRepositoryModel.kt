package tr.com.cetinkaya.data_repository.models.order

import java.util.Date

data class PlannedGoodsAcceptanceDocumentRepositoryModel(
    val documentDate: Date,
    val warehouseNumber: Int,
    val companyCode: String,
    val companyName: String,
    val documentSeries: String,
    val documentSeriesNumber: Int
)