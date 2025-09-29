package tr.com.cetinkaya.data_repository.repository

import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockDataSource
import tr.com.cetinkaya.data_repository.models.stock.toDomainModel
import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel
import tr.com.cetinkaya.domain.repository.StockRepository
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val remoteStockDataSource: RemoteStockDataSource
) : StockRepository {

    override suspend fun getStockBuyingCondition(
        currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int
    ): GetStockBuyingConditionDomainModel {
        val stock = remoteStockDataSource.getStockBuyingCondition(currentCode, stockCode, date, warehouseNumber)
        val mappedStock = stock.toDomainModel()
        return mappedStock
    }
}