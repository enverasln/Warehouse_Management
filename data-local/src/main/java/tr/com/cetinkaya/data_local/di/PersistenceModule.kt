package tr.com.cetinkaya.data_local.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_local.db.AppDatabase
import tr.com.cetinkaya.data_local.source.LocalAuthDataSourceImpl

private val Context.datastore by preferencesDataStore(name = "tr.com.cetinkaya.depoyonetim.prefs")

@Module
@InstallIn(SingletonComponent::class)
class PersistenceModule {


    @Provides
    fun provideLocalAuthDataSourceImpl(@ApplicationContext context: Context) = LocalAuthDataSourceImpl(context.datastore)

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
            context, AppDatabase::class.java, "warehouse_management.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5).build()

    @Provides
    fun provideOderDao(appDatabase: AppDatabase) = appDatabase.orderDao

    @Provides
    fun provideStockTransactionDao(appDatabase: AppDatabase) = appDatabase.stockTransactionDao

    @Provides
    fun provideTransferredDocumentDao(appDatabase: AppDatabase) = appDatabase.transferredDocumentDao
}