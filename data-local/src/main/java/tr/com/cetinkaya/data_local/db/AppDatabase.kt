package tr.com.cetinkaya.data_local.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import tr.com.cetinkaya.data_local.db.dao.OrderDao
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.entities.OrderEntity
import tr.com.cetinkaya.data_local.db.entities.StockTransactionEntity

@Database(
entities = [
    OrderEntity::class,
    StockTransactionEntity::class
],
version = 2,
    autoMigrations = [
        AutoMigration(from =  1, to = 2),
    ],
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2),
//        AutoMigration(from = 2, to = 3, spec = AppDatabase.Migration2To3::class),
//        AutoMigration(from = 3, to = 4),
//        AutoMigration(from = 4, to = 5),
//        AutoMigration(from = 5, to = 6),
//    ],
exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract val orderDao: OrderDao
    abstract val stockTransactionDao: StockTransactionDao


//    @RenameColumn(tableName = "stock_transactions", fromColumnName = "isSizedAndColored", toColumnName = "isColoredAndSized")
//    class Migration2To3 : AutoMigrationSpec

}