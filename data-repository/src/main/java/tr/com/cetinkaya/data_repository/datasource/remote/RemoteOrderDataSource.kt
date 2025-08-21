package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
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
        documentSeries: String, documentNumber: Int, warehouseNumber: Int
    ): List<ProductDataModel>

    suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes,
        orderKind: OrderTransactionKinds,
        documentSeries: String
    ): GetNextDocumentSeriesAndNumberDataModel

    suspend fun sendOrder(order: OrderDataModel): Boolean

    suspend fun isDocumentUsed(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        documentNumber: Int
    ) : Boolean
}