package tr.com.cetinkaya.data_remote.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.utils.EnumByCodeDeserializer
import tr.com.cetinkaya.data_remote.BuildConfig
import tr.com.cetinkaya.data_remote.api.AuthService
import tr.com.cetinkaya.data_remote.api.BarcodeDefinitionService
import tr.com.cetinkaya.data_remote.api.OrderService
import tr.com.cetinkaya.data_remote.api.SizeTransactionService
import tr.com.cetinkaya.data_remote.api.StockService
import tr.com.cetinkaya.data_remote.api.StockTransactionService
import tr.com.cetinkaya.data_remote.api.WarehouseService
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        //.readTimeout(60, TimeUnit.SECONDS)
        //.connectTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().registerTypeAdapter(StockTransactionType::class.java, EnumByCodeDeserializer(StockTransactionType::from))
        .registerTypeAdapter(StockTransactionKind::class.java, EnumByCodeDeserializer(StockTransactionKind::from))
        .registerTypeAdapter(StockTransactionDocumentType::class.java, EnumByCodeDeserializer(StockTransactionDocumentType::from))
        .registerTypeAdapter(OrderTransactionTypes::class.java, EnumByCodeDeserializer(OrderTransactionTypes::from))
        .registerTypeAdapter(OrderTransactionKinds::class.java, EnumByCodeDeserializer(OrderTransactionKinds::from))
        .registerTypeAdapter(SizeTransactionType::class.java, EnumByCodeDeserializer(SizeTransactionType::from))
        .registerTypeAdapter(
            Date::class.java, object : JsonDeserializer<Date> {
                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    return json?.asString?.let { dateFormat.parse(it) }
                }
            }).create()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build()

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideOrderService(retrofit: Retrofit): OrderService = retrofit.create(OrderService::class.java)

    @Provides
    @Singleton
    fun provideStockTransactionService(retrofit: Retrofit): StockTransactionService = retrofit.create(StockTransactionService::class.java)

    @Provides
    @Singleton
    fun provideWarehouseService(retrofit: Retrofit): WarehouseService = retrofit.create(WarehouseService::class.java)

    @Provides
    @Singleton
    fun provideeBarcodeDefinitionService(retrofit: Retrofit): BarcodeDefinitionService = retrofit.create(BarcodeDefinitionService::class.java)

    @Provides
    @Singleton
    fun provideSizeTransactionService(retrofit: Retrofit): SizeTransactionService = retrofit.create(SizeTransactionService::class.java)

    @Provides
    @Singleton
    fun provideStockService(retrofit: Retrofit) : StockService = retrofit.create(StockService::class.java)
}