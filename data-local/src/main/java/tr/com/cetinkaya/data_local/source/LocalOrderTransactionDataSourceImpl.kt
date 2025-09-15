package tr.com.cetinkaya.data_local.source

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.data_local.db.AppDatabase
import tr.com.cetinkaya.data_local.db.dao.OrderTransactionDao
import tr.com.cetinkaya.data_local.db.dao.SizeTransactionDao
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.OrderEntity
import tr.com.cetinkaya.data_local.db.entities.SizeTransactionEntity
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.db.entities.toProductDataModel
import tr.com.cetinkaya.data_local.models.order.toDataModel
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.GetProductByBarcodeDataModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.AddOrderTransactionDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDataModel
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import javax.inject.Inject

class LocalOrderTransactionDataSourceImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderTransactionDao: OrderTransactionDao,
    private val sizeTransactionDao: SizeTransactionDao,
) : LocalOrderTransactionDataSource {

    override suspend fun addOrders(orderTxs: List<OrderTransactionDataModel>, sizeTxs: List<SizeTransactionDataModel>) {
        try {
            val existingOrders = orderTransactionDao.getOrdersByIds(orderTxs.map { it.id + '#' + it.barcode })

            val changedOrders = orderTxs.filter { incoming ->
                val existing = existingOrders.find { it.id == incoming.id && it.barcode == incoming.barcode }
                existing == null
            }
            if (changedOrders.isNotEmpty()) {
                orderTransactionDao.addRange(changedOrders.toEntity())
            }

            if (sizeTxs.isNotEmpty()) {
                val toInsertSizeTxs = sizeTxs.map {
                    SizeTransactionEntity.create(
                        it.barcode, it.refRecordId, it.sizeTransactionType, it.quantity
                    )
                }

                toInsertSizeTxs.forEach { sizeTx ->
                    val existSizeTx = sizeTransactionDao.getByBarcodeAndRefRecord(
                        barcode = sizeTx.barcode, refRecordId = sizeTx.refRecordId, sizeTransactionType = sizeTx.sizeTransactionType
                    )

                    if (existSizeTx != null) {
                        sizeTransactionDao.update(existSizeTx.copy(quantity = existSizeTx.quantity + sizeTx.quantity))
                    } else {
                        sizeTransactionDao.add(sizeTx)
                    }
                }
            }

        } catch (e: Exception) {
            throw e
        }

    }


    override suspend fun addWithSizeTransactions(
        orderTx: AddOrderTransactionDataModel, sizeTxs: List<AddSizeTransactionDataModel>
    ) {
        db.withTransaction {

            try {
                val existOrderTxs = orderTransactionDao.getByStockCode(
                    documentSeries = orderTx.documentSeries,
                    documentNumber = orderTx.documentNumber,
                    stockCode = orderTx.stockCode,
                )

                val existOrderTx =
                    if (existOrderTxs.size > 1) existOrderTxs.firstOrNull() { it.barcode == orderTx.barcode && it.documentRowNumber == orderTx.documentRowNumber }
                    else existOrderTxs.firstOrNull()



                if (existOrderTx != null) {
                    val toUpdateOrderTransaction = existOrderTx.copy(
                        quantity = if (existOrderTxs.size > 1) existOrderTx.quantity else existOrderTx.quantity + orderTx.quantity,
                        remainingQuantity = if (existOrderTxs.size > 1) existOrderTx.remainingQuantity else existOrderTx.remainingQuantity + orderTx.quantity,
                        deliveredQuantity = existOrderTx.deliveredQuantity + orderTx.deliveredQuantity,
                        totalPrice = existOrderTx.totalPrice + orderTx.totalPrice
                    )
                    orderTransactionDao.update(toUpdateOrderTransaction)

                    if (sizeTxs.isNotEmpty()) {
                        val toInsertSizeTxs = sizeTxs.map {
                            SizeTransactionEntity.create(
                                it.barcode, existOrderTx.id, it.sizeTransactionType, it.quantity
                            )
                        }

                        toInsertSizeTxs.forEach { sizeTx ->
                            val existSizeTx = sizeTransactionDao.getByBarcodeAndRefRecord(
                                barcode = sizeTx.barcode, refRecordId = sizeTx.refRecordId, sizeTransactionType = sizeTx.sizeTransactionType
                            )

                            if (existSizeTx != null) {
                                sizeTransactionDao.update(existSizeTx.copy(quantity = existSizeTx.quantity + sizeTx.quantity))
                            } else {
                                sizeTransactionDao.add(sizeTx)
                            }
                        }
                    }

                } else {
                    val lineNumber =
                        orderTransactionDao.getNextLineNumber(documentSeries = orderTx.documentSeries, documentNumber = orderTx.documentNumber)

                    // If a record does not exist for the same document
                    // create a new record
                    val toInsert = OrderEntity.create(
                        orderDate = orderTx.orderDate,
                        documentSeries = orderTx.documentSeries,
                        documentNumber = orderTx.documentNumber,
                        documentRowNumber = lineNumber,
                        stockId = orderTx.stockId,
                        stockCode = orderTx.stockCode,
                        stockName = orderTx.stockName,
                        barcode = orderTx.barcode,
                        companyId = orderTx.companyId,
                        companyCode = orderTx.companyCode,
                        companyName = orderTx.companyName,
                        paymentPlanNumber = orderTx.paymentPlanNumber,
                        warehouseId = orderTx.warehouseId,
                        warehouseNumber = orderTx.warehouseNumber,
                        warehouseName = orderTx.warehouseName,
                        quantity = orderTx.quantity,
                        unitPrice = orderTx.unitPrice,
                        currencyType = orderTx.currencyType,
                        discount1 = orderTx.discount1,
                        discount2 = orderTx.discount2,
                        discount3 = orderTx.discount3,
                        discount4 = orderTx.discount4,
                        discount5 = orderTx.discount5,
                        totalPrice = orderTx.totalPrice,
                        taxPointer = orderTx.taxPointer,
                        currentResponsibilityCenter = orderTx.currentResponsibilityCenter,
                        stockResponsibilityCenter = orderTx.stockResponsibilityCenter,
                        remainingQuantity = orderTx.remainingQuantity,
                        deliveredQuantity = orderTx.deliveredQuantity,
                        isColoredAndSized = orderTx.isColoredAndSized,
                        syncStatus = orderTx.syncStatus,
                        dataOrigin = orderTx.dataOrigin,
                        userCode = orderTx.userCode
                    )

                    val insertedRow = orderTransactionDao.add(toInsert)

                    if (insertedRow <= 0) throw Exception("Sipariş hareketi kaydı oluşturulurken bir hata oluştu.")

                    if (sizeTxs.isNotEmpty()) {
                        val toInsertSizeTxs = sizeTxs.map {
                            SizeTransactionEntity.create(
                                it.barcode, toInsert.id, it.sizeTransactionType, it.quantity
                            )
                        }

                        toInsertSizeTxs.forEach { sizeTx ->
                            val existSizeTx = sizeTransactionDao.getByBarcodeAndRefRecord(
                                barcode = sizeTx.barcode, refRecordId = sizeTx.refRecordId, sizeTransactionType = sizeTx.sizeTransactionType
                            )

                            if (existSizeTx != null) {
                                sizeTransactionDao.update(existSizeTx.copy(quantity = existSizeTx.quantity + sizeTx.quantity))
                            } else {
                                sizeTransactionDao.add(sizeTx)
                            }
                        }
                    }

                }

            } catch (e: Exception) {
                throw e
            }


        }
    }


    override suspend fun update(product: ProductDataModel) {
        try {
            orderTransactionDao.update(product.toEntity())
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun update(orderTx: OrderTransactionDataModel) {
        try {
            orderTransactionDao.update(orderTx.toEntity())
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
        orderTransactionDao.updateOrderDocumentNumber(
            documentSeries, oldDocumentNumber, newDocumentNumber
        )
    }

    override fun getAllDocuments(
        documentSeriesAndNumber: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<ProductDataModel>> {
        val documentsStr = documentSeriesAndNumber.map { it -> "${it.first}-${it.second}" }
        return orderTransactionDao.getAllByDocuments(documentsStr, warehouseNumber).map { products ->
            products.map { it.toProductDataModel() }
        }
    }

    override fun getOrderTxsByDocuments(
        documents: List<Pair<String, Int>>, warehouseNumber: Int
    ): Flow<List<OrderTransactionDataModel>> {
        return orderTransactionDao.getAllByDocuments(documents.map { it -> "${it.first}-${it.second}" }, warehouseNumber)
            .map { list -> list.toDataModel() }
    }

    override suspend fun getProductsByBarcode(
        barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): List<ProductDataModel> {
        val documentsStr = selectedDocuments.map { it -> "${it.first}-${it.second}" }
        return orderTransactionDao.getProductsByBarcode(barcode, documentsStr, warehouseNumber).map { it.toProductDataModel() }
    }

    override suspend fun getProductByBarcode(
        barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): GetProductByBarcodeDataModel? {
        val product = orderTransactionDao.getProductByBarcode(barcode, selectedDocuments.map { it -> "${it.first}-${it.second}" }, warehouseNumber)
        return product?.toDataModel()
    }

    override suspend fun getLatestOrderByBarcode(
        barcode: String, selectedDocuments: List<String>, warehouseNumber: Int
    ): ProductDataModel? {
        return orderTransactionDao.getLatestOrderByBarcode(barcode, selectedDocuments, warehouseNumber)?.toProductDataModel()
    }

    override suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int {
        return orderTransactionDao.countByDocumentSeriesAndNumber(documentSeries, documentNumber)
    }

    override suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        orderTransactionDao.updateOrderSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override fun getUnsyncedOrdersFlow(): Flow<List<OrderTransactionDataModel>> {
        return orderTransactionDao.getBySyncStatus("Aktarılacak").map { list -> list.map { it.toDataModel() } }
    }

    override suspend fun getUnsyncedOrders(docSeries: String, docNumber: Int): List<OrderTransactionDataModel> {
        return orderTransactionDao.getUnsyncedOrders(docSeries, docNumber).map { it.toDataModel() }
    }

    override suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): GetNextDocumentSeriesAndNumberDataModel {

        val nextDocumentNumber = orderTransactionDao.getNextAvailableDocumentNumber(documentSeries)

        return if (nextDocumentNumber == null) GetNextDocumentSeriesAndNumberDataModel(documentSeries, 1)
        else GetNextDocumentSeriesAndNumberDataModel(documentSeries, nextDocumentNumber + 1)
    }

    override suspend fun markOrderTransactionSynced(orderTx: OrderTransactionDataModel) {
        orderTransactionDao.markOrderTransactionSynced(orderTx.id, orderTx.barcode)
    }

    override suspend fun markPending(orderTxDoc: OrderTransactionDocumentDataModel): Int = db.withTransaction {
        try {
            // update new order transactions as untransferred ("Yeni Kayıt" to "Aktarılacak")
            orderTransactionDao.updateOrderTxsAsUntransferred(orderTxDoc.docSeries, orderTxDoc.docNumber)
        } catch (e: Exception) {
            throw e
        }
    }


}