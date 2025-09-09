package tr.com.cetinkaya.domain.usecase.stock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel
import tr.com.cetinkaya.domain.repository.StockRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import java.util.Date

class GetStockBuyingConditionUseCase(
    configuration: Configuration, private val stockRepository: StockRepository
) : UseCase<GetStockBuyingConditionUseCase.Request, GetStockBuyingConditionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val stockBuyingCondition =
            stockRepository.getStockBuyingCondition(request.currentCode, request.stockCode, request.date, request.warehouseNumber)
        emit(Response(stockBuyingCondition))
    }


    data class Request(val currentCode: String?, val stockCode: String, val date: Long, val warehouseNumber: Int) : UseCase.Request
    data class Response(val stockBuyingConditionUseCase: GetStockBuyingConditionDomainModel) : UseCase.Response
}