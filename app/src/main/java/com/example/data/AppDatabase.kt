package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        Expense::class,
        RecurringExpense::class,
        BudgetAllocation::class,
        SavingsGoal::class,
        UserProfile::class,
        ChatMessage::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun budgetAllocationDao(): BudgetAllocationDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var factory: SupportFactory? = null
                try {
                    SQLiteDatabase.loadLibs(context.applicationContext)
                    val passphrase = SQLiteDatabase.getBytes("Student360SecureKey2026!".toCharArray())
                    factory = SupportFactory(passphrase)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student360_db"
                ).fallbackToDestructiveMigration()

                if (factory != null) {
                    builder.openHelperFactory(factory)
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
