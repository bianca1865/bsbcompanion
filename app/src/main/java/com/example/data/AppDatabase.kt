package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BSBAccount::class,
        BSBCard::class,
        ScheduledPayment::class,
        ExpenseItem::class,
        AppNotification::class,
        RegisteredUser::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): BSBAccountDao
    abstract fun cardDao(): BSBCardDao
    abstract fun paymentDao(): ScheduledPaymentDao
    abstract fun expenseDao(): ExpenseItemDao
    abstract fun notificationDao(): AppNotificationDao
    abstract fun userDao(): RegisteredUserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration: 6 -> 7 preserves existing scheduled_payments while making selectedAccountId nullable
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with the desired schema (selectedAccountId INTEGER NULL)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `scheduled_payments_new` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `paymentType` TEXT NOT NULL,
                      `payeeName` TEXT NOT NULL,
                      `amount` REAL NOT NULL,
                      `paymentDay` INTEGER NOT NULL,
                      `lastPaymentDate` INTEGER,
                      `selectedAccountId` INTEGER,
                      `selectedCardId` INTEGER,
                      `isActive` INTEGER NOT NULL,
                      `recipientAccount` TEXT,
                      `recipientBranchNumber` TEXT,
                      `recipientBranchName` TEXT,
                      `recipientName` TEXT
                    )""".trimIndent())

                // Copy existing data into the new table (works whether or not selectedAccountId existed previously)
                database.execSQL("""
                    INSERT INTO `scheduled_payments_new` (`id`,`paymentType`,`payeeName`,`amount`,`paymentDay`,`lastPaymentDate`,`selectedAccountId`,`selectedCardId`,`isActive`,`recipientAccount`,`recipientBranchNumber`,`recipientBranchName`,`recipientName`)
                    SELECT `id`,`paymentType`,`payeeName`,`amount`,`paymentDay`,`lastPaymentDate`,
                           CASE WHEN typeof(`selectedAccountId`) = 'integer' THEN `selectedAccountId` ELSE NULL END,
                           `selectedCardId`,`isActive`,`recipientAccount`,`recipientBranchNumber`,`recipientBranchName`,`recipientName`
                    FROM `scheduled_payments`""".trimIndent())

                // Drop old table and rename new one
                database.execSQL("DROP TABLE IF EXISTS `scheduled_payments`")
                database.execSQL("ALTER TABLE `scheduled_payments_new` RENAME TO `scheduled_payments`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var factory: SupportFactory? = null
                try {
                    // Initialize SQLCipher native libraries (might throw UnsatisfiedLinkError in JVM tests)
                    SQLiteDatabase.loadLibs(context.applicationContext)
                    
                    // Secure static database encryption key
                    val passphrase = SQLiteDatabase.getBytes("BSBSavingsSecureStorageKey2026!".toCharArray())
                    factory = SupportFactory(passphrase)

                    // Robust check: test if the database is openable with the passphrase.
                    // If not, delete old unencrypted database files to prevent net.sqlcipher.database.SQLiteException crash.
                    if (!testDatabaseOpen(context, "bsb_companion_db", "BSBSavingsSecureStorageKey2026!")) {
                        deleteDatabaseFiles(context, "bsb_companion_db")
                    }
                } catch (e: Throwable) {
                    // Safe fallback if native SQLCipher is unavailable (such as during unit/Robolectric tests)
                    e.printStackTrace()
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bsb_companion_db"
                ).addMigrations(MIGRATION_6_7).fallbackToDestructiveMigration()

                if (factory != null) {
                    builder.openHelperFactory(factory)
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private fun testDatabaseOpen(context: Context, databaseName: String, passphraseString: String): Boolean {
            val dbFile = context.getDatabasePath(databaseName)
            if (!dbFile.exists()) {
                return true
            }
            return try {
                val db = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    passphraseString,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                db.close()
                true
            } catch (t: Throwable) {
                t.printStackTrace()
                false
            }
        }

        private fun deleteDatabaseFiles(context: Context, databaseName: String) {
            val dbFile = context.getDatabasePath(databaseName)
            val walFile = java.io.File(dbFile.path + "-wal")
            val shmFile = java.io.File(dbFile.path + "-shm")
            try { dbFile.delete() } catch (e: Exception) {}
            try { walFile.delete() } catch (e: Exception) {}
            try { shmFile.delete() } catch (e: Exception) {}
        }
    }
}
