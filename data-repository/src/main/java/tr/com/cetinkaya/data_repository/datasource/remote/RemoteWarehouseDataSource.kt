package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.warehouse.WarehouseDataModel

interface RemoteWarehouseDataSource {

    fun getWarehouses() : Flow<List<WarehouseDataModel>>
}