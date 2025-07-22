package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.order.GetProductByBarcodeDataModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel

interface LocalOrderDataSource {

    suspend fun addOrders(products: List<ProductDataModel>)

    suspend fun addOrder(product: ProductDataModel)

    suspend fun update(product: ProductDataModel)

    fun getAllDocuments(documentSeriesAndNumber: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<ProductDataModel>>

    suspend fun getProductsByBarcode(barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int): List<ProductDataModel>

    suspend fun getProductByBarcode(barcode: String, selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int): GetProductByBarcodeDataModel?

    suspend fun getLatestOrderByBarcode(barcode: String, selectedDocuments: List<String>, warehouseNumber: Int): ProductDataModel?

    suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int

    suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    fun getUnsyncedOrders(): Flow<List<ProductDataModel>>

}