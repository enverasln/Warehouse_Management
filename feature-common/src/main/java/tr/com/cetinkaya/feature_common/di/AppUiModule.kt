package tr.com.cetinkaya.feature_common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppUiModule {
    @Provides @Singleton
    fun provideAppEventBus() : AppEventBus = AppEventBus()
}