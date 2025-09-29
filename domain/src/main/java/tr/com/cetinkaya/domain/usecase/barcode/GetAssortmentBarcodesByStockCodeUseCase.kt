package tr.com.cetinkaya.domain.usecase.barcode

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.barcode.GetAssortmentBarcodeByStockCodeDomainModel
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import tr.com.cetinkaya.domain.repository.StockRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetAssortmentBarcodesByStockCodeUseCase(
    configuration: Configuration, private val stockRepo: BarcodeDefinitionRepository
) : UseCase<GetAssortmentBarcodesByStockCodeUseCase.Request, GetAssortmentBarcodesByStockCodeUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {

        val data = stockRepo.getAssortmentBarcodeByStockCode(request.stockCode)

        emit(Response(data.barcode))

    }


    data class Request(val stockCode: String) : UseCase.Request
    data class Response(val barcode: String) : UseCase.Response
}