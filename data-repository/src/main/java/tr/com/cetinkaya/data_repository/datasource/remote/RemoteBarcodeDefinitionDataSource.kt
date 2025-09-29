package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.barcode.GetBarcodeDefinitionByBarcodeDataModel
import tr.com.cetinkaya.data_repository.models.barcode.GetAssortmentBarcodeByStockCodeDataModel

interface RemoteBarcodeDefinitionDataSource {

    fun getByBarcode(barcode: String, warehouse: Int) : Flow<GetBarcodeDefinitionByBarcodeDataModel>

    suspend fun getAssortmentBarcodeByStockCode(stockCode: String): GetAssortmentBarcodeByStockCodeDataModel
}
