package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel
import java.util.Date

interface StockRepository {

    suspend fun getStockBuyingCondition(currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int) : GetStockBuyingConditionDomainModel
}