package tr.com.cetinkaya.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.auth.LoginUseCase
import tr.com.cetinkaya.domain.usecase.order.AddOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.GetNextOrderDocumentSeriesAndNumberUseCase
import tr.com.cetinkaya.domain.usecase.order.GetPlannedGoodsAcceptanceDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.order.GetProductByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.order.GetProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.GetUnsyncedOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.ObservePlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.SyncPlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.TransferOrdersUseCase
import tr.com.cetinkaya.domain.usecase.order.UpdateOrderSyncStatusUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetUnsyncedStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.TransferStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.UpdateStockTransactionSyncStatusUseCase

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    fun provideUseCaseConfiguration(): UseCase.Configuration = UseCase.Configuration(Dispatchers.IO)

    @Provides
    fun provideLoginUseCase(configuration: UseCase.Configuration, authRepository: AuthRepository): LoginUseCase =
        LoginUseCase(configuration, authRepository)

    @Provides
    fun provideGetPlannedGoodsAcceptanceDocumentsUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): GetPlannedGoodsAcceptanceDocumentsUseCase = GetPlannedGoodsAcceptanceDocumentsUseCase(configuration, orderRepository)

    @Provides
    fun provideGetLoggedUserUseCase(configuration: UseCase.Configuration, authRepository: AuthRepository): GetLoggedUserUseCase =
        GetLoggedUserUseCase(configuration, authRepository)

    @Provides
    fun provideCheckDocumentIsUsableUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): CheckDocumentIsUsableUseCase = CheckDocumentIsUsableUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetPlannedGoodsAcceptanceProductsUseCase(configuration: UseCase.Configuration, orderRepository: OrderRepository): GetProductsUseCase =
        GetProductsUseCase(configuration, orderRepository)

    @Provides
    fun provideAddStockTransactionUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): AddStockTransactionUseCase = AddStockTransactionUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideSyncPlannedGoodsAcceptanceProductsUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): SyncPlannedGoodsAcceptanceProductsUseCase = SyncPlannedGoodsAcceptanceProductsUseCase(configuration, orderRepository)

    @Provides
    fun provideObservePlannedGoodsAcceptanceProductsUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): ObservePlannedGoodsAcceptanceProductsUseCase = ObservePlannedGoodsAcceptanceProductsUseCase(configuration, orderRepository)

    @Provides
    fun provideGetProductByBarcode(configuration: UseCase.Configuration, orderRepository: OrderRepository): GetProductByBarcodeUseCase =
        GetProductByBarcodeUseCase(configuration, orderRepository)

    @Provides
    fun provideGetStockTransactionsByDocumentUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionsByDocumentUseCase = GetStockTransactionsByDocumentUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideAddOrderUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): AddOrderUseCase = AddOrderUseCase(configuration, orderRepository)

    @Provides
    fun provideGetNextDocumentSeriesAndNumberUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): GetNextOrderDocumentSeriesAndNumberUseCase = GetNextOrderDocumentSeriesAndNumberUseCase(configuration, orderRepository)

    @Provides
    fun provideUpdateOrderSyncStatusUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): UpdateOrderSyncStatusUseCase = UpdateOrderSyncStatusUseCase(configuration, orderRepository)

    @Provides
    fun provideUpdateStockTransactionSyncStatusUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): UpdateStockTransactionSyncStatusUseCase = UpdateStockTransactionSyncStatusUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetUnsyncedStockTransactionsUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetUnsyncedStockTransactionsUseCase = GetUnsyncedStockTransactionsUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideTransferStockTransactionsUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): TransferStockTransactionsUseCase = TransferStockTransactionsUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetUnsyncedOrderUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): GetUnsyncedOrderUseCase = GetUnsyncedOrderUseCase(configuration, orderRepository)

    @Provides
    fun provideTransferOrdersUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): TransferOrdersUseCase = TransferOrdersUseCase(configuration, orderRepository)
}


