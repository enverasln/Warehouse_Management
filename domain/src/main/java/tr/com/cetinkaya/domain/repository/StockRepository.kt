package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.domain.model.barcode.GetAssortmentBarcodeByStockCodeDomainModel
import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel

interface StockRepository {
    suspend fun getStockBuyingCondition(currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int) : GetStockBuyingConditionDomainModel
}