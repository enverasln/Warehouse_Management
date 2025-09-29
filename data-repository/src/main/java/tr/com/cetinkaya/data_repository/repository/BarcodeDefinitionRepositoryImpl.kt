package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteBarcodeDefinitionDataSource
import tr.com.cetinkaya.data_repository.models.barcode.toDomainModel
import tr.com.cetinkaya.domain.model.barcode.BarcodeDefinitionDomainModel
import tr.com.cetinkaya.domain.model.barcode.GetAssortmentBarcodeByStockCodeDomainModel
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import javax.inject.Inject

class BarcodeDefinitionRepositoryImpl @Inject constructor(
    private val remoteBarcodeDefinitionDataSource: RemoteBarcodeDefinitionDataSource
) : BarcodeDefinitionRepository {

    override fun getByBarcode(
        barcode: String, warehouse: Int
    ): Flow<BarcodeDefinitionDomainModel> = remoteBarcodeDefinitionDataSource.getByBarcode(barcode, warehouse).map {
        it.toDomainModel()
    }

    override suspend fun getAssortmentBarcodeByStockCode(stockCode: String): GetAssortmentBarcodeByStockCodeDomainModel {
        val stock = remoteBarcodeDefinitionDataSource.getAssortmentBarcodeByStockCode(stockCode)
        val mappedStock = stock.toDomainModel()
        return mappedStock
    }
}