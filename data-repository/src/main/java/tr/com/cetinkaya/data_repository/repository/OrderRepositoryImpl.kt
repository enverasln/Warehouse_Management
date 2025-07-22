package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteOrderDataSource
import tr.com.cetinkaya.data_repository.models.order.toDomainModel
import tr.com.cetinkaya.data_repository.models.order.toOrderDomainModel
import tr.com.cetinkaya.data_repository.models.order.toDataModel
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.order.ProductDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val remoteOrderDataSource: RemoteOrderDataSource,
    private val localOrderDataSource: LocalOrderDataSource,
    private val localAuthDataSource: LocalAuthDataSource
) : OrderRepository {

    override fun getPlannedGoodsAcceptanceDocuments(
        warehouseNumber: Int, companyName: String, documentDate: String
    ): Flow<List<DocumentDomainModel>> {
        val longDate = DateConverter.uiToTimestamp(documentDate)
        val apiDate = longDate?.let { DateConverter.timeStampToApi(it) } ?: throw IllegalArgumentException("Tarih geçerli değildir")

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
    ): Flow<List<ProductDomainModel>> = flow {
        documents.forEach { it ->
            val apiProducts = remoteOrderDataSource.getPlannedGoodsAcceptanceProducts(it.first, it.second, warehouseNumber)
            localOrderDataSource.addOrders(apiProducts)
        }
        emit(Unit)
    }.flatMapMerge {
        localOrderDataSource.getAllDocuments(documents, warehouseNumber).map { result ->
            result.map { it.toDomainModel() }
        }
    }

    override fun getProductByBarcode(
        barcode: String, documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<GetProductByBarcodeDomainModel> = flow {
        val product = localOrderDataSource.getProductByBarcode(barcode, documents, warehouseNumber)
        if (product != null) emit(product.toDomainModel())
        else throw Exception("Girilen barkod numarasına ait stok bilgisi bulunamadı")
    }

    override fun observeLocalPlannedGoodsAcceptanceProducts(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<ProductDomainModel>> =
        localOrderDataSource.getAllDocuments(documents, warehouseNumber).map { result -> result.map { it.toDomainModel() } }

    override suspend fun syncPlannedGoodsAcceptanceProducts(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ) {
        documents.forEach { it ->
            val apiProducts = remoteOrderDataSource.getPlannedGoodsAcceptanceProducts(it.first, it.second, warehouseNumber)
            localOrderDataSource.addOrders(apiProducts)
        }

    }

    override suspend fun addOrder(
        newOrderDocumentSeries: String,
        newOrderDocumentNumber: Int,
        barcode: String,
        quantity: Double,
        warehouseNumber: Int,
        documents: List<Pair<String, Int>>
    ) {
        val existOrder =
            localOrderDataSource.getLatestOrderByBarcode(barcode, listOf("${newOrderDocumentSeries}-${newOrderDocumentNumber}"), warehouseNumber)
        if (existOrder != null) {
            val updatedOrder = existOrder.copy(
                quantity = existOrder.quantity + quantity,
                remainingQuantity = existOrder.remainingQuantity + quantity,
                totalPrice = existOrder.totalPrice + (existOrder.unitPrice * quantity),
                discount1 = (existOrder.discount1 / existOrder.quantity) * quantity,
                discount2 = (existOrder.discount2 / existOrder.quantity) * quantity,
                discount3 = (existOrder.discount3 / existOrder.quantity) * quantity,
                discount4 = (existOrder.discount4 / existOrder.quantity) * quantity,
                discount5 = (existOrder.discount5 / existOrder.quantity) * quantity
            )

            try {
                localOrderDataSource.update(updatedOrder)
            } catch (e: Exception) {
                throw e
            }
            return
        }

        val count = localOrderDataSource.countByDocumentSeriesAndNumber(newOrderDocumentSeries, newOrderDocumentNumber)

        val latestOrder = localOrderDataSource.getLatestOrderByBarcode(barcode, documents.map { it -> "${it.first}-${it.second}" }, warehouseNumber)

        if (latestOrder == null) throw Exception("$barcode numarasına ait kayda ulaşılamadı")

        val newOrder = latestOrder.copy(
            id = UUID.randomUUID().toString(),
            quantity = quantity,
            remainingQuantity = quantity,
            orderDate = Date(),
            documentSeries = newOrderDocumentSeries,
            documentNumber = newOrderDocumentNumber,
            lineNumber = count,
            totalPrice = latestOrder.unitPrice * quantity,
            discount1 = (latestOrder.discount1 / latestOrder.quantity) * quantity,
            discount2 = (latestOrder.discount2 / latestOrder.quantity) * quantity,
            discount3 = (latestOrder.discount3 / latestOrder.quantity) * quantity,
            discount4 = (latestOrder.discount4 / latestOrder.quantity) * quantity,
            discount5 = (latestOrder.discount5 / latestOrder.quantity) * quantity,
            deliveredQuantity = 0.0,
            synchronizationStatus = "Yeni Kayıt"
        )

        try {
            localOrderDataSource.addOrder(newOrder)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getNextDocumentSeriesAndNumber(
        orderType: Byte, orderKind: Byte, documentSeries: String
    ): GetNextDocumentSeriesAndNumberDomainModel {
        return remoteOrderDataSource.getNextDocumentSeriesAndNumber(
            orderType, orderKind, documentSeries
        ).toDomainModel()
    }

    override suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        localOrderDataSource.updateOrderSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    @OptIn(FlowPreview::class)
    override fun getUnsyncedOrders(): Flow<List<OrderDomainModel>> {
        return localAuthDataSource.getLoggedUser().flatMapMerge { user ->
                localOrderDataSource.getUnsyncedOrders().map { list ->
                        list.map { data -> data.toOrderDomainModel(user.mikroFlyUserId) }
                    }
            }


    }

    override suspend fun sendOrder(order: OrderDomainModel) {
       val orderDataModel = order.toDataModel()
        remoteOrderDataSource.sendOrder(orderDataModel)
        localOrderDataSource.updateOrderSyncStatus(order.documentSeries, order.documentNumber, "Aktarıldı")
    }


}