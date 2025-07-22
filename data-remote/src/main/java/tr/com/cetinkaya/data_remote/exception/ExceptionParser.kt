package tr.com.cetinkaya.data_remote.exception

import com.google.gson.Gson
import okhttp3.ResponseBody
import tr.com.cetinkaya.data_remote.models.ApiErrorResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionParser @Inject constructor(private val gson: Gson) {
    fun parse(responseBody: ResponseBody?) : ApiErrorResponse? {
        return try {
            responseBody?.let {
                gson.fromJson(it.charStream(), ApiErrorResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
}