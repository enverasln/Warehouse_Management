package tr.com.cetinkaya.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.DocumentSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.NormalGivenOrderSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.NormalPurchaseStockTransactionSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.WarehouseShipmentDocumentSyncHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SyncHandlerModule {


    @Provides
    @Singleton
    @IntoMap
    @DocumentSyncHandlerKey(TransferredDocumentType.NormalGivenOrder)
    fun provideNormalGivenOrderSyncHandler(
        orderRepository: OrderTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository,
        sizeTransactionRepository: SizeTransactionRepository
    ): DocumentSyncHandler = NormalGivenOrderSyncHandler(
        orderTxRepo = orderRepository,
        transferredDocRepo = transferredDocumentRepository,
        sizeTxRepo = sizeTransactionRepository
    )

    @Provides
    @Singleton
    @IntoMap
    @DocumentSyncHandlerKey(TransferredDocumentType.NormalPurchaseDispatch)
    fun provideNormalPurchaseDispatchSyncHandler(
        stockTransactionRepository: StockTransactionRepository,
        sizeTransactionRepository: SizeTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository
    ): DocumentSyncHandler = NormalPurchaseStockTransactionSyncHandler(
        stockTransactionRepository = stockTransactionRepository,
        sizeTransactionRepository = sizeTransactionRepository,
        transferredDocumentRepository = transferredDocumentRepository
    )

    @Provides
    @Singleton
    @IntoMap
    @DocumentSyncHandlerKey(TransferredDocumentType.WarehouseShipmentDocument)
    fun provideWarehouseShipmentDispatchHandler(
        stockTransactionRepository: StockTransactionRepository,
        sizeTransactionRepository: SizeTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository
    ): DocumentSyncHandler = WarehouseShipmentDocumentSyncHandler(
        stockTransactionRepository = stockTransactionRepository,
        sizeTransactionRepository = sizeTransactionRepository,
        transferredDocumentRepository = transferredDocumentRepository
    )

}