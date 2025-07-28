package tr.com.cetinkaya.feature_goods_transfer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tr.com.cetinkaya.domain.model.user.UserDomainModel

@Parcelize
data class UserUiModel(
    val username: String,
    val email: String,
    val warehouseName: String,
    val warehouseNumber: Int,
    val mikroFlyUserId: Int,
    val documentSeries: String,
    val newDocumentSeries: String
) : Parcelable

fun UserUiModel.toDomainModel() = UserDomainModel (
    username = username,
    email = email,
    warehouseName = warehouseName,
    warehouseNumber = warehouseNumber,
    mikroFlyUserId = mikroFlyUserId,
    documentSeries = documentSeries,
    newDocumentSeries = newDocumentSeries
)


fun UserDomainModel.toUiModel() = UserUiModel(
    username = username,
    email = email,
    warehouseName = warehouseName,
    warehouseNumber = warehouseNumber,
    mikroFlyUserId = mikroFlyUserId,
    documentSeries = documentSeries,
    newDocumentSeries = newDocumentSeries
)