package tr.com.cetinkaya.domain.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.sql.Date

@Parcelize
data class UserDomainModel(
    val username: String,
    val email: String,
    val warehouseName: String,
    val warehouseNumber: Int,
    val warehouseLockDate: Long,
    val mikroFlyUserId: Int,
    val documentSeries: String,
    val newDocumentSeries: String
) : Parcelable
