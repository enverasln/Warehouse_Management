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
import tr.com.cetinkaya.data_remote.api.AuthService
import tr.com.cetinkaya.data_remote.api.BarcodeDefinitionService
import tr.com.cetinkaya.data_remote.api.OrderService
import tr.com.cetinkaya.data_remote.api.StockTransactionService
import tr.com.cetinkaya.data_remote.api.WarehouseService
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(
            Date::class.java,
            object : JsonDeserializer<Date> {
                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    return json?.asString?.let { dateFormat.parse(it) }
                }
            }
        )
        .create()

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
//            .baseUrl("http://192.127.2.140:4300/")
//            .baseUrl("http://192.127.1.82:4300/")Mr
            .baseUrl("http://192.168.68.55:4300/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    fun provideOrderService(retrofit: Retrofit): OrderService = retrofit.create(OrderService::class.java)

    @Provides
    fun provideStockTransactionService(retrofit: Retrofit): StockTransactionService = retrofit.create(StockTransactionService::class.java)

    @Provides
    fun provideWarehouseService(retrofit: Retrofit) : WarehouseService = retrofit.create(WarehouseService::class.java)

    @Provides
    fun provinceBarcodeDefinitionService(retrofit: Retrofit) : BarcodeDefinitionService = retrofit.create(BarcodeDefinitionService::class.java)
}