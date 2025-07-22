package tr.com.cetinkaya.data_local.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.models.auth.TokenRepositoryModel
import tr.com.cetinkaya.data_repository.models.user.UserRepositoryModel

internal val KEY_ACCESS_TOKEN = stringPreferencesKey("key_access_token")
internal val KEY_USERNAME = stringPreferencesKey("key_username")
internal val KEY_EMAIL = stringPreferencesKey("key_email")
internal val KEY_WAREHOUSE_NAME = stringPreferencesKey("key_warehouse_name")
internal val KEY_WAREHOUSE_NUMBER = intPreferencesKey("key_warehouse_number")
internal val KEY_MIKRO_FLY_USER_ID = intPreferencesKey("key_mikro_fly_user_id")
internal val KEY_DOCUMENT_SERIES = stringPreferencesKey("key_document_series")
internal val KEY_NEW_ORDER_DOCUMENT_SERIES = stringPreferencesKey("key_new_order_document_series")

class LocalAuthDataSourceImpl(private val dataStore: DataStore<Preferences>) : LocalAuthDataSource {

    override suspend fun saveAccessToken(token: TokenRepositoryModel) {
        dataStore.edit {
            it[KEY_ACCESS_TOKEN] = token.accessToken.token
        }
    }

    override fun getAccessToken(): Flow<String> {
        return dataStore.data.map {
            it[KEY_ACCESS_TOKEN] ?: ""
        }
    }

    override suspend fun saveLoggedUser(userRepositoryModel: UserRepositoryModel) {
        dataStore.edit {
            it[KEY_USERNAME] = userRepositoryModel.username
            it[KEY_EMAIL] = userRepositoryModel.email
            it[KEY_WAREHOUSE_NAME] = userRepositoryModel.warehouseName
            it[KEY_WAREHOUSE_NUMBER] = userRepositoryModel.warehouseNumber
            it[KEY_MIKRO_FLY_USER_ID] = userRepositoryModel.mikroFlyUserId
            it[KEY_DOCUMENT_SERIES] = userRepositoryModel.documentSeries
            it[KEY_NEW_ORDER_DOCUMENT_SERIES] = userRepositoryModel.newOrderDocumentSeries
        }
    }

    override fun getLoggedUser(): Flow<UserRepositoryModel> {
        return dataStore.data.map {
            UserRepositoryModel(
                username = it[KEY_USERNAME] ?: "",
                email = it[KEY_EMAIL] ?: "",
                warehouseName = it[KEY_WAREHOUSE_NAME] ?: "",
                warehouseNumber = it[KEY_WAREHOUSE_NUMBER] ?: 0,
                mikroFlyUserId = it[KEY_MIKRO_FLY_USER_ID] ?: 0,
                documentSeries = it[KEY_DOCUMENT_SERIES] ?: "",
                newOrderDocumentSeries = it[KEY_NEW_ORDER_DOCUMENT_SERIES] ?: ""
            )
        }
    }
}