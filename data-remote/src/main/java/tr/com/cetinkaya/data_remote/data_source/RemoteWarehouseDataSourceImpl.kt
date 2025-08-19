package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.data_remote.api.WarehouseService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.warehouse.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteWarehouseDataSource
import tr.com.cetinkaya.data_repository.models.warehouse.WarehouseDataModel
import javax.inject.Inject

class RemoteWarehouseDataSourceImpl @Inject constructor(
    private val warehouseService: WarehouseService, private val errorParser: ExceptionParser
) : RemoteWarehouseDataSource {

    override fun getWarehouses(): Flow<List<WarehouseDataModel>> = flow {
        val warehouses = mutableListOf<WarehouseDataModel>()

        var pageIndex = 0
        val pageSize = 100

        do {
            val response = warehouseService.getWarehouses(pageIndex, pageSize)


            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val remoteWarehouses = responseBody.items
                    val dataWarehouses = remoteWarehouses.map { it.toDataModel() }
                    warehouses.addAll(dataWarehouses)
                }
            } else {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            pageIndex++
        } while (response.body()?.hasNext == true)
        emit(warehouses)
    }

    override fun getWarehouseByWarehouseNumber(warehouseNumber: Int): Flow<WarehouseDataModel> = flow {
        try {
            val response = warehouseService.getWarehouseByWarehouseNumber(warehouseNumber)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val remoteWarehouse = responseBody
                    val dataWarehouse = remoteWarehouse.toDataModel()
                    emit(dataWarehouse)
                }
            } else {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }

        } catch (e: Exception) {
            throw e
        }
    }
}