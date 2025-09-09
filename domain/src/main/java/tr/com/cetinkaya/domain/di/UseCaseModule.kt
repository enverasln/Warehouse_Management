package tr.com.cetinkaya.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.repository.WarehouseRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.auth.LoginUseCase
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.order.AddOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.CountByDocumentSeriesAndNumberUseCase
import tr.com.cetinkaya.domain.usecase.order.GetNextOrderDocumentSeriesAndNumberUseCase
import tr.com.cetinkaya.domain.usecase.order.GetPlannedGoodsAcceptanceDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.order.GetProductByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.order.GetProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.GetUnsyncedOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.ObservePlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.SyncPlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.TransferOrdersUseCase
import tr.com.cetinkaya.domain.usecase.order.UpdateOrderSyncStatusUseCase
import tr.com.cetinkaya.domain.usecase.size_transaction.AddSizeTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock.GetStockBuyingConditionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.BuildStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CountStockTransactionByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionOldUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionDocumentByDocumentNumberUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentWithRemainingQuantityUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetUnsyncedStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.RemoveStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.TransferStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.UpdateStockTransactionSyncStatusUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.AddTransferredDocumentUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.GetUntransferredDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.RemoveTransferredDocumentUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.DocumentSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncAllDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.warehouse.GetWarehousesUseCase

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
    fun provideAddStockTransactionByBarcodeUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): AddStockTransactionByBarcodeUseCase = AddStockTransactionByBarcodeUseCase(configuration, stockTransactionRepository)

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
    fun provideGetStockTransactionsByDocumentWithRemainingQuantityUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionsByDocumentWithRemainingQuantityUseCase =
        GetStockTransactionsByDocumentWithRemainingQuantityUseCase(configuration, stockTransactionRepository)

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
        configuration: UseCase.Configuration, orderRepository: OrderRepository, transferredDocumentRepository: TransferredDocumentRepository
    ): UpdateOrderSyncStatusUseCase = UpdateOrderSyncStatusUseCase(configuration, orderRepository, transferredDocumentRepository)

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

    @Provides
    fun provideGetWarehousesUseCase(
        configuration: UseCase.Configuration, warehouseRepository: WarehouseRepository
    ): GetWarehousesUseCase = GetWarehousesUseCase(configuration, warehouseRepository)

    @Provides
    fun provideGetBarcodeDefinitionByBarcodeUseCase(
        configuration: UseCase.Configuration, barcodeDefinitionRepository: BarcodeDefinitionRepository
    ): GetBarcodeDefinitionByBarcodeUseCase = GetBarcodeDefinitionByBarcodeUseCase(configuration, barcodeDefinitionRepository)

    @Provides
    fun provideGetNextStockTransactionDocumentUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetNextStockTransactionDocumentUseCase = GetNextStockTransactionDocumentUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetStockTransactionByDocumentUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionsByDocumentUseCase = GetStockTransactionsByDocumentUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideAddTransferredDocumentUseCase(
        configuration: UseCase.Configuration, transferredDocumentRepository: TransferredDocumentRepository
    ): AddTransferredDocumentUseCase = AddTransferredDocumentUseCase(configuration, transferredDocumentRepository)

    @Provides
    fun provideFinishStockTransactionOldUseCase(
        configuration: UseCase.Configuration,
        stockTransactionRepository: StockTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository
    ): FinishStockTransactionOldUseCase = FinishStockTransactionOldUseCase(configuration, stockTransactionRepository, transferredDocumentRepository)

    @Provides
    fun provideFinishStockTransactionUseCase(
        configuration: UseCase.Configuration,
        stockTransactionRepository: StockTransactionRepository
    ): FinishStockTransactionUseCase = FinishStockTransactionUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetUntransferredDocumentsUseCase(
        configuration: UseCase.Configuration, transferredDocumentRepository: TransferredDocumentRepository
    ): GetUntransferredDocumentsUseCase = GetUntransferredDocumentsUseCase(configuration, transferredDocumentRepository)

    @Provides
    fun provideSyncAllDocumentsUseCase(
        configuration: UseCase.Configuration,
        handlers: Map<TransferredDocumentTypes, @JvmSuppressWildcards DocumentSyncHandler>,
        transferredDocumentRepository: TransferredDocumentRepository
    ): SyncAllDocumentsUseCase = SyncAllDocumentsUseCase(configuration, handlers, transferredDocumentRepository)

    @Provides
    fun provideGetStockTransactionDocumentByDocumentNumberUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionDocumentByDocumentNumberUseCase =
        GetStockTransactionDocumentByDocumentNumberUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetStockTransactionDocumentByPaperNumberAndCurrentCode(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase =
        GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideCountByDocumentSeriesAndNumber(
        configuration: UseCase.Configuration, orderRepository: OrderRepository
    ): CountByDocumentSeriesAndNumberUseCase = CountByDocumentSeriesAndNumberUseCase(configuration, orderRepository)

    @Provides
    fun provideRemoveStockTransactionUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): RemoveStockTransactionUseCase = RemoveStockTransactionUseCase(
        configuration = configuration, stockTransactionRepository = stockTransactionRepository
    )

    @Provides
    fun provideRemoveTransferredDocumentUseCase(
        configuration: UseCase.Configuration, transferredDocumentRepository: TransferredDocumentRepository
    ): RemoveTransferredDocumentUseCase = RemoveTransferredDocumentUseCase(
        configuration = configuration, transferredDocumentRepository = transferredDocumentRepository
    )


    @Provides
    fun provideCountByDocumentUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): CountStockTransactionByDocumentUseCase = CountStockTransactionByDocumentUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideBuildStockTransactionsUseCase(
        configuration: UseCase.Configuration,
    ): BuildStockTransactionsUseCase = BuildStockTransactionsUseCase(
        configuration = configuration,
    )

    @Provides
    fun provideAddSizeTransactionUseCase(
        configuration: UseCase.Configuration, sizeTransactionRepository: SizeTransactionRepository
    ): AddSizeTransactionsUseCase = AddSizeTransactionsUseCase(configuration, sizeTransactionRepository)

    @Provides
    fun provideAddStockTransactionUseCase(
        configuration: UseCase.Configuration,
        stockTransactionRepository: StockTransactionRepository,
        sizeTransactionRepository: SizeTransactionRepository
    ): AddStockTransactionUseCase = AddStockTransactionUseCase(configuration, stockTransactionRepository, sizeTransactionRepository)

    @Provides
    fun provideGetStockBuyingConditionUseCase(
        configuration: UseCase.Configuration, stockRepository: StockRepository
    ): GetStockBuyingConditionUseCase = GetStockBuyingConditionUseCase(configuration, stockRepository)

}


