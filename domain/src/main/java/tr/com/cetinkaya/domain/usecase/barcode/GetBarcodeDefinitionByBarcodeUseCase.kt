package tr.com.cetinkaya.domain.usecase.barcode

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.barcode.GetBarcodeDefinitionByBarcodeDomainModel
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetBarcodeDefinitionByBarcodeUseCase(
    configuration: Configuration, private val barcodeDefinitionRepository: BarcodeDefinitionRepository
) : UseCase<GetBarcodeDefinitionByBarcodeUseCase.Request, GetBarcodeDefinitionByBarcodeUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> =
        barcodeDefinitionRepository
            .getByBarcode(request.barcode, request.warehouse).map {


                Response(it)
            }


    data class Request(val barcode: String, val warehouse: Int) : UseCase.Request
    data class Response(val barcodeDefinition: GetBarcodeDefinitionByBarcodeDomainModel) : UseCase.Response
}