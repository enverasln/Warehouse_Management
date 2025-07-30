package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteWarehouseDataSource
import tr.com.cetinkaya.data_repository.models.warehouse.toDomainModel
import tr.com.cetinkaya.domain.model.warehouse.WarehouseDomainModel
import tr.com.cetinkaya.domain.repository.WarehouseRepository
import javax.inject.Inject

class WarehouseRepositoryImpl @Inject constructor(
    private val remoteWarehouseDataSource: RemoteWarehouseDataSource
) : WarehouseRepository {
    override fun getWarehouses(): Flow<List<WarehouseDomainModel>> = remoteWarehouseDataSource.getWarehouses().map { warehouses ->
        warehouses.map { it.toDomainModel() }
    }
}