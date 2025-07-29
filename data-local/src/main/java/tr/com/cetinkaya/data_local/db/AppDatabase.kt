package tr.com.cetinkaya.data_local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tr.com.cetinkaya.data_local.db.dao.OrderDao
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.OrderEntity
import tr.com.cetinkaya.data_local.db.entities.StockTransactionEntity
import tr.com.cetinkaya.data_local.db.entities.TransferredDocumentEntity

@Database(
    entities = [OrderEntity::class, StockTransactionEntity::class, TransferredDocumentEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract val orderDao: OrderDao
    abstract val stockTransactionDao: StockTransactionDao
    abstract val transferredDocumentDao: TransferredDocumentDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN deliveredQuantity REAL NOT NULL DEFAULT 0.0")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stock_transactions (
                        id TEXT NOT NULL,
                        transactionType INTEGER NOT NULL,
                        transactionKind INTEGER NOT NULL,
                        isNormalOrReturn INTEGER NOT NULL,
                        documentType INTEGER NOT NULL,
                        documentDate INTEGER NOT NULL,
                        documentSeries TEXT NOT NULL,
                        documentNumber INTEGER NOT NULL,
                        lineNumber INTEGER NOT NULL,
                        stockCode TEXT NOT NULL,
                        companyCode TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        inputWarehouseNumber INTEGER NOT NULL,
                        outputWarehouseNumber INTEGER NOT NULL,
                        paymentPlanNumber INTEGER NOT NULL,
                        salesman TEXT NOT NULL,
                        responsibilityCenter TEXT NOT NULL,
                        userCode INTEGER NOT NULL,
                        totalPrice REAL NOT NULL,
                        discount1 REAL NOT NULL,
                        discount2 REAL NOT NULL,
                        discount3 REAL NOT NULL,
                        discount4 REAL NOT NULL,
                        discount5 REAL NOT NULL,
                        taxPointer INTEGER NOT NULL,
                        orderId TEXT NOT NULL,
                        price REAL NOT NULL,
                        paperNumber TEXT NOT NULL,
                        companyNumber INTEGER NOT NULL,
                        storeNumber INTEGER NOT NULL,
                        barcode TEXT NOT NULL,
                        isColoredAndSized INTEGER NOT NULL DEFAULT 0,
                        transportationStatus INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        synchronizationStatus TEXT NOT NULL DEFAULT 'Aktarılacak',
                        PRIMARY KEY(id, barcode),
                        FOREIGN KEY(orderId, barcode) REFERENCES orders(id, barcode) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_transactions_orderId_barcode ON stock_transactions(orderId, barcode)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN stockName TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Geçici tablo oluştur (foreign key ve not null yok)
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS stock_transactions_temp (
                id TEXT NOT NULL,
                transactionType INTEGER NOT NULL,
                transactionKind INTEGER NOT NULL,
                isNormalOrReturn INTEGER NOT NULL,
                documentType INTEGER NOT NULL,
                documentDate INTEGER NOT NULL,
                documentSeries TEXT NOT NULL,
                documentNumber INTEGER NOT NULL,
                lineNumber INTEGER NOT NULL,
                stockCode TEXT NOT NULL,
                stockName TEXT NOT NULL DEFAULT '',
                companyCode TEXT NOT NULL,
                quantity REAL NOT NULL,
                inputWarehouseNumber INTEGER NOT NULL,
                outputWarehouseNumber INTEGER NOT NULL,
                paymentPlanNumber INTEGER NOT NULL,
                salesman TEXT NOT NULL,
                responsibilityCenter TEXT NOT NULL,
                userCode INTEGER NOT NULL,
                totalPrice REAL NOT NULL,
                discount1 REAL NOT NULL,
                discount2 REAL NOT NULL,
                discount3 REAL NOT NULL,
                discount4 REAL NOT NULL,
                discount5 REAL NOT NULL,
                taxPointer INTEGER NOT NULL,
                orderId TEXT, -- artık NULL olabilir
                price REAL NOT NULL,
                paperNumber TEXT NOT NULL,
                companyNumber INTEGER NOT NULL,
                storeNumber INTEGER NOT NULL,
                barcode TEXT NOT NULL,
                isColoredAndSized INTEGER NOT NULL DEFAULT 0,
                transportationStatus INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                synchronizationStatus TEXT NOT NULL DEFAULT 'Aktarılacak',
                PRIMARY KEY(id, barcode)
            )
        """.trimIndent()
                )

                // 2. Veriyi eski tablodan yeni tabloya aktar
                db.execSQL(
                    """
            INSERT INTO stock_transactions_temp (
                id, transactionType, transactionKind, isNormalOrReturn, documentType,
                documentDate, documentSeries, documentNumber, lineNumber, stockCode,
                stockName, companyCode, quantity, inputWarehouseNumber, outputWarehouseNumber,
                paymentPlanNumber, salesman, responsibilityCenter, userCode, totalPrice,
                discount1, discount2, discount3, discount4, discount5, taxPointer, orderId,
                price, paperNumber, companyNumber, storeNumber, barcode, isColoredAndSized,
                transportationStatus, createdAt, updatedAt, synchronizationStatus
            )
            SELECT
                id, transactionType, transactionKind, isNormalOrReturn, documentType,
                documentDate, documentSeries, documentNumber, lineNumber, stockCode,
                stockName, companyCode, quantity, inputWarehouseNumber, outputWarehouseNumber,
                paymentPlanNumber, salesman, responsibilityCenter, userCode, totalPrice,
                discount1, discount2, discount3, discount4, discount5, taxPointer, orderId,
                price, paperNumber, companyNumber, storeNumber, barcode, isColoredAndSized,
                transportationStatus, createdAt, updatedAt, synchronizationStatus
            FROM stock_transactions
        """.trimIndent()
                )

                // 3. Eski tabloyu sil
                db.execSQL("DROP TABLE stock_transactions")

                // 4. Geçici tabloyu asıl tablo olarak yeniden adlandır
                db.execSQL("ALTER TABLE stock_transactions_temp RENAME TO stock_transactions")

                // 5. Gerekirse index'i tekrar oluştur
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_transactions_orderId_barcode ON stock_transactions(orderId, barcode)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transferred_documents (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transferredDocumentType INTEGER NOT NULL,
                        documentSeries TEXT NOT NULL,
                        documentNumber INTEGER NOT NULL,
                        synchronizationStatus INTEGER NOT NULL,
                        description TEXT NOT NULL
                    )
                """.trimIndent()
                )
            }
        }

    }
}


