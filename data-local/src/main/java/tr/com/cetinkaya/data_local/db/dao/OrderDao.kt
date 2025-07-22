package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_local.db.entities.OrderEntity
import tr.com.cetinkaya.data_local.models.order.GetProductByBarcodeLocalModel

@Dao
interface OrderDao {
    @Upsert
    suspend fun addRange(orders: List<OrderEntity>): List<Long>

    @Insert
    suspend fun add(order: OrderEntity): Long

    @Update
    suspend fun update(order: OrderEntity): Int

    @Query("SELECT * FROM orders o WHERE (o.documentSeries || '-' || o.documentNumber) IN (:documentsSeriesAndNumbers) AND o.warehouseNumber = :warehouseNumber ORDER BY o.documentSeries, o.documentNumber, o.documentRowNumber")
    fun getAllByDocuments(documentsSeriesAndNumbers: List<String>, warehouseNumber: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE (id || '#' || barcode) IN (:keys)")
    suspend fun getOrdersByIds(keys: List<String>): List<OrderEntity>

    @Query(
        """
            SELECT
                o.id, 
                o.orderDate, 
                o.documentSeries, 
                o.documentNumber, 
                o.documentRowNumber, 
                o.stockId, 
                o.stockCode, 
                o.stockName, 
                o.barcode, 
                o.companyId, 
                o.companyCode, 
                o.companyName, 
                o.paymentPlanNumber, 
                o.warehouseId, 
                o.warehouseNumber, 
                o.warehouseName, 
                o.quantity, 
                o.unitPrice, 
                o.currencyType, 
                o.discount1, 
                o.discount2, 
                o.discount3, 
                o.discount4, 
                o.discount5, 
                o.totalPrice,
                o.taxPointer,
                o.currentResponsibilityCenter,
                o.stockResponsibilityCenter,
                o.remainingQuantity,
                o.isColoredAndSized,
                o.synchronizationStatus,
                o.deliveredQuantity
            FROM 
                orders o
            WHERE
                (o.documentSeries || '-' || o.documentNumber) IN (:selectedDocuments) 
                AND o.barcode = :barcode AND o.warehouseNumber = :warehouseNumber
        """
    )
    suspend fun getProductsByBarcode(barcode: String, selectedDocuments: List<String>, warehouseNumber: Int): List<OrderEntity>


    @Query(
        """
            SELECT
                o.barcode, 
                o.stockName,
                SUM(o.remainingQuantity) qty,
                SUM(o.remainingQuantity - o.deliveredQuantity) remainingQty
            FROM 
                orders o
            WHERE
                (o.documentSeries || '-' || o.documentNumber) IN (:selectedDocuments) 
                AND o.barcode = :barcode AND o.warehouseNumber = :warehouseNumber
            GROUP BY o.barcode, o.stockName
        """
    )
    suspend fun getProductByBarcode(barcode: String, selectedDocuments: List<String>, warehouseNumber: Int): GetProductByBarcodeLocalModel?

    @Query("SELECT * FROM orders o WHERE o.warehouseNumber = :warehouseNumber AND (o.documentSeries || '-' || o.documentNumber) In (:selectedDocuments) AND barcode= :barcode ORDER BY o.orderDate DESC LIMIT 1        ")
    suspend fun getLatestOrderByBarcode(barcode: String, selectedDocuments: List<String>, warehouseNumber: Int): OrderEntity?

    @Query("SELECT COUNT(*) FROM orders o WHERE o.documentSeries = :documentSeries AND o.documentNumber = :documentNumber")
    suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int

    @Query(
        """
        UPDATE orders
        SET synchronizationStatus = :syncStatus
        WHERE documentSeries = :documentSeries AND documentNumber = :documentNumber
    """
    )
    suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String): Int

    @Query(
        """
        SELECT * FROM orders WHERE synchronizationStatus = :syncStatus
    """
    )
    fun getBySyncStatus(syncStatus: String): Flow<List<OrderEntity>>
}

