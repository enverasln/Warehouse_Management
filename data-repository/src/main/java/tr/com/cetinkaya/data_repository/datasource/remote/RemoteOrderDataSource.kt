package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.OrderDataModel
import tr.com.cetinkaya.data_repository.models.order.PlannedGoodsAcceptanceDocumentRepositoryModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDocumentDataModel

interface RemoteOrderDataSource {

    fun getPlannedGoodsAcceptanceDocuments(
        warehouseNumber: Int,
        companyName: String,
        documentDate: String
    ): Flow<List<PlannedGoodsAcceptanceDocumentRepositoryModel>>

    fun getPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<OrderTransactionDataModel>>

    suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes,
        orderKind: OrderTransactionKinds,
        documentSeries: String
    ): OrderTransactionDocumentDataModel

    suspend fun sendOrder(orderTx: OrderTransactionDataModel): Boolean

    suspend fun isDocumentAvailable(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        documentNumber: Int
    ) : Boolean
}