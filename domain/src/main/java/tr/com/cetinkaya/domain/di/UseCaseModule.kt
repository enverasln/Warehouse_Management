package tr.com.cetinkaya.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import tr.com.cetinkaya.common.db.TransactionRunner
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.repository.BarcodeDefinitionRepository
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.repository.WarehouseRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.auth.LoginUseCase
import tr.com.cetinkaya.domain.usecase.barcode.GetAssortmentBarcodesByStockCodeUseCase
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.AddOrderTransactionUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.FetchAndSaveOrderTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.FinishOrderTransactionUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.GetNextOrderTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.GetOrderTransactionDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.GetOrderTxsByDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.size_transaction.AddSizeTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock.GetStockBuyingConditionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.BuildStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CountStockTransactionByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.DeleteStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionDocumentByDocumentNumberUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentWithRemainingQuantityUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetTransferWareHouseNumberUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetUnsyncedStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetWarehouseTransfersByDocumentUseCase
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

    // OrderTransactions
    @Provides
    fun provideGetOrderTxsByDocumentsUseCase(
        configuration: UseCase.Configuration, orderTransactionRepository: OrderTransactionRepository
    ): GetOrderTxsByDocumentsUseCase =
        GetOrderTxsByDocumentsUseCase(configuration = configuration, orderTransactionRepository = orderTransactionRepository)

    @Provides
    fun provideFetchAndSaveOrderTransactionUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderTransactionRepository
    ): FetchAndSaveOrderTransactionsUseCase = FetchAndSaveOrderTransactionsUseCase(configuration, orderRepository)

    @Provides
    fun provideAddOrderTransactionUseCase(
        configuration: UseCase.Configuration, orderTransactionRepository: OrderTransactionRepository
    ): AddOrderTransactionUseCase = AddOrderTransactionUseCase(configuration, orderTransactionRepository)

    @Provides
    fun provideFinishOrderTransactionUseCase(
        configuration: UseCase.Configuration,
        orderTxRepo: OrderTransactionRepository,
        stockTxRepo: StockTransactionRepository,
        transferredDocRepo: TransferredDocumentRepository,
        txRunner: TransactionRunner,
    ): FinishOrderTransactionUseCase = FinishOrderTransactionUseCase(configuration, orderTxRepo, stockTxRepo, transferredDocRepo, txRunner)

    @Provides
    fun provideGetPlannedGoodsAcceptanceDocumentsUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderTransactionRepository
    ): GetOrderTransactionDocumentsUseCase = GetOrderTransactionDocumentsUseCase(configuration, orderRepository)

    @Provides
    fun provideGetLoggedUserUseCase(configuration: UseCase.Configuration, authRepository: AuthRepository): GetLoggedUserUseCase =
        GetLoggedUserUseCase(configuration, authRepository)

    @Provides
    fun provideCheckDocumentIsUsableUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): CheckDocumentIsUsableUseCase = CheckDocumentIsUsableUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideAddStockTransactionByBarcodeUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): AddStockTransactionByBarcodeUseCase = AddStockTransactionByBarcodeUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetStockTransactionsByDocumentWithRemainingQuantityUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetStockTransactionsByDocumentWithRemainingQuantityUseCase =
        GetStockTransactionsByDocumentWithRemainingQuantityUseCase(configuration, stockTransactionRepository)


    @Provides
    fun provideGetNextDocumentSeriesAndNumberUseCase(
        configuration: UseCase.Configuration, orderRepository: OrderTransactionRepository
    ): GetNextOrderTransactionDocumentUseCase = GetNextOrderTransactionDocumentUseCase(configuration, orderRepository)


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
    fun provideFinishStockTransactionUseCase(
        configuration: UseCase.Configuration,
        stockTransactionRepository: StockTransactionRepository,
        sizeTransactionRepo: SizeTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository,
        txRunner: TransactionRunner
    ): FinishStockTransactionUseCase =
        FinishStockTransactionUseCase(configuration, stockTransactionRepository, sizeTransactionRepo, transferredDocumentRepository, txRunner)

    @Provides
    fun provideGetUntransferredDocumentsUseCase(
        configuration: UseCase.Configuration, transferredDocumentRepository: TransferredDocumentRepository
    ): GetUntransferredDocumentsUseCase = GetUntransferredDocumentsUseCase(configuration, transferredDocumentRepository)

    @Provides
    fun provideSyncAllDocumentsUseCase(
        configuration: UseCase.Configuration,
        handlers: Map<TransferredDocumentType, @JvmSuppressWildcards DocumentSyncHandler>,
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
    ): AddStockTransactionUseCase = AddStockTransactionUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideGetStockBuyingConditionUseCase(
        configuration: UseCase.Configuration, stockRepository: StockRepository
    ): GetStockBuyingConditionUseCase = GetStockBuyingConditionUseCase(configuration, stockRepository)

    // region StockTransaction
    @Provides
    fun provideGetWarehouseTransfersByDocumentUseCase(
        configuration: UseCase.Configuration, stockTxRepo: StockTransactionRepository
    ): GetWarehouseTransfersByDocumentUseCase = GetWarehouseTransfersByDocumentUseCase(configuration, stockTxRepo)

    @Provides
    fun provideGetTransferWarehouseNumberUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): GetTransferWareHouseNumberUseCase = GetTransferWareHouseNumberUseCase(configuration, stockTransactionRepository)

    @Provides
    fun provideDeleteStockTransactionUseCase(
        configuration: UseCase.Configuration, stockTransactionRepository: StockTransactionRepository
    ): DeleteStockTransactionUseCase = DeleteStockTransactionUseCase(configuration, stockTransactionRepository)
    // endregion

    // region Stock
    @Provides
    fun provideGetBarcodeBarcodeByStockCodeUseCase(
        configuration: UseCase.Configuration, barcodeRepo: BarcodeDefinitionRepository
    ): GetAssortmentBarcodesByStockCodeUseCase = GetAssortmentBarcodesByStockCodeUseCase(configuration, barcodeRepo)
    // endregion

}


