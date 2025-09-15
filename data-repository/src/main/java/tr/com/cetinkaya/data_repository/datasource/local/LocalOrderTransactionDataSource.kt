package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.GetProductByBarcodeDataModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.AddOrderTransactionDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel

interface LocalOrderTransactionDataSource {
    suspend fun addOrders(orderTxs: List<OrderTransactionDataModel>, sizeTxs: List<SizeTransactionDataModel>)
    suspend fun addWithSizeTransactions(orderTx: AddOrderTransactionDataModel, sizeTxs: List<AddSizeTransactionDataModel>)
    suspend fun update(product: ProductDataModel)
    suspend fun update(orderTx: OrderTransactionDataModel)
    suspend fun updateOrderDocumentNumber(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )

    fun getAllDocuments(documentSeriesAndNumber: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<ProductDataModel>>

    fun getOrderTxsByDocuments(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<OrderTransactionDataModel>>

    suspend fun getProductsByBarcode(barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int): List<ProductDataModel>
    suspend fun getProductByBarcode(barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int): GetProductByBarcodeDataModel?
    suspend fun getLatestOrderByBarcode(barcode: String, selectedDocuments: List<String>, warehouseNumber: Int): ProductDataModel?
    suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int
    suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)
    fun getUnsyncedOrdersFlow(): Flow<List<OrderTransactionDataModel>>
    suspend fun getUnsyncedOrders(docSeries: String, docNumber: Int): List<OrderTransactionDataModel>
    suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): GetNextDocumentSeriesAndNumberDataModel

    suspend fun markOrderTransactionSynced(orderTx: OrderTransactionDataModel)

    suspend fun markPending(orderTxDoc: OrderTransactionDocumentDataModel) : Int


}