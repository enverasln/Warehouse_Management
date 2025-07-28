package tr.com.cetinkaya.data_repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_repository.repository.AuthRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.BarcodeDefinitionRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.OrderRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.StockTransactionRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.WarehouseRepositoryImpl
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.WarehouseRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(authRepository: AuthRepositoryImpl) : AuthRepository

    @Binds
    abstract fun bindOrderRepository(orderRepository: OrderRepositoryImpl) : OrderRepository

    @Binds
    abstract fun bindStockTransactionRepository(stockTransactionRepository: StockTransactionRepositoryImpl) : StockTransactionRepository

    @Binds
    abstract fun bindsWarehouseRepository(warehouseRepository: WarehouseRepositoryImpl) : WarehouseRepository

    @Binds
    abstract fun bindsBarcodeDefinitionRepository(barcodeDefinitionRepository: BarcodeDefinitionRepositoryImpl) : BarcodeDefinitionRepository
}