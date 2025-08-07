package tr.com.cetinkaya.data_local.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.data_local.db.dao.OrderDao
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.db.entities.toOrderDataModel
import tr.com.cetinkaya.data_local.models.order.toDataModel
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderDataSource
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.GetProductByBarcodeDataModel
import tr.com.cetinkaya.data_repository.models.order.OrderDataModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import javax.inject.Inject

class LocalOrderDataSourceImpl @Inject constructor(
    private val orderDao: OrderDao
) : LocalOrderDataSource {

    override suspend fun addOrders(incomingOrders: List<ProductDataModel>) {
        try {
            val existingOrders = orderDao.getOrdersByIds(incomingOrders.map { it.id + '#' + it.barcode })

            val changedOrders = incomingOrders.filter { incoming ->
                val existing = existingOrders.find { it.id == incoming.id && it.barcode == incoming.barcode }
                existing == null
            }
            if (changedOrders.isNotEmpty()) {
                orderDao.addRange(changedOrders.map { it.toEntity() })
            }
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun addOrder(product: ProductDataModel) {
        try {
            val existingOrder = orderDao.getLatestOrderByBarcode(
                product.barcode, listOf("${product.documentSeries}-${product.documentNumber}"), product.warehouseNumber
            )
            if (existingOrder != null) throw Exception("Kayıt daha önce var olduğu için ekleme yapılamadı")
            val newOrder = product.toEntity()
            orderDao.add(newOrder)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun update(product: ProductDataModel) {
        try {
            orderDao.update(product.toEntity())
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateOrderDocumentNumber(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    ) {
        orderDao.updateOrderDocumentNumber(
            documentSeries,
            oldDocumentNumber,
            newDocumentNumber
        )
    }

    override fun getAllDocuments(
        documentSeriesAndNumber: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<ProductDataModel>> {
        val documentsStr = documentSeriesAndNumber.map { it -> "${it.first}-${it.second}" }
        return orderDao.getAllByDocuments(documentsStr, warehouseNumber).map { products ->
            products.map { it.toDataModel() }
        }
    }

    override suspend fun getProductsByBarcode(
        barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): List<ProductDataModel> {
        val documentsStr = selectedDocuments.map { it -> "${it.first}-${it.second}" }
        return orderDao.getProductsByBarcode(barcode, documentsStr, warehouseNumber).map { it.toDataModel() }
    }

    override suspend fun getProductByBarcode(
        barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): GetProductByBarcodeDataModel? {
        val product = orderDao.getProductByBarcode(barcode, selectedDocuments.map { it -> "${it.first}-${it.second}" }, warehouseNumber)
        return product?.toDataModel()
    }

    override suspend fun getLatestOrderByBarcode(
        barcode: String, selectedDocuments: List<String>, warehouseNumber: Int
    ): ProductDataModel? {
        return orderDao.getLatestOrderByBarcode(barcode, selectedDocuments, warehouseNumber)?.toDataModel()
    }

    override suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int {
        return orderDao.countByDocumentSeriesAndNumber(documentSeries, documentNumber)
    }

    override suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        orderDao.updateOrderSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override fun getUnsyncedOrdersFlow(): Flow<List<ProductDataModel>> {
        return orderDao.getBySyncStatus("Aktarılacak").map { list -> list.map { it.toDataModel() } }
    }

    override suspend fun getUnsyncedOrders(): List<OrderDataModel> {
        return orderDao.getUnsyncedOrders().map { it.toOrderDataModel() }
    }

    override suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): GetNextDocumentSeriesAndNumberDataModel {

        val nextDocumentNumber = orderDao.getNextAvailableDocumentNumber(documentSeries)

        return if (nextDocumentNumber == null) GetNextDocumentSeriesAndNumberDataModel(documentSeries, 1)
        else GetNextDocumentSeriesAndNumberDataModel(documentSeries, nextDocumentNumber + 1)
    }

    override suspend fun markOrderTransactionSynced(order: OrderDataModel) {
        orderDao.markOrderTransactionSynced(order.id, order.barcode)
    }

}