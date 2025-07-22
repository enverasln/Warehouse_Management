package tr.com.cetinkaya.common.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateConverter {

    @SuppressLint("ConstantLocale")
    private val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
        // timeZone = TimeZone.getTimeZone("UTC")
    }

    @SuppressLint("ConstantLocale")
    private val uiFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    /**
     * API'den gelen tarih (yyyy-MM-ddTHH:mm:ss) -> Long(timestamp)
     */
    fun apiTimestamp(apiDate: String): Long? {
        return try {
            apiFormat.parse(apiDate)?.time
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Long(timestamp) -> UI Tarih (dd.MM.yyyy)
     */
    fun timestampToUi(date: Long): String {
        return uiFormat.format(Date(date))
    }

    /**
     * UI (dd.MM.yy) ->
     */
    fun uiToTimestamp(dateStr: String) : Long? {
        return try {
            uiFormat.parse(dateStr)?.time
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Long(timestamp) -> API (yyyy-MM-ddTHH:mm:sss)
     */
    fun timeStampToApi(date: Long): String {
        return apiFormat.format(Date(date))
    }
}