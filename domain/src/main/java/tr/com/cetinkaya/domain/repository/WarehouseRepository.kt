package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel

interface WarehouseRepository {
    fun getWarehouses() : Flow<List<WarehouseDomainModel>>
}