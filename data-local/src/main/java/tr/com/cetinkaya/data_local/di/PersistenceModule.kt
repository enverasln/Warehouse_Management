package tr.com.cetinkaya.data_local.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tr.com.cetinkaya.data_local.db.AppDatabase
import tr.com.cetinkaya.data_local.source.LocalAuthDataSourceImpl
import java.util.concurrent.Executors
import javax.inject.Singleton

private val Context.datastore by preferencesDataStore(name = "tr.com.cetinkaya.depoyonetim.prefs")

@Module
@InstallIn(SingletonComponent::class)
class PersistenceModule {

    @Provides
    fun provideLocalAuthDataSourceImpl(@ApplicationContext context: Context) = LocalAuthDataSourceImpl(context.datastore)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "warehouse_management.db"
    )


        /*.addMigrations(
        AppDatabase.MIGRATION_1_2,
        AppDatabase.MIGRATION_2_3,
        AppDatabase.MIGRATION_3_4,
        AppDatabase.MIGRATION_4_5,
        AppDatabase.MIGRATION_5_6,
        AppDatabase.MIGRATION_6_7,
        AppDatabase.MIGRATION_7_8,
        AppDatabase.MIGRATION_8_9,
        AppDatabase.MIGRATION_9_10
    )*/.build()

    @Provides
    fun provideOderDao(appDatabase: AppDatabase) = appDatabase.orderDao

    @Provides
    fun provideStockTransactionDao(appDatabase: AppDatabase) = appDatabase.stockTransactionDao

    @Provides
    fun provideTransferredDocumentDao(appDatabase: AppDatabase) = appDatabase.transferredDocumentDao

    @Provides
    fun provideSizeTransactionDao(appDatabase: AppDatabase) = appDatabase.sizeTransactionDao

}