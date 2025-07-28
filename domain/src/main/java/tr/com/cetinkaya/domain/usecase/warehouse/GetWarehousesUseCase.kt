package tr.com.cetinkaya.domain.usecase.warehouse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel
import tr.com.cetinkaya.domain.repository.WarehouseRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetWarehousesUseCase(
    configuration: Configuration, private val warehouseRepository: WarehouseRepository
) : UseCase<GetWarehousesUseCase.Request, GetWarehousesUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = warehouseRepository.getWarehouses().map {
        Response(it)
    }


    data object Request : UseCase.Request
    data class Response(val warehouses: List<WarehouseDomainModel>) : UseCase.Response
}


