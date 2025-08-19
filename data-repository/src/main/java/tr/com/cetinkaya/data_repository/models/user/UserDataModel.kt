package tr.com.cetinkaya.data_repository.models.user

data class UserRepositoryModel(
    val username: String,
    val email: String,
    val warehouseName: String,
    val warehouseNumber: Int,
    val warehouseLockDate: Long,
    val mikroFlyUserId: Int,
    val documentSeries: String,
    val newOrderDocumentSeries: String
)
