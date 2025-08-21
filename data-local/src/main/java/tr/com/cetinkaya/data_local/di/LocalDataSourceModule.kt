package tr.com.cetinkaya.data_local.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_local.source.LocalAuthDataSourceImpl
import tr.com.cetinkaya.data_local.source.LocalOrderDataSourceImpl
import tr.com.cetinkaya.data_local.source.LocalStockTransactionDataSourceImpl
import tr.com.cetinkaya.data_local.source.LocalTransferredDocumentDataSourceImpl
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bind(authDataSourceImpl: LocalAuthDataSourceImpl): LocalAuthDataSource

    @Binds
    abstract fun bindLocalOrderDataSource(localOrderDataSourceImpl: LocalOrderDataSourceImpl): LocalOrderDataSource

    @Binds
    abstract fun bindLocalStockTransactionDataSource(localStockTransactionDataSourceImpl: LocalStockTransactionDataSourceImpl): LocalStockTransactionDataSource

    @Binds
    abstract fun bindLocalTransferredDocumentDataSource(localTransferredDocumentDataSourceImpl: LocalTransferredDocumentDataSourceImpl): LocalTransferredDocumentDataSource

}