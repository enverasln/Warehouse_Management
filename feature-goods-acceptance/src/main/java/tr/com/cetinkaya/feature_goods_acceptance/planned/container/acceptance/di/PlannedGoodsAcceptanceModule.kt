package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.ColoredSizeAwareSizeTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.DefaultOrderTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.DefaultStockTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.OrderTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.SizeTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.StockTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.OverQuantityPolicy
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.QuantityAllocator
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.RemainingFirstQuantityAllocator

@Module
@InstallIn(ViewModelComponent::class)
object PlannedGoodsAcceptanceModule {
    @Provides fun provideQuantityAllocator(): QuantityAllocator = RemainingFirstQuantityAllocator()
    @Provides fun provideStockTxFactory(): StockTransactionFactory = DefaultStockTransactionFactory()
    @Provides fun provideOrderTxFactory(): OrderTransactionFactory = DefaultOrderTransactionFactory()
    @Provides fun provideSizeTxFactory(): SizeTransactionFactory = ColoredSizeAwareSizeTransactionFactory()
    @Provides fun provideOverQtyPolicy(): OverQuantityPolicy = OverQuantityPolicy.AskUser
}