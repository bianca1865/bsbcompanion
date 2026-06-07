package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BSBAccountDao {
    @Query("SELECT * FROM bsb_accounts ORDER BY id ASC")
    fun getAllAccountsFlow(): Flow<List<BSBAccount>>

    @Query("SELECT * FROM bsb_accounts")
    suspend fun getAllAccountsDirect(): List<BSBAccount>

    @Query("SELECT * FROM bsb_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Int): BSBAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BSBAccount): Long

    @Update
    suspend fun updateAccount(account: BSBAccount)

    @Delete
    suspend fun deleteAccount(account: BSBAccount)
}

@Dao
interface BSBCardDao {
    @Query("SELECT * FROM bsb_cards ORDER BY id ASC")
    fun getAllCardsFlow(): Flow<List<BSBCard>>

    @Query("SELECT * FROM bsb_cards WHERE linkedAccountId = :accountId")
    fun getCardsForAccountFlow(accountId: Int): Flow<List<BSBCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: BSBCard): Long

    @Delete
    suspend fun deleteCard(card: BSBCard)
}

@Dao
interface ScheduledPaymentDao {
    @Query("SELECT * FROM scheduled_payments ORDER BY paymentDay ASC")
    fun getAllPaymentsFlow(): Flow<List<ScheduledPayment>>

    @Query("SELECT * FROM scheduled_payments")
    suspend fun getAllPaymentsDirect(): List<ScheduledPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: ScheduledPayment): Long

    @Update
    suspend fun updatePayment(payment: ScheduledPayment)

    @Delete
    suspend fun deletePayment(payment: ScheduledPayment)
}

@Dao
interface ExpenseItemDao {
    @Query("SELECT * FROM expense_items ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseItem)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM app_notifications")
    suspend fun clearAll()
}

@Dao
interface RegisteredUserDao {
    @Query("SELECT * FROM registered_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): RegisteredUser?

    @Query("SELECT * FROM registered_users LIMIT 1")
    suspend fun getFirstUser(): RegisteredUser?

    @Query("SELECT * FROM registered_users")
    fun getAllRegisteredUsers(): Flow<List<RegisteredUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: RegisteredUser)

    @Query("DELETE FROM registered_users")
    suspend fun clearUser()
}
