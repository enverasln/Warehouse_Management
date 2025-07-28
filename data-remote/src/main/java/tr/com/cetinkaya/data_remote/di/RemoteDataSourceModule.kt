package tr.com.cetinkaya.data_remote.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_remote.data_source.RemoteAuthDataSourceImpl
import tr.com.cetinkaya.data_remote.data_source.RemoteBarcodeDefinitionDataSourceImpl
import tr.com.cetinkaya.data_remote.data_source.RemoteOrderDataSourceImpl
import tr.com.cetinkaya.data_remote.data_source.RemoteStockTransactionDataSourceImpl
import tr.com.cetinkaya.data_remote.data_source.RemoteWarehouseDataSourceImpl
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteBarcodeDefinitionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteOrderDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteWarehouseDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindAuthDataSource(authDataSource: RemoteAuthDataSourceImpl): RemoteAuthDataSource

    @Binds
    abstract fun bindOrderDataSource(orderDataSource: RemoteOrderDataSourceImpl): RemoteOrderDataSource

    @Binds
    abstract fun bindStockTransactionDataSource(stockTransactionDataSource: RemoteStockTransactionDataSourceImpl): RemoteStockTransactionDataSource

    @Binds
    abstract fun bindWarehouseDataSource(warehouseDataSource: RemoteWarehouseDataSourceImpl): RemoteWarehouseDataSource

    @Binds
    abstract fun bindBarcodeDefinitionDataSource(barcodeDefinitionDataSource: RemoteBarcodeDefinitionDataSourceImpl): RemoteBarcodeDefinitionDataSource
}