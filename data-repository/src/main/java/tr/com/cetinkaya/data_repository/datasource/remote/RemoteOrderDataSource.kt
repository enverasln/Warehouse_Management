package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.OrderDataModel
import tr.com.cetinkaya.data_repository.models.order.PlannedGoodsAcceptanceDocumentRepositoryModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel

interface RemoteOrderDataSource {

    fun getPlannedGoodsAcceptanceDocuments(
        warehouseNumber: Int,
        companyName: String,
        documentDate: String
    ): Flow<List<PlannedGoodsAcceptanceDocumentRepositoryModel>>

    suspend fun getPlannedGoodsAcceptanceProducts(
         documentSeries: String,  documentNumber: Int, warehouseNumber: Int
    ) : List<ProductDataModel>

    suspend fun getNextDocumentSeriesAndNumber(
        orderType: Byte,
        orderKind: Byte,
        documentSeries: String
    ) : GetNextDocumentSeriesAndNumberDataModel

    suspend fun sendOrder(order: OrderDataModel)
}