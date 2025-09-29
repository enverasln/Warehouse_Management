package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.domain.model.barcode.BarcodeDefinitionDomainModel
import tr.com.cetinkaya.domain.model.barcode.GetAssortmentBarcodeByStockCodeDomainModel

interface BarcodeDefinitionRepository {

    fun getByBarcode(barcode: String, warehouse: Int) : Flow<BarcodeDefinitionDomainModel>

    suspend fun getAssortmentBarcodeByStockCode(stockCode: String) : GetAssortmentBarcodeByStockCodeDomainModel
}