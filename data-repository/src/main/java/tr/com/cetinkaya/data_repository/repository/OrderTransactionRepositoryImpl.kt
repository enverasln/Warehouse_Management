package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteOrderDataSource
import tr.com.cetinkaya.data_repository.models.order.toProductDomainModel
import tr.com.cetinkaya.data_repository.models.order_transaction.toDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.toDomainModel
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.toDataModel
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel
import tr.com.cetinkaya.domain.model.order.ProductDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.AddOrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import javax.inject.Inject
import kotlin.math.max

class OrderTransactionRepositoryImpl @Inject constructor(
    private val remoteOrderDataSource: RemoteOrderDataSource,
    private val localOrderDataSource: LocalOrderTransactionDataSource,
    private val localAuthDataSource: LocalAuthDataSource
) : OrderTransactionRepository {

    override suspend fun addWithSizeTransactions(
        orderTx: AddOrderTransactionDomainModel, sizeTx: List<AddSizeTransactionDomainModel>
    ) {
        localOrderDataSource.addWithSizeTransactions(orderTx.toDataModel(), sizeTx.toDataModel())
    }

    override suspend fun update(orderTx: OrderTransactionDomainModel) {
        localOrderDataSource.update(orderTx.toDataModel())
    }

    override fun getPlannedGoodsAcceptanceDocuments(
        warehouseNumber: Int, companyName: String, documentDate: String
    ): Flow<List<DocumentDomainModel>> {
        val longDate = DateConverter.uiToTimestamp(documentDate)
        val apiDate = longDate.let { DateConverter.timeStampToApi(it) }

        return remoteOrderDataSource.getPlannedGoodsAcceptanceDocuments(warehouseNumber, companyName, apiDate).map { result ->
            result.map {
                DocumentDomainModel(
                    documentDate = it.documentDate,
                    warehouseNumber = it.warehouseNumber,
                    companyCode = it.companyCode,
                    companyName = it.companyName,
                    documentSeries = it.documentSeries,
                    documentNumber = it.documentSeriesNumber,
                    isSelected = false
                )
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun getPlannedGoodsAcceptanceProducts(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<Unit> = localAuthDataSource.getLoggedUser().flatMapMerge { loggedUser ->
        remoteOrderDataSource.getPlannedGoodsAcceptanceProducts(documents, warehouseNumber).onEach { orderTxs ->
            val mappedOrderTxs = orderTxs.map { it.copy(userCode = loggedUser.mikroFlyUserId) }


            val sizedOrderTxs = orderTxs.filter { it.isColoredAndSized }

            val sizeTxs = sizedOrderTxs.map { sizeTx ->
                SizeTransactionDataModel(
                    id = "",
                    barcode = sizeTx.barcode,
                    refRecordId = sizeTx.id,
                    sizeTransactionType = SizeTransactionType.Order,
                    documentDate = sizeTx.orderDate,
                    quantity = sizeTx.remainingQuantity,
                    syncStatus = sizeTx.syncStatus
                )
            }
            localOrderDataSource.addOrders(mappedOrderTxs, sizeTxs)

        }
    }.map { }


    override fun getProductByBarcode(
        barcode: String, documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<GetProductByBarcodeDomainModel> = flow {
        val product = localOrderDataSource.getProductByBarcode(barcode, documents, warehouseNumber)
        if (product != null) emit(product.toProductDomainModel())
        else throw Exception("Girilen barkod numarasına ait stok bilgisi bulunamadı")
    }

    override fun getOrderTxsByDocuments(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<OrderTransactionDomainModel>> {
        return localOrderDataSource.getOrderTxsByDocuments(documents, warehouseNumber).map { result -> result.toDomainModel() }
    }

    override suspend fun getOrderTransactionsByBarcodeAndDocuments(
        barcode: String, orderTxDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): List<OrderTransactionDomainModel> {
        TODO("Not yet implemented")
    }

    override fun observeLocalPlannedGoodsAcceptanceProducts(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<ProductDomainModel>> =
        localOrderDataSource.getAllDocuments(documents, warehouseNumber).map { result -> result.map { it.toProductDomainModel() } }

    @OptIn(FlowPreview::class)
    override fun fetchAndSaveOrderTransactions(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<Unit> =
        localAuthDataSource.getLoggedUser().flatMapMerge { loggedUser ->
            remoteOrderDataSource.getPlannedGoodsAcceptanceProducts(documents, warehouseNumber).onEach { orderTxs ->
                val mappedOrderTxs = orderTxs.map { it.copy(userCode = loggedUser.mikroFlyUserId) }

                val sizedOrderTxs = orderTxs.filter { it.isColoredAndSized }

                val sizeTxs = sizedOrderTxs.map { sizedTx ->
                    SizeTransactionDataModel(
                        id = "",
                        barcode = sizedTx.barcode,
                        refRecordId = sizedTx.id,
                        sizeTransactionType = SizeTransactionType.Order,
                        documentDate = sizedTx.orderDate,
                        quantity = sizedTx.remainingQuantity,
                        syncStatus = sizedTx.syncStatus
                    )
                }
                localOrderDataSource.addOrders(mappedOrderTxs, sizeTxs)
            }.map { }
        }

    override suspend fun getNextDocumentSeriesAndNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): OrderTransactionDocumentDomainModel {
        return remoteOrderDataSource.getNextAvailableDocumentNumber(
            orderType, orderKind, documentSeries
        ).toDomainModel()
    }

    override suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        localOrderDataSource.updateOrderSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override fun getUnsyncedOrders(): Flow<List<OrderTransactionDomainModel>> = localOrderDataSource.getUnsyncedOrdersFlow().map { list ->
        list.map { data -> data.toDomainModel() }
    }

    override suspend fun sendOrder(orderTx: OrderTransactionDomainModel): Boolean {
        val mappedOrderTx = orderTx.toDataModel()
        return remoteOrderDataSource.sendOrder(mappedOrderTx)
    }

    override suspend fun isDocumentAvailable(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): Boolean {
        return remoteOrderDataSource.isDocumentAvailable(
            transactionType, transactionKind, documentSeries, documentNumber
        )
    }

    override suspend fun getUnsyncedOrdersByDocument(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, docSeries: String, docNumber: Int
    ): List<OrderTransactionDomainModel> {
        return localOrderDataSource.getUnsyncedOrders(docSeries, docNumber).map { it.toDomainModel() }
    }

    override suspend fun getNextAvailableDocumentNumber(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String
    ): Int {
        val remoteDocument = remoteOrderDataSource.getNextAvailableDocumentNumber(
            orderType = transactionType, orderKind = transactionKind, documentSeries = documentSeries
        )
        val localDocument = localOrderDataSource.getNextAvailableDocumentNumber(
            orderType = transactionType, orderKind = transactionKind, documentSeries = documentSeries
        )

        return max(localDocument.documentNumber, remoteDocument.docNumber)
    }

    override suspend fun markOrderTransactionSynced(orderTx: OrderTransactionDomainModel) {
        val updatedOrder = orderTx.toDataModel()
        localOrderDataSource.markOrderTransactionSynced(updatedOrder)
    }

    override suspend fun updateOrderDocumentNumber(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    ) {
        localOrderDataSource.updateOrderDocumentNumber(
            transactionType, transactionKind, documentSeries, oldDocumentNumber, newDocumentNumber
        )
    }

    override suspend fun countByDocumentSeriesAndNumber(
        documentSeries: String, documentNumber: Int
    ): Int {
        return localOrderDataSource.countByDocumentSeriesAndNumber(documentSeries, documentNumber)
    }

    override suspend fun markPending(orderTxDoc: OrderTransactionDocumentDomainModel) : Int = localOrderDataSource.markPending(orderTxDoc.toDataModel())


}