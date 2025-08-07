package tr.com.cetinkaya.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.DocumentSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.NormalGivenOrderSyncHandler
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.NormalPurchaseStockTransactionSyncHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SyncHandlerModule {


    @Provides
    @Singleton
    @IntoMap
    @DocumentSyncHandlerKey(TransferredDocumentTypes.NormalGivenOrder)
    fun provideNormalGivenOrderSyncHandler(
        orderRepository: OrderRepository,
        transferredDocumentRepository: TransferredDocumentRepository
    ): DocumentSyncHandler = NormalGivenOrderSyncHandler(
        orderRepository = orderRepository, transferredDocumentRepository = transferredDocumentRepository
    )

    @Provides
    @Singleton
    @IntoMap
    @DocumentSyncHandlerKey(TransferredDocumentTypes.NormalPurchaseDispatch)
    fun provideNormalPurchaseDispatchSyncHandler(
        stockTransactionRepository: StockTransactionRepository,
        transferredDocumentRepository: TransferredDocumentRepository
    ): DocumentSyncHandler = NormalPurchaseStockTransactionSyncHandler(
        stockTransactionRepository = stockTransactionRepository, transferredDocumentRepository = transferredDocumentRepository
    )



}