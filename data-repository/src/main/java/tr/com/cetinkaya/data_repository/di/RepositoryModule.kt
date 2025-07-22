package tr.com.cetinkaya.data_repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_repository.repository.AuthRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.OrderRepositoryImpl
import tr.com.cetinkaya.data_repository.repository.StockTransactionRepositoryImpl
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(authRepository: AuthRepositoryImpl) : AuthRepository

    @Binds
    abstract fun bindOrderRepository(orderRepository: OrderRepositoryImpl) : OrderRepository

    @Binds
    abstract fun bindStockTransactionRepository(stockTransactionRepository: StockTransactionRepositoryImpl) : StockTransactionRepository
}